// Edge Function : dispatch_arbitration_push (FR26, FR30).
//
// Invoked by the `trg_dispatch_arbitration_push` trigger when a member
// records an arbitration. Looks up the creators of both conflicting
// meetings (the one the arbitrating member is *skipping* and the one they
// are *attending*), fetches their FCM/APNs tokens, and emits a data-only
// push so each organizer sees in real-time how many people are coming
// (Marc's dashboard in Epic 4 — FR27).
//
// Retries up to 3 times with exponential backoff (300ms, 900ms, 2700ms).
// After 3 failures the row goes to `push_failure_log` for ops triage.
//
// Latency budget: < 5 s end-to-end from `arbitration INSERT` to push
// arrival (NFR-P4).

import { createClient } from "https://esm.sh/@supabase/supabase-js@2.49.4";

const SUPABASE_URL = Deno.env.get("SUPABASE_URL")!;
const SERVICE_ROLE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;
const FCM_PROJECT_ID = Deno.env.get("FCM_PROJECT_ID") ?? "";
const FCM_ACCESS_TOKEN = Deno.env.get("FCM_ACCESS_TOKEN") ?? "";

const supabase = createClient(SUPABASE_URL, SERVICE_ROLE_KEY, {
  auth: { persistSession: false },
});

const BACKOFFS_MS = [300, 900, 2700];

interface DeviceRow {
  id: string;
  fcm_token: string | null;
  apns_token: string | null;
  platform: "android" | "ios" | null;
}

interface MemberRow {
  id: string;
  display_name: string;
  device_id: string | null;
  device: DeviceRow | null;
}

interface MeetingRow {
  id: string;
  title: string | null;
  starts_at: string;
  created_by_member_id: string | null;
}

interface ArbitrationRow {
  id: string;
  member_id: string;
  meeting_id: string;
  conflicting_meeting_id: string;
}

Deno.serve(async (req) => {
  const { arbitration_id } = await req.json();
  if (!arbitration_id) {
    return new Response("missing arbitration_id", { status: 400 });
  }

  const { data: arbitration, error: aErr } = await supabase
    .from("arbitration")
    .select("id, member_id, meeting_id, conflicting_meeting_id")
    .eq("id", arbitration_id)
    .single<ArbitrationRow>();
  if (aErr || !arbitration) {
    return new Response(`arbitration not found: ${aErr?.message ?? "none"}`, {
      status: 404,
    });
  }

  const { data: arbitrating, error: meErr } = await supabase
    .from("member")
    .select("id, display_name")
    .eq("id", arbitration.member_id)
    .single<{ id: string; display_name: string }>();
  if (meErr || !arbitrating) {
    return new Response(`arbitrating member not found: ${meErr?.message ?? "none"}`, {
      status: 404,
    });
  }

  // Resolve both meetings + commissions so we can label the push payload.
  const meetingIds = [arbitration.meeting_id, arbitration.conflicting_meeting_id];
  const { data: meetings, error: mErr } = await supabase
    .from("meeting")
    .select("id, title, starts_at, created_by_member_id")
    .in("id", meetingIds);
  if (mErr || !meetings || meetings.length === 0) {
    return new Response(`meetings not found: ${mErr?.message ?? "none"}`, {
      status: 404,
    });
  }

  const byId = new Map<string, MeetingRow>(
    meetings.map((m) => [m.id as string, m as unknown as MeetingRow]),
  );
  const chosen = byId.get(arbitration.conflicting_meeting_id);
  const skipped = byId.get(arbitration.meeting_id);
  if (!chosen || !skipped) {
    return new Response("meeting pair incomplete", { status: 404 });
  }

  const { data: chosenCommissions } = await supabase
    .from("meeting_commission")
    .select("commission:commission(id, name)")
    .eq("meeting_id", chosen.id);
  const chosenCommissionName =
    (chosenCommissions ?? [])
      .map((r) => (r as unknown as { commission: { name: string } | null }).commission?.name)
      .find((n) => !!n) ?? "l’autre réunion";

  const creatorIds = Array.from(
    new Set(
      [chosen.created_by_member_id, skipped.created_by_member_id].filter(
        (v): v is string => !!v && v !== arbitration.member_id,
      ),
    ),
  );
  if (creatorIds.length === 0) {
    return new Response("no creators to notify", { status: 204 });
  }

  const { data: creators } = await supabase
    .from("member")
    .select("id, display_name, device_id, device:device(id, fcm_token, apns_token, platform)")
    .in("id", creatorIds);

  const targets = (creators ?? [])
    .map((m) => m as unknown as MemberRow)
    .filter((m) => m.device !== null && (m.device.fcm_token || m.device.apns_token));
  if (targets.length === 0) {
    return new Response("no push tokens", { status: 204 });
  }

  const bodyText = formatPushBody(
    arbitrating.display_name,
    chosenCommissionName,
    chosen.starts_at,
  );

  await Promise.all(
    targets.map((t) => {
      // Each creator's deep-link should land them on *their own* meeting's
      // detail screen where the "Statut des conflictés" section reflects
      // the new arbitration (FR27, story 4.6).
      const ownsChosen = t.id === chosen.created_by_member_id;
      const ownMeetingId = ownsChosen ? chosen.id : skipped.id;
      return dispatchWithRetry(t, ownMeetingId, bodyText);
    }),
  );
  return new Response("ok", { status: 200 });
});

function formatPushBody(
  arbitratorName: string,
  chosenCommissionName: string,
  startsAtIso: string,
): string {
  // Compact French phrasing aligned with FR30: "{Sophie} ira à {commission
  // gagnante} jeudi 21 mai". We only have the ISO date here, so we just
  // surface the calendar date — month names are formatted client-side in
  // the agenda screens but here we keep it simple to avoid pulling locale
  // tables into the edge runtime.
  const date = startsAtIso.slice(0, 10);
  return `${arbitratorName} ira à ${chosenCommissionName} le ${date}`;
}

async function dispatchWithRetry(target: MemberRow, meetingId: string, body: string) {
  for (let attempt = 0; attempt <= BACKOFFS_MS.length; attempt++) {
    try {
      await sendPush(target, meetingId, body);
      return;
    } catch (err) {
      if (attempt === BACKOFFS_MS.length) {
        await logFailure(target, meetingId, err);
        return;
      }
      await sleep(BACKOFFS_MS[attempt]);
    }
  }
}

async function sendPush(target: MemberRow, meetingId: string, body: string) {
  const device = target.device!;
  if (device.platform === "android" && device.fcm_token) {
    const res = await fetch(
      `https://fcm.googleapis.com/v1/projects/${FCM_PROJECT_ID}/messages:send`,
      {
        method: "POST",
        headers: {
          Authorization: `Bearer ${FCM_ACCESS_TOKEN}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          message: {
            token: device.fcm_token,
            data: {
              type: "arbitration_recorded",
              meeting_id: meetingId,
              body,
              deep_link: `colision://meeting/${meetingId}`,
            },
          },
        }),
      },
    );
    if (res.status >= 500) throw new Error(`fcm ${res.status}`);
    if (!res.ok) throw new Error(`fcm fatal ${res.status}`);
  } else if (device.platform === "ios" && device.apns_token) {
    // APNs left as a TODO until Epic 6 (launch), mirroring
    // dispatch_conflict_push.
    return;
  }
}

async function logFailure(target: MemberRow, meetingId: string, err: unknown) {
  await supabase.from("push_failure_log").insert({
    member_id: target.id,
    device_id: target.device_id,
    meeting_id: meetingId,
    push_type: "arbitration_recorded",
    error_message: err instanceof Error ? err.message : String(err),
  });
}

function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

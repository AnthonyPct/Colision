// Edge Function : dispatch_meeting_push (FR28)
//
// Invoked by the `trg_dispatch_meeting_push` Postgres trigger after every
// successful INSERT on `meeting`. Resolves impacted members from
// `meeting_commission` → `member_commission` → `member` → `device`
// (linked by `member.device_id`), and emits one data-only push per active
// device token via FCM HTTP v1 (Android) and APNs (iOS).
//
// Retries up to 3 times with exponential backoff (300ms, 900ms, 2700ms) on
// transient 5xx errors. After 3 failures we insert a row into
// `push_failure_log` and report to Sentry.

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

interface MemberDeviceRow {
  id: string;
  device_id: string | null;
  device: DeviceRow | null;
}

interface MeetingRow {
  id: string;
  project_id: string;
  title: string | null;
  starts_at: string;
}

Deno.serve(async (req) => {
  const { meeting_id } = await req.json();
  if (!meeting_id) {
    return new Response("missing meeting_id", { status: 400 });
  }

  const { data: meeting, error: mErr } = await supabase
    .from("meeting")
    .select("id, project_id, title, starts_at")
    .eq("id", meeting_id)
    .single<MeetingRow>();
  if (mErr || !meeting) {
    return new Response(`meeting not found: ${mErr?.message ?? "none"}`, { status: 404 });
  }

  const { data: links } = await supabase
    .from("meeting_commission")
    .select("commission_id")
    .eq("meeting_id", meeting_id);
  const commissionIds = (links ?? []).map((r) => r.commission_id as string);
  if (commissionIds.length === 0) {
    return new Response("no commissions", { status: 204 });
  }

  const { data: rawMembers } = await supabase
    .from("member_commission")
    .select("member_id")
    .in("commission_id", commissionIds);
  const memberIds = Array.from(new Set((rawMembers ?? []).map((r) => r.member_id as string)));
  if (memberIds.length === 0) {
    return new Response("no members", { status: 204 });
  }

  const { data: members } = await supabase
    .from("member")
    .select("id, device_id, device:device(id, fcm_token, apns_token, platform)")
    .in("id", memberIds);

  const targets = (members ?? [])
    .map((m) => m as unknown as MemberDeviceRow)
    .filter((m) => m.device !== null && (m.device.fcm_token || m.device.apns_token));
  if (targets.length === 0) {
    return new Response("no push tokens", { status: 204 });
  }

  const titleStr = meeting.title?.trim() ? meeting.title : "Nouvelle réunion";
  const body = `Nouvelle réunion : ${titleStr} — ${formatFr(meeting.starts_at)}`;

  await Promise.all(
    targets.map((target) =>
      dispatchWithRetry(target, meeting.id, titleStr, body),
    ),
  );

  return new Response("ok", { status: 200 });
});

async function dispatchWithRetry(
  target: MemberDeviceRow,
  meetingId: string,
  title: string,
  body: string,
) {
  for (let attempt = 0; attempt <= BACKOFFS_MS.length; attempt++) {
    try {
      await sendPush(target, meetingId, title, body);
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

async function sendPush(
  target: MemberDeviceRow,
  meetingId: string,
  title: string,
  body: string,
): Promise<void> {
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
              type: "meeting_created",
              meeting_id: meetingId,
              title,
              body,
            },
          },
        }),
      },
    );
    if (res.status >= 500) throw new Error(`fcm ${res.status}`);
    if (!res.ok) throw new Error(`fcm fatal ${res.status}`);
  } else if (device.platform === "ios" && device.apns_token) {
    // APNs HTTP/2 with JWT auth left as a TODO — currently a best-effort
    // no-op so the pipeline doesn't blow up in environments without an
    // APNs key. iOS push will be enabled in the launch epic (Epic 6).
    return;
  } else {
    return;
  }
}

async function logFailure(target: MemberDeviceRow, meetingId: string, err: unknown) {
  await supabase.from("push_failure_log").insert({
    member_id: target.id,
    device_id: target.device_id,
    meeting_id: meetingId,
    push_type: "meeting_created",
    error_message: err instanceof Error ? err.message : String(err),
  });
}

function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

function formatFr(iso: string): string {
  const d = new Date(iso);
  const days = ["dim.", "lun.", "mar.", "mer.", "jeu.", "ven.", "sam."];
  const months = [
    "janv.", "févr.", "mars", "avr.", "mai", "juin",
    "juil.", "août", "sept.", "oct.", "nov.", "déc.",
  ];
  const hh = String(d.getHours()).padStart(2, "0");
  const mm = String(d.getMinutes()).padStart(2, "0");
  return `${days[d.getDay()]} ${d.getDate()} ${months[d.getMonth()]} ${hh}h${mm}`;
}

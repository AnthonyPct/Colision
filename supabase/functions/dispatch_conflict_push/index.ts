// Edge Function : dispatch_conflict_push (FR29, FR32)
//
// Invoked by the `trg_dispatch_meeting_push` trigger when a new meeting
// overlaps an existing engagement of one or more members. Resolves the
// conflicted members via `detect_conflicts`, then emits a data-only push
// "conflict_detected" with a deep-link to the arbitration screen so the
// member can choose which meeting they go to (Epic 5).
//
// Retries up to 3 times with exponential backoff (300ms, 900ms, 2700ms).
// After 3 failures the row goes to `push_failure_log` and Sentry.
//
// Latency budget: < 5 s end-to-end from `meeting INSERT` to push arrival
// (NFR-P4).

import { createClient } from "https://esm.sh/@supabase/supabase-js@2.49.4";
import { sendApns } from "../_shared/apns.ts";

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
}

interface ConflictRow {
  member_id: string;
}

Deno.serve(async (req) => {
  const { meeting_id } = await req.json();
  if (!meeting_id) {
    return new Response("missing meeting_id", { status: 400 });
  }

  const { data: meeting, error: mErr } = await supabase
    .from("meeting")
    .select("id, project_id, title")
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

  // Re-compute the authoritative conflict list (AC2).
  const { data: conflictRows, error: cErr } = await supabase.rpc("detect_conflicts", {
    p_project_id: meeting.project_id,
    p_commission_ids: commissionIds,
    p_start: (await freshTimes(meeting_id)).start,
    p_end: (await freshTimes(meeting_id)).end,
  });
  if (cErr) {
    return new Response(`detect_conflicts failed: ${cErr.message}`, { status: 500 });
  }

  const conflictedMemberIds = Array.from(
    new Set(((conflictRows as ConflictRow[]) ?? []).map((r) => r.member_id)),
  );
  if (conflictedMemberIds.length === 0) {
    return new Response("no conflicts", { status: 204 });
  }

  const { data: members } = await supabase
    .from("member")
    .select("id, device_id, device:device(id, fcm_token, apns_token, platform)")
    .in("id", conflictedMemberIds);

  const targets = (members ?? [])
    .map((m) => m as unknown as MemberDeviceRow)
    .filter((m) => m.device !== null && (m.device.fcm_token || m.device.apns_token));
  if (targets.length === 0) {
    return new Response("no push tokens", { status: 204 });
  }

  const titleStr = meeting.title?.trim() ? meeting.title : "Réunion en conflit";

  await Promise.all(targets.map((t) => dispatchWithRetry(t, meeting.id, titleStr)));
  return new Response("ok", { status: 200 });
});

async function freshTimes(meetingId: string): Promise<{ start: string; end: string }> {
  const { data } = await supabase
    .from("meeting")
    .select("starts_at, ends_at")
    .eq("id", meetingId)
    .single<{ starts_at: string; ends_at: string }>();
  return { start: data!.starts_at, end: data!.ends_at };
}

async function dispatchWithRetry(target: MemberDeviceRow, meetingId: string, title: string) {
  for (let attempt = 0; attempt <= BACKOFFS_MS.length; attempt++) {
    try {
      await sendPush(target, meetingId, title);
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

async function sendPush(target: MemberDeviceRow, meetingId: string, title: string) {
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
              type: "conflict_detected",
              meeting_id: meetingId,
              title,
              deep_link: `colision://arbitration/${meetingId}`,
            },
          },
        }),
      },
    );
    if (res.status >= 500) throw new Error(`fcm ${res.status}`);
    if (!res.ok) throw new Error(`fcm fatal ${res.status}`);
  } else if (device.platform === "ios" && device.apns_token) {
    await sendApns(device.apns_token, {
      title,
      data: {
        type: "conflict_detected",
        meeting_id: meetingId,
        deep_link: `colision://arbitration/${meetingId}`,
      },
    });
  }
}

async function logFailure(target: MemberDeviceRow, meetingId: string, err: unknown) {
  await supabase.from("push_failure_log").insert({
    member_id: target.id,
    device_id: target.device_id,
    meeting_id: meetingId,
    push_type: "conflict_detected",
    error_message: err instanceof Error ? err.message : String(err),
  });
}

function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

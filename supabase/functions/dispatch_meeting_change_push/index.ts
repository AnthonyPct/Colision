// Edge Function : dispatch_meeting_change_push (FR31)
//
// Single endpoint used for both "Réunion modifiée" and "Réunion annulée"
// pushes. The Postgres helper RPCs (update_meeting_with_commissions /
// delete_meeting_with_dispatch in migration 013) call us with:
//
//   {
//     meeting_id: uuid,
//     kind: "updated" | "cancelled",
//     previous_commission_ids: uuid[],
//     new_commission_ids:      uuid[],
//     // cancelled only — the row may already be gone by the time we run:
//     meeting_title?: string,
//     meeting_starts_at?: string,
//   }
//
// We notify every member of the union (previous ∪ new) of those
// commissions exactly once, retrying 3x with exponential backoff on
// transient errors.

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
  title: string | null;
  starts_at: string;
}

interface Payload {
  meeting_id: string;
  kind: "updated" | "cancelled";
  previous_commission_ids: string[];
  new_commission_ids: string[];
  meeting_title?: string;
  meeting_starts_at?: string;
}

Deno.serve(async (req) => {
  const payload = (await req.json()) as Payload;
  if (!payload?.meeting_id) {
    return new Response("missing meeting_id", { status: 400 });
  }

  let title: string;
  let startsAt: string;
  if (payload.kind === "cancelled") {
    title = payload.meeting_title?.trim() || "Réunion sans titre";
    startsAt = payload.meeting_starts_at ?? new Date().toISOString();
  } else {
    const { data: meeting, error } = await supabase
      .from("meeting")
      .select("id, title, starts_at")
      .eq("id", payload.meeting_id)
      .single<MeetingRow>();
    if (error || !meeting) {
      return new Response(`meeting not found: ${error?.message ?? "none"}`, { status: 404 });
    }
    title = meeting.title?.trim() ? meeting.title : "Réunion sans titre";
    startsAt = meeting.starts_at;
  }

  const commissionIds = Array.from(
    new Set([...(payload.previous_commission_ids ?? []), ...(payload.new_commission_ids ?? [])]),
  );
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

  const body =
    payload.kind === "cancelled"
      ? `Réunion annulée : ${title}`
      : `Réunion modifiée : ${title} — ${formatFr(startsAt)}`;
  const pushType = payload.kind === "cancelled" ? "meeting_cancelled" : "meeting_updated";

  await Promise.all(targets.map((t) => dispatchWithRetry(t, payload.meeting_id, title, body, pushType)));

  return new Response("ok", { status: 200 });
});

async function dispatchWithRetry(
  target: MemberDeviceRow,
  meetingId: string,
  title: string,
  body: string,
  pushType: string,
) {
  for (let attempt = 0; attempt <= BACKOFFS_MS.length; attempt++) {
    try {
      await sendPush(target, meetingId, title, body, pushType);
      return;
    } catch (err) {
      if (attempt === BACKOFFS_MS.length) {
        await logFailure(target, meetingId, pushType, err);
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
  pushType: string,
) {
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
              type: pushType,
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
    await sendApns(device.apns_token, {
      title,
      body,
      data: { type: pushType, meeting_id: meetingId },
    });
  }
}

async function logFailure(target: MemberDeviceRow, meetingId: string, pushType: string, err: unknown) {
  await supabase.from("push_failure_log").insert({
    member_id: target.id,
    device_id: target.device_id,
    meeting_id: meetingId,
    push_type: pushType,
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

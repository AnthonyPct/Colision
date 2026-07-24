// Edge Function : dispatch_poll_push
//
// Invoked by the deferred `trg_dispatch_poll_push` Postgres constraint
// trigger after every successful INSERT on `poll` (at COMMIT time, so the
// poll_option / poll_commission rows are visible). Resolves the members
// eligible to vote and emits one data-only push per active device token via
// FCM HTTP v1 (Android) and APNs (iOS).
//
// Eligibility:
//   - target_type = 'public'      -> every member of the poll's project.
//   - target_type = 'commissions' -> members of the targeted commissions,
//                                     via poll_commission -> member_commission.
//
// Retries up to 3 times with exponential backoff (300ms, 900ms, 2700ms) on
// transient errors. After 3 failures we insert a row into push_failure_log.

import { createClient } from "https://esm.sh/@supabase/supabase-js@2.49.4";
import { sendApns } from "../_shared/apns.ts";
import { sendFcm } from "../_shared/fcm.ts";

const SUPABASE_URL = Deno.env.get("SUPABASE_URL")!;
const SERVICE_ROLE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;

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

interface PollRow {
  id: string;
  project_id: string;
  question: string;
  target_type: "public" | "commissions";
  created_by_member_id: string | null;
}

Deno.serve(async (req) => {
  const { poll_id } = await req.json();
  if (!poll_id) {
    return new Response("missing poll_id", { status: 400 });
  }

  const { data: poll, error: pErr } = await supabase
    .from("poll")
    .select("id, project_id, question, target_type, created_by_member_id")
    .eq("id", poll_id)
    .single<PollRow>();
  if (pErr || !poll) {
    return new Response(`poll not found: ${pErr?.message ?? "none"}`, { status: 404 });
  }

  // Resolve eligible member ids depending on the poll scope.
  let memberIds: string[];
  if (poll.target_type === "public") {
    const { data: rows } = await supabase
      .from("member")
      .select("id")
      .eq("project_id", poll.project_id);
    memberIds = (rows ?? []).map((r) => r.id as string);
  } else {
    const { data: links } = await supabase
      .from("poll_commission")
      .select("commission_id")
      .eq("poll_id", poll_id);
    const commissionIds = (links ?? []).map((r) => r.commission_id as string);
    if (commissionIds.length === 0) {
      return new Response("no commissions", { status: 204 });
    }
    const { data: rows } = await supabase
      .from("member_commission")
      .select("member_id")
      .in("commission_id", commissionIds);
    memberIds = Array.from(new Set((rows ?? []).map((r) => r.member_id as string)));
  }

  // Don't notify the creator of their own poll.
  if (poll.created_by_member_id) {
    memberIds = memberIds.filter((id) => id !== poll.created_by_member_id);
  }
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

  const title = "Nouveau sondage";
  const body = `Nouveau sondage : ${poll.question}`;

  await Promise.all(
    targets.map((target) => dispatchWithRetry(target, poll.id, title, body)),
  );

  return new Response("ok", { status: 200 });
});

async function dispatchWithRetry(
  target: MemberDeviceRow,
  pollId: string,
  title: string,
  body: string,
) {
  for (let attempt = 0; attempt <= BACKOFFS_MS.length; attempt++) {
    try {
      await sendPush(target, pollId, title, body);
      return;
    } catch (err) {
      if (attempt === BACKOFFS_MS.length) {
        await logFailure(target, pollId, err);
        return;
      }
      await sleep(BACKOFFS_MS[attempt]);
    }
  }
}

async function sendPush(
  target: MemberDeviceRow,
  pollId: string,
  title: string,
  body: string,
): Promise<void> {
  const device = target.device!;
  const deepLink = `colision://poll/${pollId}`;
  if (device.platform === "android" && device.fcm_token) {
    await sendFcm(device.fcm_token, {
      type: "poll_created",
      poll_id: pollId,
      title,
      body,
      deep_link: deepLink,
    });
  } else if (device.platform === "ios" && device.apns_token) {
    await sendApns(device.apns_token, {
      title,
      body,
      data: { type: "poll_created", poll_id: pollId, deep_link: deepLink },
    });
  }
}

async function logFailure(target: MemberDeviceRow, pollId: string, err: unknown) {
  await supabase.from("push_failure_log").insert({
    member_id: target.id,
    device_id: target.device_id,
    poll_id: pollId,
    push_type: "poll_created",
    error_message: err instanceof Error ? err.message : String(err),
  });
}

function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

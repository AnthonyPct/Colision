-- Migration 010 : push dispatch on meeting INSERT.
--
-- Calls the `dispatch_meeting_push` Edge Function asynchronously via pg_net
-- whenever a new meeting is inserted. The Edge Function looks up the
-- impacted FCM/APNs tokens (via `member.device_id → device`) for the
-- selected commissions, and emits a data-only push notification.
--
-- Conflict-aware push routing (story 4.4) is layered on top via a separate
-- audit table populated by the client when the creator chose "Créer quand
-- même" — see migration 011.

-- ----- push_failure_log : audit of pushes that exhausted retries. Read by
-- Sentry / Grafana for ops alerting (NFR-P4).
create table if not exists public.push_failure_log (
  id uuid primary key default gen_random_uuid(),
  member_id uuid references public.member(id) on delete set null,
  device_id uuid references public.device(id) on delete set null,
  meeting_id uuid references public.meeting(id) on delete set null,
  push_type text not null,
  error_message text,
  created_at timestamptz not null default now()
);

create index if not exists idx_push_failure_log_created_at
  on public.push_failure_log (created_at desc);

create or replace function public.fn_dispatch_meeting_push()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
  v_url text;
  v_key text;
begin
  v_url := current_setting('app.edge_functions_base_url', true);
  v_key := current_setting('app.service_role_key', true);

  -- Best-effort: only enqueue the HTTP call when the deployment has
  -- configured the Edge Function base URL + service-role key. In local
  -- dev/test the GUCs are unset and we silently skip.
  if v_url is null or v_key is null or v_url = '' or v_key = '' then
    return new;
  end if;

  perform net.http_post(
    url := v_url || '/dispatch_meeting_push',
    headers := jsonb_build_object(
      'Content-Type', 'application/json',
      'Authorization', 'Bearer ' || v_key
    ),
    body := jsonb_build_object('meeting_id', new.id::text)
  );

  return new;
end;
$$;

drop trigger if exists trg_dispatch_meeting_push on public.meeting;
create trigger trg_dispatch_meeting_push
  after insert on public.meeting
  for each row execute function public.fn_dispatch_meeting_push();

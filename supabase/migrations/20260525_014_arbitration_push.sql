-- Migration 014 : push dispatch on `arbitration` INSERT (Epic 5, FR26/FR30).
--
-- Calls the `dispatch_arbitration_push` Edge Function asynchronously via
-- pg_net whenever a member commits an arbitration. The Edge Function looks
-- up the creators of both conflicting meetings, fetches their FCM/APNs
-- tokens, and emits a data-only push so each organizer sees in real-time
-- how many people are coming.
--
-- Story 5.2 cleans an existing arbitration row before inserting the new
-- one when a member changes their mind, so the trigger fires once per
-- effective decision rather than once per submission.

create or replace function public.fn_dispatch_arbitration_push()
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
    url := v_url || '/dispatch_arbitration_push',
    headers := jsonb_build_object(
      'Content-Type', 'application/json',
      'Authorization', 'Bearer ' || v_key
    ),
    body := jsonb_build_object('arbitration_id', new.id::text)
  );

  return new;
end;
$$;

drop trigger if exists trg_dispatch_arbitration_push on public.arbitration;
create trigger trg_dispatch_arbitration_push
  after insert on public.arbitration
  for each row execute function public.fn_dispatch_arbitration_push();

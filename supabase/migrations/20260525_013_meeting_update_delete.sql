-- Migration 013 : update_meeting_with_commissions + delete_meeting_with_dispatch.
--
-- Atomic edit and cancel flows for meetings (FR17, FR18, FR31). Each RPC
-- snapshots the previous commission set, performs the write, and dispatches
-- a "Réunion modifiée" or "Réunion annulée" push via the
-- `dispatch_meeting_change_push` Edge Function. The union of old + new
-- commission IDs is sent in the payload so the Edge Function can notify
-- every impacted member exactly once, including members who lost (or
-- gained) the meeting when commissions were swapped.

-- ----- update_meeting_with_commissions
create or replace function public.update_meeting_with_commissions(
  p_meeting_id     uuid,
  p_title          text,
  p_starts_at      timestamptz,
  p_ends_at        timestamptz,
  p_commission_ids uuid[]
)
returns public.meeting
language plpgsql
security definer
set search_path = public
as $$
declare
  v_meeting   public.meeting;
  v_previous  uuid[];
  v_url       text;
  v_key       text;
begin
  if array_length(p_commission_ids, 1) is null then
    raise exception 'at least one commission required';
  end if;

  select coalesce(array_agg(commission_id), array[]::uuid[])
    into v_previous
    from public.meeting_commission
   where meeting_id = p_meeting_id;

  update public.meeting
     set title       = nullif(trim(p_title), ''),
         starts_at   = p_starts_at,
         ends_at     = p_ends_at
   where id = p_meeting_id
   returning * into v_meeting;

  if v_meeting.id is null then
    raise exception 'meeting % not found', p_meeting_id;
  end if;

  delete from public.meeting_commission where meeting_id = p_meeting_id;
  insert into public.meeting_commission (meeting_id, commission_id)
  select p_meeting_id, unnest(p_commission_ids);

  v_url := current_setting('app.edge_functions_base_url', true);
  v_key := current_setting('app.service_role_key', true);
  if v_url is not null and v_key is not null and v_url <> '' and v_key <> '' then
    perform net.http_post(
      url     := v_url || '/dispatch_meeting_change_push',
      headers := jsonb_build_object(
        'Content-Type', 'application/json',
        'Authorization', 'Bearer ' || v_key
      ),
      body := jsonb_build_object(
        'meeting_id',                p_meeting_id::text,
        'kind',                      'updated',
        'previous_commission_ids',   to_jsonb(v_previous),
        'new_commission_ids',        to_jsonb(p_commission_ids)
      )
    );
  end if;

  return v_meeting;
end;
$$;

grant execute on function public.update_meeting_with_commissions(
  uuid, text, timestamptz, timestamptz, uuid[]
) to anon, authenticated;

-- ----- delete_meeting_with_dispatch
create or replace function public.delete_meeting_with_dispatch(
  p_meeting_id uuid
)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
  v_meeting     public.meeting;
  v_commissions uuid[];
  v_url         text;
  v_key         text;
begin
  select * into v_meeting from public.meeting where id = p_meeting_id;
  if v_meeting.id is null then
    return;
  end if;
  select coalesce(array_agg(commission_id), array[]::uuid[])
    into v_commissions
    from public.meeting_commission
   where meeting_id = p_meeting_id;

  v_url := current_setting('app.edge_functions_base_url', true);
  v_key := current_setting('app.service_role_key', true);
  if v_url is not null and v_key is not null and v_url <> '' and v_key <> '' then
    perform net.http_post(
      url     := v_url || '/dispatch_meeting_change_push',
      headers := jsonb_build_object(
        'Content-Type', 'application/json',
        'Authorization', 'Bearer ' || v_key
      ),
      body := jsonb_build_object(
        'meeting_id',                p_meeting_id::text,
        'kind',                      'cancelled',
        'meeting_title',             coalesce(v_meeting.title, ''),
        'meeting_starts_at',         v_meeting.starts_at::text,
        'previous_commission_ids',   to_jsonb(v_commissions),
        'new_commission_ids',        '[]'::jsonb
      )
    );
  end if;

  delete from public.meeting where id = p_meeting_id;
end;
$$;

grant execute on function public.delete_meeting_with_dispatch(uuid) to anon, authenticated;

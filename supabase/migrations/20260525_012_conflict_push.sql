-- Migration 012 : conflict-aware push routing on meeting INSERT.
--
-- Updates `fn_dispatch_meeting_push` so it ALSO invokes the dedicated
-- `dispatch_conflict_push` Edge Function when the new meeting overlaps an
-- existing engagement of one or more members. The standard
-- `dispatch_meeting_push` Edge Function (migration 010) is then expected to
-- exclude conflicted members from its target list so each user receives
-- exactly one push: either "Nouvelle réunion" (FR28) or "Conflit détecté"
-- (FR29).
--
-- Server re-computes detection at trigger time (AC2 in story 4.4) so the
-- client cannot suppress the conflict push by lying about its draft state.

-- The previous trigger fired at AFTER INSERT on `meeting` *before* the
-- client wrote its meeting_commission rows in the same transaction. Switch
-- to a deferred constraint trigger so it runs at COMMIT time, by which
-- point all link rows are visible.
drop trigger if exists trg_dispatch_meeting_push on public.meeting;

create or replace function public.fn_dispatch_meeting_push()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
  v_url        text;
  v_key        text;
  v_has_conflict boolean;
  v_commission_ids uuid[];
begin
  v_url := current_setting('app.edge_functions_base_url', true);
  v_key := current_setting('app.service_role_key', true);
  if v_url is null or v_key is null or v_url = '' or v_key = '' then
    return new;
  end if;

  -- Gather the commissions linked to this meeting. The client inserts the
  -- meeting row first and the meeting_commission rows in a separate
  -- statement, so the link rows may not exist yet at AFTER INSERT time.
  -- We re-read them here defensively.
  select coalesce(array_agg(commission_id), array[]::uuid[])
    into v_commission_ids
    from public.meeting_commission
   where meeting_id = new.id;

  if array_length(v_commission_ids, 1) is null then
    -- No commissions linked yet — the client will trigger the dispatch
    -- via the dedicated edge function call (see CreateMeetingRepository).
    return new;
  end if;

  select exists (
    select 1
      from public.detect_conflicts(new.project_id, v_commission_ids, new.starts_at, new.ends_at)
  ) into v_has_conflict;

  -- Always notify non-conflicted members of the newly created meeting.
  perform net.http_post(
    url := v_url || '/dispatch_meeting_push',
    headers := jsonb_build_object(
      'Content-Type', 'application/json',
      'Authorization', 'Bearer ' || v_key
    ),
    body := jsonb_build_object('meeting_id', new.id::text)
  );

  if v_has_conflict then
    -- Notify the conflicted members with the dedicated conflict push so
    -- they can arbitrate (FR29). The Edge Function recomputes the list,
    -- looks up FCM/APNs tokens, and retries 3x with backoff (300/900/2700).
    perform net.http_post(
      url := v_url || '/dispatch_conflict_push',
      headers := jsonb_build_object(
        'Content-Type', 'application/json',
        'Authorization', 'Bearer ' || v_key
      ),
      body := jsonb_build_object('meeting_id', new.id::text)
    );
  end if;

  return new;
end;
$$;

-- Constraint triggers can be deferred to the end of the transaction. When
-- the client calls `create_meeting_with_commissions` (defined below), the
-- meeting row + its meeting_commission links are inserted in the same
-- transaction, so the links are visible to fn_dispatch_meeting_push by
-- the time it runs.
create constraint trigger trg_dispatch_meeting_push
  after insert on public.meeting
  deferrable initially deferred
  for each row execute function public.fn_dispatch_meeting_push();

-- ----- create_meeting_with_commissions : single-transaction insert of a
-- meeting row plus its meeting_commission link rows. Used by
-- DefaultMeetingsRepository.create so the deferred trigger has the full
-- (meeting + links) picture at COMMIT time.
create or replace function public.create_meeting_with_commissions(
  p_project_id           uuid,
  p_title                text,
  p_starts_at            timestamptz,
  p_ends_at              timestamptz,
  p_commission_ids       uuid[],
  p_created_by_member_id uuid
)
returns public.meeting
language plpgsql
security definer
set search_path = public
as $$
declare
  v_meeting public.meeting;
  v_cid     uuid;
begin
  if array_length(p_commission_ids, 1) is null then
    raise exception 'at least one commission required';
  end if;

  insert into public.meeting (
    project_id, title, starts_at, ends_at, created_by_member_id
  ) values (
    p_project_id, nullif(trim(p_title), ''), p_starts_at, p_ends_at, p_created_by_member_id
  )
  returning * into v_meeting;

  foreach v_cid in array p_commission_ids loop
    insert into public.meeting_commission (meeting_id, commission_id)
    values (v_meeting.id, v_cid);
  end loop;

  return v_meeting;
end;
$$;

grant execute on function public.create_meeting_with_commissions(
  uuid, text, timestamptz, timestamptz, uuid[], uuid
) to anon, authenticated;

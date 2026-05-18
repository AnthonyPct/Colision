-- Migration 006 : performance advisor fixes.
--
-- (1) Two FK columns lacked covering indexes (perf advisor 0001):
--     - meeting.created_by_member_id (FK → member.id)
--     - arbitration.conflicting_meeting_id (FK → meeting.id)
-- (2) Three RLS policies call auth.uid() / auth.role() once per row
--     (perf advisor 0003 auth_rls_initplan). Wrapping in `(select ...)`
--     turns these into initplan-cached calls evaluated once per query.
--     Other RLS policies route auth.uid() through current_device_id()
--     (a security-definer wrapper) and are already efficient.

-- (1) ---------------------------------------------------------------
create index if not exists meeting_created_by_member_id_idx
  on public.meeting (created_by_member_id);

create index if not exists arbitration_conflicting_meeting_id_idx
  on public.arbitration (conflicting_meeting_id);

-- (2) ---------------------------------------------------------------
drop policy if exists "device sees its own row" on public.device;
create policy "device sees its own row"
  on public.device for select
  using (auth_user_id = (select auth.uid()));

drop policy if exists "device updates its own row" on public.device;
create policy "device updates its own row"
  on public.device for update
  using (auth_user_id = (select auth.uid()))
  with check (auth_user_id = (select auth.uid()));

drop policy if exists "anyone authenticated can create project" on public.project;
create policy "anyone authenticated can create project"
  on public.project for insert
  with check ((select auth.role()) = 'authenticated');

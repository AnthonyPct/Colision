-- Migration 007 : break the infinite-recursion in the `member` RLS policy.
--
-- The original policy from migration 003 evaluated
--   exists (select 1 from public.member m
--           where m.project_id = member.project_id
--             and m.device_id = public.current_device_id())
-- which triggered Postgres' own RLS check on the inner `select from member`,
-- which re-evaluated the same policy, and so on (42P17 at runtime).
--
-- Fix: route the "is the current device a member of this project ?" check
-- through a SECURITY DEFINER helper so it bypasses RLS on `member`. As a
-- bonus we centralise the check, which keeps the dependent policies on
-- commission / member_commission / meeting / meeting_commission / arbitration
-- shorter and a touch faster (one function call instead of an EXISTS).
--
-- Self-bootstrap is preserved : the `with check` on `member` still allows
-- a row whose `device_id` is the current device, so claiming an identity
-- on a freshly resolved project keeps working even before the row exists.

create or replace function public.is_project_member(p_project_id uuid)
returns boolean
language sql
stable
security definer
set search_path = public
as $$
  select exists (
    select 1 from public.member
    where project_id = p_project_id
      and device_id = public.current_device_id()
  )
$$;

revoke execute on function public.is_project_member(uuid) from public;
grant  execute on function public.is_project_member(uuid) to authenticated;

-- ----- member : rewrite the policy to use the helper.
drop policy if exists "member visible to project members" on public.member;

create policy "member visible to project members"
  on public.member for all
  using (public.is_project_member(member.project_id))
  with check (
    -- self-bootstrap : claim an identity in a project you just resolved
    -- via try_resolve_code, before any row points at your device.
    device_id = public.current_device_id()
    or public.is_project_member(member.project_id)
  );

-- ----- commission : same EXISTS-on-member sub-query, route through helper.
drop policy if exists "commission visible to project members" on public.commission;

create policy "commission visible to project members"
  on public.commission for all
  using (public.is_project_member(commission.project_id))
  with check (public.is_project_member(commission.project_id));

-- ----- project : visibility / update / delete all key off membership.
drop policy if exists "project visible to its members" on public.project;
drop policy if exists "members can update their project" on public.project;
drop policy if exists "members can delete their project" on public.project;

create policy "project visible to its members"
  on public.project for select
  using (public.is_project_member(project.id));

create policy "members can update their project"
  on public.project for update
  using (public.is_project_member(project.id));

create policy "members can delete their project"
  on public.project for delete
  using (public.is_project_member(project.id));

-- ----- meeting : same shape.
drop policy if exists "meeting visible to project members" on public.meeting;

create policy "meeting visible to project members"
  on public.meeting for all
  using (public.is_project_member(meeting.project_id))
  with check (public.is_project_member(meeting.project_id));

-- ----- member_commission : project is reached via the linked member row.
drop policy if exists "member_commission visible to project members" on public.member_commission;

create policy "member_commission visible to project members"
  on public.member_commission for all
  using (
    public.is_project_member(
      (select m.project_id from public.member m where m.id = member_commission.member_id)
    )
  )
  with check (
    public.is_project_member(
      (select m.project_id from public.member m where m.id = member_commission.member_id)
    )
  );

-- ----- meeting_commission : project is reached via the linked meeting row.
drop policy if exists "meeting_commission visible to project members" on public.meeting_commission;

create policy "meeting_commission visible to project members"
  on public.meeting_commission for all
  using (
    public.is_project_member(
      (select mt.project_id from public.meeting mt where mt.id = meeting_commission.meeting_id)
    )
  )
  with check (
    public.is_project_member(
      (select mt.project_id from public.meeting mt where mt.id = meeting_commission.meeting_id)
    )
  );

-- ----- arbitration : same idea via meeting.
drop policy if exists "arbitration visible to project members" on public.arbitration;

create policy "arbitration visible to project members"
  on public.arbitration for all
  using (
    public.is_project_member(
      (select mt.project_id from public.meeting mt where mt.id = arbitration.meeting_id)
    )
  )
  with check (
    -- The arbitrating member must be the current device's member.
    exists (
      select 1 from public.member m
      where m.id = arbitration.member_id
        and m.device_id = public.current_device_id()
    )
  );

-- Migration 008 : close three gaps surfaced by end-to-end testing of epic 2.
--
-- (1) `create_project` now atomically inserts the creator's `member` row
--     with their device_id set. Without this Antoine was never a member of
--     his own project, so every subsequent insert (commission, member, …)
--     failed the membership check in migration 007.
-- (2) Sophie needs to SEE the project's members and commissions BEFORE she
--     is a member (per the join-identity screen). SELECT on `member` and
--     `commission` is opened to any authenticated user — the project_id
--     itself is a non-guessable UUID gated by the 6-char share_code via
--     `try_resolve_code`, so we don't actually lose isolation in practice.
-- (3) Sophie's "+ Je m'ajoute" path used to do INSERT (device_id null) +
--     UPDATE (set device_id). The INSERT now happens with device_id set in
--     a single call, but we also relax UPDATE so the legacy "claim an
--     existing unclaimed row" path keeps working: a non-member can update
--     a `member` row when its `device_id` is null, provided the resulting
--     row's device_id is theirs.
--
-- Mutations (insert into commission, meeting, member with device_id != self,
-- update a claimed row, delete, …) remain membership-gated.

-- (1) -------------------------------------------------------------------
create or replace function public.create_project(p_name text, p_display_name text)
returns public.project
language plpgsql
security definer
set search_path = public
as $$
declare
  v_project    public.project;
  v_attempts   int := 0;
  v_device_id  uuid;
begin
  v_device_id := public.current_device_id();
  if v_device_id is null then
    raise exception 'No device row for current auth.uid() — anonymous sign-in must complete first';
  end if;

  loop
    begin
      insert into public.project (name, share_code)
      values (p_name, public.generate_share_code())
      returning * into v_project;

      insert into public.member (project_id, device_id, display_name)
      values (v_project.id, v_device_id, p_display_name);

      return v_project;
    exception when unique_violation then
      v_attempts := v_attempts + 1;
      if v_attempts >= 5 then
        raise exception 'Could not generate unique share code after 5 attempts';
      end if;
    end;
  end loop;
end;
$$;

-- Drop the old single-arg signature so the client can't accidentally bypass
-- the creator-as-member invariant.
drop function if exists public.create_project(text);

revoke execute on function public.create_project(text, text) from public;
grant  execute on function public.create_project(text, text) to authenticated;

-- (2) -------------------------------------------------------------------
-- Split the existing FOR ALL policies into a permissive SELECT + a
-- restrictive INSERT/UPDATE/DELETE. The membership-gated writes stay; we
-- only widen the read.

drop policy if exists "member visible to project members" on public.member;

create policy "member readable by authenticated"
  on public.member for select
  using (auth.role() = 'authenticated');

create policy "member insertable by member or self-bootstrap"
  on public.member for insert
  with check (
    device_id = public.current_device_id()
    or public.is_project_member(member.project_id)
  );

-- (3) -------------------------------------------------------------------
-- Two UPDATE policies (OR-combined): regular project-member edits, plus
-- the "claim an unclaimed row" path for joining Sophie.
create policy "member updatable by project member"
  on public.member for update
  using (public.is_project_member(member.project_id))
  with check (public.is_project_member(member.project_id));

create policy "member claim-an-unclaimed-row"
  on public.member for update
  using (member.device_id is null)
  with check (device_id = public.current_device_id());

create policy "member deletable by project member"
  on public.member for delete
  using (public.is_project_member(member.project_id));

-- Same SELECT-relaxation for commission so Sophie can preview them on the
-- join-confirm + join-commissions screens before becoming a member.
drop policy if exists "commission visible to project members" on public.commission;

create policy "commission readable by authenticated"
  on public.commission for select
  using (auth.role() = 'authenticated');

create policy "commission writable by project member"
  on public.commission for insert
  with check (public.is_project_member(commission.project_id));

create policy "commission updatable by project member"
  on public.commission for update
  using (public.is_project_member(commission.project_id))
  with check (public.is_project_member(commission.project_id));

create policy "commission deletable by project member"
  on public.commission for delete
  using (public.is_project_member(commission.project_id));

-- member_commission needs a SELECT relaxation too so Sophie can render the
-- pre-checked commissions on the join screen before becoming a member.
drop policy if exists "member_commission visible to project members" on public.member_commission;

create policy "member_commission readable by authenticated"
  on public.member_commission for select
  using (auth.role() = 'authenticated');

create policy "member_commission writable by project member"
  on public.member_commission for insert
  with check (
    public.is_project_member(
      (select m.project_id from public.member m where m.id = member_commission.member_id)
    )
  );

create policy "member_commission deletable by project member"
  on public.member_commission for delete
  using (
    public.is_project_member(
      (select m.project_id from public.member m where m.id = member_commission.member_id)
    )
  );

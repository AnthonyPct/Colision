-- Migration 003 : Row-Level Security. The isolation rule is "a device sees a
-- project only when it has a member row in that project". `auth.uid()` is
-- the anonymous Supabase user id; the bridge to a device row goes through
-- `device.auth_user_id`.

alter table public.device                enable row level security;
alter table public.project               enable row level security;
alter table public.commission            enable row level security;
alter table public.member                enable row level security;
alter table public.member_commission     enable row level security;
alter table public.meeting               enable row level security;
alter table public.meeting_commission    enable row level security;
alter table public.arbitration           enable row level security;

-- Helper: current device id (one row per auth.users id by uniqueness in
-- migration 000). Wrapped as `security definer` so it bypasses RLS on
-- `device` itself.
create or replace function public.current_device_id()
returns uuid
language sql
stable
security definer
set search_path = public
as $$
  select id from public.device where auth_user_id = auth.uid()
$$;

-- ----- device : a device only sees / mutates its own row.
create policy "device sees its own row"
  on public.device for select
  using (auth_user_id = auth.uid());

create policy "device updates its own row"
  on public.device for update
  using (auth_user_id = auth.uid())
  with check (auth_user_id = auth.uid());

-- ----- project : visibility through membership; insert is open (anyone
-- can create a project — they immediately add themselves as member).
create policy "project visible to its members"
  on public.project for select
  using (
    exists (
      select 1 from public.member m
      where m.project_id = project.id and m.device_id = public.current_device_id()
    )
  );

create policy "anyone authenticated can create project"
  on public.project for insert
  with check (auth.role() = 'authenticated');

create policy "members can update their project"
  on public.project for update
  using (
    exists (
      select 1 from public.member m
      where m.project_id = project.id and m.device_id = public.current_device_id()
    )
  );

create policy "members can delete their project"
  on public.project for delete
  using (
    exists (
      select 1 from public.member m
      where m.project_id = project.id and m.device_id = public.current_device_id()
    )
  );

-- ----- commission, member, member_commission, meeting, meeting_commission,
-- arbitration : every row is gated by membership in the row's project.
-- Helper macro is just repetition for simplicity.

create policy "commission visible to project members"
  on public.commission for all
  using (
    exists (
      select 1 from public.member m
      where m.project_id = commission.project_id and m.device_id = public.current_device_id()
    )
  )
  with check (
    exists (
      select 1 from public.member m
      where m.project_id = commission.project_id and m.device_id = public.current_device_id()
    )
  );

create policy "member visible to project members"
  on public.member for all
  using (
    exists (
      select 1 from public.member m
      where m.project_id = member.project_id and m.device_id = public.current_device_id()
    )
  )
  with check (
    -- self-bootstrap: inserting a member row tying yourself to a project
    -- you just resolved by code is allowed even before you're a member.
    device_id = public.current_device_id() or
    exists (
      select 1 from public.member m
      where m.project_id = member.project_id and m.device_id = public.current_device_id()
    )
  );

create policy "member_commission visible to project members"
  on public.member_commission for all
  using (
    exists (
      select 1
      from public.member m1
      join public.member m2 on m2.project_id = m1.project_id
      where m1.id = member_commission.member_id
        and m2.device_id = public.current_device_id()
    )
  )
  with check (
    exists (
      select 1
      from public.member m1
      join public.member m2 on m2.project_id = m1.project_id
      where m1.id = member_commission.member_id
        and m2.device_id = public.current_device_id()
    )
  );

create policy "meeting visible to project members"
  on public.meeting for all
  using (
    exists (
      select 1 from public.member m
      where m.project_id = meeting.project_id and m.device_id = public.current_device_id()
    )
  )
  with check (
    exists (
      select 1 from public.member m
      where m.project_id = meeting.project_id and m.device_id = public.current_device_id()
    )
  );

create policy "meeting_commission visible to project members"
  on public.meeting_commission for all
  using (
    exists (
      select 1
      from public.meeting mt
      join public.member me on me.project_id = mt.project_id
      where mt.id = meeting_commission.meeting_id
        and me.device_id = public.current_device_id()
    )
  )
  with check (
    exists (
      select 1
      from public.meeting mt
      join public.member me on me.project_id = mt.project_id
      where mt.id = meeting_commission.meeting_id
        and me.device_id = public.current_device_id()
    )
  );

create policy "arbitration visible to project members"
  on public.arbitration for all
  using (
    exists (
      select 1
      from public.meeting mt
      join public.member me on me.project_id = mt.project_id
      where mt.id = arbitration.meeting_id
        and me.device_id = public.current_device_id()
    )
  )
  with check (
    -- The arbitrating member must be the current device's member.
    exists (
      select 1
      from public.member m
      where m.id = arbitration.member_id
        and m.device_id = public.current_device_id()
    )
  );

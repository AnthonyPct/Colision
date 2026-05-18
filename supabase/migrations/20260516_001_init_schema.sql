-- Migration 001 : core domain schema (7 tables + their FK / CHECK
-- constraints + `updated_at` triggers). The 8th table (`device`) is created
-- in migration 000.

create table if not exists public.project (
  id uuid primary key default gen_random_uuid(),
  name text not null,
  share_code char(6) not null unique,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create trigger set_updated_at_project
  before update on public.project
  for each row execute function public.set_updated_at();

create table if not exists public.commission (
  id uuid primary key default gen_random_uuid(),
  project_id uuid not null references public.project(id) on delete cascade,
  name text not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create trigger set_updated_at_commission
  before update on public.commission
  for each row execute function public.set_updated_at();

create table if not exists public.member (
  id uuid primary key default gen_random_uuid(),
  project_id uuid not null references public.project(id) on delete cascade,
  device_id uuid references public.device(id) on delete set null,
  display_name text not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create trigger set_updated_at_member
  before update on public.member
  for each row execute function public.set_updated_at();

create table if not exists public.member_commission (
  member_id uuid not null references public.member(id) on delete cascade,
  commission_id uuid not null references public.commission(id) on delete cascade,
  primary key (member_id, commission_id)
);

create table if not exists public.meeting (
  id uuid primary key default gen_random_uuid(),
  project_id uuid not null references public.project(id) on delete cascade,
  title text,
  starts_at timestamptz not null,
  ends_at timestamptz not null,
  created_by_member_id uuid references public.member(id) on delete set null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  check (ends_at > starts_at)
);

create trigger set_updated_at_meeting
  before update on public.meeting
  for each row execute function public.set_updated_at();

create table if not exists public.meeting_commission (
  meeting_id uuid not null references public.meeting(id) on delete cascade,
  commission_id uuid not null references public.commission(id) on delete cascade,
  primary key (meeting_id, commission_id)
);

create table if not exists public.arbitration (
  id uuid primary key default gen_random_uuid(),
  member_id uuid not null references public.member(id) on delete cascade,
  meeting_id uuid not null references public.meeting(id) on delete cascade,
  conflicting_meeting_id uuid not null references public.meeting(id) on delete cascade,
  decided_at timestamptz not null default now(),
  unique (member_id, meeting_id, conflicting_meeting_id)
);

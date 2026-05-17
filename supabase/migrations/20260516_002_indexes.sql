-- Migration 002 : performance indexes. NFR-P2 (`detect_conflicts < 200ms`
-- over 1000 meetings) leans on the `meeting (project_id, starts_at, ends_at)`
-- composite index. Other indexes accelerate lookups by FK / share_code.

create index if not exists project_share_code_idx on public.project (share_code);

create index if not exists commission_project_id_idx on public.commission (project_id);

create index if not exists member_project_id_idx on public.member (project_id);
create index if not exists member_device_id_idx on public.member (device_id);

create index if not exists meeting_project_id_starts_at_ends_at_idx
  on public.meeting (project_id, starts_at, ends_at);

create index if not exists meeting_commission_commission_id_idx
  on public.meeting_commission (commission_id);

create index if not exists member_commission_commission_id_idx
  on public.member_commission (commission_id);

create index if not exists arbitration_meeting_id_idx on public.arbitration (meeting_id);
create index if not exists arbitration_member_id_idx on public.arbitration (member_id);

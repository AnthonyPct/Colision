-- Migration 004 : business-logic Postgres functions consumed via
-- `supabase.postgrest.rpc(...)`. All exposed functions are `security definer`
-- with a locked-down `search_path` so RLS does not block them and so they
-- can be safely called from the anonymous-auth client.

-- ----- share code generator (30-char ambiguity-free alphabet → ~730M space)
create or replace function public.generate_share_code()
returns char(6)
language plpgsql
volatile
as $$
declare
  alphabet text := '23456789ACDEFGHJKMNPQRSTUVWXYZ';
  result   text := '';
  i        int;
begin
  for i in 1..6 loop
    result := result || substr(alphabet, 1 + floor(random() * length(alphabet))::int, 1);
  end loop;
  return result;
end;
$$;

-- ----- create_project : insert a project with a unique share_code. Retries
-- up to 5 times on the rare unique-violation case (730M space → essentially
-- never collides in practice).
create or replace function public.create_project(p_name text)
returns public.project
language plpgsql
security definer
set search_path = public
as $$
declare
  v_project public.project;
  v_attempts int := 0;
begin
  loop
    begin
      insert into public.project (name, share_code)
      values (p_name, public.generate_share_code())
      returning * into v_project;
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

-- ----- try_resolve_code : look up a project by its share_code. Raises
-- P0002 (NO_DATA_FOUND) when the code does not exist, which maps to
-- AppError.ProjectCodeInvalid client-side.
create or replace function public.try_resolve_code(p_code text)
returns public.project
language plpgsql
security definer
set search_path = public
as $$
declare
  v_project public.project;
begin
  select * into v_project
  from public.project
  where share_code = upper(p_code)
  limit 1;

  if not found then
    raise exception 'project code not found' using errcode = 'P0002';
  end if;

  return v_project;
end;
$$;

-- ----- detect_conflicts : core scheduling primitive. Given a project, a
-- list of commission ids and a [start, end) range, returns one row per
-- (member, conflicting meeting) pair where:
--   * the member belongs to at least one of p_commission_ids,
--   * the member also belongs to another commission that already has a
--     meeting overlapping the requested range.
-- Conflict definition: two meetings overlap iff
--   m.starts_at < p_end AND m.ends_at > p_start
-- The composite index on meeting (project_id, starts_at, ends_at) keeps
-- this under the NFR-P2 200ms budget for 1000 seeded meetings.
create or replace function public.detect_conflicts(
  p_project_id      uuid,
  p_commission_ids  uuid[],
  p_start           timestamptz,
  p_end             timestamptz
)
returns table (
  member_id            uuid,
  member_display_name  text,
  meeting_id           uuid,
  meeting_title        text,
  meeting_starts_at    timestamptz,
  meeting_ends_at      timestamptz,
  commission_id        uuid,
  commission_name      text
)
language sql
stable
security definer
set search_path = public
as $$
  with affected_members as (
    select distinct m.id, m.display_name
    from public.member m
    join public.member_commission mc on mc.member_id = m.id
    where m.project_id = p_project_id
      and mc.commission_id = any (p_commission_ids)
  )
  select
    am.id              as member_id,
    am.display_name    as member_display_name,
    mt.id              as meeting_id,
    mt.title           as meeting_title,
    mt.starts_at       as meeting_starts_at,
    mt.ends_at         as meeting_ends_at,
    c.id               as commission_id,
    c.name             as commission_name
  from affected_members am
  join public.member_commission mc2 on mc2.member_id = am.id
  join public.commission c on c.id = mc2.commission_id
  join public.meeting_commission mtc on mtc.commission_id = c.id
  join public.meeting mt on mt.id = mtc.meeting_id
  where mt.project_id = p_project_id
    and mt.starts_at < p_end
    and mt.ends_at   > p_start
  order by am.display_name, mt.starts_at;
$$;

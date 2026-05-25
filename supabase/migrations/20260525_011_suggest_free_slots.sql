-- Migration 011 : suggest_free_slots — Postgres function returning N
-- conflict-free time slots near the requested anchor for a given set of
-- commissions. Backs story 4.3 (FR23).
--
-- Algorithm: step every 30 minutes through [anchor − window, anchor + window],
-- filter to the 08:00–22:00 local window (Europe/Paris — civic-association
-- working hours), and emit slots where `detect_conflicts` returns no rows.
-- After a hit we jump 4h ahead to avoid clustering several near-identical
-- proposals.
--
-- Latency budget: < 500 ms (story 4.3 AC5). The composite index on
-- meeting(project_id, starts_at, ends_at) keeps detect_conflicts cheap.

create or replace function public.suggest_free_slots(
  p_project_id     uuid,
  p_commission_ids uuid[],
  p_anchor         timestamptz,
  p_duration_min   int,
  p_window_days    int default 7,
  p_limit          int default 5
)
returns table (
  slot_starts_at timestamptz,
  slot_ends_at   timestamptz
)
language plpgsql
stable
security definer
set search_path = public
as $$
declare
  v_step      interval := interval '30 minutes';
  v_dur       interval := (p_duration_min || ' minutes')::interval;
  -- Clamp the lower bound to now() so we never propose a slot in the past
  -- when the anchor is close to (or behind) the current time. v_cur is then
  -- rounded *up* to the next 30 min for a clean grid.
  v_lower     timestamptz := greatest(p_anchor - (p_window_days || ' days')::interval, now());
  v_upper     timestamptz := p_anchor + (p_window_days || ' days')::interval;
  v_cur       timestamptz := date_trunc('hour', v_lower)
                             + (ceil(extract(minute from v_lower) / 30.0)::int * interval '30 minutes');
  v_count     int := 0;
  v_local     time;
begin
  if p_duration_min is null or p_duration_min <= 0 then
    return;
  end if;
  while v_cur <= v_upper and v_count < p_limit loop
    v_local := (v_cur at time zone 'Europe/Paris')::time;
    if v_local >= time '08:00' and v_local <= time '22:00' then
      if not exists (
        select 1
        from public.detect_conflicts(
          p_project_id,
          p_commission_ids,
          v_cur,
          v_cur + v_dur
        )
      ) then
        slot_starts_at := v_cur;
        slot_ends_at   := v_cur + v_dur;
        return next;
        v_count := v_count + 1;
        v_cur := v_cur + interval '4 hours';
        continue;
      end if;
    end if;
    v_cur := v_cur + v_step;
  end loop;
end;
$$;

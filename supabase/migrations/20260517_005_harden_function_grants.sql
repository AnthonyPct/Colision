-- Migration 005 : hardening per Supabase database-linter advisors.
--
-- (1) Fix the SECURITY lint 0011 (function_search_path_mutable) on the two
--     functions that were missing `set search_path = public`.
-- (2) Tighten EXECUTE grants on every public function. Postgres' default
--     `grant execute on function ... to public` is what makes them
--     callable by anon/authenticated, so the only effective fix is to
--     REVOKE from PUBLIC and then GRANT back to `authenticated` only for
--     the three RPCs the client actually calls.

-- (1) ---------------------------------------------------------------
create or replace function public.set_updated_at()
returns trigger
language plpgsql
set search_path = public
as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

create or replace function public.generate_share_code()
returns char(6)
language plpgsql
volatile
set search_path = public
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

-- (2) ---------------------------------------------------------------
revoke execute on function public.set_updated_at()                                       from public;
revoke execute on function public.handle_new_auth_user()                                 from public;
revoke execute on function public.current_device_id()                                    from public;
revoke execute on function public.generate_share_code()                                  from public;
revoke execute on function public.create_project(text)                                   from public;
revoke execute on function public.try_resolve_code(text)                                 from public;
revoke execute on function public.detect_conflicts(uuid, uuid[], timestamptz, timestamptz) from public;

grant execute on function public.create_project(text)                                    to authenticated;
grant execute on function public.try_resolve_code(text)                                  to authenticated;
grant execute on function public.detect_conflicts(uuid, uuid[], timestamptz, timestamptz) to authenticated;

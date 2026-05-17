-- Migration 000 : auth bridge — every anonymous Supabase user gets a row in
-- the public `device` table on signup. The trigger fires after the user is
-- inserted in `auth.users` by `supabase.auth.signInAnonymously()`.

create table if not exists public.device (
  id uuid primary key default gen_random_uuid(),
  auth_user_id uuid not null references auth.users(id) on delete cascade,
  fcm_token text,
  apns_token text,
  platform text check (platform in ('android', 'ios')),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique (auth_user_id)
);

create or replace function public.set_updated_at()
returns trigger
language plpgsql
as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

create trigger set_updated_at_device
  before update on public.device
  for each row execute function public.set_updated_at();

-- on_auth_user_created : whenever auth.users gets a new row (anonymous or
-- otherwise), insert a corresponding device row. Platform is unknown at
-- this point — the client fills it later via UPDATE.
create or replace function public.handle_new_auth_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  insert into public.device (auth_user_id)
  values (new.id)
  on conflict (auth_user_id) do nothing;
  return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.handle_new_auth_user();

-- Migration 016 : app_config — configuration applicative lue au démarrage.
--
-- Sert la "gate" de mise à jour in-app : l'app compare sa version installée à
-- latest_version / min_supported_version et affiche un dialog (optionnel ou
-- bloquant) renvoyant vers le store. Table singleton (une seule ligne, id=1),
-- lisible par tous les clients (aucune donnée sensible).

create table if not exists public.app_config (
  id int primary key default 1,
  min_supported_version text not null default '1.0.0',
  latest_version text not null default '1.0.0',
  android_store_url text not null default '',
  ios_store_url text not null default '',
  update_message text,
  updated_at timestamptz not null default now(),
  constraint app_config_singleton check (id = 1)
);

create trigger set_updated_at_app_config
  before update on public.app_config
  for each row execute function public.set_updated_at();

alter table public.app_config enable row level security;

create policy "app_config readable by everyone"
  on public.app_config for select
  using (true);

insert into public.app_config (
  id, min_supported_version, latest_version, android_store_url, ios_store_url, update_message
) values (
  1,
  '1.0.0',
  '1.2.1',
  'https://play.google.com/store/apps/details?id=com.anthooop.colision',
  'https://apps.apple.com/app/id000000000',
  null
) on conflict (id) do nothing;

-- Migration 015 : Sondages (polls).
--
-- Feature demandée par le conseil de Caulnes (premiers testeurs). Un membre
-- crée un sondage (question + >=2 réponses + date de clôture) ciblé soit sur
-- une/plusieurs commissions (seuls leurs membres votent), soit "public"
-- (tous les membres du projet votent). Après `closes_at`, le vote est refusé
-- côté serveur. Un membre a un seul vote par sondage, modifiable tant que le
-- sondage est ouvert.
--
-- Le modèle reprend la structure meeting/meeting_commission (migration 001) :
-- `poll_commission` est la table de jonction ciblage, renseignée uniquement
-- quand `target_type = 'commissions'`. Le statut ouvert/clos n'est PAS stocké
-- (dérivé de closes_at vs now()) — pas de cron de clôture.
--
-- Écritures via RPC SECURITY DEFINER (create_poll / cast_vote / delete_poll),
-- comme create_meeting_with_commissions (migration 012). Push aux membres
-- éligibles à la création via dispatch_poll_push (miroir migration 010/012).

-- ---------------------------------------------------------------------------
-- Tables
-- ---------------------------------------------------------------------------

create table if not exists public.poll (
  id uuid primary key default gen_random_uuid(),
  project_id uuid not null references public.project(id) on delete cascade,
  created_by_member_id uuid references public.member(id) on delete set null,
  question text not null,
  target_type text not null check (target_type in ('public', 'commissions')),
  closes_at timestamptz not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create trigger set_updated_at_poll
  before update on public.poll
  for each row execute function public.set_updated_at();

create table if not exists public.poll_option (
  id uuid primary key default gen_random_uuid(),
  poll_id uuid not null references public.poll(id) on delete cascade,
  label text not null,
  position int not null,
  created_at timestamptz not null default now()
);

-- Jonction ciblage : présente seulement si target_type = 'commissions'.
create table if not exists public.poll_commission (
  poll_id uuid not null references public.poll(id) on delete cascade,
  commission_id uuid not null references public.commission(id) on delete cascade,
  primary key (poll_id, commission_id)
);

-- Un vote par (sondage, membre) ; changer de vote = update de option_id.
create table if not exists public.poll_vote (
  poll_id uuid not null references public.poll(id) on delete cascade,
  member_id uuid not null references public.member(id) on delete cascade,
  option_id uuid not null references public.poll_option(id) on delete cascade,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  primary key (poll_id, member_id)
);

create trigger set_updated_at_poll_vote
  before update on public.poll_vote
  for each row execute function public.set_updated_at();

-- ---------------------------------------------------------------------------
-- Indexes (cf. migration 002)
-- ---------------------------------------------------------------------------

create index if not exists poll_project_id_idx on public.poll (project_id);
create index if not exists poll_option_poll_id_idx on public.poll_option (poll_id);
create index if not exists poll_commission_commission_id_idx on public.poll_commission (commission_id);
create index if not exists poll_vote_poll_id_idx on public.poll_vote (poll_id);
create index if not exists poll_vote_option_id_idx on public.poll_vote (option_id);

-- ---------------------------------------------------------------------------
-- RLS : lecture gatée par is_project_member (migration 007). Toutes les
-- écritures passent par des RPC SECURITY DEFINER (ci-dessous) qui contournent
-- la RLS, donc seules des policies SELECT sont nécessaires.
-- ---------------------------------------------------------------------------

alter table public.poll            enable row level security;
alter table public.poll_option     enable row level security;
alter table public.poll_commission enable row level security;
alter table public.poll_vote       enable row level security;

create policy "poll visible to project members"
  on public.poll for select
  using (public.is_project_member(poll.project_id));

create policy "poll_option visible to project members"
  on public.poll_option for select
  using (
    public.is_project_member(
      (select p.project_id from public.poll p where p.id = poll_option.poll_id)
    )
  );

create policy "poll_commission visible to project members"
  on public.poll_commission for select
  using (
    public.is_project_member(
      (select p.project_id from public.poll p where p.id = poll_commission.poll_id)
    )
  );

create policy "poll_vote visible to project members"
  on public.poll_vote for select
  using (
    public.is_project_member(
      (select p.project_id from public.poll p where p.id = poll_vote.poll_id)
    )
  );

-- ---------------------------------------------------------------------------
-- push_failure_log : ajouter poll_id pour tracer les échecs de push sondage.
-- ---------------------------------------------------------------------------

alter table public.push_failure_log
  add column if not exists poll_id uuid references public.poll(id) on delete set null;

-- ---------------------------------------------------------------------------
-- RPC create_poll : insère le sondage + ses options + (si ciblage commission)
-- ses liens poll_commission dans une seule transaction, comme
-- create_meeting_with_commissions (migration 012). L'insert sur `poll`
-- déclenche le trigger différé de dispatch push au COMMIT, quand options et
-- liens sont visibles.
-- ---------------------------------------------------------------------------

create or replace function public.create_poll(
  p_project_id           uuid,
  p_question             text,
  p_target_type          text,
  p_commission_ids       uuid[],
  p_option_labels        text[],
  p_closes_at            timestamptz,
  p_created_by_member_id uuid
)
returns public.poll
language plpgsql
security definer
set search_path = public
as $$
declare
  v_poll  public.poll;
  v_label text;
  v_pos   int := 0;
  v_cid   uuid;
begin
  if p_target_type not in ('public', 'commissions') then
    raise exception 'invalid target_type %', p_target_type;
  end if;
  if array_length(p_option_labels, 1) is null or array_length(p_option_labels, 1) < 2 then
    raise exception 'at least two options required';
  end if;
  if p_target_type = 'commissions'
     and (array_length(p_commission_ids, 1) is null or array_length(p_commission_ids, 1) < 1) then
    raise exception 'at least one commission required for a commission-scoped poll';
  end if;

  insert into public.poll (
    project_id, created_by_member_id, question, target_type, closes_at
  ) values (
    p_project_id, p_created_by_member_id, trim(p_question), p_target_type, p_closes_at
  )
  returning * into v_poll;

  foreach v_label in array p_option_labels loop
    if trim(v_label) <> '' then
      insert into public.poll_option (poll_id, label, position)
      values (v_poll.id, trim(v_label), v_pos);
      v_pos := v_pos + 1;
    end if;
  end loop;

  if v_pos < 2 then
    raise exception 'at least two non-empty options required';
  end if;

  if p_target_type = 'commissions' then
    foreach v_cid in array p_commission_ids loop
      insert into public.poll_commission (poll_id, commission_id)
      values (v_poll.id, v_cid);
    end loop;
  end if;

  return v_poll;
end;
$$;

grant execute on function public.create_poll(
  uuid, text, text, uuid[], text[], timestamptz, uuid
) to anon, authenticated;

-- ---------------------------------------------------------------------------
-- RPC cast_vote : enregistre / met à jour le vote du membre courant. Valide
-- que le sondage est ouvert, que l'option appartient au sondage, que
-- p_member_id est bien le membre du device courant, et qu'il est éligible
-- (public => membre du projet ; commissions => présent dans une commission
-- ciblée). Upsert sur la PK (poll_id, member_id) => changement de vote.
-- ---------------------------------------------------------------------------

create or replace function public.cast_vote(
  p_poll_id   uuid,
  p_option_id uuid,
  p_member_id uuid
)
returns public.poll_vote
language plpgsql
security definer
set search_path = public
as $$
declare
  v_poll   public.poll;
  v_vote   public.poll_vote;
begin
  select * into v_poll from public.poll where id = p_poll_id;
  if v_poll.id is null then
    raise exception 'poll % not found', p_poll_id;
  end if;

  if v_poll.closes_at <= now() then
    raise exception 'poll % is closed', p_poll_id;
  end if;

  -- L'option doit appartenir au sondage.
  if not exists (
    select 1 from public.poll_option
    where id = p_option_id and poll_id = p_poll_id
  ) then
    raise exception 'option % does not belong to poll %', p_option_id, p_poll_id;
  end if;

  -- Le membre voté doit être celui du device courant, dans le projet du sondage.
  if not exists (
    select 1 from public.member m
    where m.id = p_member_id
      and m.project_id = v_poll.project_id
      and m.device_id = public.current_device_id()
  ) then
    raise exception 'member % is not the current device member of this project', p_member_id;
  end if;

  -- Éligibilité selon le ciblage.
  if v_poll.target_type = 'commissions' then
    if not exists (
      select 1
        from public.poll_commission pc
        join public.member_commission mc on mc.commission_id = pc.commission_id
       where pc.poll_id = p_poll_id
         and mc.member_id = p_member_id
    ) then
      raise exception 'member % not eligible for poll %', p_member_id, p_poll_id;
    end if;
  end if;

  insert into public.poll_vote (poll_id, member_id, option_id)
  values (p_poll_id, p_member_id, p_option_id)
  on conflict (poll_id, member_id)
  do update set option_id = excluded.option_id, updated_at = now()
  returning * into v_vote;

  return v_vote;
end;
$$;

grant execute on function public.cast_vote(uuid, uuid, uuid) to anon, authenticated;

-- ---------------------------------------------------------------------------
-- RPC delete_poll : suppression réservée au créateur (device courant).
-- Cascade sur options / votes / liens commissions.
-- ---------------------------------------------------------------------------

create or replace function public.delete_poll(p_poll_id uuid)
returns void
language plpgsql
security definer
set search_path = public
as $$
declare
  v_poll public.poll;
begin
  select * into v_poll from public.poll where id = p_poll_id;
  if v_poll.id is null then
    return;
  end if;

  if not exists (
    select 1 from public.member m
    where m.id = v_poll.created_by_member_id
      and m.device_id = public.current_device_id()
  ) then
    raise exception 'only the poll creator may delete poll %', p_poll_id;
  end if;

  delete from public.poll where id = p_poll_id;
end;
$$;

grant execute on function public.delete_poll(uuid) to anon, authenticated;

-- ---------------------------------------------------------------------------
-- Push à la création : trigger de contrainte différé (comme migration 012)
-- pour que les options / liens commissions soient visibles au COMMIT. Poste
-- le poll_id à l'Edge Function dispatch_poll_push qui résout les membres
-- éligibles et leurs tokens FCM/APNs.
-- ---------------------------------------------------------------------------

create or replace function public.fn_dispatch_poll_push()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
  v_url text;
  v_key text;
begin
  v_url := current_setting('app.edge_functions_base_url', true);
  v_key := current_setting('app.service_role_key', true);
  if v_url is null or v_key is null or v_url = '' or v_key = '' then
    return new;
  end if;

  perform net.http_post(
    url := v_url || '/dispatch_poll_push',
    headers := jsonb_build_object(
      'Content-Type', 'application/json',
      'Authorization', 'Bearer ' || v_key
    ),
    body := jsonb_build_object('poll_id', new.id::text)
  );

  return new;
end;
$$;

drop trigger if exists trg_dispatch_poll_push on public.poll;
create constraint trigger trg_dispatch_poll_push
  after insert on public.poll
  deferrable initially deferred
  for each row execute function public.fn_dispatch_poll_push();

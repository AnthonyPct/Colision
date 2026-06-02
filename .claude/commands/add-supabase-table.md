---
description: Mirror a Supabase Postgres table into Room (entity, DAO) and the repository layer
argument-hint: <table_name>
---

Mirror the Supabase Postgres table `$ARGUMENTS` into the local Room cache and expose it through a repository.

1. Inspect the Supabase schema for `$ARGUMENTS` (columns, types, nullability, primary key, RLS). Use the Supabase MCP if available; otherwise ask the user to paste the schema.
2. Create the Room entity in `core/database/entity/` — match Postgres types (timestamps → `kotlinx.datetime.Instant` stored via type converter, UUIDs → `String`). Use `@PrimaryKey` on the Postgres primary key.
3. Create the DAO in `core/database/dao/` — at minimum: `observeAll(): Flow<List<Entity>>`, `getById(id): Entity?`, `upsertAll(items)`, `deleteById(id)`.
4. Register the entity in the central `AppDatabase` (`@Database(entities = [...]`) and bump the schema version. Add a migration if existing tables are touched.
5. Create / update the matching `*Repository` to: expose `Flow` from the DAO as source of truth, refresh from supabase-kt in the background, and subscribe to Realtime if the table benefits from live sync.
6. Add the binding to the relevant Koin module.
7. Compile-check and report.

Do not call Supabase REST directly with raw Ktor — always go through the supabase-kt client so auth + RLS are handled.
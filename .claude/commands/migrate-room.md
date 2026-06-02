---
description: Add a Room schema migration when entities or the database version change
argument-hint: <short-description-of-change>
---

Generate a Room schema migration for: `$ARGUMENTS`.

1. Locate `AppDatabase` under `composeApp/src/commonMain/kotlin/com/anthooop/colision/core/database/`. Read its `@Database(entities = [...], version = N)` declaration and the current entity definitions.
2. Compare the **previous** entity shape (from git: `git show HEAD:<path-to-entity>`) with the current one. Build a column-level diff per entity: added / dropped / renamed / type-changed columns, new indices, new entities, dropped entities.
3. Bump the database version to `N + 1` in the `@Database` annotation.
4. Confirm the schema export folder is configured (Room KSP arg `room.schemaLocation` → `composeApp/schemas`). If not, add it to `build.gradle.kts` and tell the user to commit the generated JSON after the build.
5. Create the migration in `core/database/migration/MigrationN_N+1.kt`:
   - Use `Migration(N, N+1)` object with `override fun migrate(connection: SQLiteConnection)`.
   - Use raw SQL — Room does not auto-generate destructive migrations. Hand-write `ALTER TABLE`, `CREATE TABLE`, `INSERT INTO ... SELECT` (for renames / type changes), and `DROP TABLE` as needed.
   - For column renames or type changes on SQLite: create a new table with the right schema, copy data, drop the old table, rename the new one.
6. Register the migration in the database builder where Koin wires `AppDatabase` (typically `core/database/DatabaseModule.kt` or platform builders in `androidMain` / `iosMain`): `.addMigrations(MigrationN_N+1)`.
7. Verify nothing relies on `.fallbackToDestructiveMigration()` — if it does, flag it. Destructive fallback is **not acceptable** in production; user data must survive upgrades.
8. Run `./gradlew :composeApp:compileDevelopmentDebugKotlinAndroid` and `./gradlew :composeApp:testDebugUnitTest --tests "*Migration*"` if migration tests exist.
9. Remind the user to:
   - Commit the new `composeApp/schemas/.../N+1.json` Room exports it.
   - Write a `MigrationTest` using `MigrationTestHelper` (Android instrumented test) if one doesn't exist for this jump.

Report the diff, the SQL written, and any data-loss risk you spotted.
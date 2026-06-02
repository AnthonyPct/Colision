---
description: Generate Kotlin DTOs (kotlinx.serialization) from the current Supabase Postgres schema
argument-hint: [table_name | --all]
---

Generate Kotlin DTOs from the Supabase schema for `$ARGUMENTS` (a specific table, or `--all` for every public table).

Place generated files under `composeApp/src/commonMain/kotlin/com/anthooop/colision/core/network/dto/`. DTOs are the wire format consumed by the supabase-kt client — they are **not** Room entities (entities live in `core/database/entity/` and may differ).

1. Read the schema:
   - Prefer the Supabase MCP if available (`mcp__plugin_supabase_supabase__*` tools or the staging server `mcp__claude_ai_Magma_Staging__*` — verify which project is current).
   - Otherwise ask the user to paste the output of `supabase gen types typescript --linked` or the `information_schema.columns` rows for the requested tables.
2. For each table, emit a `@Serializable data class <PascalTable>Dto(...)`:
   - Use `@SerialName("snake_case_column")` on every field; Kotlin properties stay `camelCase`.
   - Map Postgres types: `uuid` / `text` / `varchar` → `String`; `int2|int4` → `Int`; `int8` → `Long`; `numeric` → `Double` (flag if precision matters); `bool` → `Boolean`; `timestamptz` / `timestamp` → `kotlinx.datetime.Instant`; `date` → `kotlinx.datetime.LocalDate`; `jsonb` → `kotlinx.serialization.json.JsonElement` unless a known shape exists.
   - Nullable columns → nullable Kotlin types with `= null` defaults so insert payloads can omit them.
   - Enums → Kotlin `enum class` annotated `@Serializable`, with `@SerialName` mirroring the Postgres enum label.
3. Group enums and shared types in `core/network/dto/Enums.kt`. One DTO per file otherwise.
4. **Do not** generate Room entities here — that's the job of `/add-supabase-table`. If the user wants both, run `/add-supabase-table` afterwards and reuse the DTO field shape.
5. Run `./gradlew :composeApp:compileDevelopmentDebugKotlinAndroid` to verify everything compiles.
6. Report:
   - Files created / overwritten.
   - Any Postgres types you couldn't map and how you fell back.
   - RLS policies you noticed that the client code will need to respect (for visibility, not blocking).

Never hand-edit a generated DTO to "fix" a mismatch — fix the schema or the mapping rule and regenerate.
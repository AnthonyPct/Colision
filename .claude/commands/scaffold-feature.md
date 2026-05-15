---
description: Scaffold a feature with its navigation graph + one Route/Screen/ViewModel per sub-screen
argument-hint: <FeatureName> <subscreen1> <subscreen2> ...
---

Scaffold the feature `$ARGUMENTS` under `composeApp/src/commonMain/kotlin/com/anthooop/colision/feature/`.

Parse `$ARGUMENTS`:

- **First token** = feature name (PascalCase, e.g. `Onboarding`). Lowercase it for the package (`onboarding`).
- **Remaining tokens** = sub-screen names (lowercase, e.g. `welcome projectcode projectconfirm identity commission notifications`). If none are provided, **stop and ask the user** to list the sub-screens — never scaffold a feature with a single combined Route/Screen/ViewModel, that violates the MVI contract in CLAUDE.md.

**Before generating anything**, ground the implementation in the source-of-truth docs (in this order):

1. `docs/prd.md` — find the section/journey for this feature. Extract: requirements, acceptance criteria, edge cases, copy.
2. User stories — currently embedded in the PRD; if separate story files exist under `docs/` for this feature, read them too.
3. `docs/architecture.md` — confirm the patterns / constraints that apply (data flow, sync, auth state).
4. `docs/design/chats/chat1.md` (once per session) + `docs/design/project/tokens.jsx` + `docs/design/project/components.jsx`.
5. `docs/design/project/screens-<feature_lowercase>.jsx` — enumerate the sub-screens shown in the design.

Diff checks before scaffolding:

- **User-provided sub-screens vs design** — if they diverge, report the diff and stop.
- **PRD vs design** — if a sub-screen in the design has no PRD coverage (or vice versa), surface it and ask before generating.
- **Architecture compliance** — if the feature implies a deviation from `docs/architecture.md`, flag it; do not silently invent a new pattern.

Produce a short pre-scaffold brief (under 200 words) listing: PRD requirements covered, sub-screens to be generated, design tokens consumed, ambiguities. **Wait for user confirmation** before writing files.

Then generate exactly this tree:

```
feature/<feature_lowercase>/
├── data/
│   └── <Feature>Repository.kt          # interface + impl, shared across all sub-screens
├── navigation/
│   ├── <Feature>Destination.kt         # sealed interface, one @Serializable object/class per sub-screen
│   └── <Feature>Graph.kt               # fun NavGraphBuilder.<feature>Graph(navController) { navigation<…> { composable<…> { …Route(...) } … } }
├── <subscreen1>/
│   ├── <Subscreen1>Route.kt            # koinViewModel(), collectAsStateWithLifecycle(), LaunchedEffect on events, navigation lambdas
│   ├── <Subscreen1>Screen.kt           # pure @Composable(state, onIntent, modifier), Material3 + core/design tokens
│   ├── <Subscreen1>ViewModel.kt        # StateFlow<State>, SharedFlow<Event>, onIntent(Intent)
│   └── <Subscreen1>Contract.kt         # data class State, sealed interface Intent, sealed interface Event
├── <subscreen2>/                       # same four files
│   └── ...
└── di/
    └── <Feature>Module.kt              # one Koin module: viewModelOf for every sub-screen + singleOf for repo
```

Requirements for each artifact:

1. **Destination** — `sealed interface <Feature>Destination`, one `@Serializable data object` per sub-screen (use `data class` only when nav args are needed). Group in `navigation/<Feature>Destination.kt`.
2. **Graph** — `fun NavGraphBuilder.<feature_lowercase>Graph(navController: NavController)` wrapping a `navigation<<Feature>Graph>(startDestination = ...) { composable<<Feature>Destination.X> { …Route(...) } }`. The graph itself is a `@Serializable data object <Feature>Graph` at the top of the file. Inter sub-screen navigation goes through `navController.navigate(<Feature>Destination.Next)` passed into Routes as lambdas.
3. **Route** — signature `@Composable fun <Subscreen>Route(onNavigateTo<NextSubscreen>: () -> Unit, onBack: () -> Unit, ...)`. Uses `koinViewModel()`, collects `state` and `events`, forwards intents. **No UI** beyond calling `<Subscreen>Screen`.
4. **Screen** — pure: `@Composable fun <Subscreen>Screen(state: <Subscreen>State, onIntent: (<Subscreen>Intent) -> Unit, modifier: Modifier = Modifier)`. Material3 only. Uses the design tokens in `core/design/`. No Koin, no ViewModel, no nav, no side effects. Should be previewable.
5. **ViewModel** — exposes `val state: StateFlow<<Subscreen>State>`, `val events: SharedFlow<<Subscreen>Event>` (extra buffer 1), `fun onIntent(intent: <Subscreen>Intent)`. Constructor-inject the feature repository.
6. **Contract** — `data class <Subscreen>State(...)` (start with sensible defaults), `sealed interface <Subscreen>Intent`, `sealed interface <Subscreen>Event`. Keep them small; expand as the feature grows.
7. **Repository** — interface + impl in `data/<Feature>Repository.kt`. Single source of truth: expose `Flow<…>` from Room DAO, refresh from supabase-kt in the background. Constructor-inject `HttpClient` / supabase client + DAO.
8. **Koin module** — `<feature_lowercase>Module`: `viewModelOf(::<Subscreen1>ViewModel)` for **every** sub-screen, `singleOf(::<Feature>RepositoryImpl) bind <Feature>Repository::class`. After creating it, add the entry to the central feature module list (`core/di/FeatureModules.kt` — create it if missing) and point the user at the file.

Pitfalls to avoid:

- Do **not** create a single combined `<Feature>ViewModel` that handles every sub-screen — one ViewModel per sub-screen. Share state via the repository.
- Do **not** put navigation logic in `<Subscreen>Screen` — only in `<Subscreen>Route` via lambda params.
- Do **not** add a `domain/` use-case layer unless the logic warrants it. State the rationale if you decide to.

After generating, run `./gradlew :composeApp:compileDevelopmentDebugKotlinAndroid` and report the first error if any. Wrap up with:

- Files created (paths).
- The exact line to add to the root `NavHost` to wire this graph in.
- The Koin module list update.
- Anything the user must confirm (copy strings, missing tokens in `core/design/`, design-vs-args mismatch).

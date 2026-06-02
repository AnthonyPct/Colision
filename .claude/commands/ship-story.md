---
description: Implement a GitHub issue end-to-end (branch → commit per sub-issue → PR), then chain to /ship-next
argument-hint: <parent-issue-number>
---

Implement parent issue **#$ARGUMENTS** end-to-end and open a PR. This command runs autonomously — no per-step confirmation — but **never** merges, never force-pushes, never touches `main`.

## 0. Preflight (stop on any failure)

1. `git status` — working tree must be clean. If dirty, stop and tell the user.
2. `git fetch origin && git switch main && git pull --ff-only origin main` — sync `main`.
3. `gh issue view $ARGUMENTS --json number,title,body,labels,state,assignees,projectItems,milestone,url` — load the parent story. If `state != OPEN`, stop.
4. Parse the parent issue body for **sub-issues / tasks**:
   - Native GitHub sub-issues: query `gh api graphql -f query='query { repository(...) { issue(number: $ARGUMENTS) { subIssues(first: 50) { nodes { number title state } } } } }'`.
   - Fallback: extract a `- [ ] #<num>` checklist from the body. Each unchecked `#<num>` reference is a sub-issue.
   - If neither yields a list, **stop** and ask the user to break the story into sub-issues — this command commits one-per-sub-issue and cannot proceed without them.
5. For each sub-issue, `gh issue view <n> --json title,body,state,labels` and confirm `state == OPEN`. Skip closed ones, report which.

## 1. Ground the work in the source-of-truth docs

Follow the order from CLAUDE.md's "Source-of-truth documentation":

1. `docs/prd.md` — find the section matching this story. Extract acceptance criteria + edge cases. If the issue references a story file (e.g. `docs/stories/...`), read it.
2. `docs/architecture.md` — confirm applicable patterns / constraints.
3. `docs/design/chats/chat1.md` (once per session), `tokens.jsx`, `components.jsx`, and `screens-<feature>.jsx` for the relevant feature.

Produce a short internal plan (don't print it unless something is ambiguous): for each sub-issue → files to touch, layer (data / DI / VM / UI / tests), MVI artifacts impacted.

**Bail conditions** — stop and surface to the user, do not improvise:
- PRD and design contradict each other on this story.
- A sub-issue requires a token / component that doesn't exist in `core/design/` yet (run `/sync-tokens` or create the missing component first).
- The work implies deviating from `docs/architecture.md`.

## 2. Branch

Create `story/<parent-issue>-<kebab-slug-of-title>` from `main`:

```bash
git switch -c story/$ARGUMENTS-<slug>
```

If the branch already exists locally or remotely, stop — the story is already in progress; do not overwrite.

## 3. Implement & commit, one sub-issue at a time

For **each** open sub-issue, in the order they're listed on the parent:

1. Read the sub-issue body in full.
2. Implement only what that sub-issue describes — no scope creep across sub-issues.
3. Respect all patterns from CLAUDE.md (MVI, Koin, flavors, Material3, design tokens, no Firebase, no SQLDelight).
4. Compile-check: `./gradlew :composeApp:compileDevelopmentDebugKotlinAndroid`. If broken, fix before committing.
5. Stage **only** the files relevant to this sub-issue (never `git add -A`).
6. Commit with a Conventional Commits message that closes the sub-issue:

   ```
   <type>(<scope>): <short summary>

   <optional body — what + why, max 5 lines>

   Closes #<sub-issue-number>

   Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
   ```

   `<type>` ∈ `feat|fix|refactor|test|chore|docs|style`. `<scope>` = feature folder name (`onboarding`, `agenda`, …) or `core` for cross-cutting.
7. If a sub-issue has its own checklist, all items must be done before committing. Do **not** commit a half-done sub-issue.

If a sub-issue turns out to be wrongly scoped (depends on work not yet planned, or no longer relevant): stop, leave the in-progress changes uncommitted, and report to the user — do not invent a fix.

## 4. Run the full check before pushing

```bash
./gradlew :composeApp:check
./gradlew :composeApp:iosSimulatorArm64Test
```

- Compilation failure → fix in a new commit on the same branch (Conventional `fix:` linked to the relevant sub-issue if applicable, otherwise no `Closes`).
- Test failure caused by code under this story → fix it.
- Test failure unrelated → report, don't paper over it.

## 5. Push & open PR

```bash
git push -u origin story/$ARGUMENTS-<slug>
```

Open a PR against `main`:

```bash
gh pr create --base main --title "<type>(<scope>): <parent story title>" --body "$(cat <<'EOF'
## Summary
<2–4 bullets: what this story delivers, anchored to the PRD requirement>

## Sub-issues closed
- Closes #<sub1> — <one-liner>
- Closes #<sub2> — <one-liner>
- ...

## Acceptance criteria (from PRD)
- [x] <AC1>
- [x] <AC2>
- ...

## Design reference
- `docs/design/project/screens-<feature>.jsx` — <which sub-screens were touched>

## Test plan
- [x] `./gradlew :composeApp:check` green
- [x] `./gradlew :composeApp:iosSimulatorArm64Test` green
- [ ] Manual: <key flow to smoke-test on device>

Closes #$ARGUMENTS

🤖 Generated with [Claude Code](https://claude.com/claude-code)
EOF
)"
```

Capture the returned PR URL.

## 6. Chain to the next story

PR is opened, not merged — review happens on GitHub. Do **not** wait, do **not** merge.

Immediately invoke `/ship-next` to pick the next story off the Project board. If `/ship-next` reports the board is empty, stop with a one-line summary of PRs opened in this session.

## Safety rails (non-negotiable)

- Never push to `main`, never force-push (`--force` / `--force-with-lease` forbidden).
- Never run `git reset --hard`, `git checkout -- .`, `git clean -f`, or delete branches.
- Never use `--no-verify` to skip hooks. If a hook fails, fix the cause.
- Never merge the PR. Never approve the PR. Never request reviewers automatically (unless the repo's CODEOWNERS handles it).
- Never edit `gradle/libs.versions.toml` versions unless the story explicitly requires it.
- If `gh auth status` fails, stop and tell the user to authenticate.
---
description: Implement a full epic (1 commit per story, one PR at the end) and update GitHub Project statuses
argument-hint: <epic-number>
---

Implement epic **#$ARGUMENTS** end-to-end: one branch, one commit per story sub-issue, one PR opened only after **all** stories are done. Updates the Project board status along the way.

## 0. Preflight — stop on any failure

1. `git status` must be clean. If dirty, stop.
2. `git fetch origin && git switch main && git pull --ff-only origin main`.
3. `gh auth status` — needs the `project` scope. If missing, stop and tell the user to run `gh auth refresh -s project`.
4. `gh issue view $ARGUMENTS --json number,title,body,labels,state,projectItems,url`. The issue must be `OPEN` and labeled `epic` (or its title must start with "Epic"). If unsure it's an epic, stop and ask.

## 1. Load the epic spec from `docs/epics.md`

Open `docs/epics.md` and locate the section for **Epic $ARGUMENTS** (header pattern: `## Epic $ARGUMENTS:` or similar — match by epic number). Extract:

- Epic goal / scope.
- Stories list (titles + numbers) — cross-check against the GitHub sub-issues; if they diverge, report and stop.
- Linked FRs / requirements from `docs/prd.md` (the epic section usually cites them).

If `docs/epics.md` doesn't cover this epic, stop and ask the user to add it — implementing without a spec violates the source-of-truth rule in CLAUDE.md.

## 2. Load source-of-truth docs (in order)

Mandatory before writing any code:

1. **`docs/prd.md`** — read the FRs cited by the epic + their acceptance criteria + edge cases.
2. **`docs/architecture.md`** — confirm patterns / constraints that apply to this epic's feature.
3. **`docs/design/chats/chat1.md`** (once per session) for design intent.
4. **`docs/design/project/tokens.jsx`** + **`docs/design/project/components.jsx`** — confirm needed tokens/components exist in `core/design/`. If anything is missing, run `/sync-tokens` first and stop.
5. **`docs/design/project/screens-<feature>.jsx`** — read end-to-end. This is the pixel-perfect contract: copy strings, spacing, colors, component composition, interaction states. The Compose output must visually match this file.

If PRD ↔ design ↔ epic spec contradict each other on this epic, stop and surface the diff. Do not improvise a resolution.

## 3. Fetch the story sub-issues from GitHub

```bash
gh api graphql -f query='
  query($num: Int!) {
    repository(owner: "<owner>", name: "<repo>") {
      issue(number: $num) {
        subIssues(first: 50) { nodes { number title body state url } }
      }
    }
  }' -F num=$ARGUMENTS
```

Fallback: parse `- [ ] #<num>` checklist items from the epic body.

For each story:

- `state` must be `OPEN`. Skip closed, report which.
- If a story already has a linked open PR (check `linkedPullRequests` on the issue), skip with a one-line note.

Order the stories by the order they appear in the epic body (or `docs/epics.md` if GitHub doesn't preserve order). Implementation will respect that order — earlier stories may set up scaffolding later ones depend on.

If the resulting list is empty, stop with `No actionable stories for epic #$ARGUMENTS.`

## 4. Branch + epic status

```bash
git switch -c epic/$ARGUMENTS-<kebab-slug-of-epic-title>
```

If the branch already exists locally or remotely, stop — the epic is already in progress.

Move the epic on the Project board to **In Progress** (helper below).

## 5. Implement story by story — 1 commit per story

For **each** open story in order:

### 5.1 Story → "In Progress"

Set the story's Project board status to `In Progress`.

### 5.2 Read the story

`gh issue view <story-number> --json title,body,labels` — read the full body including its own acceptance criteria. If the story has sub-issues / a sub-checklist, you implement them all, but they roll up into **one** commit at the story level (this is `/ship-epic`'s contract, not `/ship-story`'s).

### 5.3 Implement

- Respect MVI / Koin / flavors / Material3 / Room / Ktor / supabase-kt — exactly as CLAUDE.md describes.
- Pixel-perfect Compose: every spacing, color, typography, copy string, and component variation must match `screens-<feature>.jsx`. If something in the design is ambiguous, ask the user; don't guess.
- Use design tokens from `core/design/` — never hardcode colors or font sizes.
- Stay within the story's scope. Cross-story refactors go in their own story or a follow-up.

### 5.4 Compile-check

```bash
./gradlew :composeApp:compileDevelopmentDebugKotlinAndroid
```

Broken build → fix before committing.

### 5.5 Commit (stage only this story's files)

```
<type>(<scope>): <story title>

<2–4 line body: what + why, anchored to the FR(s) the story covers>

Closes #<story-number>
Refs #$ARGUMENTS

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
```

`<type>` ∈ `feat|fix|refactor|test|chore|docs`. `<scope>` = feature folder name. Never `git add -A` — stage only the files this story touches.

### 5.6 Story → "In Review"

Set the story's Project board status to `In Review` (commit is local; status reflects "code done, awaiting PR review").

### 5.7 Stop conditions inside the loop

- Compilation can't be fixed without expanding scope → stop, leave uncommitted changes, report.
- Design / PRD ambiguity discovered → stop, report, ask.
- A story needs work clearly assigned to a later story → stop, do not pre-implement; flag the ordering issue.

## 6. After all stories — full check

```bash
./gradlew :composeApp:check
./gradlew :composeApp:iosSimulatorArm64Test
```

- Failures caused by this epic → fix in a new commit (`fix:` scope, no `Closes` line unless it actually closes a story).
- Failures unrelated to this epic → report, do not paper over.

## 7. Push + open PR

```bash
git push -u origin epic/$ARGUMENTS-<slug>

gh pr create --base main --title "epic(<scope>): <epic title>" --body "$(cat <<'EOF'
## Epic
Closes #$ARGUMENTS

## Stories shipped
- Closes #<storyA> — <title>
- Closes #<storyB> — <title>
- ...

## Requirements covered (PRD)
- FR<x>, FR<y>, ... — <one-line summary>

## Design reference
- `docs/design/project/screens-<feature>.jsx` — implemented pixel-perfect against this file
- Tokens used: <list any new ones added to `core/design/`>

## Architecture
- Patterns followed: MVI (Route/Screen/ViewModel/Contract per sub-screen), Koin DI, Ktor + supabase-kt, Room cache, multi-graph navigation.
- Deviations from `docs/architecture.md`: <none, or list with rationale>

## Test plan
- [x] `./gradlew :composeApp:check` green
- [x] `./gradlew :composeApp:iosSimulatorArm64Test` green
- [ ] Manual on Android (development flavor): <key flows to smoke-test>
- [ ] Manual on iOS simulator: <same>

🤖 Generated with [Claude Code](https://claude.com/claude-code)
EOF
)"
```

Capture the PR URL.

## 8. Final status updates

- Epic → **In Review**.
- All shipped stories → confirm they're at **In Review** (no-op if step 5.6 set them).
- Report: PR URL, list of stories shipped, any stories deferred and why.

Do **not** merge the PR. Review happens on GitHub.

## Helper — moving an issue on the Project board

GitHub Projects v2 status is a single-select field. The transition is:

```bash
# One-time per session: discover IDs (cache them)
gh project list --owner <owner> --format json
gh project field-list <project-number> --owner <owner> --format json   # find 'Status' field id + option ids for In Progress / In Review

# Per issue:
gh api graphql -f query='
  mutation($project:ID!,$item:ID!,$field:ID!,$value:String!){
    updateProjectV2ItemFieldValue(input:{
      projectId:$project, itemId:$item, fieldId:$field,
      value:{singleSelectOptionId:$value}
    }){ projectV2Item { id } }
  }' -F project=<PROJECT_NODE_ID> -F item=<ITEM_NODE_ID> -F field=<STATUS_FIELD_ID> -F value=<OPTION_ID>
```

Status option names assumed: `Todo`, `In Progress`, `In Review`, `Done`. If the project uses different names, ask the user once and cache the mapping for the rest of the session.

## Safety rails (non-negotiable)

- Never push to `main`, never force-push, never `git reset --hard`, never delete branches.
- Never use `--no-verify`. Hook failures get fixed.
- Never merge the PR. Never close issues manually — `Closes #N` in commits/PR handles it on merge.
- Never change Project board fields other than `Status`. Don't reassign, relabel, or move epics across milestones.
- If anything is ambiguous (epic scope, design intent, PRD copy), **stop and ask** rather than ship a wrong-but-plausible implementation.
---
description: Pick the next 'Ready' / 'Todo' story from the GitHub Project board and ship it via /ship-story
---

Pick the next story to work on from the GitHub Project board and ship it.

## 1. Locate the Project board

If the repo's `.github/` config or a previous session already pinned `PROJECT_OWNER` / `PROJECT_NUMBER`, reuse them. Otherwise discover:

```bash
gh repo view --json owner,name           # owner = login
gh project list --owner <owner> --format json
```

Pick the project whose name matches the current repo (ask the user if multiple plausibly match). Remember the choice for the rest of the session — don't re-list every call.

## 2. Find the next item

Query the project for items with `Status` field = `Ready` (preferred) or `Todo` (fallback), in board order:

```bash
gh project item-list <project-number> --owner <owner> --format json --limit 100
```

Filter to:

- `content.type == "Issue"` and `content.state == "OPEN"` and not closed.
- `fieldValues` has `Status` ∈ {`Ready`, `Todo`}.
- Sorted: `Ready` first, then `Todo`; within a status, by the board's item order (the JSON preserves it).

The first matching item = the next story. If none match, **stop** with: `Project board has no Ready/Todo stories. Done.`

Skip items that:

- Already have an open PR linked to them (`gh pr list --search "linked:<issue-url>"` or check `linkedPullRequests` in the GraphQL `issue` node).
- Are assigned to a human other than the current `gh auth status` user (don't steal someone's work). If unassigned or assigned to "me", proceed.
- Are blocked by an open `tracked-by` / `blocked-by` relationship to another open issue.

If you skip an item, report **why** in one line and move to the next.

## 3. Hand off to /ship-story

Once you've identified the parent issue number, **invoke `/ship-story <number>`**. That command handles preflight, doc grounding, branching, commits, checks, and PR creation, then calls `/ship-next` again to chain.

Do not re-implement the shipping logic here — `/ship-next` is only a picker.

## Safety rails

- If `gh auth status` fails or scopes are missing (need `project` scope for Project v2 reads), stop and tell the user to run `gh auth refresh -s project`.
- If the Project board query returns >100 items, do **not** paginate silently — ask the user to narrow the board (current sprint / milestone) before continuing.
- Never modify the Project board (no status changes, no assignments) from this command — the PR linking from `/ship-story` is enough for GitHub to surface the work.
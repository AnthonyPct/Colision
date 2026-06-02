---
description: Load the design contract (tokens + screen) before implementing UI
argument-hint: <screen-name: onboarding|agenda|meeting|arbitrage>
---

Load the design contract for `$ARGUMENTS` before writing any UI code.

Steps:

1. Read `docs/design/chats/chat1.md` to understand design intent (skip if already in context this session).
2. Read `docs/design/project/tokens.jsx` — extract colors, typography, spacing, shapes the screen uses.
3. Read `docs/design/project/components.jsx` — list which shared components the screen relies on.
4. Read `docs/design/project/screens-$ARGUMENTS.jsx` top-to-bottom.
5. Verify that `core/design/` exposes Kotlin equivalents for every token the screen needs (colors named to match `ColorScheme`, DM Sans `Typography`, spacing). Report any missing token mapping as a blocker.

Then produce a short implementation brief (under 200 words):

- Composables to create (with their public signature).
- Tokens consumed (light + dark).
- Shared components needed (existing or new).
- Copy strings (FR — Sophie persona, plain language).
- Anything ambiguous that needs user confirmation before coding.

Do **not** start implementing until the user confirms the brief.
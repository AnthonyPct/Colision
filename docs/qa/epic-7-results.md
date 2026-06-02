# Epic 7 — QA test results

End-to-end smoke test campaign run against the `develop` branch
(`composeApp` development flavor) on a single device — Pixel 7
(Android 16). Backend : Supabase project `Collision`
(`uxmzeqlnrpydiiephfem`).

Run on 2026-05-25 → 2026-05-26 via the mobile MCP + Supabase MCP.
Verdict is based on smoke coverage (1–2 representative scenarios per
story) and not on the full scenario matrix of each parent issue.

## Summary

| Story | Issue | Scope | Verdict | Notes |
|-------|-------|-------|---------|-------|
| 7.1 — Cold start & entry routing | #47 | All 4 scenarios | PASS (avec 2 bugs) | Voir Bug #1, Bug #2 ci-dessous |
| 7.2 — Créer un projet (J1) | #48 | Scénarios 1, 2, 3 + symétrie 4 | PASS | Sc. 5 (réseau coupé) déjà couvert par 7.1#4 ; sc. 6 (unicité 10×) jugé sur 3 codes |
| 7.3 — Rejoindre un projet via code | #49 | Sc. 1 (partiel) + sc. 3 (code partiel) | PARTIAL | Self-add "+ Je m'ajoute moi-même" → DB pas créée (à investiguer en manuel — peut être un click MCP off, peut être un vrai bug) |
| 7.4 — CRUD commissions | #50 | Create + Delete | PASS | Bug #3 découvert ici (Room restore via Auto Backup) |
| 7.5 — CRUD membres | #51 | Create | PASS | Assign-to-commission skip dans le smoke |
| 7.6 — Quitter / Supprimer un projet | #52 | — | SKIPPED | Destructif + nécessite re-onboarding complet. À tester en manuel avant release |
| 7.7 — Agenda semaine/mois/empty | #53 | Toggle + empty state | PASS | « Aucune réunion à venir — bravo ! » + bascule visuelle OK |
| 7.8 — Détail réunion / commission / deleted state | #54 | — | SKIPPED | Nécessite une réunion créée (pas dans la portée smoke). À tester avec un meeting réel |
| 7.9 — Mode hors-ligne lecture + sync | #55 | Airplane + lecture + write gating | PASS | « + Ajouter » correctement désactivé en airplane mode |

## Bugs surfacés

### Bug #1 — i18n: `\'` rendu littéralement
**Statut : fixé sur cette branche** (commit `9c86901`).

`strings.xml` utilisait l'échappement Android `\'` qui n'est pas
interprété par Compose Multiplatform Resources. Résultat à l'écran :
« conflits d**\\**agenda », « coup d**\\**œil », « qu**\\**ils
rejoignent », etc. (29 occurrences). `\n` fonctionne mais pas `\'`.

Fix : remplacer toutes les occurrences `\'` par `'`.

### Bug #2 — Double `signInAnonymously` au cold start
**Statut : ouvert** (issue #66).

Sur fresh install, 2 rows `auth.users` (+ 2 rows `public.device` via
trigger) sont créées au lieu d'une. Race entre `AppViewModel.init`
et `ProjectSyncManager.syncInternal` qui appellent tous les deux
`ensureSession()` ; le check `sessionStatus.value` ne sérialise pas
les appels concurrents. Voir issue pour fix proposé (Mutex).

### Bug #3 — `allowBackup="true"` restaure la Room DB après uninstall
**Statut : ouvert** (issue #67).

`AndroidManifest.xml:9` a `allowBackup="true"`. Après uninstall +
réinstall, Android Auto Backup restaure la base Room — mais la
session Supabase anonyme est neuve, donc on a un mismatch
`device_id`. Conséquences :

- L'UI affiche commissions/membres d'un projet précédent qui
  n'existent pas dans la DB du projet courant.
- Risque privacy si l'appareil est partagé.
- Compromet les test fixtures « fresh install » (il faut
  `adb shell pm clear` au lieu de uninstall).

Voir issue pour fix proposé (`allowBackup="false"` pour le MVP).

## Observations méthodologiques

- **mobile_type_keys ne fonctionne pas avec `BasicTextField`
  hidden** (cas du OTP code sur `JoinCodeScreen`). Workaround :
  `adb -s <device> shell input text "..."`.
- **Les coordonnées du screenshot MCP sont scaled** par rapport aux
  coordonnées physiques retournées par `mobile_list_elements`.
  Toujours partir des coords listées par `list_elements`.
- Pour éviter Bug #3 pendant la QA : `adb shell pm clear <package>`
  au lieu de `mobile_uninstall_app`.

## Non couvert dans ce smoke

- Multi-device (push entre deux devices, déjà SKIPPED par scope).
- Story 7.6 (quitter projet) — destructif.
- Story 7.8 (détail réunion / deleted state) — nécessite un meeting
  réel.
- Story 7.3 self-add member end-to-end — à valider en manuel.
- Story 7.5 assign member → commission — non testé.

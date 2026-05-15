# Collision App Design

_Started 2026-05-14 14:48 UTC_

---

## User

Je veux que tu me fasses le design de l'application colision. Tu as tous les détails de l'application dans le PRD. Colision est une application mobile utilisant compose multipalteforme donc c'est la même UI sur android et iOS. Tu es libre sur le choix du design et la création du design system, je veux quelques chose de moderne, qui est faisable avec compose multiplateforme et qui respecte les guideline UX mobile.

## Assistant

_[tool: read_file]_

_[tool: list_files]_

_[tool: local_ls]_

_[tool: list_files]_

_[tool: local_ls]_

_[tool: local_read]_

_[tool: local_ls]_

_[tool: local_ls]_

_[tool: questions_v2]_

J'ai bien lu le PRD — un produit clair, persona non-tech (Sophie), 9 features MVP, valeur cœur = détection de conflit cross-commissions. Le design system attaché est vide, donc je pars d'une page blanche : créer une identité visuelle pour Colision en plus du prototype.

Avant de me lancer, quelques questions pour cadrer la direction :

## User

Questions answered:
- platform: Les deux côte à côte
- personality: Decide for me
- accent_color: 3
- screens: Decide for me — couvre les écrans clés
- fidelity: Hi-fi interactif (clic entre écrans, animations légères)
- typography: Decide for me
- tweaks: Decide for me

## User

Mais moi je voulais voir l'UI que ca donne, comment je fais ?

## Assistant

_[tool: update_todos]_

_[tool: copy_starter_component]_

_[tool: copy_starter_component]_

_[tool: copy_starter_component]_

Bonne question — j'aurais dû te montrer un fichier dès le départ même vide. Je m'y mets tout de suite, je te livre une première version visible dans la minute, puis j'itère.

Direction que je prends (je décide pour toi sur les points laissés ouverts) :
- **Personnalité** : chaleureux + civic. Palette terreuse off-white + accent vert forêt (#0E7C66), un coral coral pour les conflits. Rassurant pour Sophie sans être infantilisant.
- **Type** : DM Sans (humaniste, lisible, supportée nativement par Compose).
- **Frames** : iPhone et Android côte à côte, synchronisés.
- **Tweaks** : accent color, mode clair/sombre, densité, style de cartes.

_[tool: read_file]_

_[tool: read_file]_

_[tool: write_file]_

_[tool: write_file]_

OK je me lance. Pendant que je construis, voici la philosophie design que je vais appliquer :

**Design system "Colision"** (chaleureux + civic, optimisé Compose Multiplatform)
- Palette terreuse off-white `#FAF7F2`, vert forêt `#0E7C66`, coral `#C8553D` (conflits)
- DM Sans humaniste
- Cartes plates contournées + radius 16-20 (Material 3 expressive)
- Touch targets 52px+ minimum
- Une action primaire par écran (Sophie n'a pas à chercher)
- Copy conversationnel : "Voir d'autres horaires" pas "Suggestions"

Je commence par les tokens + composants, puis empile les écrans.

_[tool: write_file]_

_[tool: write_file]_

_[tool: write_file]_

_[tool: write_file]_

_[tool: write_file]_

_[tool: write_file]_

_[tool: write_file]_

_[tool: write_file]_

_[tool: write_file]_

_[tool: read_file]_

_[tool: grep]_

_[tool: grep]_

_[tool: str_replace_edit]_

_[tool: write_file]_

_[tool: show_html]_

_[tool: done]_

Voilà — première version visible. Tu peux maintenant :

- 👀 **Voir les deux téléphones côte à côte** (iPhone + Pixel, même UI partagée Compose)
- 🎯 **Naviguer** : barre du bas dans l'app, ou bouton **"Aller à un écran"** en bas de la page pour sauter direct à n'importe lequel des 17 écrans (welcome, onboarding, agenda, création réunion, conflit, suggestions, arbitrage, suivi organisateur, etc.)
- 🔔 **Bouton "Simuler un push"** en haut à droite pour voir la notification descendre et tapper dessus
- 🎨 **Panel Tweaks** en bas à droite : palette (forêt / coral / indigo / plum), mode clair/sombre, style des cartes, masquer iOS ou Android

Je lance la vérif visuelle pour repérer ce qui mérite un polish, puis j'attends ton retour sur ce qui te plaît / pas plaît.

_[tool: fork_verifier_agent]_

Quelques points pour ton retour :
- **Tu peux cliquer sur chaque écran** pour naviguer naturellement (welcome → join → agenda, etc.) ou utiliser le menu en bas
- **Le test clé** : tape sur la bannière rouge "1 conflit à arbitrer" dans l'agenda → c'est l'expérience cœur du produit
- **À regarder en priorité** : Welcome, Agenda (semaine), Création réunion → Conflit → Suggestions, Arbitrage. C'est le chemin de la valeur.

Dis-moi ce qui colle / cloche, ce qu'on garde ou pivote (couleur, type, layout, copy, hiérarchie), et j'itère.

## User

Verifier agent check completed

## Assistant

_[tool: str_replace_edit]_

_[tool: str_replace_edit]_

_[tool: str_replace_edit]_

Bien vu. Je fixe le safe area iOS.

_[tool: str_replace_edit]_

_[tool: str_replace_edit]_

_[tool: str_replace_edit]_

_[tool: str_replace_edit]_

_[tool: str_replace_edit]_

_[tool: done]_

Safe area iOS corrigée (top + bottom) + FAB repositionné correctement par-dessus la barre de nav. Je relance la vérif.

_[tool: fork_verifier_agent]_

Voilà — corrigé. Dis-moi ce que tu veux ajuster (couleurs, typo, hiérarchie, copy, écrans manquants) et j'itère.

## User

Save this design as a PDF: Colision.html

## Assistant

_[tool: snip]_

_[tool: read_file]_

_[tool: write_file]_

_[tool: write_file]_

_[tool: show_html]_

_[tool: show_html]_

_[tool: sleep]_

_[tool: get_webview_logs]_

_[tool: open_for_print]_

Voilà le PDF prêt à imprimer — il s'ouvre dans un nouvel onglet et lance automatiquement la boîte d'impression. Choisis **"Enregistrer en PDF"** dans la destination. 20 pages au total : 1 page de garde, 1 page tokens (palette + typo), puis 18 écrans avec iPhone + Pixel côte à côte.

👉 [Ouvrir pour imprimer / sauver en PDF](https://019e26f5-8449-73bb-9891-f26f7ed7bb68.claudeusercontent.com/v1/design/projects/019e26f5-8449-73bb-9891-f26f7ed7bb68/serve/Colision-print.html?t=d7f54b8b271825e94306bfa3b5a1f12491970188e9c8f37c3f0770b0ef4023fa.45e25c00-7383-4777-879e-4a16dbc8dd6e.ad112d54-2a77-46b4-aebf-6ab422f0a592.1778881436&direct=1)


---
stepsCompleted: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, "step-11-complete"]
inputDocuments:
  - docs/product-brief-Colision-2026-05-10.md
workflowType: 'prd'
workflowStatus: 'complete'
lastStep: 11
project_name: Colision
author: Anthony Picquet
date: 2026-05-14
completed: 2026-05-14
---

# Product Requirements Document - Colision

**Author:** Anthony Picquet
**Date:** 2026-05-14

## Executive Summary

Colision est une application mobile native (iOS + Android, Kotlin Multiplatform avec UI Compose partagée) qui détecte automatiquement les conflits de réunions entre plusieurs sous-groupes auxquels une même personne appartient. Le produit adresse un problème observé dans tout collectif structuré en commissions, équipes ou cellules — typiquement un conseil municipal — où la planification en silo génère des doublons d'agenda systématiques.

Le mécanisme d'usage est calqué sur **Tricount** : un utilisateur crée un "projet" (par exemple "Conseil municipal de Saint-Machin"), définit les sous-groupes, ajoute des membres, et invite les autres via un code à 6 caractères. Pas de compte requis. Lorsqu'un membre crée une réunion pour un sous-groupe, Colision affiche en temps réel les membres déjà mobilisés ailleurs et propose un décalage. Si la réunion est créée malgré le conflit, les conflictés reçoivent une notification push native (FCM Android, APNs iOS) et voient l'arbitrage à faire dans leur dashboard.

Le persona racine est **Sophie**, conseillère municipale fraîchement élue, avec un rapport à la technologie proche de zéro. Cette contrainte UX est load-bearing pour l'ensemble du PRD : chaque flux, chaque écran, chaque message doit être plus simple que WhatsApp, faute de quoi le produit ne sera pas adopté par sa cible primaire. Le périmètre V1 reste mono-persona ; les extensions à d'autres collectifs (associations, clubs sportifs, CSE) sont identifiées en roadmap V2 sans détailler ici.

Colision est positionné en **side project gratuit** (scénario "b" du brief), publié sur les stores Android et iOS, avec une fenêtre d'évolution vers un modèle commercial (freemium ou abonnement projet) si l'adoption décolle. L'objectif PRD est de définir un MVP livrable en solo par un développeur unique, avec un coût d'hébergement maîtrisé (Supabase tier gratuit ou Pro à $25/mois selon le volume).

### What Makes This Special

Colision est défini par **quatre choix produit non-négociables**, chacun défendu contre son alternative la plus tentante :

1. **Détection de conflit cross-groupes en temps réel** — c'est le cœur du produit et l'unique moat fonctionnel. WhatsApp ne le fait pas. Google Calendar ne le fait pas (sa notion de "groupe" n'inclut pas l'appartenance multi-groupes avec calcul de chevauchement). Doodle ne le fait pas (un sondage par réunion, pas d'agenda partagé). Colision le fait.

2. **Modèle d'adoption Tricount** — code à 6 caractères, pas d'inscription, pas de compte. Friction d'onboarding inférieure à 60 secondes. C'est la seule façon de gagner contre WhatsApp dans la tête de Sophie.

3. **Modèle plat des rôles** — n'importe quel membre peut créer un projet, ajouter une commission, organiser une réunion. Pas de hiérarchie de droits dans l'app. Cela reflète la réalité observée dans les collectifs visés et simplifie radicalement l'UX (pas de mode admin distinct, une seule surface produit).

4. **Push natifs dès le MVP** — les notifications curatives (alerte à un conflicté quand une réunion conflictuelle est posée malgré l'alerte) ne fonctionnent que si elles arrivent immédiatement, en background. Un badge in-app retarderait l'arbitrage et casserait la valeur cœur.

## Project Classification

**Technical Type:** mobile_app (iOS + Android via Kotlin Multiplatform + Compose Multiplatform)
**Domain:** general (low complexity domain)
**Complexity:** medium
**Project Context:** Greenfield — nouveau projet, repo avec scaffolding KMP de base uniquement

**Stack technique confirmée :**
- **Front mobile** : Kotlin Multiplatform + Compose Multiplatform. UI 100 % partagée entre iOS et Android via `commonMain`. Code spécifique réservé à l'intégration FCM (Android) et APNs (iOS).
- **Backend** : **Supabase**. Postgres pour la persistance (projets, commissions, membres, réunions, conflits). Realtime pour la synchronisation multi-device. Edge Functions pour la logique serveur (notamment dispatch des pushes). Auth Supabase non utilisée en V1 (modèle no-auth) — pourra être activée en V2.
- **Notifications push** : FCM côté Android, APNs côté iOS, déclenchées depuis une Edge Function Supabase via les SDK officiels.

**Implications pour le PRD :**
- Sections **incluses** : exigences plateforme (iOS/Android, versions min), stratégie push, mode hors-ligne (lecture uniquement au MVP), permissions appareil (notifications), conformité stores (App Store + Play Store).
- Sections **exclues** : design visuel exhaustif (déléguée au workflow UX), fonctionnalités desktop, interface CLI, conformité gouvernementale (govtech) ou healthcare.
- **Considérations spéciales** : la contrainte UX "persona non-tech" guide toutes les décisions de design, de copy et de flux. À vérifier à chaque décision PRD : "Sophie comprendrait-elle ça sans aide ?"

## Success Criteria

### User Success

Mesurées du point de vue de **Sophie**, persona racine non-tech. Le produit réussit pour ses utilisateurs si :

- **Onboarding sub-60s** : entre l'install et le premier écran utile (calendrier du projet rejoint), Sophie n'a pas besoin d'aide extérieure. Mesure : durée moyenne du flux `install → identité sélectionnée → projet rejoint`.
- **≥ 1 ouverture par semaine** après la deuxième semaine d'usage : preuve que l'app est entrée dans le réflexe.
- **≥ 1 conflit détecté par mois** pour un utilisateur actif dans ≥ 2 commissions : preuve que la valeur cœur s'active réellement.
- **Rétention J30 ≥ 50 %** : la moitié des utilisateurs encore actifs un mois après leur première utilisation. Seuil de validation du product/market fit sur le persona ciblé.
- **NPS qualitatif** : sur un échantillon, "Recommanderais-tu Colision à un autre élu ?" — réponse libre, qualitatif.
- **Test "Sophie autonome"** : Sophie ouvre, consulte, et crée une réunion sans aide de son frère développeur. C'est l'épreuve UX ultime du MVP.

### Business Success

Cadre **scénario (b)** confirmé : side project gratuit, publié sur les stores, sans monétisation V1. Les jalons business sont des jalons d'adoption.

**Horizon 3 mois post-lancement V1 :**
- Le projet "Conseil municipal de [Commune de Sophie]" est actif (≥ 10 conseillers hebdomadaires, ≥ 5 commissions, ≥ 10 réunions planifiées).
- ≥ 3 conflits détectés et résolus en amont (preuve de la valeur cœur).
- 1 à 3 autres collectifs ont essayé spontanément (bouche-à-oreille initial).
- ≥ 5 témoignages qualitatifs concrets.

**Horizon 6 mois :**
- 5 à 10 projets actifs.
- Rétention J30 ≥ 50 % sur l'ensemble des utilisateurs.
- Note Store ≥ 4,3 / 5 sur ≥ 20 avis.

**Horizon 12 mois :**
- 30 à 50 projets actifs.
- Décision explicite : maintenir (b) gratuit ou basculer (c) freemium.

> Tous les chiffres sont des **hypothèses de départ à recalibrer** dès que les premières données arrivent.

### Technical Success

Le MVP est techniquement réussi si les critères suivants sont tenus. Ces chiffres sont les seuils auxquels on évalue les choix d'architecture et de plateforme.

| Catégorie | Critère | Cible MVP |
|---|---|---|
| **Stabilité** | Crash-free sessions (toutes plateformes) | ≥ 99,5 % |
| **Performance — cold start** | Temps du tap sur l'icône au premier écran utile (Android moyen, iOS moyen) | < 2 s |
| **Performance — conflit detection** | Latence entre "saisie du créneau" et "affichage des conflits" en mode online | < 200 ms |
| **Performance — sync** | Délai de propagation d'une réunion créée à la vue d'un autre membre actif sur le projet | < 1 s sur connexion 4G/Wi-Fi |
| **Performance — push** | Latence end-to-end "conflit créé malgré l'alerte" → push reçu sur l'appareil du conflicté | < 5 s |
| **Taille app** | Download initial App Store + Play Store | < 30 Mo |
| **Compatibilité Android** | minSdk | 28 (Android 9.0) |
| **Compatibilité iOS** | iOS minimum | 15 |
| **Conformité stores** | Acceptation à la première soumission App Store + Play Store | 100 % |
| **Hébergement** | Supabase tier gratuit suffit jusqu'à | ≥ 5 projets actifs / 100 utilisateurs |
| **Mode dégradé** | Lecture du dernier état connu fonctionne sans réseau (cache local) | 100 % des écrans de consultation |
| **Sécurité des données** | Pas de PII sensible stockée, RLS Postgres correctement configurée pour isolation entre projets | Audit manuel pré-lancement |

### Measurable Outcomes

Les KPIs concrets à instrumenter dès la V1 — agrégateur des indicateurs ci-dessus :

| KPI | Définition | Cible V1 |
|---|---|---|
| Onboarding completion rate | % installs → identité sélectionnée | ≥ 80 % |
| Time-to-first-value | Install → première création/consultation de réunion | < 5 min |
| WAU / installed | Utilisateurs actifs hebdo / installs cumulés | ≥ 40 % à J30 |
| Rétention J30 | Actifs 30 jours après onboarding | ≥ 50 % |
| Conflits détectés / mois | Volume de conflits affichés | Tracking |
| Conflits résolus en amont | % de conflits → décalage de créneau | ≥ 60 % |
| Invitation-to-join rate | % d'invitations code → rejoint effectif | ≥ 70 % |
| Crash-free sessions | Sessions sans crash | ≥ 99,5 % |

**Vanity metrics explicitement ignorées** : downloads totaux (sans rétention), temps passé dans l'app (Colision doit être *brève*), MAU pur, likes store, volume de réunions créées.

## Product Scope

### MVP — Minimum Viable Product

Périmètre figé. Toute feature ci-dessous est **défendable** au sens où elle est indispensable au "moment Aha!" de Sophie.

1. **Création de projet** (nom + code 6 caractères auto-généré).
2. **Gestion des commissions** (créer, éditer, supprimer ; modèle plat, tout le monde peut tout faire).
3. **Gestion des membres** (ajouter avec un prénom, assigner à des commissions).
4. **Rejoindre un projet via code** (saisir code → choisir identité → cocher commissions).
5. **Création d'une réunion** (date, heure, durée, commission(s), titre optionnel) avec **détection de conflit cross-commissions en temps réel**.
6. **Notification curative** pour les conflictés (dashboard + push).
7. **Vue calendrier personnelle agrégée** (semaine + mois).
8. **Vue par commission** (liste des réunions à venir).
9. **Notifications push natives** via FCM (Android) + APNs (iOS), pilotées par Supabase Edge Function.

Hypothèses fortes assumées au MVP :
- Pas d'authentification (code projet = identifiant partagé).
- Un appareil = un projet rejoint à la fois.
- Pas de réunions récurrentes (chaque réunion saisie individuellement).
- Pas de lieu / d'adresse de réunion.
- Pas de RSVP (le calendrier suffit à se positionner).
- Pas d'export iCal / Google Calendar.
- Français uniquement.
- Mode offline en lecture seule (cache du dernier état connu).

### Growth Features (Post-MVP — V1.1 → V2)

**V1.1 (3 à 6 mois post-MVP)** :
- Réunions récurrentes (hebdo, mensuelle) avec détection de conflit sur l'ensemble des occurrences.
- Lieu / adresse de réunion (string libre + géolocalisation optionnelle).
- RSVP basique (présent / absent / peut-être).
- Mode hors-ligne en lecture renforcé (synchronisation différentielle).

**V2 (6 à 12 mois)** :
- Authentification optionnelle (PIN au choix du projet ou lien email magique via Supabase Auth).
- Multi-projets par utilisateur (Sophie peut être dans 2 conseils).
- Export / synchronisation calendrier (iCal, Google Calendar).
- Sondage de créneaux (à la Doodle, intégré nativement quand pas d'accord).
- Extension à d'autres personas (associations, clubs sportifs, CSE) si signal organique observé.
- Mode web léger pour consultation desktop.

### Vision (Future — V3+)

**Horizon 12+ mois** :
- Modèle freemium ou abonnement projet (passage scénario "c" si l'adoption et le coût d'hébergement le justifient).
- Intégrations municipales (export PV, lien avec les outils de mairie).
- Mode "intercommunal" pour coordonner plusieurs projets liés (ex. un élu départemental qui suit plusieurs communes).
- Analytics privé pour le créateur du projet (taux de présence, participation par commission).
- Internationalisation (anglais d'abord, puis selon demande).

## User Journeys

### Journey 1 — L'Initiateur : Antoine met le conseil en ordre

Antoine, 42 ans, prof de SVT, a été élu il y a 4 mois au conseil municipal de Saint-Machin (1 200 habitants). Il est dans la commission Travaux et celle des Finances. Comme il est le plus à l'aise tech du conseil — c'est lui qui a configuré la box Wi-Fi de la mairie — c'est aussi lui que sa collègue Sophie a appelé en panique un dimanche soir : *"Antoine, j'ai eu trois SMS pour trois réunions différentes la même semaine, je sais plus où aller."*

Antoine ouvre l'App Store ce soir-là, télécharge Colision. **Premier écran** : un choix simple — "Créer un projet" ou "Rejoindre un projet". Il crée. Il tape le nom : "Conseil municipal de Saint-Machin". L'app génère un code à 6 caractères : `KQ7H2P`. Il l'écrit sur un post-it.

**Création des commissions** : Antoine appuie sur "+ Commission" et tape "Jeunesse", "Sport", "Travaux", "Culture", "École", "Finances". Cinq minutes. **Ajout des membres** : il liste les 18 conseillers de tête, juste prénom + nom — pas de mail, pas de mot de passe. Pour chacun, il coche les commissions qu'il connaît. Pour ceux dont il n'est pas sûr, il laisse vide — l'intéressé complétera lui-même.

Le lendemain matin, dans le groupe WhatsApp du conseil, il poste : *"On essaie un truc qui devrait nous sauver des conflits de réunions. Téléchargez Colision sur le store, code `KQ7H2P`, choisissez votre nom et cochez vos commissions. 30 secondes."*

**Six mois plus tard**, l'app est devenue l'unique source de vérité pour les réunions du conseil. Le groupe WhatsApp ne sert plus qu'aux échanges de politesse et aux photos de chantier.

*Cette journey révèle les capacités requises :*
- Onboarding sans compte avec création de projet en < 2 min
- Génération de code de partage unique
- CRUD complet sur les commissions par n'importe quel membre
- Ajout en masse de membres avec prénom seul
- Assignment membre↔commissions modifiable par tout le monde

---

### Journey 2 — Le Membre Invité : Sophie rejoint et respire

Sophie, 38 ans, assistante maternelle, est conseillère depuis 3 mois — son premier mandat. Elle est dans les commissions Jeunesse, Sport et École. Elle a reçu le message d'Antoine sur WhatsApp ce matin. Elle s'y prend pendant la sieste des petits qu'elle garde.

Elle télécharge Colision. **Écran d'accueil** : deux boutons. Elle tape sur "Rejoindre un projet". Elle saisit `KQ7H2P` — l'app affiche immédiatement "Conseil municipal de Saint-Machin" pour qu'elle confirme qu'elle est au bon endroit. **Oui.**

**Sélection d'identité** : l'app affiche la liste des 18 conseillers. Elle trouve son nom : *"Sophie Picquet"*. Elle tape dessus. L'app lui demande de confirmer puis affiche les commissions qu'Antoine avait pré-cochées pour elle. Elle voit Jeunesse et École cochées. Elle ajoute Sport — Antoine ne savait pas. **Total écoulé depuis le tap sur l'icône : 47 secondes.**

Elle arrive sur son dashboard : son **agenda agrégé** affiche déjà 4 réunions à venir — la réunion Jeunesse de jeudi, la commission École de lundi, le conseil plénier du 28. Pour la première fois depuis qu'elle est élue, **elle voit tout au même endroit**. Le sentiment ne la quitte plus : "Ah ouais, en fait c'est dense."

Le soir même, en cuisinant, elle ouvre l'app pour vérifier si le créneau de la kermesse de l'école de ses enfants tombe sur une commission. Pas de conflit. Elle ferme l'app en moins de 10 secondes. **Premier réflexe installé.**

*Cette journey révèle les capacités requises :*
- Saisie de code + résolution projet < 3 s
- Liste des membres triée alphabétiquement, recherche acceptable
- Confirmation d'identité (anti-erreur de sélection)
- Vue calendrier perso agrégée immédiate post-onboarding
- Vue mensuelle ergonomique sur mobile (compact + lisible)
- Latence d'ouverture rapide pour usage "rapide check"

---

### Journey 3 — L'Organisateur : Marc planifie la commission Sport

Marc, 51 ans, kinésithérapeute, préside la commission Sport. Il veut organiser une réunion de coordination du tournoi inter-quartiers pour le jeudi 21 mai à 20h. Avant Colision, il aurait envoyé le créneau dans le groupe WhatsApp dédié, attendu les réponses, et découvert deux jours plus tard que trois personnes avaient une commission Jeunesse pile à la même heure.

Cette fois, il ouvre l'app, tape **"+ Nouvelle réunion"**. **Date** : 21 mai. **Heure** : 20h00. **Durée** : 1h30 (sélection rapide via puces). **Commission concernée** : il sélectionne "Sport" — la liste lui montre les autres commissions s'il veut faire une réunion mixte ; il n'en a pas besoin. **Titre (optionnel)** : "Coordination tournoi inter-quartiers".

Au moment où il appuie sur "Vérifier les conflits", **l'app affiche en moins d'une seconde** :

> ⚠️ **3 membres de la commission Sport sont déjà mobilisés sur ce créneau :**
> - Sophie Picquet — Commission Jeunesse (20h - 21h30)
> - Pierre Garnier — Commission Travaux (19h30 - 21h)
> - Léa Dubois — Conseil plénier (20h - 22h)
>
> [Décaler la réunion] [Voir d'autres créneaux libres] [Créer quand même]

Marc avait un Plan B en tête — il tape sur **"Voir d'autres créneaux libres"**. L'app lui propose le mardi 19h, le mercredi 20h30, et le samedi matin 10h. Il choisit **mercredi 20h30** — zéro conflit affiché. Il valide. La réunion est créée. Tous les membres de Sport reçoivent un push en quelques secondes.

Marc range son téléphone. **Trois minutes**, là où il fallait avant deux jours d'allers-retours WhatsApp.

*Cette journey révèle les capacités requises :*
- Sélection rapide de date/heure adaptée au pouce mobile (pas le date picker pourri par défaut)
- Sélection multi-commissions possible
- **Algorithme de détection de conflit cross-commissions** opérant sur tous les membres des commissions sélectionnées (logique : "membre ∈ commission sélectionnée AND membre ∈ une autre commission AND cette autre commission a une réunion overlapping")
- Affichage des conflits avec **nom + commission + créneau** des conflictés (transparence totale)
- Suggestion automatique de créneaux libres dans une fenêtre temporelle proche (algo "find next free slot")
- Trois choix explicites : décaler, suggérer ailleurs, créer quand même
- Push de notification aux membres de la commission concernée à la création

---

### Journey 4 — Le Conflicté : Sophie arbitre sans drame

Vendredi 22h45, Sophie est en train de coucher ses enfants. Son téléphone vibre — **notification push Colision** :

> 🔔 Conflit détecté sur ton agenda
> Marc a créé "Réunion Sport - point urgent" le mercredi 26 mai à 19h, qui entre en conflit avec "Commission Jeunesse - budget 2026" (19h - 20h30).

Sophie tape la notification. L'app s'ouvre directement sur **l'écran d'arbitrage** : les deux réunions affichées côte à côte, avec la commission, le créneau, et le nombre de membres déjà engagés sur chacune.

Trois actions possibles :
- **"Je vais à Sport"** — l'app marque son arbitrage, prévient les organisateurs des deux réunions (in-app + push).
- **"Je vais à Jeunesse"** — idem.
- **"Je trancherai plus tard"** — l'arbitrage reste en attente sur son dashboard, visible aussi par les organisateurs comme "Sophie : en attente".

Sophie pèse 4 secondes. Le budget 2026 de Jeunesse est plus stratégique pour elle ce trimestre. Elle tape **"Je vais à Jeunesse"**. Marc reçoit une notif de son côté : *"Sophie : Jeunesse"*. Pas de SMS d'excuse, pas de mauvaise conscience, pas de débat WhatsApp à 23h. **Arbitrage tranché en 6 secondes chrono.**

Le lendemain, en regardant son dashboard, Marc voit que sur les trois conflits qu'il avait à la création, deux sont résolus (Sophie va Jeunesse, Pierre va Travaux) et un est encore en attente (Léa). Il peut décider en toute conscience s'il décale ou pas.

*Cette journey révèle les capacités requises :*
- Push notification avec deep-link vers l'écran d'arbitrage
- Écran d'arbitrage clair : 2 réunions côte à côte + 3 actions
- Stockage de l'arbitrage utilisateur (état : "ira à X", "ira à Y", "en attente")
- Notification de l'arbitrage aux deux organisateurs
- Dashboard organisateur : statut consolidé des arbitrages sur ses propres réunions

---

### Journey Requirements Summary

Les 4 journeys ci-dessus révèlent les **capacités produit** que le PRD doit détailler dans les sections "Functional Requirements" et "Non-Functional Requirements" à venir :

| Capacité | Journey(s) source |
|---|---|
| Création / gestion de projet sans compte | J1 |
| Génération et résolution de code de partage | J1, J2 |
| CRUD commissions (modèle plat) | J1 |
| CRUD membres + assignation aux commissions | J1, J2 |
| Onboarding ultra-court (< 60 s) avec sélection d'identité confirmée | J2 |
| Vue calendrier personnelle agrégée multi-commissions | J2 |
| Création d'une réunion avec sélection multi-commissions | J3 |
| **Détection de conflit cross-commissions en temps réel** | J3 (cœur) |
| Suggestion automatique de créneaux libres alternatifs | J3 |
| 3 choix à la création conflictuelle (décaler / suggérer / créer quand même) | J3 |
| Push notifications natives (FCM + APNs) avec deep-link | J3, J4 |
| Écran d'arbitrage post-conflit avec 3 actions | J4 |
| Statut d'arbitrage par utilisateur (ira à X / ira à Y / en attente) | J4 |
| Dashboard organisateur avec consolidation des arbitrages | J4 |
| Latence cible : conflit detection < 200 ms, sync < 1 s, push < 5 s | J3, J4 |

## Mobile App Specific Requirements

### Project-Type Overview

Colision est une application mobile native cross-platform développée en Kotlin Multiplatform avec UI partagée via Compose Multiplatform. Cibles : **Android 9.0+ (API 28)** et **iOS 15+**. Cette stratégie permet à un seul développeur (Anthony) de maintenir une base de code unique pour les deux plateformes, tout en livrant une expérience visuelle et ergonomique native sur chacune. Les portions plateforme-spécifiques sont réduites au strict nécessaire : intégration FCM côté Android, intégration APNs côté iOS, configuration des entitlements iOS pour les notifications.

### Technical Architecture Considerations

**Stack confirmée** (centralisée dans `gradle/libs.versions.toml`) :
- Kotlin **2.3.21**
- AGP **8.11.2** (Android Gradle Plugin)
- Compose Multiplatform **1.10.3**
- Material3 **1.10.0-alpha05**
- JVM target **11**
- Android `compileSdk` / `targetSdk` **36**, `minSdk` **28**
- iOS cible **15.0+** (arm64 device + arm64 simulator)

**Module Gradle unique** : `:composeApp`, avec `settings.gradle.kts` activant `TYPESAFE_PROJECT_ACCESSORS`.

**Structure source** :
- `commonMain/` — code Kotlin et Compose partagé (toute l'UI, toute la logique métier, le client Supabase, le moteur de détection de conflit).
- `androidMain/` — entry point `MainActivity`, intégration FCM, Platform actual pour Android.
- `iosMain/` — entry point `MainViewController`, intégration APNs, Platform actual pour iOS.
- `commonMain/composeResources/` — assets partagés (images, polices) accédés via `Res` généré.

**Backend** : Supabase (Postgres + Realtime + Edge Functions). Le client Kotlin officiel `supabase-kt` est consommé depuis `commonMain`. Les Edge Functions sont déclenchées :
- À la création d'une réunion (pour calculer les conflits côté serveur et pousser des notifications aux conflictés).
- À l'arbitrage d'un conflit par un membre (pour notifier les organisateurs concernés).

**Stratégie de synchronisation** : Realtime Postgres Channels par projet (channel ID = id du projet). Chaque appareil rejoint le channel du projet qu'il a rejoint et reçoit les mises à jour live (insert/update/delete sur les tables réunions, membres, commissions, arbitrages).

**Persistence locale** : **Room (KMP-compatible)** en cache lecture côté client, alimenté par Realtime Supabase. La table locale est l'écran source des vues calendrier — la sync Realtime nourrit la table, la vue observe la table. Pattern offline-first read.

### Platform Requirements

| Plateforme | Version min | Justification |
|---|---|---|
| Android | API 28 (Android 9.0 Pie) | Couvre 95 %+ des appareils actifs en 2026, support natif des notifications avec actions, JNI stable pour KMP. |
| iOS | 15.0 | Support de SwiftUI mature, APNs token-based auth, requestNotificationsAuthorization en sync avec iOS 15+. |
| Tablettes | Non prioritaire au MVP | UX optimisée téléphone. Compatible tablette (Compose responsive) sans optimisations layout dédiées. |
| Foldables | Non prioritaire au MVP | Compose s'adapte naturellement, mais pas de testing dédié. |

**Locales supportées au MVP** : Français (`fr-FR`) uniquement. L'i18n multi-langue est en V2.

### Device Permissions

Le MVP ne demande qu'**une seule permission** :

| Permission | Plateforme | Obligatoire / Optionnelle | Moment de demande |
|---|---|---|---|
| Notifications push | Android 13+ (POST_NOTIFICATIONS), iOS 15+ (UNUserNotificationCenter) | **Recommandée fortement** mais non bloquante | À la fin de l'onboarding (après sélection d'identité + commissions), avec un écran explicatif natif justifiant l'usage : "Colision te prévient quand une réunion entre en conflit avec ton agenda — accepte pour ne plus rater une coordination." |

**Permissions volontairement non demandées au MVP** : caméra, microphone, géolocalisation, contacts, Bluetooth, biométrie. Le MVP n'en a aucun besoin fonctionnel, et chaque permission demandée dégrade le taux d'onboarding.

### Offline Mode

**Stratégie : cache lecture seule.**

- À chaque ouverture, l'app affiche le dernier état connu (réunions, membres, commissions, arbitrages) cached localement.
- Si la connexion réseau est absente, l'app reste utilisable en consultation intégrale (vues calendrier, listes, détails). Un badge subtil "Mode hors ligne" est affiché en haut de l'écran.
- **Les actions d'écriture** (créer une réunion, créer une commission, arbitrer un conflit) sont **désactivées hors ligne** au MVP. Le bouton d'action affiche "Connexion requise pour cette action" plutôt que de permettre une création optimiste qui devrait être réconciliée plus tard.
- Justification : la détection de conflit nécessite un état serveur cohérent. Une création offline réconciliée plus tard pourrait introduire des conflits silencieux entre la création locale et l'état serveur. Le coût UX d'un mode write-offline est trop élevé pour un MVP solo dev.
- **V1.1** : envisager une réconciliation différée pour les actions de consultation pures (lecture renforcée), pas pour la création.

**Couche technique** : **Room (KMP-compatible depuis 2.7.0)**. Choix confirmé — détails de modélisation à approfondir en étape architecture.

### Push Strategy

**Cas d'usage push au MVP** :

| Trigger | Destinataires | Contenu | Deep-link |
|---|---|---|---|
| Création d'une réunion (sans conflit) | Tous les membres de la commission concernée | "Nouvelle réunion: {titre} — {date} à {heure}" | Détail de la réunion |
| Création d'une réunion **avec conflit créé malgré l'alerte** | Tous les membres conflictés | "Conflit détecté : {nom organisateur} a créé {titre} qui chevauche {titre conflictuel}" | **Écran d'arbitrage** (J4) |
| Arbitrage d'un conflicté | Organisateurs des deux réunions en conflit | "{nom} a choisi d'aller à {commission}" | Détail de la réunion arbitrée |
| Modification ou annulation d'une réunion | Tous les membres de la commission | "Réunion modifiée/annulée : {titre}" | Détail de la réunion |

**Architecture technique** :
- Stockage des tokens device (FCM token Android, APNs token iOS) en base Supabase, attaché à l'enregistrement membre du projet.
- Dispatch piloté par **Supabase Edge Function** qui :
  - Reçoit le trigger via Postgres webhook (sur insert/update des tables réunions ou arbitrages).
  - Calcule les destinataires.
  - Appelle FCM HTTP v1 API (Android) et APNs token-based auth (iOS) via leurs SDK respectifs en TypeScript Deno.
  - Loggue les échecs pour retry manuel si besoin.
- **Latence cible** : < 5 secondes end-to-end (trigger → push reçu).
- **Deep-linking** : iOS via Universal Links / `notificationHandler`, Android via Notification PendingIntent ouvrant l'écran cible. Le payload push inclut `screen`, `meetingId` (ou `conflictId`) pour router correctement.

### Store Compliance

**App Store (iOS)** :
- Bundle ID : `com.anthooop.colision`
- Catégorie principale : `Productivity`
- Catégorie secondaire : `Utilities`
- Classification d'âge : **4+** (pas de contenu sensible)
- Privacy nutrition labels : *Data Not Collected* — Colision n'enregistre ni nom légal complet, ni email, ni identifiant unique pour le tracking publicitaire. À déclarer honnêtement.
- Pas d'IAP au MVP. La case "App uses non-public APIs" : **non**.
- Encryption : SDK Supabase utilise TLS standard → déclaration d'export encryption : "uses standard encryption only (exempt)".

**Play Store (Android)** :
- Application ID : `com.anthooop.colision`
- Catégorie : `Productivity`
- Classification d'âge : **PEGI 3 / Everyone**
- Data safety form : préciser que l'app collecte prénom + appartenance à des commissions (data type : *Personal Info — Name*). Préciser que la collecte n'est **pas pour la publicité**, **pas vendue à des tiers**, et que l'utilisateur peut effacer ses données en quittant le projet.
- Target API level : 36 (compileSdk/targetSdk) pour passer le filtre Play Console "must target current API level".

**Conformité RGPD** :
- Pas de PII sensible (pas d'email, pas de téléphone, pas d'adresse, pas de date de naissance) collectée par défaut au MVP.
- Le seul "PII" est le **prénom** saisi par les membres pour s'identifier dans un projet — donnée à but exclusivement fonctionnel, stockée en Postgres Supabase (hébergement UE à choisir : région Frankfurt ou Paris).
- Page Privacy Policy à publier (obligatoire pour les stores) — version française, concise, listant : ce qui est collecté (prénom + appartenances), où c'est stocké (Supabase UE), combien de temps (jusqu'à suppression du projet ou départ du membre), et comment effacer (depuis l'app).
- Pas de Cookie Banner (mobile native, pas de cookies web).

### Implementation Considerations

**Priorités d'implémentation suggérées pour un solo dev** (ordre indicatif, sera repris en epics/stories) :

1. **Setup KMP + Compose Multiplatform** : confirmer que `App.kt` est accessible depuis les deux plateformes, smoke test sur les deux runners (Android Studio + Xcode).
2. **Setup Supabase** : créer le projet, schéma SQL initial, configurer la RLS par projet.
3. **Client Supabase dans `commonMain`** : authentification du device (token anonyme, pas auth utilisateur), CRUD basique sur les tables.
4. **Flux Création de projet + Rejoindre via code** (J1 + J2).
5. **Flux Création de réunion sans conflit** (J3 — version basique).
6. **Algorithme de détection de conflit** + UI de conflit (J3 — core value).
7. **Push notifications FCM/APNs + Edge Function dispatch** (J3 + J4).
8. **Écran d'arbitrage** (J4).
9. **Vue calendrier perso agrégée** + vue par commission (J2).
10. **Polish UX pour persona non-tech** : copy, transitions, retry-friendly.
11. **Cache offline lecture** (Room) : base locale + lecture du dernier état.
12. **Soumission stores** + Privacy Policy + assets store + screenshots.

**Considérations spécifiques à un développeur solo** :
- Privilégier les briques managées : Supabase plutôt que custom backend, Crashlytics ou Sentry plutôt que custom error tracking, Firebase Analytics ou PostHog plutôt que custom analytics.
- Tester continuellement sur un device réel à bas-de-gamme (Android moyen de gamme + iPhone SE 2020 par exemple) pour valider la perf.
- **CI en place dès le démarrage** : GitHub Actions ou Bitrise (à arbitrer en étape architecture). Pipelines minimums :
  - Build Android (`assembleDebug` + `testDebugUnitTest`) sur chaque push.
  - Build iOS framework (`linkDebugFrameworkIosSimulatorArm64`) sur chaque push.
  - Tests communs (`iosSimulatorArm64Test`) sur chaque push.
  - Optionnel V1.1+ : signing + upload TestFlight (iOS) + Internal Track (Play) sur tag `v*`.

## Project Scoping & Phased Development

### MVP Strategy & Philosophy

**Approche MVP retenue : Problem-Solving MVP.**

Colision n'est ni un Experience MVP (qui chercherait à délivrer une UX polie), ni un Platform MVP (qui prépare une expansion future), ni un Revenue MVP (pas de monétisation V1). C'est un **Problem-Solving MVP** au sens strict : la plus petite chose qui résout *concrètement* la douleur cœur de Sophie — les conflits de réunions cross-commissions — avec une qualité suffisante pour qu'elle l'adopte sans aide.

**Critères d'adéquation à l'approche Problem-Solving** :
- ✅ La douleur est nette, fréquente et coûteuse (vérifié dans le brief).
- ✅ Il existe un substitut subi (WhatsApp), donc le seuil d'adoption est bas dès que la valeur est démontrée.
- ✅ Le périmètre fonctionnel est resserrable sans dégrader la valeur cœur (la détection cross-commissions reste possible avec 9 features).
- ✅ Le ROI utilisateur est démontrable dès la première semaine d'usage.

**Resource Requirements** :
- **Équipe** : 1 développeur (Anthony, fullstack/mobile via KMP).
- **Compétences requises** : Kotlin, Compose, Supabase (Postgres + Edge Functions TypeScript Deno), notifications push (FCM + APNs).
- **Durée cible MVP** : à estimer en étape architecture / création d'epics. Ordre de grandeur indicatif pour un solo : 3 à 5 mois.
- **Coût infrastructure** : tier gratuit Supabase au démarrage, < 30 €/mois à la première montée en charge (tier Pro à 25 $/mois si dépassement), comptes développeurs Apple (99 $/an) et Google Play (25 $ one-shot).

### MVP Feature Set (Phase 1)

**User Journeys supportées au MVP** : J1 (Initiateur), J2 (Membre Invité), J3 (Organisateur), J4 (Conflicté). Les 4 journeys du PRD sont MVP.

**Must-Have Capabilities** — chacune justifiée par l'analyse :

| Capability | Sans cela, le produit échoue ? | Manuel possible ? | Deal-breaker early adopters ? | Décision |
|---|---|---|---|---|
| Création de projet + code 6 chars | Oui | Non | Oui | MVP |
| Rejoindre via code + sélection identité | Oui | Non | Oui | MVP |
| CRUD commissions + assignation membres | Oui | Non | Oui | MVP |
| Création de réunion (date/heure/commissions) | Oui | Non | Oui | MVP |
| **Détection de conflit cross-commissions** | Oui (cœur) | Non | Oui | **MVP — non négociable** |
| Vue calendrier personnelle agrégée | Oui | Non | Oui | MVP |
| Vue par commission | Non strictement, mais attendu | Oui | Non | MVP (faible coût) |
| Suggestion de créneaux libres alternatifs | Non, mais "wow" UX | Oui (l'organisateur tâtonne) | Non | MVP (réduit la friction du décalage) |
| Notification curative + écran d'arbitrage | Oui (valeur post-conflit) | Non | Oui | MVP |
| Push natives FCM + APNs | Oui (les notifs in-app retardent l'arbitrage) | Non | Oui (cas curatif) | MVP |
| Cache offline lecture seule (Room) | Non strictement | Oui | Non | MVP (UX dégradée mais robuste) |

### Post-MVP Features

**Phase 2 — V1.1 (3 à 6 mois post-MVP)** :
- Réunions récurrentes (hebdo, mensuelle) avec détection de conflit sur l'ensemble des occurrences.
- Lieu / adresse de réunion (string libre + géolocalisation optionnelle).
- RSVP basique (présent / absent / peut-être).
- Mode hors-ligne renforcé : synchronisation différentielle des lectures, pas encore de création offline.
- Pipeline CI/CD étendu : signing automatique, upload TestFlight + Internal Track sur tag.

**Phase 3 — V2 (6 à 12 mois)** :
- Authentification optionnelle (PIN au choix du projet ou lien email magique via Supabase Auth).
- Multi-projets par utilisateur (Sophie peut être dans 2 conseils simultanément sur un même appareil).
- Export / synchronisation calendrier (iCal, Google Calendar).
- Sondage de créneaux intégré nativement (à la Doodle) quand pas d'accord.
- Extension aux personas secondaires (associations, clubs sportifs, CSE) si signal organique observé.
- Mode web léger pour consultation desktop.

**Phase 4 — Vision (12+ mois)** :
- Modèle freemium ou abonnement projet (passage scénario "c").
- Intégrations municipales (export PV, lien outils de mairie).
- Mode "intercommunal" pour coordonner plusieurs projets liés.
- Analytics privé pour le créateur du projet.
- Internationalisation.

### Risk Mitigation Strategy

#### Risques Techniques

| Risque | Probabilité | Impact | Mitigation |
|---|---|---|---|
| **Edge Function Supabase instable pour le dispatch push** | Faible | Élevé (push = valeur cœur curatif) | Retry automatique avec backoff + logging dans une table d'audit ; fallback in-app notif si le push échoue 3 fois. |
| **Algorithme de détection de conflit > 200 ms à grande échelle** | Moyenne | Moyen | Limiter le calcul aux fenêtres temporelles pertinentes (± 3 mois) ; indexer Postgres sur `(commission_id, start_time, end_time)` ; profiler tôt sur jeu de test à 1000 réunions. |
| **Room KMP encore "alpha-ish" sur iOS** | Moyenne | Moyen | Tester intégration iOS dès la première semaine d'implémentation ; plan B : abstraire la persistence via interface dans `commonMain` et plugger une impl plateforme-spécifique si Room casse. |
| **Compose Multiplatform perf iOS sous-optimale** | Moyenne | Moyen | Tester sur device iOS (iPhone SE 2020) dès la 1re feature complète ; viser des écrans simples (pas d'animations lourdes) ; profiler avec Xcode Instruments si frame drop. |
| **APNs token issuance en local dev sans device réel** | Faible | Faible | Document du setup Apple Developer + provisioning profile dès l'amorçage. |

#### Risques Marché

| Risque | Probabilité | Impact | Mitigation |
|---|---|---|---|
| **Non-tech persona trouve l'app trop complexe** | Moyenne | **Critique** (mort du produit) | Test continu avec Sophie/équivalent dès les premières maquettes ; obsession sur le test "Sophie autonome" ; user testing live filmé pour identifier les frictions invisibles. |
| **Adoption virale ne décolle pas** (un projet créé, jamais utilisé au-delà du créateur) | Moyenne | Élevé | Mesurer dès le J7 le coefficient viral par projet ; si < 1.0 au bout de 3 projets, pivoter sur l'onboarding (simplifier encore, ajouter du copy, etc.). |
| **WhatsApp inertia : l'équipe préfère revenir au groupe par habitude** | Élevée | Moyen | Push notifs fluides (un push Colision est plus actionnable qu'un message WhatsApp) ; faire un onboarding "guidé" du premier conseiller qui crée le projet pour qu'il porte le changement. |
| **Pas de problème assez fréquent : 1 conflit/trimestre vs 1/mois** | Faible | Élevé | Validation in vivo dans le conseil de la sœur dès la V1 ; si le volume est < 1 conflit/mois sur 3 mois, repenser le pitch (utilité plutôt que résolution de douleur). |

#### Risques Ressources (solo dev)

| Risque | Probabilité | Impact | Mitigation |
|---|---|---|---|
| **Sous-estimation du temps requis** (notamment iOS / push / store submission) | Élevée | Élevé | Buffer 40 % sur les estimations initiales ; découpage en epics courts (< 2 semaines chacun) ; mesurer la vélocité dès les 2 premiers epics. |
| **Burnout / désengagement du dev solo** (le projet n'est pas la priorité pro) | Moyenne | Critique | Cadence soutenable (10-15 h/sem max), pas de deadline externe, possibilité de mettre en pause sans perdre le contexte (PRD = pivot, code commenté). |
| **Maintenance long-terme** : si le produit décolle, le coût de support / corrections / nouveaux releases dépasse la capacité solo | Moyenne | Élevé | Documenter et automatiser tout ce qui peut l'être dès le MVP (CI, monitoring, doc utilisateur) ; envisager un contributeur ou la communauté open source si la base utilisateurs croît. |
| **Coût Supabase non-anticipé en cas de viralité** | Faible | Moyen | Surveiller les quotas tier gratuit dès J7 ; alertes automatiques à 80 % du quota ; basculement plan Pro 25 $/mois si nécessaire (gérable). |

**Stratégie globale de mitigation** : **valider tôt, itérer petit**. La plus grande mitigation à tous ces risques est de mettre le MVP entre les mains de Sophie et 5-10 autres conseillers réels dans les semaines qui suivent le premier build fonctionnel, et de laisser leur usage réel piloter les ajustements.

## Functional Requirements

### Project Management

- **FR1** : Un membre peut créer un projet en fournissant un nom de projet.
- **FR2** : Le système génère automatiquement un code de partage unique à 6 caractères à la création d'un projet.
- **FR3** : Un utilisateur peut rejoindre un projet existant en saisissant un code de partage valide.
- **FR4** : Un membre peut consulter le code de partage de son projet pour le copier ou le transmettre.
- **FR5** : Un membre peut quitter un projet ; son identité et ses affectations sont retirées du projet.

### Commission Management

- **FR6** : Un membre peut créer une commission au sein de son projet en fournissant uniquement un nom.
- **FR7** : Un membre peut modifier le nom d'une commission existante.
- **FR8** : Un membre peut supprimer une commission ; les réunions rattachées à cette commission sont également supprimées (avec confirmation).
- **FR9** : Un membre peut consulter la liste des commissions de son projet.

### Member Management

- **FR10** : Un membre peut ajouter un nouveau membre au projet en fournissant uniquement un prénom (ou prénom + nom).
- **FR11** : Un utilisateur rejoignant un projet peut sélectionner son identité dans la liste existante des membres ou créer une nouvelle identité pour lui-même.
- **FR12** : Un membre peut confirmer son identité après sélection avant de finaliser son rattachement au projet.
- **FR13** : Un membre peut assigner ou désassigner un membre (lui-même ou un autre) à une ou plusieurs commissions.
- **FR14** : Un membre peut consulter la liste des membres du projet et leurs commissions d'appartenance.

### Meeting Scheduling

- **FR15** : Un membre peut créer une réunion en spécifiant date, heure de début, durée, et une ou plusieurs commissions concernées.
- **FR16** : Un membre peut associer un titre optionnel à une réunion.
- **FR17** : Un membre peut modifier une réunion existante (date, heure, durée, commissions, titre).
- **FR18** : Un membre peut supprimer une réunion existante (avec confirmation).
- **FR19** : Un membre peut consulter le détail d'une réunion : titre, date, créneau, commissions concernées, créateur, conflits associés et statut d'arbitrage.

### Conflict Detection & Resolution

- **FR20** : Lors de la création d'une réunion et **avant validation**, le système identifie tous les membres des commissions sélectionnées qui ont déjà un engagement chevauchant le créneau proposé dans une autre commission.
- **FR21** : Le système affiche pour chaque conflit détecté en amont : le nom du membre conflicté, la commission concurrente et le créneau de la réunion concurrente.
- **FR22** : Face à des conflits détectés en amont, le créateur peut choisir parmi trois actions : décaler la réunion à un autre créneau, consulter des suggestions de créneaux libres, ou créer la réunion malgré les conflits.
- **FR23** : Le système peut calculer et proposer des créneaux alternatifs (dans une fenêtre temporelle proche) qui sont libres pour l'ensemble des membres des commissions concernées.
- **FR24** : Lorsqu'une réunion est créée malgré des conflits détectés, le système enregistre les conflits et déclenche une notification aux membres conflictés.
- **FR25** : Un membre conflicté peut choisir d'aller à l'une des deux réunions en conflit, ou reporter son arbitrage.
- **FR26** : Lorsqu'un membre fait un arbitrage, le système enregistre la décision et notifie les créateurs des deux réunions en conflit.
- **FR27** : Un créateur de réunion peut consulter le statut d'arbitrage consolidé pour chacune de ses réunions ayant généré des conflits.

### Notifications

- **FR28** : Le système peut envoyer une notification push native aux membres d'une commission lorsqu'une nouvelle réunion est créée sans conflit dans cette commission.
- **FR29** : Le système peut envoyer une notification push native aux membres conflictés lorsqu'une réunion est créée malgré l'alerte de conflit.
- **FR30** : Le système peut envoyer une notification push native aux créateurs des deux réunions concernées lorsqu'un membre conflicté fait un arbitrage.
- **FR31** : Le système peut envoyer une notification push native aux membres d'une commission lorsqu'une réunion est modifiée ou annulée.
- **FR32** : Une notification push tapée ouvre l'application directement sur l'écran le plus pertinent (détail de réunion, écran d'arbitrage, etc.) via deep-link.
- **FR33** : Un membre peut accepter ou refuser l'autorisation de notifications ; l'application reste fonctionnelle même sans cette permission (avec une utilité dégradée).

### Calendar & Discovery

- **FR34** : Un membre peut consulter son agenda personnel **agrégé** sur l'ensemble des commissions auxquelles il appartient, en vue **semaine**.
- **FR35** : Un membre peut consulter son agenda personnel agrégé en vue **mois**.
- **FR36** : Un membre peut consulter la liste des réunions à venir d'une commission spécifique, même s'il n'en est pas membre.

### Offline & Sync

- **FR37** : L'application conserve une copie locale du dernier état connu du projet et permet la consultation intégrale des données sans réseau.
- **FR38** : L'application indique visuellement à l'utilisateur lorsqu'elle fonctionne en mode hors-ligne.
- **FR39** : Les actions d'écriture (créer, modifier, supprimer une réunion, commission, membre ou arbitrage) sont désactivées en mode hors-ligne ; l'utilisateur reçoit un message explicite indiquant que la connexion est requise.
- **FR40** : Lorsque la connexion est rétablie, l'application synchronise automatiquement son état local avec le serveur, sans intervention de l'utilisateur.
- **FR41** : Lorsqu'un autre membre crée, modifie ou supprime une donnée pendant que l'application est ouverte, le changement est répercuté en quasi-temps-réel sur l'appareil du membre connecté.

### Privacy & Data Management

- **FR42** : Un membre peut supprimer son identité d'un projet ; ses affectations aux commissions et son historique d'arbitrage sont retirés du projet.
- **FR43** : Un membre peut supprimer un projet qu'il a créé ; toutes les données associées (commissions, membres, réunions, arbitrages) sont supprimées en cascade.

## Non-Functional Requirements

### Performance

- **NFR-P1** : Le temps de démarrage à froid (cold start) de l'application, du tap sur l'icône au premier écran utile, est inférieur à **2 secondes** sur un appareil Android moyen de gamme (référence : Pixel 6a) et sur un iPhone SE 2020.
- **NFR-P2** : La latence de détection de conflit, entre la validation des paramètres d'une réunion et l'affichage des conflits, est inférieure à **200 ms** en mode online, pour un projet contenant jusqu'à 1 000 réunions actives.
- **NFR-P3** : La latence de synchronisation Realtime, entre la création d'une réunion par un membre et son apparition chez les autres membres connectés, est inférieure à **1 seconde** sur connexion 4G ou Wi-Fi.
- **NFR-P4** : La latence end-to-end de notification push, entre la création d'une réunion conflictuelle et la réception du push sur l'appareil du conflicté, est inférieure à **5 secondes** sur réseau cellulaire ou Wi-Fi.
- **NFR-P5** : La taille de téléchargement initiale de l'application (depuis App Store ou Play Store) est inférieure à **30 Mo**.
- **NFR-P6** : L'application maintient une frame rate cible de **60 FPS** sur l'ensemble des animations et scrolls, sans frame drop perceptible.

### Security

- **NFR-S1** : L'isolation des données entre projets distincts est garantie par des Row-Level Security policies Postgres ; aucun membre d'un projet A ne peut accéder, en lecture ou en écriture, aux données d'un projet B.
- **NFR-S2** : Le code de partage à 6 caractères est généré dans un alphabet alphanumérique excluant les caractères ambigus (0/O, 1/I/l) ; il offre un espace d'au moins 30^6 ≈ 730 millions de combinaisons pour résister au brute-force opportuniste. Le rate-limiting côté Edge Function (max 5 tentatives par minute et par IP) renforce cette protection.
- **NFR-S3** : Toutes les communications client ↔ Supabase utilisent TLS 1.2 ou supérieur (standard Supabase). Aucun appel HTTP non chiffré n'est émis par l'application.
- **NFR-S4** : Aucune donnée personnelle identifiable au sens RGPD strict (email, téléphone, adresse, date de naissance, identifiant officiel) n'est collectée par défaut au MVP. Seuls le prénom (et éventuellement nom) saisis par les membres sont stockés.
- **NFR-S5** : L'infrastructure Supabase est hébergée dans une région UE (Frankfurt ou Paris) pour conformité RGPD.
- **NFR-S6** : Une politique de confidentialité publique (en français) est disponible avant la première publication sur les stores, listant : les données collectées, l'usage, la durée de conservation, les droits des utilisateurs, et le contact pour exercer ces droits.
- **NFR-S7** : Un membre dispose à tout moment de la capacité de supprimer son identité d'un projet (FR42) ; les données associées sont effectivement supprimées du serveur dans un délai inférieur à 24 heures.

### Reliability

- **NFR-R1** : Le taux de sessions sans crash (crash-free sessions) est supérieur à **99,5 %** sur l'ensemble des appareils supportés, mesuré par un outil de crash reporting (Crashlytics, Sentry ou équivalent à arbitrer en étape architecture).
- **NFR-R2** : En cas d'échec d'envoi d'une notification push, le système effectue jusqu'à 3 tentatives avec backoff exponentiel ; tout échec définitif est journalisé pour diagnostic.
- **NFR-R3** : Lorsqu'une opération réseau échoue (création de réunion, arbitrage, etc.), l'utilisateur reçoit un message d'erreur clair en français et l'opération peut être ré-essayée d'un seul tap.
- **NFR-R4** : L'application gère gracieusement la perte de connexion : la consultation des données reste possible (FR37) et aucun crash ne se produit lors d'une bascule online → offline → online.
- **NFR-R5** : La disponibilité du backend Supabase utilisée est au moins celle garantie par le SLA de Supabase (99,9 % sur le plan Pro). Pas d'engagement de SLA explicite côté Colision MVP.

### Accessibility

Étant donné le persona racine (Sophie, non-tech, profil non-spécialiste), l'accessibilité est traitée comme un sous-ensemble pragmatique de WCAG plutôt qu'une conformité complète :

- **NFR-A1** : Toutes les cibles tactiles principales (boutons, items de liste) mesurent au moins **48 × 48 dp** (Android Material guideline) / **44 × 44 pt** (iOS HIG) pour garantir une frappe précise.
- **NFR-A2** : Le contraste de texte respecte le ratio **4,5:1** minimum pour le corps de texte (WCAG 2.1 AA).
- **NFR-A3** : Toutes les chaînes de texte de l'interface sont internalisables (pas de texte en dur dans le code des écrans) et formulées dans un français accessible (niveau B1 max).
- **NFR-A4** : L'application supporte la taille de texte système (Dynamic Type iOS, fontScale Android) jusqu'à 130 % sans casser le layout.
- **NFR-A5** : Les éléments interactifs sont étiquetés pour les technologies d'assistance (VoiceOver iOS, TalkBack Android) avec des labels descriptifs en français.
- **NFR-A6 (hors MVP, V1.1)** : Conformité WCAG 2.1 AA complète avec audit externe. Au MVP, on vise une conformité de bon sens sans audit.

### Scalability

- **NFR-SC1** : L'architecture supporte sans modification jusqu'à **100 projets actifs simultanés** et **2 000 membres totaux** sur l'ensemble des projets, avec maintien des cibles de performance (NFR-P1 à NFR-P4).
- **NFR-SC2** : L'algorithme de détection de conflit reste sous le seuil des **200 ms** pour un projet contenant jusqu'à **1 000 réunions actives** et **50 membres**.
- **NFR-SC3** : En cas de croissance imprévue dépassant le tier gratuit Supabase (500 Mo DB, 2 Go egress mensuel), le passage au plan Pro (25 $/mois) doit être réalisable sans interruption de service et sans changement de code.
- **NFR-SC4** : La structure des Edge Functions Supabase pour le dispatch push doit supporter au moins **100 pushes par minute** sans dépasser les quotas FCM ou APNs.

### Maintainability

- **NFR-M1** : Le code partagé (`commonMain`) atteint une couverture de tests unitaires d'au moins **60 %** sur la logique métier critique (détection de conflit, génération de code, validation des entrées).
- **NFR-M2** : Le pipeline CI exécute build + tests unitaires + tests d'instrumentation Android + tests iOS simulator sur chaque push sur les branches `main` et toute pull request ; un échec bloque le merge.
- **NFR-M3** : Toutes les dépendances tierces (Supabase, Compose, Room, etc.) sont centralisées dans `gradle/libs.versions.toml` (déjà en place dans le scaffolding KMP) ; aucune dépendance n'est déclarée en dur dans les `build.gradle.kts` de modules.
- **NFR-M4** : Un outil de monitoring des erreurs (Crashlytics, Sentry ou équivalent) est intégré dès le MVP, avec capture automatique des crashes et erreurs réseau non gérées.
- **NFR-M5** : Un outil d'analytique produit (Firebase Analytics, PostHog, ou Amplitude — à arbitrer en architecture) collecte les événements clés : install, project_joined, meeting_created, conflict_detected, arbitrage_submitted, app_opened, push_received. Pas d'événements identifiant nominalement les utilisateurs (analytics anonymisé).
- **NFR-M6** : Le code source est versionné dans un dépôt Git public ou privé selon le choix d'Anthony ; chaque feature ou fix non trivial passe par une pull request avec description, même si l'auteur et le reviewer sont la même personne.
- **NFR-M7** : La documentation interne (CLAUDE.md déjà en place, ARCHITECTURE.md à venir en étape architecture) est mise à jour à chaque décision technique significative.

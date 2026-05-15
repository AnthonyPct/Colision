---
stepsCompleted: [1, 2, 3, 4, 5, 6]
inputDocuments: []
date: 2026-05-10
author: Anthony Picquet
project_name: Colision
---

# Product Brief: Colision

## Executive Summary

**Colision** est une application mobile (iOS + Android, Kotlin Multiplatform / Compose) de planification collaborative qui détecte automatiquement les conflits de réunions entre plusieurs groupes auxquels une même personne appartient.

Inspirée de la simplicité d'usage de **Tricount**, Colision permet de rejoindre un calendrier partagé via un code, sans création de compte obligatoire. Le premier utilisateur crée un "projet" (par exemple : un conseil municipal, une association, un comité d'entreprise), définit ses sous-groupes (commissions, équipes, cellules), et invite les autres via un code. Chaque membre choisit son identité dans la liste, rejoint les groupes auxquels il appartient, et peut désormais voir et créer des réunions sans risque de doublon.

Le cœur produit : **dès qu'un organisateur planifie une réunion pour un groupe, Colision détecte si certains membres de ce groupe sont déjà mobilisés ailleurs et l'alerte avant validation** — supprimant le casse-tête récurrent que vivent aujourd'hui les conseils municipaux où les commissions s'organisent en silo via des groupes WhatsApp séparés.

Colision est conçu pour s'adapter bien au-delà du contexte municipal : associations, clubs sportifs multi-équipes, comités d'entreprise, équipes pédagogiques, ou tout collectif où les mêmes personnes circulent entre plusieurs sous-groupes.

---

## Core Vision

### Problem Statement

Dans toute organisation structurée en sous-groupes où les mêmes personnes appartiennent à plusieurs sous-groupes simultanément — typiquement un conseil municipal et ses commissions (Jeunesse, Sport, Travaux, Culture…) — la planification des réunions se fait en **silo**. Chaque organisateur de sous-groupe propose un créneau sans visibilité sur les engagements des membres dans les autres sous-groupes.

Résultat : **les conflits d'agenda sont la norme, pas l'exception**. Une réunion Jeunesse le jeudi 20h tombe pile sur la réunion Sport déjà calée. Plusieurs membres doivent choisir, s'excuser, ou tout faire reprogrammer. Le coût n'est pas seulement organisationnel — c'est de la friction qui érode l'engagement bénévole.

### Problem Impact

- **Membres polyvalents lésés** : les plus engagés (souvent dans 2-3 commissions) sont ceux qui subissent le plus de doublons.
- **Reprogrammations en cascade** : mails de relance, sondages Doodle de rattrapage, plusieurs allers-retours WhatsApp par réunion.
- **Démotivation** : devoir arbitrer entre deux engagements pris envers ses pairs crée de la culpabilité et de la lassitude.
- **Sous-représentation aux décisions** : quand quelqu'un manque une réunion à cause d'un conflit, sa voix disparaît du débat.
- **Charge mentale de l'organisateur** : vérifier manuellement les agendas de 5–10 personnes via WhatsApp avant chaque créneau, sans jamais avoir la certitude d'avoir tout vu.

### Why Existing Solutions Fall Short

- **WhatsApp / SMS (état de l'art actuel pour ce persona)** : zéro vue d'ensemble. Chacun connaît son propre planning, personne ne connaît celui des autres groupes.
- **Google Calendar / agendas partagés** : exigent un compte Google, un partage explicite, et ne modélisent pas la notion de "groupe à appartenance multiple". Les conflits ne sont pas calculés *au croisement des groupes*.
- **Doodle / When2meet** : utiles pour fixer *un* créneau, mais sondage par réunion, pas d'historique, pas de détection de chevauchements entre sondages.
- **Outlook / agendas pro** : inadaptés aux contextes bénévoles, municipaux ou associatifs où les gens ne sont pas tous dans la même organisation IT.
- **Aucune solution dédiée** existante ne combine les trois ingrédients clés : *appartenance multi-groupes* + *détection de conflit cross-groupes* + *onboarding sans compte*.

### Proposed Solution

Une application mobile native (iOS + Android, Kotlin Multiplatform avec UI Compose partagée) construite autour de cinq fonctions essentielles :

1. **Créer un projet sans friction** — le premier utilisateur crée le calendrier partagé, définit les sous-groupes, ajoute les personnes (un prénom suffit). Un code de partage est généré.
2. **Rejoindre via code** — à la Tricount : les autres membres entrent le code, choisissent leur identité dans la liste pré-remplie (ou s'ajoutent), et rejoignent les groupes auxquels ils appartiennent.
3. **Planifier avec détection de conflit en temps réel** — lorsqu'un membre crée une réunion pour un groupe, Colision affiche immédiatement les membres du groupe déjà mobilisés sur ce créneau dans d'autres groupes, avec proposition de décaler.
4. **Notification curative** — si la réunion est créée *malgré* le conflit, les conflictés reçoivent une notification et voient l'arbitrage à faire dans leur dashboard.
5. **Vue calendrier personnelle agrégée** — chaque membre voit son agenda consolidé sur tous les groupes du projet.

L'authentification reste **optionnelle** : la confiance dans le collectif suffit pour la plupart des usages, et l'authentification peut être proposée pour les projets sensibles.

### Key Differentiators

- **"Le Tricount du calendrier"** : modèle mental immédiat, zéro compte requis, prise en main en moins d'une minute.
- **Conflits *cross-groupes* calculés automatiquement** — la fonctionnalité que ni WhatsApp, ni Google Calendar, ni Doodle ne couvrent.
- **Modèle agnostique du contexte** : pensé pour un conseil municipal, utilisable par une association, un club sportif multi-équipes, un CSE, une équipe pédagogique, un groupe de bénévoles.
- **Mobile-first natif** (Kotlin Multiplatform) : pas une web app bricolée, pas un Notion détourné — une app fluide pensée pour usage quotidien.
- **Friction d'onboarding minimale** : c'est *la* raison pour laquelle WhatsApp l'emporte sur tous les outils plus puissants. Colision applique la même règle.
- **Insight terrain** : produit né d'un cas réel (conseil municipal d'une commune française), pas d'une intuition de bureau.

---

## Target Users

### Primary Users

**Persona racine — "Sophie", conseillère municipale fraîchement élue**

*Profil :*
- 35-50 ans, métier hors-tech (par exemple : assistante maternelle, infirmière, enseignante, artisan).
- Récemment élue lors du renouvellement des conseils municipaux français — premier mandat ou tout début de mandat (moins de 6 mois).
- Membre de **2 à 3 commissions thématiques** parmi les 6 à 10 commissions du conseil (Jeunesse, Sport, Travaux, Culture, École, Finances…).
- **Rapport à la technologie : faible à très faible**. Utilise son smartphone principalement pour WhatsApp, les appels, parfois Facebook ou Instagram. Appelle un proche quand le Wi-Fi tombe. N'a jamais installé d'app "pro" de sa propre initiative.

*Contexte d'usage :*
- Sophie découvre la vie municipale et est surprise par le **volume des réunions**, le plus souvent en soirée, après son vrai travail.
- Les invitations lui arrivent via WhatsApp ou SMS, dans plusieurs groupes séparés (un par commission).
- Elle **ne crée pas les réunions** — elle les subit. Ce sont d'autres conseillers, parfois différents selon la commission, qui posent les créneaux.
- Quand deux commissions se chevauchent, c'est elle qui doit choisir, prévenir, s'excuser, parfois sécher.

*Frustration vécue :*
- Tension permanente entre engagement civique, vie professionnelle et vie familiale.
- Sentiment de "je rate des choses sans même savoir pourquoi".
- Charge mentale d'agréger mentalement les engagements de chaque commission.
- Aucun lieu unique où voir l'ensemble de ses obligations municipales.

*Vision du succès :*
- Une app qu'elle ouvre une à deux fois par semaine pour vérifier son planning agrégé.
- Notifications discrètes uniquement quand un conflit la concerne directement.
- Aucune configuration de sa part : quelqu'un d'autre a créé le projet, elle rejoint via un code, et c'est tout.

**Modèle plat des rôles dans l'app**

Colision **n'impose aucune hiérarchie**. Tout membre d'un projet peut créer ce projet, ajouter une commission, organiser une réunion, ou simplement consommer le planning. Les rôles formels de la vie réelle (président de commission, maire, secrétaire de mairie) existent en dehors de l'app — Colision ne les modélise pas. Cela reflète une réalité observée : dans un conseil municipal de taille modeste, l'initiative de coordination peut venir de n'importe qui.

### Secondary Users

Pour la V1, Colision se concentre **exclusivement** sur le persona "conseiller municipal". Les autres segments identifiés dans la vision — associations à commissions, clubs sportifs multi-équipes, CSE, équipes pédagogiques, collectifs de bénévoles — restent **non développés à ce stade**. La discipline produit consiste à valider d'abord le produit sur un segment précis avant d'élargir.

### User Journey

**1. Découverte**
Sophie entend parler de Colision par un autre conseiller, par bouche-à-oreille interne au conseil, ou par un proche développeur qui lui montre. Elle ne va pas chercher l'app dans le store de sa propre initiative — l'acquisition se fait par contagion virale interne au collectif.

**2. Onboarding**
Un conseiller plus à l'aise tech a déjà créé le projet "Conseil municipal de [Commune]" et ajouté les commissions. Sophie reçoit un code via WhatsApp ("Tiens, télécharge l'app et entre ce code"). Elle télécharge, entre le code, voit la liste des conseillers, sélectionne son nom, coche les commissions auxquelles elle appartient. **Total : moins de 60 secondes.**

**3. Usage courant**
Sophie ouvre l'app dans deux situations :
- Notification reçue (nouveau créneau dans une de ses commissions, ou conflit la concernant).
- Avant de prendre un engagement personnel (anniversaire, rendez-vous), elle vérifie son planning agrégé.

**4. Moment "Aha!"**
Hypothèse à valider : c'est le **premier vrai conflit détecté en amont** qui lui évite d'avoir à arbitrer à la dernière minute, ou la **première semaine sans avoir reçu 8 messages WhatsApp de coordination**. La valeur devient évidente après 2 à 3 semaines d'usage, quand le réflexe est installé.

**5. Long-terme**
L'app devient un réflexe avant tout engagement extra-conseil. Les groupes WhatsApp de coordination se vident progressivement, leur fonction étant absorbée par Colision. Sophie recommande l'app à des conseillers d'autres communes qu'elle croise en formation ou en réunion intercommunale — amorçage du modèle viral.

---

## Success Metrics

Colision est positionné en **side project gratuit et public** (scénario "b"), avec une fenêtre d'évolution vers un modèle commercial si l'adoption décolle. Les métriques sont donc orientées **validation produit** et **adoption virale**, pas revenue à court terme.

### User Success Metrics

Mesurées du point de vue de **Sophie** (la persona racine) — les comportements qui prouvent que le produit crée de la valeur réelle :

- **Onboarding réussi en moins de 60 secondes** : du téléchargement au premier écran utile (calendrier de son projet), sans avoir besoin d'aide extérieure. Mesure : durée mesurée entre install et fin de l'onboarding.
- **Au moins 1 ouverture par semaine** après les deux premières semaines — signe que l'app est entrée dans le réflexe.
- **Au moins 1 conflit détecté par mois** pour un utilisateur actif dans 2+ commissions — la preuve que la fonction cœur sert vraiment.
- **Rétention J30 ≥ 50 %** : la moitié des utilisateurs encore actifs un mois après leur première utilisation. Sous ce seuil, le produit n'a pas trouvé son product/market fit pour ce persona.
- **Coefficient viral ≥ 1.0** dans un projet : en moyenne, chaque membre invité rejoint effectivement (et idéalement en invite ou en motive d'autres). Mesure : ratio inscriptions effectives / invitations envoyées via code.
- **NPS qualitatif** : sur un échantillon de premiers utilisateurs, la question unique "Recommanderais-tu Colision à un autre élu ?" — réponse libre, qualitatif.

### Business Objectives

Étant donné le positionnement (b) gratuit, les objectifs business sont des **jalons d'adoption** plutôt que des objectifs financiers.

**Horizon 3 mois (post-lancement V1)** :
- Validation locale : la commune de la persona racine utilise activement l'app (10+ conseillers actifs hebdomadaires sur ~20 membres du conseil).
- 1 à 3 autres communes ou collectifs ont essayé spontanément (bouche-à-oreille initial).
- Au moins 5 témoignages qualitatifs concrets d'utilité ("ça m'a évité un conflit", "j'ai compris à quelle réunion aller").

**Horizon 6 mois** :
- 5 à 10 projets actifs (conseils municipaux, associations à commissions, ou autres collectifs ayant adopté spontanément).
- Rétention J30 ≥ 50 % sur l'ensemble des utilisateurs.
- Note App Store / Play Store ≥ 4,3 / 5 sur au moins 20 avis.

**Horizon 12 mois** :
- 30 à 50 projets actifs (objectif élargi vers d'autres communes via le bouche-à-oreille intercommunal, et potentiellement vers d'autres personas si des cas d'usage émergent organiquement).
- Décision explicite : maintenir en (b) gratuit, ou basculer en (c) avec freemium / abonnement projet. La décision se fonde sur le coût d'hébergement vs. taux de rétention et signal de willingness-to-pay.

> Note : Tous les chiffres ci-dessus sont des **hypothèses de départ à recalibrer** dès les premières semaines de données réelles. Ils servent à donner une direction, pas à fixer un contrat.

### Key Performance Indicators

KPIs concrets à instrumenter dès la V1 :

| KPI | Définition | Cible V1 |
|---|---|---|
| **Onboarding completion rate** | % d'installs qui arrivent jusqu'à la sélection d'identité dans un projet | ≥ 80 % |
| **Time-to-first-value** | Délai install → première création ou consultation de réunion | < 5 min |
| **WAU / installed** | Utilisateurs actifs hebdo rapportés aux installs cumulés | ≥ 40 % à J30 |
| **Rétention J30** | % d'utilisateurs encore actifs 30 jours après onboarding | ≥ 50 % |
| **Conflits détectés / mois** | Nombre de conflits cross-commissions affichés par l'app | Tracking, pas de cible — informe la valeur livrée |
| **Conflits résolus en amont** | % de conflits qui aboutissent à un décalage du créneau plutôt qu'à une création quand même | ≥ 60 % |
| **Invitation-to-join rate** | % d'invitations par code qui se concrétisent en rejoint effectif | ≥ 70 % |
| **Crash-free sessions** | Sessions sans crash | ≥ 99,5 % |

### Vanity Metrics — Ce qu'on Ignore Explicitement

Pour éviter de se mentir à soi-même, on **bannit** ces métriques comme indicateurs de succès :

- **Téléchargements totaux** sans regarder la rétention : ouvrir l'app une fois n'a aucune valeur.
- **Temps passé dans l'app** : Colision doit être *brève*. Plus on y passe de temps, pire c'est. L'app est un outil de coordination, pas une plateforme d'engagement.
- **Likes / abonnés sur les stores** : signaux purement décoratifs.
- **Nombre total de réunions créées** : volume sans qualité. Mieux vaut 10 réunions sans conflit que 100 réunions chaotiques.
- **MAU pur** : pour un usage hebdomadaire à mensuel comme Colision, le DAU n'a pas de sens et le MAU peut masquer une rétention faible. On regarde surtout WAU + rétention par cohorte.

---

## MVP Scope

### Core Features

Le MVP de Colision est volontairement **réduit au strict minimum** capable de résoudre la douleur cœur — la détection de conflits cross-commissions. Chaque fonctionnalité ci-dessous est défendue parce qu'elle est indispensable au "moment Aha!" de Sophie.

**1. Création d'un projet**
- Un utilisateur crée un projet en saisissant un nom (ex. "Conseil municipal de Saint-Machin").
- Un code de partage à 6 caractères est généré automatiquement.
- Le créateur est ajouté comme premier membre du projet.

**2. Gestion des commissions (groupes)**
- N'importe quel membre du projet peut créer une commission (nom libre).
- N'importe quel membre peut éditer ou supprimer une commission existante.
- Pas de hiérarchie : tout le monde a les mêmes droits.

**3. Gestion des membres**
- N'importe quel membre peut ajouter un nouveau membre au projet (juste un prénom suffit).
- Chaque membre peut être assigné à 0, 1 ou plusieurs commissions, par n'importe quel membre.

**4. Rejoindre un projet via code**
- L'utilisateur entre le code de 6 caractères.
- Il voit la liste des membres déjà ajoutés et sélectionne son identité.
- Si son nom n'est pas dans la liste, il s'ajoute.
- Il coche les commissions auxquelles il appartient.
- Total : moins de 60 secondes du téléchargement à la première utilisation.

**5. Création d'une réunion (le moment "wow")**
- N'importe quel membre crée une réunion en spécifiant :
  - Date et heure de début
  - Durée (présélections : 30min, 1h, 1h30, 2h, ou libre)
  - Commission(s) concernée(s) — sélection multiple possible
  - Titre court (optionnel)
- **Avant validation**, Colision affiche les membres des commissions concernées qui ont **déjà** un engagement sur ce créneau dans une autre commission.
- Option proposée : décaler le créneau, ou créer quand même.

**6. Notification curative en cas de conflit créé malgré l'alerte**
- Les membres conflictés voient le conflit affiché dans leur dashboard, avec les deux réunions en cause et la possibilité de marquer leur arbitrage (manuellement : "je vais à celle-ci").

**7. Vue calendrier personnelle agrégée**
- Chaque membre voit son agenda consolidé sur toutes ses commissions, en vue semaine et vue mois.

**8. Vue par commission**
- Liste des réunions à venir de chaque commission, accessible à tous les membres du projet (pas seulement aux membres de la commission).

**9. Notifications push natives**
- Push envoyé aux membres conflictés quand une réunion est créée malgré l'alerte de conflit (cas curatif).
- Push envoyé à tous les membres d'une commission quand une nouvelle réunion est planifiée (annonce standard).
- Stack : FCM pour Android, APNs pour iOS, déclenchés depuis le backend de synchronisation.

**Stack technique MVP** :
- App mobile native iOS + Android via Kotlin Multiplatform / Compose Multiplatform (UI partagée).
- Backend minimal : un service de synchronisation des projets (ex. Firebase Firestore, Supabase, ou solution backend léger), capable d'émettre des notifications push via FCM (Android) et APNs (iOS).
- Pas d'authentification utilisateur — le code projet sert d'identifiant partagé. Confiance interne au collectif.

### Out of Scope for MVP

Ces fonctionnalités sont **identifiées comme utiles** mais **explicitement exclues** du MVP. Chacune est défendue par un coût/valeur défavorable à ce stade.

| Fonctionnalité | Pourquoi exclue du MVP |
|---|---|
| Authentification (PIN, email, password) | Friction d'onboarding contraire au pari Tricount. À considérer V2 si usurpation devient un problème. |
| Réunions récurrentes (hebdo, mensuelle) | Complexité importante côté détection de conflit. Peut être contournée à la main au MVP. |
| Lieu / adresse de la réunion | Information utile mais pas critique à la résolution du conflit. |
| RSVP / liste de présence | Modèle plus riche, peut attendre V2. |
| Ordre du jour, compte rendu, pièces jointes | Hors-périmètre — Colision n'est pas un outil de gestion de réunion, c'est un outil de planification. |
| Export iCal / Google Calendar | Tentant, mais ajoute une complexité tech et brouille le modèle "tout est dans Colision". À envisager V2. |
| Multi-projets par utilisateur | Au MVP, un appareil = un projet rejoint. Pivot facile vers multi si demande émerge. |
| Internationalisation | Français uniquement. |
| Mode hors-ligne complet | Cache léger des données déjà chargées, mais pas de création offline. |
| Web app / desktop | Mobile uniquement. |
| Rôles / permissions différenciées | Modèle plat assumé. |
| Sondages de créneaux (style Doodle) | Hors-périmètre — on propose un créneau, on détecte les conflits, on ajuste. Pas un sondage. |

### MVP Success Criteria

Le MVP est considéré **réussi** si les conditions suivantes sont réunies dans les 3 mois post-lancement :

- **Le projet "Conseil municipal de [Commune de Sophie]" est actif** : 10+ conseillers ont rejoint, 5+ commissions ont été créées, 10+ réunions ont été planifiées via l'app.
- **Au moins 3 conflits ont été détectés** par l'app et ont mené à un décalage de créneau (preuve de la valeur cœur).
- **Sophie utilise l'app sans demander d'aide à son frère** pour ouvrir, consulter, ou créer une réunion. (Test de l'UX pour non-tech.)
- **Au moins 1 autre commune ou collectif** a essayé spontanément Colision via bouche-à-oreille.
- **Rétention J30 ≥ 50 %** sur la cohorte de la première commune.

Si ces critères sont **atteints**, on engage la roadmap post-MVP (voir Future Vision). Sinon, on diagnostique : la valeur n'est-elle pas perçue ? L'UX est-elle trop complexe ? Le problème est-il moins fréquent qu'imaginé ?

### Future Vision (post-MVP)

**Horizon court (V1.1 — 3 à 6 mois post-MVP)** :
- Réunions récurrentes.
- Lieu / adresse de réunion.
- RSVP basique (présent / absent / peut-être).
- Mode hors-ligne en lecture renforcé.

**Horizon moyen (V2 — 6 à 12 mois)** :
- Authentification optionnelle (PIN ou lien email magique).
- Multi-projets par utilisateur (un même Sophie dans 2 conseils différents).
- Export / synchronisation calendrier (iCal, Google).
- Sondage de créneaux quand pas d'accord immédiat sur une date.
- Extension à d'autres personas (associations, clubs sportifs, CSE) si signal organique.
- Mode web / desktop léger.

**Horizon long (V3 — au-delà de 12 mois)** :
- Modèle freemium ou abonnement projet si le coût d'hébergement le justifie (passage potentiel au scénario commercial "c").
- Intégrations municipales (export PV, lien avec outils de mairie).
- Mode "intercommunal" pour coordonner plusieurs projets liés.
- Analytics privé pour le créateur du projet (taux de présence, participation par commission).

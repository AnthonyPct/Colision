# Descriptions store — FR

Source de vérité pour le texte des fiches Colision sur l'App Store et le Play
Store. Mettre à jour ici, puis copier dans les consoles.

---

## App Store (iOS)

### Nom (≤ 30 caractères)
```
Colision
```

### Sous-titre (≤ 30 caractères)
```
Fini les réunions en double
```
*27 caractères.*

### Texte promotionnel (≤ 170 caractères)
```
Détecte en temps réel quand deux commissions vous convoquent au même créneau. Sans compte, sans inscription, en moins d'une minute pour rejoindre.
```
*147 caractères.*

### Mots-clés (≤ 100 caractères, séparés par virgule)
```
conseil,commission,réunion,agenda,conflit,planning,municipal,collectif,élu,association
```
*87 caractères.*

### Description (≤ 4000 caractères)
*Voir [Description longue](#description-longue) ci-dessous.*

### URL politique de confidentialité
```
https://anthonypct.github.io/Colision/
```

---

## Play Store (Android)

### Nom de l'application
```
Colision
```

### Description courte (≤ 80 caractères)
```
Détecte automatiquement les conflits de réunions entre vos commissions.
```
*71 caractères.*

### Description complète (≤ 4000 caractères)
*Voir [Description longue](#description-longue) ci-dessous.*

### URL politique de confidentialité
```
https://anthonypct.github.io/Colision/
```

---

## Description longue

Texte commun aux deux stores (≤ 4000 caractères).

```
Colision est l'app simple qui empêche deux de vos commissions de vous convoquer au même créneau.

Vous siégez dans un conseil municipal, une association, un comité ? Vous êtes dans plusieurs commissions, chacune planifie ses réunions dans son coin, et vous vous retrouvez chaque mois avec trois rendez-vous le même soir — sans personne pour le voir venir.

Colision résout ce problème, en une minute, sans compte ni inscription.

CE QUE COLISION FAIT POUR VOUS

• Détection automatique des conflits — Quand quelqu'un crée une réunion qui chevauche un créneau déjà pris par une autre commission, l'app prévient immédiatement les personnes concernées.

• Agenda agrégé — Toutes vos réunions, toutes commissions confondues, dans une seule vue calendrier. Vous voyez enfin votre semaine d'un coup d'œil.

• Notifications de conflit — Une réunion entre en collision avec votre agenda ? Vous recevez une notification push pour pouvoir trancher tout de suite.

• Arbitrage en un geste — Quand un conflit vous concerne, vous choisissez où vous allez en deux taps. Les organisateurs des deux réunions sont prévenus automatiquement.

• Mode hors ligne — Consultez votre agenda même sans réseau, dans le métro ou en réunion. La synchronisation est automatique au retour de connexion.

COMMENT ÇA MARCHE

1. Vous créez votre projet (par exemple « Conseil municipal de Saint-Machin »).
2. Vous ajoutez vos commissions et listez les membres avec leur prénom.
3. Vous partagez un code à 6 caractères. Vos collègues rejoignent en 30 secondes, choisissent leur nom dans la liste et cochent leurs commissions.

C'est tout. Pas d'email, pas de mot de passe, pas de hiérarchie de droits. N'importe quel membre peut ajouter une réunion, modifier une commission, ou inviter quelqu'un.

POUR QUI

Colision est pensée pour les collectifs structurés en sous-groupes : conseils municipaux, conseils de quartier, conseils syndicaux, comités d'association, bureaux d'élus. Si plusieurs personnes appartiennent à plusieurs équipes qui se réunissent en parallèle, vous êtes au bon endroit.

L'app est conçue pour être plus simple qu'un fil WhatsApp. Aucune connaissance technique nécessaire.

VIE PRIVÉE

Pas de publicité, pas de traceur, pas de revente de données. Colision ne demande que votre prénom et les commissions auxquelles vous appartenez. Vos données sont hébergées en Union européenne (Francfort, Allemagne). Vous pouvez les supprimer à tout moment en quittant le projet.

Politique de confidentialité : https://anthonypct.github.io/Colision/
Contact : picquetanthony@gmail.com
```

*≈ 2 720 caractères — confortablement sous la limite des 4 000.*

---

## Notes de cohérence

- **Voix** : « vous » (pluriel collectif), aligné avec le persona Sophie et le ton civic/warm du design system.
- **Promesse** : « fini les réunions en double » — réfère au pain principal du PRD (J1+J3+J4).
- **Différenciateur** : « en une minute, sans compte ni inscription » — réfère à la contrainte UX (« plus simple que WhatsApp », `colision_persona_non_tech`).
- **RGPD** : la mention vie privée renvoie à la même page que `NSPrivacyPolicyURL` / Data safety form, pour cohérence.

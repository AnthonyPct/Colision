# CI / Build pipelines

Ce document décrit le pipeline d'intégration et de release de Colision. Pour les
décisions d'architecture qui ont motivé ce setup, voir `docs/architecture.md`
section *Infrastructure & Deployment*.

## Vue d'ensemble — un hybride à deux étages

Colision utilise **deux runners CI complémentaires** :

| Runner | Rôle | Coût | Déclenchement |
|---|---|---|---|
| **GitHub Actions** | Vérif de build + tests sur chaque push/PR | Gratuit (quota GH) | Auto sur `push` / `pull_request` (branches `main`, `develop`) |
| **Bitrise** | Artefacts de release (AAB/APK signés, IPA), signature, distribution | Payant (minutes macOS) | Manuel ou auto via triggers définis |

**Pourquoi les deux** : GHA est gratuit et rapide pour les vérifs continues, mais
la signature iOS App Store et la distribution TestFlight/Play sont nettement plus
simples à orchestrer côté Bitrise (Code Signing & Files, Apple service connection
intégrée). On évite ainsi de gérer manuellement les certificats Apple dans des
secrets GHA.

## GitHub Actions

Trois workflows dans `.github/workflows/`, chacun déclenché sur `push` et
`pull_request` vers `main` ou `develop`, avec annulation des runs concurrents
sur le même ref (`concurrency.cancel-in-progress`).

### `build-android.yml`
- Runner : `ubuntu-latest`
- Tâches : `:composeApp:assembleDevelopmentDebug` + `:composeApp:testDevelopmentDebugUnitTest`
- Artifact sur échec : `composeApp/build/reports/tests/**` (7 jours)

### `build-ios.yml`
- Runner : `macos-15` (Xcode 16+ requis — `macos-14` casse le link Kotlin/Native
  des tests à cause de l'autolink Swift runtime sur les deps CryptoKit interop ;
  voir le commentaire en tête du workflow)
- Tâches : `:composeApp:linkDebugFrameworkIosSimulatorArm64` +
  `:composeApp:iosSimulatorArm64Test`
- **Workaround Sentry xcframework** identique à Bitrise (cf. section dédiée plus
  bas) : télécharge `Sentry.xcframework.zip` depuis la release sentry-cocoa
  pinnée et exporte `SENTRY_FRAMEWORK_PATH`. Cache GHA via `actions/cache@v4`
  sur la version Sentry.
- Variable `SENTRY_COCOA_VERSION` au niveau du workflow — **bump en lockstep**
  avec `iosApp/iosApp.xcodeproj/project.xcworkspace/xcshareddata/swiftpm/Package.resolved`.

### `lint.yml`
- Runner : `ubuntu-latest`
- Tâche : `:composeApp:lintDevelopmentDebug` (Android lint)
- TODO documenté dans le workflow : brancher `detekt` + `ktlintCheck` quand les
  plugins Gradle seront ajoutés.

## Bitrise

### Identifiants

- **App slug** : `80a2ca79-ec67-4620-9fae-646d339e0af8`
- **Workspace** : `AnthonyPcqt's workspace` (`5db92acb0d26966a`)
- **Project type** : `kotlin-multiplatform`
- **Stack** : `osx-xcode-26.5.x` (macOS pour pouvoir builder iOS depuis le même
  app, et Android tourne sans souci sur cette stack aussi)
- **Machine** : `g2.mac.medium`

### Où vit `bitrise.yml`

**Le `bitrise.yml` est stocké côté Bitrise, pas dans le repo.** Les modifications
passent par l'UI Bitrise, ou par l'outillage MCP (`update_bitrise_yml`). Si on
décide un jour de versionner la config dans le repo, il faudra activer l'option
*Store bitrise.yml in repository* côté Bitrise et committer le fichier — mais
ce n'est pas le cas actuellement.

### Workflows

#### `run_tests`
- Déclencheurs : `push` sur `main`, `pull_request` toutes branches.
- Steps : git-clone → gradle cache (restore/save + build cache) → `gradle-unit-test@2` → deploy artifacts.

#### `android_build` *(release artifacts, manuel)*
- Steps :
  1. `git-clone` → `restore-gradle-cache` → `activate-build-cache-for-gradle`
  2. `android-build@1` — variant `productionRelease`, `build_type: aab`
  3. `android-build@1` — variant `productionRelease`, `build_type: apk`
  4. `sign-apk@2` — `run_if: '{{getenv "BITRISEIO_ANDROID_KEYSTORE_URL" | ne ""}}'`
     → signe l'AAB **ET** l'APK (cf. section signature)
  5. `save-gradle-cache` → `deploy-to-bitrise-io`
- Artefacts produits : `composeApp-production-release-bitrise-signed.aab`,
  `composeApp-production-release-bitrise-signed.apk` (+ versions unsigned et
  aligned conservées).
- Durée typique : ~2,5 min (cache chaud).

#### `ios_build` *(validation, simulateur non-signé — état actuel)*
- Steps :
  1. `git-clone` → `restore-gradle-cache` → `activate-build-cache-for-gradle`
  2. **Script** *Fetch Sentry.xcframework* — télécharge la version pinnée et
     exporte `SENTRY_FRAMEWORK_PATH` vers le slice simulateur (cf. section iOS).
  3. **Script** *Build iOS app (simulator, unsigned)* —
     `xcodebuild build -sdk iphonesimulator -destination 'generic/platform=iOS Simulator' CODE_SIGNING_ALLOWED=NO`
  4. `save-gradle-cache` → `deploy-to-bitrise-io`
- Ce workflow valide la chaîne KMP → Kotlin framework → Sentry sans signer. La
  version signée (`xcode-archive@6` + App Store Connect API key) est planifiée
  une fois l'enrôlement Apple Developer + l'app dans App Store Connect en place.

### Pipeline

Un pipeline `build` combine `android_build` + `ios_build` en parallèle pour les
release runs manuels.

## Android — release signing

### Keystore

- **Type** : *upload key* pour Google Play App Signing (Google détient le clé
  d'application finale ; cette clé sert uniquement à uploader).
- **Fichier local** : `config/signature/colision-upload.jks` (gitignoré — le
  dossier complet `config/signature/` et tout `*.jks` / `*.keystore` sont
  exclus par `.gitignore`).
- **Alias** : `colision-upload`.

### Upload sur Bitrise

Le keystore et ses mots de passe sont uploadés dans
**Bitrise → App settings → Code Signing & Files → Android Keystore**. Une fois
en place, Bitrise expose au step `sign-apk` les variables :
- `BITRISEIO_ANDROID_KEYSTORE_URL`
- `BITRISEIO_ANDROID_KEYSTORE_PASSWORD`
- `BITRISEIO_ANDROID_KEYSTORE_ALIAS`
- `BITRISEIO_ANDROID_KEY_PASSWORD`

Le `run_if` du step `sign-apk@2` teste la présence de `BITRISEIO_ANDROID_KEYSTORE_URL` ;
si le keystore n'est pas uploadé, la signature est sautée silencieusement et le
build se termine avec des artefacts unsigned.

### `versionCode` dynamique

`composeApp/build.gradle.kts` :

```kotlin
versionCode = System.getenv("BITRISE_BUILD_NUMBER")?.toIntOrNull() ?: 1
```

Bitrise injecte `BITRISE_BUILD_NUMBER` (compteur monotone par app) → chaque
upload Play obtient un `versionCode` strictement supérieur. En local sans cette
env, la valeur fallback `1` permet à `./gradlew build` de rester vert.

### Vérification après un run signé

`list_artifacts` (MCP Bitrise) sur le build slug retourne les `signed_by` ; pour
un keystore généré sans DN explicite (cas Colision), `signed_by` ressort
`CN=Unknown, OU=Unknown, …`. C'est cosmétique et **sans impact** pour Google
Play App Signing : Play s'appuie sur l'empreinte du certificat d'upload, pas
sur le DN.

## iOS — état actuel et plan de signature

### État actuel : validation only

Le workflow `ios_build` actuel **ne signe pas** — il compile + link sur
simulateur avec `CODE_SIGNING_ALLOWED=NO`. Objectif : valider la chaîne
KMP → Kotlin framework → Sentry sur runner CI sans dépendre d'une connexion
Apple, avant d'investir dans la mise en place du code signing.

### Prérequis repo (déjà en place)

1. **Scheme partagé** : `iosApp/iosApp.xcodeproj/xcshareddata/xcschemes/iosApp.xcscheme`.
   Avant ce setup, le scheme n'existait que dans `xcuserdata/`, donc invisible
   pour `xcodebuild` après un `git clone`. Si tu crées un nouveau scheme dans
   Xcode, **coche « Shared »** et commit le fichier dans `xcshareddata/`.
2. **Bundle id stable** dans `iosApp/Configuration/Config.xcconfig` :
   ```
   PRODUCT_BUNDLE_IDENTIFIER=com.anthooop.colision
   ```
   Aligné avec l'`applicationId` Android. Le template Compose Multiplatform met
   par défaut `…Colision$(TEAM_ID)` (id unique par dev pour free provisioning) —
   à virer absolument avant la signature App Store.
3. **TEAM_ID env injectée par CI** : `Config.xcconfig` laisse `TEAM_ID=` vide ;
   pour les builds signés, CI doit injecter `TEAM_ID=<10-char>` (via
   `xcconfig_content` du step `xcode-archive@6` ou un secret env Bitrise).

### Le contournement Sentry — pourquoi et comment

Le plugin Gradle `io.sentry.kotlin.multiplatform.gradle` configure le linker
Kotlin/Native pour qu'il trouve `Sentry.framework` au moment du link KMP. En
local, il lit le chemin depuis `DerivedData` de Xcode. **Sur CI c'est
circulaire** : `xcodebuild` doit appeler la script phase « Compile Kotlin
Framework », qui appelle Gradle, qui appelle le linker Sentry — qui a besoin
du framework Sentry que `xcodebuild` essaie de construire. Pas de DerivedData
pré-existante, donc pas de framework disponible.

**Workaround** (déjà implémenté dans `build-ios.yml` GHA et dans le workflow
`ios_build` Bitrise) :
1. Télécharger `Sentry.xcframework.zip` depuis la release sentry-cocoa pinnée
   (https://github.com/getsentry/sentry-cocoa/releases/download/`$VERSION`/Sentry.xcframework.zip).
2. Décompresser, identifier le **slice** correspondant à la cible :
   - Simulateur (`iphonesimulator`, arm64/x86_64) → `Sentry.xcframework/ios-arm64_x86_64-simulator`
   - Device (`iphoneos`, arm64/arm64e) → `Sentry.xcframework/ios-arm64_arm64e`
3. Exporter `SENTRY_FRAMEWORK_PATH=<slice-dir>` avant le build.
4. `composeApp/build.gradle.kts` lit cette env et l'utilise au lieu de
   `xcodeprojPath` :
   ```kotlin
   val ciFrameworkPath: String? = System.getenv("SENTRY_FRAMEWORK_PATH")
   if (!ciFrameworkPath.isNullOrBlank()) {
       frameworkPath = ciFrameworkPath
   } else {
       xcodeprojPath = file("../iosApp/iosApp.xcodeproj").absolutePath
   }
   ```

**Important** : la version de Sentry téléchargée DOIT correspondre à celle
pinnée dans `Package.resolved` du projet Xcode. La variable
`SENTRY_COCOA_VERSION` est centralisée dans chaque workflow CI — bumper en
lockstep avec Package.resolved sinon le link échoue sur un mismatch de
symboles.

### Signature App Store (en place)

`ios_build` produit un IPA App Store signé via `xcode-archive@6` + clef API
App Store Connect. Setup actuel :

1. **Apple Developer Program** : compte Individual, Team ID = `6J96GZQV2Q`.
2. **App ID** : `com.anthooop.colision` enregistré dans Apple Developer →
   Identifiers, aligné avec l'`applicationId` Android et avec
   `iosApp/Configuration/Config.xcconfig`.
3. **App Store Connect** : app `Colision` créée avec bundle id ci-dessus.
4. **App Store Connect API Key** : rôle Admin, uploadée à Bitrise →
   Workspace Settings → Apple Service Connection → liée à l'app Colision.
5. **Certificats** uploadés dans App settings → Code Signing & Files :
   - **Apple Distribution** `.p12` (généré dans Xcode → Manage Certificates,
     exporté via Keychain Access)
   - **Apple Development** `.p12` (même procédure — nécessaire car Xcode
     valide aussi le profile Dev pendant l'archive, cf. troubleshooting)
6. **Device** : ≥1 UDID iPhone enregistré dans Apple Developer → Devices,
   pour permettre la création du profile Development par l'API.
7. **`bitrise.yml`** : `xcode-archive@6` avec
   `automatic_code_signing: api-key`, `distribution_method: app-store`,
   `xcconfig_content` injectant `TEAM_ID` + `CURRENT_PROJECT_VERSION = $BITRISE_BUILD_NUMBER`.
   Slice Sentry = `ios-arm64_arm64e`.

Provisioning profiles : **gérés automatiquement** par la clef API à chaque
build (App Store + Development). Rien à uploader manuellement.

Artefacts produits par `ios_build` :
- `Colision.ipa` — distribution App Store
- `Colision.xcarchive.zip` — archive Xcode complète
- `Colision.dSYM.zip` — symboles de debug, à uploader à Sentry pour
  symboliser les crashes en prod
- `xcodebuild-archive.log`, `xcodebuild-export-archive.log`, `export_options.plist`

### Auto-upload TestFlight

Le step `deploy-to-itunesconnect-application-loader@2` enchaîne après
`xcode-archive` et uploade automatiquement le `.ipa` vers App Store Connect
(disponible aussitôt dans TestFlight → Builds). Il utilise la même connexion
Apple Service (`automatic`) déjà liée à l'app pour s'authentifier — aucun
secret supplémentaire à provisionner.

Implication : **chaque exécution réussie de `ios_build` (manuelle ou via tag
`v*`) crée un build TestFlight**. Si un build de test ne doit pas atterrir
là-bas, ne le déclenche pas via `ios_build`.

### Deployment target iOS

`IPHONEOS_DEPLOYMENT_TARGET = 16.0` dans `iosApp/iosApp.xcodeproj/project.pbxproj`
(Debug + Release). Couvre ~96% du parc iPhone en 2026. Si tu bumps un jour
pour utiliser des features iOS 17/18, **bump aussi le filtre côté store**
(Phased Release / âge minimum) sinon des utilisateurs reçoivent une mise à
jour qui ne s'installe pas.

## Branch strategy & triggers

### Gotcha à connaître

**`main` ne contient que le commit initial** au moment de la rédaction de ce doc.
Tout le travail réel vit sur des branches `feat/*`. Conséquences :
- Un build Bitrise lancé sur `main` échoue immédiatement (les flavors Android,
  par exemple, n'existent pas → `Cannot locate tasks that match ':composeApp:bundleProductionRelease'`).
- Les triggers auto GHA + Bitrise sur `push main` ne servent à rien tant que ce
  point n'est pas réglé (merger la branche de travail vers `main` ou ajuster
  les triggers).

Pour un build de release intentionnel, déclencher manuellement sur la branche
active (`feat/...`) via Bitrise.

### Triggers actuels

| Workflow | Push `main` | PR | Tag `v*` | Manuel |
|---|---|---|---|---|
| GHA `build-android` | ✅ | ✅ | — | ❌ |
| GHA `build-ios` | ✅ | ✅ | — | ❌ |
| GHA `lint` | ✅ | ✅ | — | ❌ |
| Bitrise `run_tests` | ✅ | ✅ | ❌ | ✅ |
| Bitrise `android_build` | ❌ | ❌ | ✅ | ✅ |
| Bitrise `ios_build` | ❌ | ❌ | ✅ | ✅ |

Pour déclencher un build signé Android + iOS : créer et pousser un tag SemVer.

```sh
git tag v0.1.0
git push origin v0.1.0
```

Le pattern `v*` inclut tout tag commençant par `v` — affiner à `v*.*.*` si on veut être strict SemVer.

## Troubleshooting

### `Cannot locate tasks that match ':composeApp:bundleProductionRelease'`
La branche buildée n'a pas les `productFlavors` dans `composeApp/build.gradle.kts`.
Vérifier que le build tourne sur la bonne branche (`feat/*` ou une branche
qui a mergé les flavors), pas sur `main` si `main` est encore au commit initial.

### `sign-apk` sauté, artefacts unsigned
Le step `sign-apk@2` a un `run_if` qui teste `BITRISEIO_ANDROID_KEYSTORE_URL`.
Si vide, le step ne tourne pas. Vérifier que le keystore est bien uploadé dans
*Code Signing & Files → Android Keystore* côté Bitrise app settings.

### iOS : `ld: framework 'Sentry' not found`
Le slice Sentry choisi ne correspond pas à la cible buildée (sim vs device),
ou `SENTRY_FRAMEWORK_PATH` n'a pas été exporté avant le step `xcodebuild`.
Vérifier l'ordre des steps dans `ios_build` et la valeur de `SENTRY_COCOA_VERSION`.

### iOS : `xcodebuild: error: Scheme 'iosApp' is not currently configured`
Le scheme partagé n'est pas dans `xcshareddata/xcschemes/`. Voir prérequis
repo section iOS.

### `versionCode` reste à 1 alors qu'on est en CI
Le `composeApp/build.gradle.kts` lit `BITRISE_BUILD_NUMBER`. Vérifier que cette
env est bien définie (auto par Bitrise) et que le code est bien mergé/poussé
sur la branche buildée.

### iOS `failed to prepare automatic code signing: code signing certificate URL is not specified`
La clef API App Store Connect **ne gère PAS les certificats**, uniquement les
provisioning profiles. Un `.p12` (Apple Distribution) doit être uploadé
manuellement à Bitrise → Code Signing & Files. Si la section iOS Certificates
est vide, c'est ça.

### iOS `Your team has no devices from which to generate a provisioning profile`
Xcode pendant `xcodebuild archive` (avec `-allowProvisioningUpdates`) essaie
de provisionner **les deux** profiles iOS (Distribution **et** Development)
pour le bundle id, même si on archive en Release. Apple refuse de créer un
profile Development sans device enregistré → enregistrer ≥1 UDID iPhone à
developer.apple.com/account/resources/devices/list. L'IPA final reste un
build App Store, accessible à tous via App Store/TestFlight (le profile Dev
n'est créé que pour satisfaire Xcode, il n'est embarqué dans rien).

### iOS `Your account already has an Apple Development signing certificate for this machine, but its private key is not installed`
Un cert Dev a été créé par l'API sur un build précédent, mais sa clé privée
est restée sur le runner Bitrise détruit. Apple voit le cert mais Bitrise ne
peut pas le réutiliser. Fix : révoquer le cert "Created via API" sur
developer.apple.com/account/resources/certificates/list, créer un nouveau
cert Apple Development *localement* via Xcode → Settings → Accounts →
Manage Certificates → + Apple Development, exporter en `.p12` via Keychain
Access, uploader à Bitrise → Code Signing & Files. À partir de là, tous les
builds réutilisent ce cert.

## Mises à jour de la config Bitrise

Trois moyens :
- **UI Bitrise** : Workflows → édition pas-à-pas (le plus pédagogique).
- **`bitrise.yml` direct** dans l'UI : pour des modifs structurelles.
- **MCP** (`update_bitrise_yml`) : pour des modifs depuis l'éditeur, après
  validation via `validate_bitrise_yml`. Le YAML complet est passé en string ;
  pas de patch incrémental.

Toujours **valider via `validate_bitrise_yml`** avant un `update_bitrise_yml` —
Bitrise n'a pas de history/rollback automatique sur les modifs YAML.

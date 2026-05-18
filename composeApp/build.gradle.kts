import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.sentryKmp)
}

// Secrets reader: local.properties (gitignored) overrides the in-repo
// defaults. CI loads secrets via env vars — the same key wins regardless of
// source. Public values (Supabase anon key, Sentry DSN — both meant to be
// client-visible) are kept as defaults so a fresh clone builds out of the
// box without any setup.
val localProperties: Properties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun secret(name: String, default: String = ""): String =
    System.getenv(name)
        ?: localProperties.getProperty(name)
        ?: default

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
            implementation(libs.ktor.client.okhttp)
            // sentry-kotlin-multiplatform is auto-installed by the
            // `io.sentry.kotlin.multiplatform.gradle` plugin (commonMain).
            // Product events also go through Sentry — no PostHog dep.
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // DI
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.composeViewModel)

            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.serialization.kotlinxJson)
            implementation(libs.ktor.client.logging)

            // Supabase (BOM aligns the versions of the modules below)
            implementation(project.dependencies.platform(libs.supabase.bom))
            implementation(libs.supabase.auth)
            implementation(libs.supabase.postgrest)
            implementation(libs.supabase.functions)

            // Room (commonMain)
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)

            // kotlinx
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)

            // Navigation Compose
            implementation(libs.androidx.navigation.compose)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.turbine)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}

android {
    namespace = "com.anthooop.colision"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.anthooop.colision"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    flavorDimensions += "environment"
    productFlavors {
        create("development") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
        }
        create("production") {
            dimension = "environment"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

// Sentry Kotlin Multiplatform configuration.
//
// The plugin auto-installs sentry-kotlin-multiplatform into commonMain
// (no manual dependency declaration needed) and configures the iOS
// framework link path to point at the Sentry Cocoa SDK resolved by SPM
// in the Xcode project's derived data.
//
// iOS setup (already done in iosApp/iosApp.xcodeproj):
// - Swift Package: https://github.com/getsentry/sentry-cocoa
// - Dependency Rule: **Exact Version 8.58.2** — DO NOT bump to 9.x.
//   The sentry-kotlin-multiplatform 0.26.0 plugin we use here is
//   compiled against Sentry Cocoa 8.x; 9.x changed internal symbols
//   (SentryDependencyContainer, SentryId Swift-port) and produces
//   "Undefined symbols" at the iOS link step. Unpin when the Kotlin
//   plugin ships a release that supports Sentry Cocoa 9.x — track
//   https://github.com/getsentry/sentry-kotlin-multiplatform/releases.
// - Product: `Sentry`, linked to the `iosApp` target.
// - Build Settings: Other Linker Flags contains `-ObjC`.
sentryKmp {
    autoInstall {
        enabled = true
        // CocoaPods is intentionally NOT used — JetBrains is moving away
        // from the cocoapods Gradle plugin, SPM is the long-term path.
        cocoapods { enabled = false }
        linker {
            xcodeprojPath = file("../iosApp/iosApp.xcodeproj").absolutePath
        }
    }
}

// Auto-align the BuildKonfig flavor with the Android product flavor being
// built. If any task name on the Gradle command line contains "Production",
// switch BuildKonfig to `prod` before its config block evaluates. This
// removes the need for CI / users to pass `-Pbuildkonfig.flavor=prod`
// manually for production builds.
run {
    val producingProd = gradle.startParameter.taskNames.any { task ->
        task.contains("Production", ignoreCase = false) || task.contains("production")
    }
    if (producingProd) {
        project.extensions.extraProperties.set("buildkonfig.flavor", "prod")
    }
}

// Generates `com.anthooop.colision.config.BuildKonfig` per BuildKonfig
// flavor (`dev` / `prod`). The active flavor is picked via the Gradle
// property `buildkonfig.flavor` — `dev` by default (set in
// gradle.properties), `prod` for release builds (passed by CI or auto-
// detected above when an Android `*Production*` task is in the graph).
//
// Secret resolution order for every field:
//   1. environment variable (CI)
//   2. local.properties (gitignored — for ops to override locally)
//   3. literal default below
// The Supabase URL + anon JWT and the Sentry DSN are intentionally public
// per docs/architecture.md (RLS enforces isolation; Sentry DSNs are
// client-visible by design), so committing them as defaults is fine and
// keeps `git clone && ./gradlew build` green.
buildkonfig {
    packageName = "com.anthooop.colision.config"

    val supabaseUrlDev = "https://uxmzeqlnrpydiiephfem.supabase.co"
    val supabaseAnonKeyDev = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
        "eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InV4bXplcWxucnB5ZGlpZXBoZmVtIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzkwMzg1NDgsImV4cCI6MjA5NDYxNDU0OH0." +
        "0XrWNO1qSMfVK3E9FflmtrACSTbLjRdRap-N26O-k_A"
    val sentryDsnShared = "https://6ea8ec9466dd998073c6d372d39885f4@o4511406962966528.ingest.de.sentry.io/4511406968406096"

    // Shared defaults — every flavor inherits these unless overridden.
    defaultConfigs {
        buildConfigField(STRING, "SUPABASE_URL", secret("SUPABASE_URL", default = supabaseUrlDev))
        buildConfigField(STRING, "SUPABASE_ANON_KEY", secret("SUPABASE_ANON_KEY", default = supabaseAnonKeyDev))
        buildConfigField(STRING, "SENTRY_DSN", secret("SENTRY_DSN", default = sentryDsnShared))
        buildConfigField(BOOLEAN, "IS_DEVELOPMENT_FLAVOR", "true")
    }

    defaultConfigs("dev") {
        buildConfigField(BOOLEAN, "IS_DEVELOPMENT_FLAVOR", "true")
    }

    defaultConfigs("prod") {
        // A dedicated production Supabase project is a follow-up — until
        // it lands prod builds point at the same dev project but tag
        // Sentry events with environment="production". Override these via
        // SUPABASE_URL_PROD / SUPABASE_ANON_KEY_PROD env vars when the
        // prod project is provisioned.
        buildConfigField(
            STRING,
            "SUPABASE_URL",
            secret("SUPABASE_URL_PROD", default = secret("SUPABASE_URL", default = supabaseUrlDev)),
        )
        buildConfigField(
            STRING,
            "SUPABASE_ANON_KEY",
            secret("SUPABASE_ANON_KEY_PROD", default = secret("SUPABASE_ANON_KEY", default = supabaseAnonKeyDev)),
        )
        buildConfigField(BOOLEAN, "IS_DEVELOPMENT_FLAVOR", "false")
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
    add("kspAndroid", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)

    // Firebase BOM aligns the versions of all Firebase libs declared below.
    add("implementation", platform(libs.firebase.bom))
    add("implementation", libs.firebase.messaging)
}

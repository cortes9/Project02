
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)

    // Enables @Serializable on Kotlin data classes.
    alias(libs.plugins.kotlin.serialization)
}

// Read existing local authentication settings.
val dotEnvLocalFile = rootProject.file(".env.local")
val dotEnvFile = rootProject.file(".env")
val envFile = rootProject.file("env.local")
val localPropertiesFile = rootProject.file("local.properties")

val envProperties = Properties()

when {
    dotEnvLocalFile.exists() -> envProperties.load(dotEnvLocalFile.inputStream())
    dotEnvFile.exists() -> envProperties.load(dotEnvFile.inputStream())
    envFile.exists() -> envProperties.load(envFile.inputStream())
    localPropertiesFile.exists() -> envProperties.load(localPropertiesFile.inputStream())
}

// Preserve the existing Google and Supabase configuration.
val supabaseUrl = envProperties.getProperty("SUPABASE_URL") ?: ""
val supabaseAnonKey = envProperties.getProperty("SUPABASE_ANON_KEY") ?: ""
val webClientId = envProperties.getProperty("WEB_CLIENT_ID")
    ?: envProperties.getProperty("GOOGLE_CLIENT_ID")
    ?: ""

// Spring Boot API URL is stored in one place.
val studySessionApiBaseUrl = envProperties.getProperty("STUDY_SESSION_API_BASE_URL")
    ?: "https://study-group-finder-api-bm09.onrender.com"

android {
    namespace = "com.example.studygroupfinder"

    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.studygroupfinder"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Existing login configuration.
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")
        buildConfigField("String", "WEB_CLIENT_ID", "\"$webClientId\"")

        // URL used by the new session API client.
        buildConfigField(
            "String",
            "STUDY_SESSION_API_BASE_URL",
            "\"$studySessionApiBaseUrl\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Existing Android and Compose libraries.
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Existing Supabase and Ktor libraries.
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.auth)
    implementation(libs.ktor.android)

    // Read JSON from the Spring Boot backend.
    implementation(libs.ktor.content.negotiation)
    implementation(libs.ktor.serialization.json)

    // Existing Google authentication libraries.
    implementation(libs.credentials)
    implementation(libs.credentials.play.services)
    implementation(libs.googleid)

    // Unit testing.
    testImplementation(libs.junit)

    // Simulates HTTP responses without contacting Render.
    testImplementation("io.ktor:ktor-client-mock:3.0.3")

    // Existing Android test libraries.
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

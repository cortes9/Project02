import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

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

val supabaseUrl = envProperties.getProperty("SUPABASE_URL") ?: ""
val supabaseAnonKey = envProperties.getProperty("SUPABASE_ANON_KEY") ?: ""
val webClientId = envProperties.getProperty("WEB_CLIENT_ID") ?: envProperties.getProperty("GOOGLE_CLIENT_ID") ?: ""

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

        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")
        buildConfigField("String", "WEB_CLIENT_ID", "\"$webClientId\"")
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
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Supabase & Ktor
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.auth)
    implementation(libs.ktor.android)

    // Credential Manager & Google ID
    implementation(libs.credentials)
    implementation(libs.credentials.play.services)
    implementation(libs.googleid)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

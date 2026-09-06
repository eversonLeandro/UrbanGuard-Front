import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.ksp)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

fun getPropertyOrEnv(key: String, defaultValue: String = ""): String {
    val envVal = System.getenv(key)
    if (!envVal.isNullOrBlank()) return envVal

    val propVal = localProperties.getProperty(key)
    if (!propVal.isNullOrBlank()) return propVal

    return defaultValue
}

android {
    namespace = "co.edu.unicauca.urbanguard"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "co.edu.unicauca.urbanguard"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val auth0Domain = getPropertyOrEnv("AUTH0_DOMAIN", "dev-5fpoxdyk0iffzgs3.us.auth0.com")
        val auth0ClientId = getPropertyOrEnv("AUTH0_CLIENT_ID", "F2geNd4CiKZTlsjjwWIz0JqqcX8FqnDo")
        val auth0Scheme = getPropertyOrEnv("AUTH0_SCHEME", "co.edu.unicauca.urbanguard")
        val auth0DbConnection = getPropertyOrEnv("AUTH0_DB_CONNECTION", "Username-Password-Authentication")

        manifestPlaceholders["auth0Domain"] = auth0Domain
        manifestPlaceholders["auth0Scheme"] = auth0Scheme

        buildConfigField("String", "AUTH0_DOMAIN", "\"$auth0Domain\"")
        buildConfigField("String", "AUTH0_CLIENT_ID", "\"$auth0ClientId\"")
        buildConfigField("String", "AUTH0_DB_CONNECTION", "\"$auth0DbConnection\"")
    }

    buildTypes {
        debug {
            val devBaseUrl = getPropertyOrEnv("BASE_URL_DEV", "https://api-dev.urbanguard.co/")
            buildConfigField("String", "BASE_URL", "\"$devBaseUrl\"")
        }
        release {
            val prodBaseUrl = getPropertyOrEnv("BASE_URL_PROD", "https://api.urbanguard.co/")
            buildConfigField("String", "BASE_URL", "\"$prodBaseUrl\"")
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true   // 🆕 necesario para poder usar BuildConfig.BASE_URL
    }
}

dependencies {
    implementation(libs.auth0.android)
    implementation(libs.auth0.jwtdecode)
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.compiler)
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
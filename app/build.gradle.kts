import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.ksp)
    id("com.google.gms.google-services")
}

val detektVersion = "1.23.6"

val detektCli: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

// El plugin Gradle de detekt se ejecuta dentro del daemon (JDK 25) y su compilador
// de Kotlin 1.9 embebido no soporta esa version de JVM, por eso se corre el CLI
// de forma aislada en un toolchain JDK 21.
val detektConfigFile = rootProject.file("reglas_detekt.yml")
val detektReportDir = rootProject.file("build/reports/detekt")
val detektTxtReport = rootProject.file("detekt-report.txt")
val detektHtmlReport = detektReportDir.resolve("detekt-report.html")
val detektXmlReport = detektReportDir.resolve("detekt-report.xml")
val detektMdReport = detektReportDir.resolve("detekt-report.md")
val detektSources = listOf("src/main/java", "src/test/java", "src/androidTest/java")

val detektArgs: List<String> = listOf(
    "--input", detektSources.joinToString(","),
    "--config", detektConfigFile.absolutePath,
    "--build-upon-default-config",
    "--parallel",
    "--jvm-target", "21",
    "--report", "txt:${detektTxtReport.absolutePath}",
    "--report", "html:${detektHtmlReport.absolutePath}",
    "--report", "xml:${detektXmlReport.absolutePath}",
    "--report", "md:${detektMdReport.absolutePath}",
)

tasks.register<JavaExec>("detekt") {
    group = "verification"
    description = "Ejecuta detekt sobre el modulo :app usando reglas_detekt.yml"

    mainClass.set("io.gitlab.arturbosch.detekt.cli.Main")
    classpath = detektCli
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    )

    args(detektArgs)

    inputs.file(detektConfigFile).withPathSensitivity(PathSensitivity.RELATIVE)
    detektSources.forEach { inputs.dir(it) }

    isIgnoreExitValue = false
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
        val auth0Audience = getPropertyOrEnv("AUTH0_AUDIENCE", "https://urbanguard.com")

        manifestPlaceholders["auth0Domain"] = auth0Domain
        manifestPlaceholders["auth0Scheme"] = auth0Scheme

        buildConfigField("String", "AUTH0_DOMAIN", "\"$auth0Domain\"")
        buildConfigField("String", "AUTH0_CLIENT_ID", "\"$auth0ClientId\"")
        buildConfigField("String", "AUTH0_DB_CONNECTION", "\"$auth0DbConnection\"")
        buildConfigField("String", "AUTH0_AUDIENCE", "\"$auth0Audience\"")
    }

    buildTypes {
        debug {
            val devBaseUrl = getPropertyOrEnv("BASE_URL_DEV", "http://10.0.2.2:8080/")
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
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
    buildFeatures {
        compose = true
        buildConfig = true   // 🆕 necesario para poder usar BuildConfig.BASE_URL
    }
}

dependencies {
    detektCli("io.gitlab.arturbosch.detekt:detekt-cli:$detektVersion")
    implementation(libs.auth0.android)
    implementation(libs.auth0.jwtdecode)
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.compiler)
    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.zxing.core)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // CameraX for QR scanning
    implementation("androidx.camera:camera-core:1.4.1")
    implementation("androidx.camera:camera-camera2:1.4.1")
    implementation("androidx.camera:camera-lifecycle:1.4.1")
    implementation("androidx.camera:camera-view:1.4.1")

    // ML Kit Barcode for QR decoding
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    //Dependencias de Firebase
    // El BOM controla las versiones compatibles de todas las librerías de Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))

    //Agrega los módulos específicos que vas a usar en UrbanGuard
    // (Ejemplo: Firebase Cloud Messaging para notificaciones push desde tu backend)
    implementation("com.google.firebase:firebase-messaging")
}

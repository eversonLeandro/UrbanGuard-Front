// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.dagger.hilt) apply false
    alias(libs.plugins.ksp) apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
}
detekt {
    config.setFrom("reglas_detekt.yml")
    buildUponDefaultConfig = true
    parallel = true
}

tasks.detekt {
    ignoreFailures = false
    reports {
        txt.required.set(true)
        txt.outputLocation.set(file("detekt-report.txt"))
    }
}
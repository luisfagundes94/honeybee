import dev.detekt.gradle.extensions.DetektExtension
import dev.detekt.gradle.extensions.FailOnSeverity

plugins {
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.google.gms.google.services) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.baselineprofile) apply false
    alias(libs.plugins.detekt) apply false
}

val detektAll = tasks.register("detektAll") {
    group = "verification"
    description = "Runs Detekt for every module."
    dependsOn(subprojects.map { "${it.path}:detekt" })
}

subprojects {
    plugins.withId("dev.detekt") {
        extensions.configure<DetektExtension> {
            config.setFrom(rootProject.file("config/detekt/detekt.yml"))
            ignoreFailures = false
            failOnSeverity = FailOnSeverity.Warning
        }
    }
}


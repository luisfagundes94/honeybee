import java.util.Properties
import org.gradle.api.DefaultTask
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class ValidateReleaseMonetizationConfig : DefaultTask() {
    @get:Input
    abstract val values: MapProperty<String, String>

    @TaskAction
    fun validate() {
        values.get().forEach { (name, value) ->
            require(!value.startsWith("MISSING_")) { "Missing required release configuration: $name" }
            require(!value.contains("ca-app-pub-3940256099942544")) {
                "$name must not use a Google test ad identifier"
            }
            if (name == "HONEYBEE_REVENUECAT_API_KEY") {
                require(!value.startsWith("test_")) {
                    "$name must use a production RevenueCat public SDK key"
                }
            }
        }
    }
}

val localMonetizationProperties = Properties().apply {
    val propertiesFile = rootProject.file("local.properties")
    if (propertiesFile.exists()) {
        propertiesFile.inputStream().use(::load)
    }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.baselineprofile)
    alias(libs.plugins.detekt)
    alias(libs.plugins.google.firebase.crashlytics)
}

android {
    namespace = "com.luisfagundes.honeybee"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.luisfagundes.honeybee"
        minSdk = 26
        targetSdk = 37
        versionCode = 10
        versionName = "0.9.61"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"
            buildConfigField("String", "ADMOB_SETTINGS_BANNER_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/9214589741\"")
            buildConfigField("String", "ADMOB_CLEANUP_INTERSTITIAL_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "REVENUECAT_API_KEY", "\"test_YmqIBYWSDBUDWuETPsPAKyTgCcg\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val admobAppId = releaseValue("HONEYBEE_ADMOB_APP_ID")
            manifestPlaceholders["admobAppId"] = admobAppId
            buildConfigField("String", "ADMOB_SETTINGS_BANNER_AD_UNIT_ID", quotedReleaseValue("HONEYBEE_SETTINGS_BANNER_AD_UNIT_ID"))
            buildConfigField("String", "ADMOB_CLEANUP_INTERSTITIAL_AD_UNIT_ID", quotedReleaseValue("HONEYBEE_CLEANUP_INTERSTITIAL_AD_UNIT_ID"))
            buildConfigField("String", "REVENUECAT_API_KEY", quotedReleaseValue("HONEYBEE_REVENUECAT_API_KEY"))
        }
        create("benchmark") {
            initWith(buildTypes.getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
            all {
                it.useJUnitPlatform()
            }
        }
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:ads"))

    implementation(project(":feature:library:api"))
    implementation(project(":feature:library:impl"))

    implementation(project(":feature:onboarding:api"))
    implementation(project(":feature:onboarding:impl"))

    implementation(project(":feature:albums:api"))
    implementation(project(":feature:albums:impl"))

    implementation(project(":feature:config:api"))
    implementation(project(":feature:config:impl"))
    implementation(project(":feature:premium:api"))
    implementation(project(":feature:premium:impl"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.messaging)
    implementation(libs.coil.compose)
    implementation(libs.coil.video)
    implementation(libs.revenuecat.purchases)

    // Hilt
    implementation(libs.hilt.android)
    "baselineProfile"(project(":baselineProfile"))
    ksp(libs.hilt.compiler)
    ksp(libs.kotlinx.metadata.jvm)

    // Navigation 3
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    debugImplementation(libs.androidx.compose.ui.tooling)

    // Testing
    testImplementation(project(":core:testing"))
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit5.jupiter.api)
    testRuntimeOnly(libs.junit5.jupiter.engine)
    testRuntimeOnly(libs.junit5.platform.launcher)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.junit)
}

fun Project.releaseValue(name: String): String {
    val value = (providers.gradleProperty(name)
        .orElse(providers.environmentVariable(name))
        .orNull
        ?: localMonetizationProperties.getProperty(name))
        .orEmpty()
        .trim()
    return value.ifBlank { "MISSING_$name" }
}

fun Project.quotedReleaseValue(name: String): String = "\"${releaseValue(name)}\""

val requiredReleaseMonetizationValues = listOf(
    "HONEYBEE_ADMOB_APP_ID",
    "HONEYBEE_SETTINGS_BANNER_AD_UNIT_ID",
    "HONEYBEE_CLEANUP_INTERSTITIAL_AD_UNIT_ID",
    "HONEYBEE_REVENUECAT_API_KEY",
)

val releaseMonetizationValues = mapOf(
    "HONEYBEE_ADMOB_APP_ID" to releaseValue("HONEYBEE_ADMOB_APP_ID"),
    "HONEYBEE_SETTINGS_BANNER_AD_UNIT_ID" to releaseValue("HONEYBEE_SETTINGS_BANNER_AD_UNIT_ID"),
    "HONEYBEE_CLEANUP_INTERSTITIAL_AD_UNIT_ID" to releaseValue("HONEYBEE_CLEANUP_INTERSTITIAL_AD_UNIT_ID"),
    "HONEYBEE_REVENUECAT_API_KEY" to releaseValue("HONEYBEE_REVENUECAT_API_KEY"),
)

val validateReleaseMonetizationConfig = tasks.register<ValidateReleaseMonetizationConfig>("validateReleaseMonetizationConfig") {
    values.putAll(releaseMonetizationValues)
}

tasks.matching { task ->
    task.name.contains("Release", ignoreCase = true) && task.name != "validateReleaseMonetizationConfig"
}.configureEach {
    dependsOn(validateReleaseMonetizationConfig)
}

composeCompiler {
    stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("compose-stability.pro"))
}

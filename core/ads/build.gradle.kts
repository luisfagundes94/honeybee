plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.detekt)
}

android {
    namespace = "com.luisfagundes.core.ads"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures { compose = true }
    testOptions {
        unitTests {
            all {
                it.useJUnitPlatform()
            }
        }
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.google.mobile.ads)
    implementation(libs.google.ump)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    ksp(libs.kotlinx.metadata.jvm)

    testImplementation(libs.junit5.jupiter.api)
    testRuntimeOnly(libs.junit5.jupiter.engine)
    testRuntimeOnly(libs.junit5.platform.launcher)
}

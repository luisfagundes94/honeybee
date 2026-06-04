plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.luisfagundes.core.testing"
    compileSdk = 37

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
        }
    }

}

dependencies {
    implementation(libs.kotlinx.coroutines.test)
    implementation(libs.junit5.jupiter.api)
    implementation(libs.junit5.jupiter.engine)
}
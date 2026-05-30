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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

}

dependencies {
    implementation(libs.kotlinx.coroutines.test)
    implementation(libs.junit5.jupiter.api)
    implementation(libs.junit5.jupiter.engine)
}
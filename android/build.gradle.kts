plugins {
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.kotlin.android)
}


android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        //TODO: change your application id
        applicationId = "com.kashif.android"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0-SNAPSHOT"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildTypes { getByName("release") { isMinifyEnabled = false } }
    namespace = "com.kashif.android"
    kotlinOptions {
        jvmTarget = "17"
    }
    dependencies {
        implementation(projects.common)
        implementation(libs.androidx.activity.compose)
        implementation(libs.koin.compose)
    }
}
dependencies {
    implementation(libs.androidx.coreKtx)
}

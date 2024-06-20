import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.jetbrains.compose)
    id("com.android.library")
//    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.native.cocoapods)
    alias(libs.plugins.plugin.serialization)
    alias(libs.plugins.compose.compiler)
}

//TODO: change your group

group = "com.kashif"
version = "1.0-SNAPSHOT"

fun composeDependency(groupWithArtifact: String) = "$groupWithArtifact:${libs.versions.compose.plugin}"

kotlin {
    applyDefaultHierarchyTemplate()
    androidTarget()
    jvm("desktop") {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions{
            jvmTarget.set(JvmTarget.fromTarget("17"))
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        ios.deploymentTarget = "14.1"
        podfile = project.file("../ios/Podfile")
        framework {
            baseName = "common"
            isStatic = true
        }
    }



    sourceSets {
        commonMain.get().dependencies {
            api(compose.runtime)
            api(compose.foundation)
            api(compose.material)
            api(compose.uiUtil)
            implementation(project.dependencies.platform(libs.compose.bom))
            implementation(project.dependencies.platform(libs.coroutines.bom))

            implementation(libs.koin.core)
            implementation(libs.ktor.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization)
            implementation(libs.ktor.contentnegotiation)
            implementation(libs.ktor.serialization.json)
            implementation(libs.kotlin.serialization)
            implementation(libs.material.icon.extended)
            api(libs.image.loader)
        }
        commonTest.get().dependencies {
            implementation(kotlin("test"))
        }
        androidMain.get().dependencies {
            api(libs.androidx.appcompat)
            api(libs.androidx.coreKtx)
            implementation(libs.ktor.android)
            implementation(libs.koin.compose)
            implementation(libs.androidx.media3.exoplayer)
            implementation(libs.androidx.media3.exoplayer.dash)
            implementation(libs.androidx.media3.ui)
        }
        val desktopMain by getting {
            dependencies {
                api(compose.preview)
                implementation(libs.koin.core)
                implementation(libs.ktor.java)
                implementation(libs.koin.compose)
                implementation(libs.vlcj)

            }
        }
        val desktopTest by getting
        iosMain.get().apply {
            dependencies {
                implementation(libs.ktor.ios)
            }
        }

    }
}


android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    lint {
        targetSdk = libs.versions.targetSdk.get().toInt()
    }
    namespace = "com.kashif.common"
    dependencies{
        testImplementation(libs.junit)
    }
}
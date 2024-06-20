import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
}

group = "com.kashif"
version = "1.0-SNAPSHOT"


kotlin {
    applyDefaultHierarchyTemplate()
    jvm {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget("17"))
        }
        withJava()
    }
    sourceSets {
        jvmMain.get().apply {
            dependencies {
                implementation(projects.common)
                implementation(compose.desktop.currentOs)
            }
            configurations.all {
                // some dependencies contains it, this causes an exception to initialize the Main dispatcher in desktop for image loader
                exclude("org.jetbrains.kotlinx", "kotlinx-coroutines-android")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.kashif.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ComposeVideoPlayer"
            packageVersion = "1.0.0"
        }
    }
}

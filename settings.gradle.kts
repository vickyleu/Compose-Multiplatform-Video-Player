@file:Suppress("UnstableApiUsage")

import org.gradle.internal.extensibility.DefaultExtraPropertiesExtension


rootProject.name = "ComposeVideoPlayer"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        mavenCentral()
    }
    listOf(repositories, dependencyResolutionManagement.repositories).forEach {
        it.apply {
            mavenCentral{
                content{
                    excludeGroupByRegex("com.vickyleu.*")
                    excludeGroupByRegex("com.android.tools.*")
                    excludeGroupByRegex("androidx.compose.*")
                    excludeGroupByRegex("com.github.(?!johnrengelman|oshi).*")
                }
            }
            gradlePluginPortal {
                content{
                    excludeGroupByRegex("com.vickyleu.*")
                    excludeGroupByRegex("org.jetbrains.compose.*")
                    excludeGroupByRegex("androidx.databinding.*")
                    // 避免无效请求,加快gradle 同步依赖的速度
                    excludeGroupByRegex("com.github.(?!johnrengelman).*")
                }
            }
            google {
                content {
                    includeGroupByRegex(".*google.*")
                    includeGroupByRegex(".*android.*")
                    excludeGroupByRegex("com.vickyleu.*")
                    excludeGroupByRegex("com.github.*")
                }
            }
            maven(url = "https://androidx.dev/storage/compose-compiler/repository") {
                content {
                    excludeGroupByRegex("org.jetbrains.compose.*")
                    excludeGroupByRegex("com.vickyleu.*")
                    excludeGroupByRegex("com.github.*")
                }
            }
            maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev") {
                content {
                    excludeGroupByRegex("com.vickyleu.*")
                    excludeGroupByRegex("com.github.*")
                }
            }
        }
    }
    resolutionStrategy {
        val properties = java.util.Properties()
        rootDir.resolve("gradle/libs.versions.toml").inputStream().use(properties::load)
        val kotlinVersion = properties.getProperty("kotlin").removeSurrounding("\"")
        eachPlugin {
            if(requested.id.id.startsWith("org.jetbrains.kotlin")){
                useVersion(kotlinVersion)
            }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}


dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenCentral{
            content {
                excludeGroupByRegex("com.vickyleu.*")
                excludeGroupByRegex("org.jetbrains.compose.*")
                excludeGroupByRegex("com.github.(?!johnrengelman|oshi|bumptech|mzule|pwittchen|filippudak|asyl|florent37).*")
            }
        }
        google {
            content {
                includeGroupByRegex(".*google.*")
                includeGroupByRegex(".*android.*")
                excludeGroupByRegex("org.jetbrains.compose.*")
                excludeGroupByRegex("com.vickyleu.*")
                excludeGroupByRegex("com.github.(?!johnrengelman|oshi|bumptech).*")
            }
        }

        // workaround for https://youtrack.jetbrains.com/issue/KT-51379
        maven { setUrl("https://repo.maven.apache.org/maven2")
            content {
                excludeGroupByRegex("org.jetbrains.compose.*")
                excludeGroupByRegex("com.vickyleu.*")
                excludeGroupByRegex("com.github.(?!johnrengelman|oshi|bumptech|mzule|pwittchen|filippudak|asyl|florent37).*")
            }
        }
        ivy {
            name = "Node.js"
            setUrl("https://nodejs.org/dist")
            patternLayout {
                artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]")
            }
            metadataSources {
                artifact()
            }
            content {
                includeModule("org.nodejs", "node")
            }
            isAllowInsecureProtocol = false
        }
        maven {
            setUrl("https://jitpack.io")
            content {
                includeGroupByRegex("com.github.*")
            }
        }

        maven {
            setUrl("https://repo1.maven.org/maven2/")
            content {
                includeGroupByRegex("org.jogamp.gluegen.*")
            }
        }
        maven {
            setUrl("https://maven.aliyun.com/repository/public/")
            content {
                includeGroupByRegex("com.aliyun.*")
                excludeGroupByRegex("com.vickyleu.*")
                excludeGroupByRegex("com.github.vickyleu.*")
                excludeGroupByRegex("io.github.vickyleu.*")
            }
        }
        maven {
            setUrl("https://maven.aliyun.com/nexus/content/repositories/jcenter/")
            content {
                excludeGroupByRegex("com.vickyleu.*")
                excludeGroupByRegex("com.github.(?!johnrengelman|oshi|bumptech|mzule|pwittchen|filippudak|asyl|florent37).*")
            }
        }
        maven { setUrl("https://maven.pkg.jetbrains.space/public/p/compose/dev")
            content {
                excludeGroupByRegex("com.vickyleu.*")
                excludeGroupByRegex("com.github.*")
                excludeGroupByRegex("io.github.*")
            }
        }
        maven { setUrl("https://dl.bintray.com/kotlin/kotlin-dev")
            content {
                excludeGroupByRegex("com.vickyleu.*")
                excludeGroupByRegex("com.github.*")
                excludeGroupByRegex("io.github.*")
            }
        }
        maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap")
            content {
                excludeGroupByRegex("com.vickyleu.*")
                excludeGroupByRegex("com.github.*")
                excludeGroupByRegex("io.github.*")
            }
        }
        maven {
            setUrl("https://jogamp.org/deployment/maven")
            content {
                includeGroupByRegex("dev.datlag.*")
            }
        }
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental"){
            content {
                excludeGroupByRegex("com.vickyleu.*")
                excludeGroupByRegex("com.github.*")
                excludeGroupByRegex("io.github.*")
            }
        }
    }
}

include(":common")
include(":desktop")
include(":android")

import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import java.util.Properties

plugins {
    alias(libs.plugins.jetbrains.compose)
    id("com.android.library")
//    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.native.cocoapods)
    alias(libs.plugins.plugin.serialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.dokka)
    id("maven-publish")
}

kotlin {
    targets.withType<KotlinNativeTarget> {
        val path = projectDir.resolve("src/nativeInterop/cinterop/CVPObserver")
        binaries.all {
            linkerOpts("-F $path")
            linkerOpts("-ObjC")
        }
        compilations.getByName("main") {
            cinterops.create("CVPObserver") {
                compilerOpts("-F $path")
            }
        }
    }
    applyDefaultHierarchyTemplate()
    androidTarget {
        publishLibraryVariants("release")
    }
    jvm("desktop") {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget("17"))
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
//      it.binaries
    }


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
    dependencies {
        testImplementation(libs.junit)
    }
}



buildscript {
    dependencies {
        val dokkaVersion = libs.versions.dokka.get()
        classpath("org.jetbrains.dokka:dokka-base:$dokkaVersion")
    }
}

//group = "io.github.ltttttttttttt"
////上传到mavenCentral命令: ./gradlew publishAllPublicationsToSonatypeRepository
////mavenCentral后台: https://s01.oss.sonatype.org/#stagingRepositories
//version = "${libs.versions.compose.plugin.get()}.beta1"

group = "com.vickyleu.compose-videoplayer"
version = "1.0.2"


tasks.withType<PublishToMavenRepository> {
    val isMac = DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX
    onlyIf {
        isMac.also {
            if (!isMac) logger.error(
                """
                    Publishing the library requires macOS to be able to generate iOS artifacts.
                    Run the task on a mac or use the project GitHub workflows for publication and release.
                """
            )
        }
    }
}

val javadocJar by tasks.registering(Jar::class) {
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.flatMap(DokkaTask::outputDirectory))
    archiveClassifier = "javadoc"
}


tasks.dokkaHtml {
    // outputDirectory = layout.buildDirectory.get().resolve("dokka")
    offlineMode = false
    moduleName = "compose-videoplayer"

    // See the buildscript block above and also
    // https://github.com/Kotlin/dokka/issues/2406
//    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
////        customAssets = listOf(file("../asset/logo-icon.svg"))
////        customStyleSheets = listOf(file("../asset/logo-styles.css"))
//        separateInheritedMembers = true
//    }

    dokkaSourceSets {
        configureEach {
            reportUndocumented = true
            noAndroidSdkLink = false
            noStdlibLink = false
            noJdkLink = false
            jdkVersion = 17
            // sourceLink {
            //     // Unix based directory relative path to the root of the project (where you execute gradle respectively).
            //     // localDirectory.set(file("src/main/kotlin"))
            //     // URL showing where the source code can be accessed through the web browser
            //     // remoteUrl = uri("https://github.com/mahozad/${project.name}/blob/main/${project.name}/src/main/kotlin").toURL()
            //     // Suffix which is used to append the line number to the URL. Use #L for GitHub
            //     remoteLineSuffix = "#L"
            // }
        }
    }
}

val properties = Properties().apply {
    runCatching { rootProject.file("local.properties") }
        .getOrNull()
        .takeIf { it?.exists() ?: false }
        ?.reader()
        ?.use(::load)
}
// For information about signing.* properties,
// see comments on signing { ... } block below
val environment: Map<String, String?> = System.getenv()
extra["githubToken"] = properties["github.token"] as? String
    ?: environment["GITHUB_TOKEN"] ?: ""

publishing {
    val gitRepoName = "Compose-Multiplatform-Video-Player"
    val projectName = "compose-videoplayer"//rootProject.name
    repositories {
        /*maven {
            name = "CustomLocal"
            url = uri("file://${layout.buildDirectory.get()}/local-repository")
        }
        maven {
            name = "MavenCentral"
            setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = extra["ossrhUsername"]?.toString()
                password = extra["ossrhPassword"]?.toString()
            }
        }*/
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/vickyleu/${gitRepoName}")
            credentials {
                username = "vickyleu"
                password = extra["githubToken"]?.toString()
            }
        }
    }

    afterEvaluate {
        publications.withType<MavenPublication> {
            artifactId = artifactId.replace(project.name, projectName.lowercase())
            artifact(javadocJar) // Required a workaround. See below
            pom {
                url = "https://github.com/vickyleu/${gitRepoName}"
                name = projectName
                description = """
                Visit the project on GitHub to learn more.
            """.trimIndent()
                inceptionYear = "2024"
                licenses {
                    license {
                        name = "Apache-2.0 License"
                        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
                developers {
                    developer {
                        id = "kashif"
                        name = "kashif"
                        email = ""
                        roles = listOf("Mobile Developer")
                        timezone = "GMT+8"
                    }
                }
                contributors {
                    // contributor {}
                }
                scm {
                    tag = "HEAD"
                    url = "https://github.com/vickyleu/${gitRepoName}"
                    connection = "scm:git:github.com/vickyleu/${gitRepoName}.git"
                    developerConnection = "scm:git:ssh://github.com/vickyleu/${gitRepoName}.git"
                }
                issueManagement {
                    system = "GitHub"
                    url = "https://github.com/vickyleu/${gitRepoName}/issues"
                }
                ciManagement {
                    system = "GitHub Actions"
                    url = "https://github.com/vickyleu/${gitRepoName}/actions"
                }
            }
        }
    }
}

// TODO: Remove after https://youtrack.jetbrains.com/issue/KT-46466 is fixed
//  Thanks to KSoup repository for this code snippet
tasks.withType(AbstractPublishToMaven::class).configureEach {
    dependsOn(tasks.withType(Sign::class))
}

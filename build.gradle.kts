import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.Properties
import java.util.concurrent.Executors
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

plugins {
    alias(libs.plugins.jetbrains.compose) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.native.cocoapods) apply false
    alias(libs.plugins.plugin.serialization) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.dokka)
}


tasks.register("deletePackages") {
    val rootProjectName = "compose-videoplayer"//rootDir.name.lowercase(Locale.getDefault())

    val mavenAuthor = "vickyleu"
    val mavenGroup = "com.$mavenAuthor.$rootProjectName"

    group = "publishing"
    description = "Delete all packages in the GitHub Packages registry"


    val keyword = "${mavenGroup}"
    println("keyword: $keyword")
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
    val myExtra = mutableMapOf<String, Any>()
    myExtra["githubToken"] = properties["github.token"] as? String
        ?: environment["GITHUB_TOKEN"] ?: ""
    val headers = mapOf(
        "Accept" to "application/vnd.github.v3+json",
        "Authorization" to "Bearer ${myExtra["githubToken"]}",
        "X-GitHub-Api-Version" to "2022-11-28"
    )
    doLast {
        runBlocking {
            val executor = Executors.newFixedThreadPool(10)
            val scope = CoroutineScope(executor.asCoroutineDispatcher())
            val fetchJobs = packageTypes.flatMap { packageType ->
                visibilityTypes.map { visibility ->
                    scope.async {
                        fetchPackages(packageType, visibility, headers)
                    }
                }
            }
            fetchJobs.awaitAll().forEach { packages ->
                allPackages.addAll(packages)
            }
            val deleteJobs = allPackages.filter { pkg ->
                val packageName = pkg["name"] as String
                packageName.contains(keyword)
            }.map { pkg ->
                val packageType = pkg["package_type"] as String
                val packageName = pkg["name"] as String
                scope.async {
                    deletePackage(packageType, packageName, headers)
                }
            }
            try {
                deleteJobs.awaitAll()
                executor.shutdown()
            } catch (e: Exception) {
                println("删除包失败: ${e.message}")
            }
        }
    }
}


val packageTypes = listOf("npm", "maven", "docker", "container")
val visibilityTypes = listOf("public", "private", "internal")
val allPackages = mutableListOf<Map<String, Any>>()

fun fetchPackages(packageType: String, visibility: String, headers: Map<String, String>): List<Map<String, Any>> {
    val packages = mutableListOf<Map<String, Any>>()
    var page = 1

    while (true) {
        val url =
            URL("https://api.github.com/user/packages?package_type=$packageType&visibility=$visibility&page=$page&per_page=100")
        val connection = url.openConnection() as HttpURLConnection

        headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }

        if (connection.responseCode == 200) {
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val batch: List<Map<String, Any>> = jacksonObjectMapper().readValue(response)
            if (batch.isEmpty()) break
            packages.addAll(batch)
            page++
        } else {
            println("获取$packageType ($visibility) 包列表失败，错误代码: ${connection.responseCode} ${connection.responseMessage}")
            println(connection.inputStream.bufferedReader().use { it.readText() })
            break
        }
    }

    return packages
}

fun deletePackage(packageType: String, packageName: String, headers: Map<String, String>) {
    val url = URL("https://api.github.com/user/packages/$packageType/$packageName")
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "DELETE"
    headers.forEach { (key, value) -> connection.setRequestProperty(key, value) }

    if (connection.responseCode == 204 || connection.responseCode == 200) {
        println("$packageName 删除成功")
    } else {
        println("$packageName 删除失败，错误代码: ${connection.responseCode}")
        println(connection.inputStream.bufferedReader().use { it.readText() })
    }
}

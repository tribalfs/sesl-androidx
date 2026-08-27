@file:Suppress("UNCHECKED_CAST")

import java.util.Properties

plugins {
    alias(libs.plugins.androidLibrary)
    id("org.jetbrains.dokka")
}

// Android block for root project required by dokka
android {
    compileSdk = 37
    namespace = "androidx"
}

fun String.toEnvVarStyle(): String =
    this.replace(Regex("([a-z])([A-Z])"), "$1_$2")
        .uppercase()

fun getGithubProperty(key: String): String {
    val githubProperties = Properties().apply {
        val file = rootProject.file("github.properties")
        if (file.exists()) {
            file.inputStream().use { load(it) }
        }
    }
    return githubProperties.getProperty(key)
        ?: rootProject.findProperty(key)?.toString()
        ?: System.getenv(key.toEnvVarStyle())
        ?: throw GradleException("GitHub $key not found")
}

val githubUsername = getGithubProperty("ghUsername")
val githubAccessToken = getGithubProperty("ghAccessToken")

// Expose to convention plugins
extra.apply {
    set("githubUsername", githubUsername)
    set("githubAccessToken", githubAccessToken)
}

apply(plugin = "sesl.android.common")

fun String.escaped(): String = replace(".", "\\.").replace("-", "--")

afterEvaluate {
    val readmeFile = file("README.md")
    if (!readmeFile.exists()) return@afterEvaluate
    
    var readmeContent = readmeFile.readText()
    val baseUrl = "https://img.shields.io/badge/sesl.androidx."
    var modified = false

    SeslManifest.moduleInfo.forEach { (moduleKey, artifactInfo) ->
        val stockVersion = artifactInfo[0] as String
        val seslVersion = artifactInfo[1] as String
        val revision = artifactInfo[2] as String
        
        val modulePath = moduleKey.removePrefix("androidx.")
        val badgeGroup: String
        val badgeName: String
        
        when {
            modulePath == "core" -> { badgeGroup = "core"; badgeName = "core" }
            modulePath == "core-ktx" -> { badgeGroup = "core"; badgeName = "core--ktx" }
            modulePath.startsWith("picker-") -> { badgeGroup = "picker"; badgeName = "picker--" + modulePath.removePrefix("picker-") }
            else -> { badgeGroup = modulePath; badgeName = modulePath }
        }
        
        val escapedGroupAndName = "$badgeGroup:$badgeName"
        val escapedVersion = "${stockVersion.escaped()}%2B${seslVersion.escaped()}%2B$revision"
        val badgeUrl = "$baseUrl$escapedGroupAndName-$escapedVersion-blue?logo=GitHub"
        
        // Use Kotlin Regex for easier matching
        val regex = Regex("${baseUrl.replace(".", "\\.")}${escapedGroupAndName.replace(".", "\\.")}-\\d+.*blue\\?logo=GitHub")
        
        val newContent = regex.replaceFirst(readmeContent, badgeUrl)
        if (newContent != readmeContent) {
            readmeContent = newContent
            modified = true
        }
    }
    
    if (modified) {
        readmeFile.writeText(readmeContent)
        println("Updated README version badges.")
    }
}

subprojects {
    plugins.withId("com.android.library") {
        if (!project.name.endsWith("-lint")) {
            apply(plugin = "sesl.android.library")
        }
    }

    configurations.all {
        resolutionStrategy {
            componentSelection {
                all {
                    if (candidate.version.matches(".*-sesl[67].*".toRegex())) {
                        reject("Rejecting sesl6 and sesl7 versions")
                    }
                }
            }
        }
    }
}

dependencies {
    add("dokka", project(":core"))
    add("dokka", project(":core-ktx"))
    add("dokka", project(":appcompat"))
    add("dokka", project(":customview"))
    add("dokka", project(":coordinatorlayout"))
    add("dokka", project(":drawerlayout"))
    add("dokka", project(":recyclerview"))
    add("dokka", project(":preference"))
    add("dokka", project(":fragment"))
    add("dokka", project(":viewpager2"))
    add("dokka", project(":swiperefreshlayout"))
    add("dokka", project(":viewpager"))
    add("dokka", project(":slidingpanelayout"))
    add("dokka", project(":indexscroll"))
    add("dokka", project(":picker-basic"))
    add("dokka", project(":picker-color"))
    add("dokka", project(":picker-app"))
    add("dokka", project(":apppickerview"))
}

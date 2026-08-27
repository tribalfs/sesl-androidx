import com.android.build.api.dsl.LibraryExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.regex.Pattern

plugins {
    id("com.android.library")
    id("maven-publish")
    id("org.jetbrains.dokka")
}

val moduleInfo = SeslManifest.moduleInfo
val pomInfo = SeslManifest.pomInfo

val githubUsername = project.rootProject.extra["githubUsername"] as String
val githubAccessToken = project.rootProject.extra["githubAccessToken"] as String

android {
    val versionInfoKey = "androidx." + project.projectDir.name
    val artifactInfo = moduleInfo[versionInfoKey]
        ?: throw GradleException("No version info found for module: $versionInfoKey")
    
    val stockVersion = artifactInfo[0] as String
    val seslVersion = artifactInfo[1] as String
    val revision = artifactInfo[2] as String
    val versionName = "${stockVersion}+${seslVersion}+${revision}"

    compileSdk = artifactInfo[4] as Int
    defaultConfig.minSdk = artifactInfo[3] as Int

    when (compileSdk) {
        35 -> buildToolsVersion = "35.0.1"
        36 -> buildToolsVersion = "36.0.0"
        37 -> buildToolsVersion = "37.0.0"
    }

    compileOptions {
        if (project.name in listOf("core", "recyclerview")) {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        } else {
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
        }
    }

    project.version = versionName

    lint { baseline = file("lint-baseline.xml") }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        if (project.name in listOf("core", "recyclerview")) {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }
}

dokka {
    dokkaPublications.html {
        suppressObviousFunctions.set(true)
        failOnWarning.set(false)
        suppressInheritedMembers.set(true)
        modulePath.set(project.name)
    }

    dokkaSourceSets.configureEach {
        if (name == "main") {
            sourceRoots.from(file("src"))
            displayName.set(name)

            sourceLink {
                localDirectory.set(projectDir.resolve("src"))
                val moduleDir = "${projectDir.parentFile.name}/${project.name}"
                remoteUrl("https://github.com/tribalfs/sesl-androidx/blob/sesl-androidx-main/${moduleDir}/src")
                remoteLineSuffix.set("#L")
            }

            externalDocumentationLinks {
                register("sesl.material") {
                    url("https://tribalfs.github.io/sesl-material-components-android/")
                    packageListUrl("https://tribalfs.github.io/sesl-material-components-android/-s-e-s-l%20-material/package-list")
                }
            }
        }
    }
}

// Register version file task
val writeVersionFile = tasks.register("writeVersionFile") {
    doLast {
        val extension = project.extensions.getByType<LibraryExtension>()
        val namespace = extension.namespace ?: throw GradleException("namespace not set for ${project.path}")
        val versionName = project.version.toString()
        val versionFileName = "${namespace}_${projectDir.name}.version"
        val versionFileDir = file("build/javaResources/META-INF")
        versionFileDir.mkdirs()
        val versionFile = File(versionFileDir, versionFileName)
        versionFile.writeText("${versionName}\n")
    }
}

// Hook into resource processing
tasks.matching { it.name.startsWith("process") && it.name.endsWith("JavaRes") }.configureEach {
    dependsOn(writeVersionFile)
}

publishing {
    publications {
        create<MavenPublication>("gpr") {
            val versionName = project.version.toString()
            
            group = "sesl.androidx." + projectDir.parentFile.name
            version = versionName
            from(components.findByName("release"))

            pom {
                name.set(project.name)
                description.set("SESL variant of $group:${project.name}")
                url.set(pomInfo["url"])
                inceptionYear.set(pomInfo["inceptionYear"])
                developers {
                    developer {
                        id.set(pomInfo["developerId"])
                        name.set(pomInfo["developName"])
                        url.set(pomInfo["developerUrl"])
                    }
                }
                licenses {
                    license {
                        name.set(pomInfo["licenceName"])
                        url.set(pomInfo["licenseUrl"])
                        distribution.set(pomInfo["licenceDist"])
                    }
                }
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/tribalfs/sesl-androidx")
            credentials {
                username = githubUsername
                password = githubAccessToken
            }
        }
    }
}

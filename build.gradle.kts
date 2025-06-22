import com.android.build.gradle.LibraryExtension
import java.util.Properties
import java.util.regex.Pattern
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.androidLibrary) apply true
    alias(libs.plugins.kotlinAndroid) apply true
    alias(libs.plugins.dokka) apply true
}


apply(from = File("manifest.gradle.kts"))

// Android block for root project
// required by dokka
android {
    compileSdk = 35
    namespace = "androidx"
}

/**
 * Converts a camelCase or mixedCase string to ENV_VAR_STYLE (uppercase with underscores).
 * Example: githubAccessToken -> GITHUB_ACCESS_TOKEN
 */
fun String.toEnvVarStyle(): String =
    this.replace(Regex("([a-z])([A-Z])"), "$1_$2")
        .uppercase()

/**
 * Note: To configure GitHub credentials, you have to generate an access token with at least
 * `read:packages` scope at https://github.com/settings/tokens/new and then
 * add it to any of the following:
 *
 * - Add `ghUsername` and `ghAccessToken` to Global Gradle Properties
 * - Set `GH_USERNAME` and `GH_ACCESS_TOKEN` in your environment variables or
 * - Create a `github.properties` file in your project folder with the following content:
 *      ghUsername=&lt;YOUR_GITHUB_USERNAME&gt;
 *      ghAccessToken=&lt;YOUR_GITHUB_ACCESS_TOKEN&gt;
 */
// Load GitHub credentials from properties file, gradle properties, or environment variables
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

allprojects {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/tribalfs/sesl-androidx")
            credentials {
                username = githubUsername
                password = githubAccessToken
            }
        }
        maven {
            url = uri("https://maven.pkg.github.com/tribalfs/sesl-material-components-android")
            credentials {
                username = githubUsername
                password = githubAccessToken
            }
        }
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://plugins.gradle.org/m2/") }
        mavenLocal()
    }

    tasks.withType<Javadoc>().configureEach {
        (options as? StandardJavadocDocletOptions)?.apply {
            addStringOption("Xdoclint:none", "-quiet")
            addStringOption("encoding", "UTF-8")
            addStringOption("charSet", "UTF-8")
        }
    }
}

subprojects {
    plugins.whenPluginAdded {
        val requiresDocs = javaClass.name == "com.android.build.gradle.LibraryPlugin" && !name.endsWith("-lint")

        if (requiresDocs) {
            plugins.apply("kotlin-android")
            plugins.apply("org.jetbrains.dokka")
        }
    }
}

subprojects {
    plugins.withId("org.jetbrains.dokka") {
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
                            packageListUrl("https://tribalfs.github.io/sesl-material-components-android/sesl.com.google.android.material/package-list")
                        }
                    }
                }
            }
        }
    }
}

val pomInfo by lazy { rootProject.extra["pomInfo"] as Map<String, String> }

subprojects {
    plugins.withType<KotlinBasePluginWrapper> {
        dependencies {
            constraints {
                //Remove when not anymore necessary
                implementation(libs.sesl.androidx.coreKtx) {
                    version { reject("1.16.0+1.0.15-sesl7+rev0") }
                }
            }
        }
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(
                JvmTarget.fromTarget("21")
            )
        }
    }

    plugins.whenPluginAdded {
        val isAndroidLibrary = "com.android.build.gradle.LibraryPlugin" == javaClass.name
        val isAndroidApp = "com.android.build.gradle.AppPlugin" == javaClass.name
        val isAndroidTest = "com.android.build.gradle.TestPlugin" == javaClass.name

        if (isAndroidLibrary || isAndroidApp || isAndroidTest) {
            project.android {
                val rootExtra = rootProject.extensions.extraProperties
                val versionInfoKey = "androidx." + project.projectDir.name
                val versionInfo = rootExtra.get("moduleInfo") as Map<String, List<Any>>
                val artifactInfo = versionInfo[versionInfoKey]
                if (artifactInfo == null) {
                    throw GradleException("No version info found for module: $versionInfoKey")
                }
                val stockVersion = artifactInfo[0] as String
                val seslVersion = artifactInfo[1] as String
                val revision = artifactInfo[2] as String
                val versionName = "${stockVersion}+${seslVersion}+${revision}"

                compileSdk = artifactInfo[4] as Int
                defaultConfig.minSdk = artifactInfo[3] as Int
                defaultConfig.targetSdk = compileSdk

                if (isAndroidLibrary) {
                    compileOptions {
                        sourceCompatibility = JavaVersion.VERSION_21
                        targetCompatibility = JavaVersion.VERSION_21
                    }

                    defaultConfig.versionName = versionName
                    println("set versionName=${defaultConfig.versionName}")

                    lint { baseline = file("lint-baseline.xml") }

                    publishing {
                        singleVariant("release") {
                            withSourcesJar()
                            withJavadocJar()
                        }
                    }

                    afterEvaluate {
                        tasks.register("writeVersionFile") {
                            val versionFileName = "${namespace}_${projectDir.name}.version"
                            val versionFileDir = file("build/javaResources/META-INF")
                            versionFileDir.mkdirs()
                            val versionFile = File(versionFileDir, versionFileName)
                            versionFile.writeText("${defaultConfig.versionName}\n")
                            println("writeVersionFile ${defaultConfig.versionName} >> $versionFileName")
                        }

                        extensions.findByType<LibraryExtension>()?.libraryVariants?.all {
                            processJavaResourcesProvider.get().dependsOn(tasks["writeVersionFile"])
                        }

                        tasks.register("updateVersionBadge") {
                            fun String.escaped(): String = replace(".", "\\.").replace("-", "--")
                            val readmeFile = file("${rootProject.projectDir}/README.md")
                            val readmeContent = readmeFile.readText()
                            val baseUrl = "https://img.shields.io/badge/sesl.androidx."
                            val escapedGroupAndName = "${projectDir.parentFile.name.escaped()}:${projectDir.name.escaped()}"
                            val escapedVersion = "${stockVersion.escaped()}%2B${seslVersion.escaped()}%2B$revision"
                            val badgeUrl = "$baseUrl$escapedGroupAndName-$escapedVersion-blue?logo=GitHub"
                            val pattern = Pattern.compile("${baseUrl.escaped()}$escapedGroupAndName-\\d+.*blue\\?logo=GitHub")
                            val updatedContent = pattern.matcher(readmeContent).replaceFirst(badgeUrl)
                            readmeFile.writeText(updatedContent)
                        }
                    }

                    afterEvaluate {
                        extensions.findByType(PublishingExtension::class.java)?.apply {
                            publications {
                                create("gpr", MavenPublication::class.java) {
                                    group = "sesl.androidx." + projectDir.parentFile.name
                                    version = versionName
                                    afterEvaluate { from(components.findByName("release")) }

                                    val modulePomInfo = project.extra["pomInfo"] as Map<String, String>

                                    pom {
                                        name.set(modulePomInfo["name"])
                                        description.set(modulePomInfo["description"])
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
                    }
                }
            }
        }
    }
}

dependencies {
    dokka(project(":core:"))
    dokka(project(":core-ktx:"))
    dokka(project(":appcompat:"))
    dokka(project(":customview:"))
    dokka(project(":coordinatorlayout:"))
    dokka(project(":drawerlayout:"))
    dokka(project(":appcompat:"))
    dokka(project(":recyclerview:"))
    dokka(project(":preference:"))
    dokka(project(":fragment:"))
    dokka(project(":viewpager2:"))
    dokka(project(":swiperefreshlayout:"))
    dokka(project(":viewpager:"))
    dokka(project(":slidingpanelayout:"))
    dokka(project(":indexscroll:"))
    dokka(project(":picker-basic:"))
    dokka(project(":picker-color:"))
    dokka(project(":apppickerview:"))
}

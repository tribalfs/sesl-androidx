plugins {
    alias(libs.plugins.androidLibrary)
}

android {
    namespace = "androidx.indexscroll"

    sourceSets {
        named("main") {
            resources.directories.add("build/javaResources")
        }
    }
}

dependencies {
    api(libs.androidx.annotation)
    api(libs.jspecify)

    api(libs.sesl.androidx.appcompat)
    api(libs.sesl.androidx.core)
    api(libs.sesl.androidx.customview)
    api(libs.sesl.androidx.recyclerview)
}

extra.set(
    "pomInfo", mapOf(
        "name" to "SESL IndexScroll",
        "description" to "SESL androidx.indexscroll Library.")
)

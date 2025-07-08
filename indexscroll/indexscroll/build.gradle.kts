plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.mavenPublish)
}

android {
    namespace = "androidx.indexscroll"

    sourceSets.named("main") {
        resources.srcDir("build/javaResources")
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

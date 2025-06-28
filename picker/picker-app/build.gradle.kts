plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.kotlinParcelize)
}

android {
    namespace = "androidx.picker"

    sourceSets.named("main") {
        resources.srcDirs("build/javaResources")
        res.srcDirs("src/main/res-public")
    }
}

dependencies {
    api(libs.androidx.annotation)
    implementation(libs.androidx.constraintLayout)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.shimmer)

    api(libs.sesl.androidx.appcompat)
    api(libs.sesl.androidx.core)
    api(libs.sesl.androidx.recyclerview)
}

extra.set(
    "pomInfo", mapOf(
        "name" to "SESL Picker App",
        "description" to "SESL androidx.picker:picker-app Library."
    )
)

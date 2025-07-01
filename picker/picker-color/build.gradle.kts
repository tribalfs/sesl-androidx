plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.mavenPublish)
}

android {
    namespace = "androidx.picker"

    sourceSets.named("main") {
        resources.srcDir("buildjavaResources")
        res.srcDirs("src/main/res-public")
    }
}

dependencies {
    implementation(libs.sesl.material)
    api(libs.sesl.androidx.appcompat)
    api(libs.sesl.androidx.core)
    api(libs.sesl.androidx.fragment)
    constraints {
        implementation(libs.sesl.androidx.coreKtx)
    }
}

extra.set(
    "pomInfo", mapOf(
        "name" to "SESL Color Picker",
        "description" to "SESL androidx.picker:picker-color Library.")
)
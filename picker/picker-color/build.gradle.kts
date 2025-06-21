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
        implementation(libs.sesl.androidx.coreKtx) {
            version {
                require("1.16.0+1.0.0-sesl7+rev0")
                reject("1.16.0+1.0.15-sesl7+rev0")
            }
        }
    }
}

extra.set(
    "pomInfo", mapOf(
        "name" to "SESL Color Picker",
        "description" to "SESL androidx.picker:picker-color Library.")
)
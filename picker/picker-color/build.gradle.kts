plugins {
    alias(libs.plugins.androidLibrary)
}

android {
    namespace = "androidx.picker"

    sourceSets {
        named("main") {
            resources.directories.add("buildjavaResources")
            res.directories.add("src/main/res-public")
        }
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

    api(libs.jspecify)
}

extra.set(
    "pomInfo", mapOf(
        "name" to "SESL Color Picker",
        "description" to "SESL androidx.picker:picker-color Library.")
)
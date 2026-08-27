plugins {
    alias(libs.plugins.androidLibrary)
}

android {
    namespace = "androidx.apppickerview"

    sourceSets {
        named("main") {
            resources.directories.add("build/javaResources")
        }
    }
}

dependencies {
    api(libs.androidx.annotation)

    api(libs.sesl.androidx.core)
    api(libs.sesl.androidx.coreKtx)
    api(libs.sesl.androidx.appcompat)
    api(libs.sesl.androidx.recyclerview)
}

extra.set(
    "pomInfo", mapOf(
        "name" to "SESL AppPickerView",
        "description" to "SESL androidx:apppickerview Library."
    )
)

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.mavenPublish)
}

android {
    namespace = "androidx.apppickerview"

    sourceSets.named("main") {
        resources.srcDir("build/javaResources")
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

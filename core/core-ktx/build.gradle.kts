plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.mavenPublish)
}

android {
    namespace = "androidx.core.ktx"
}

dependencies {
    api(libs.kotlinStdlib)
    api(libs.androidx.annotation)

    api(libs.sesl.androidx.core)
}

extra.set(
    "pomInfo", mapOf(
        "name" to "SESL Core Kotlin Extensions",
        "description" to "SESL variant of androidx.core:core-ktx module - Kotlin extensions for 'core' artifact"
    )
)
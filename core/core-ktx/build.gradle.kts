plugins {
    alias(libs.plugins.androidLibrary)
}

android {
    compileSdkMinor = 1
    namespace = "androidx.core.ktx"
}

dependencies {
    api(libs.kotlinStdlib)
    api(libs.androidx.annotation)

    api(libs.sesl.androidx.core){
        version { require("1.19.0+1.0.7-sesl8+rev1") }
    }
}

extra.set(
    "pomInfo", mapOf(
        "name" to "SESL Core Kotlin Extensions",
        "description" to "SESL variant of androidx.core:core-ktx module - empty compatibility artifact; Kotlin extensions are now part of the core artifact"
    )
)
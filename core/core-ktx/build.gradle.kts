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
        version { require("1.18.0+1.0.7-sesl8+rev0") }
    }
}

extra.set(
    "pomInfo", mapOf(
        "name" to "SESL Core Kotlin Extensions",
        "description" to "SESL variant of androidx.core:core-ktx module - Kotlin extensions for 'core' artifact"
    )
)
plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.mavenPublish)
}

android {
    namespace = "androidx.drawerlayout"

    defaultConfig.vectorDrawables. useSupportLibrary = true
}

dependencies {
    api(libs.androidx.annotation)
    api(libs.sesl.androidx.core)
    api(libs.sesl.androidx.customview)
}

extra.set(
    "pomInfo", mapOf(
        "name" to "SESL Drawer Layout",
        "description" to "SESL variant of androidx.drawerlayout:drawerlayout module. " +
            "The Support Library is a static library that you can add to your Android application " +
            "in order to use APIs that are either not available for older platform versions " +
            "or utility APIs that aren't a part of the framework APIs. Compatible on devices " +
            "running API 19 or later."
    )
)

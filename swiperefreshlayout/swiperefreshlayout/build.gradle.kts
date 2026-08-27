plugins {
    alias(libs.plugins.androidLibrary)
}


dependencies {
    api(libs.androidx.annotation)
    api(libs.androidx.interpolator)

    implementation(libs.sesl.androidx.core)
}

android {
    namespace = "androidx.swiperefreshlayout"
}

extra.set(
    "pomInfo", mapOf(
        "name" to "SESL Swipe Refresh Layout",
        "description" to "SESL variant of androidx.swiperefreshlayout:swiperefreshlayout module. " +
            "Compatible on devices running API 21 or later.")
)
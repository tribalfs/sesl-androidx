plugins {
    alias(libs.plugins.androidLibrary)
}


dependencies {
    api(libs.jspecify)
    api(libs.androidx.annotation)
    implementation(libs.androidx.collection)

    api(libs.sesl.androidx.core)
}


android {
    defaultConfig.vectorDrawables.useSupportLibrary = true

    namespace = "androidx.customview"
}

extra.set(
    "pomInfo", mapOf(
        "name" to "SESL Custom View",
        "description" to "SESL variant of android jetpack androidx.customview:customView module. " +
            "The Support Library is a static library that you can add to your Android application in order to use APIs that " +
            "are either not available for older platform versions or utility APIs that aren't a part of the framework APIs. " +
            "Compatible on devices running API 19 or later."
    )
)


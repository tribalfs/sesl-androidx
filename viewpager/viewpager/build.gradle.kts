plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.mavenPublish)
}

dependencies {
    api(libs.androidx.annotation)

    implementation(libs.sesl.androidx.core)
    api(libs.sesl.androidx.customview)
}

android {
    namespace = "androidx.viewpager"
}

extra.set(
    "pomInfo", mapOf(
        "name" to "SESL ViewPager",
        "description" to "SESL variant of androidx.viewpager:viewpager module. " +
            "The Support Library is a static library that you can add to your Android application " +
            "in order to use APIs that are either not available for  older platform versions or " +
            "utility APIs that aren't a part of the framework APIs. Compatible on devices running API 19 or later."
    )
)

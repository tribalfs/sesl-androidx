plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.kotlinAndroid)
}

dependencies {
    api(libs.androidx.annotation)
    api(libs.androidx.annotation.experimental)
    implementation(libs.androidx.collection)

    implementation(libs.sesl.androidx.core)
    api(libs.sesl.androidx.fragment)
    api(libs.sesl.androidx.recyclerview)
}

android {
    namespace = "androidx.viewpager2"
}

extra.set(
    "pomInfo", mapOf(
        "name" to "SESL ViewPager2",
        "description" to "SESL variant of androidx.viewpager2:viewpager2 module - AndroidX Widget ViewPager2"
    )
)


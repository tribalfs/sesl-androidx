plugins {
    alias(libs.plugins.androidLibrary)
}

dependencies {
    api(libs.androidx.annotation)
    api(libs.androidx.annotation.experimental)
    api(libs.androidx.lifecycle.runtime)
    api(libs.androidx.versionedparcelable)
    implementation(libs.androidx.core.viewtree)
    implementation(libs.androidx.collection)
    implementation(libs.androidx.concurrent.futures)
    implementation(libs.androidx.interpolator)
    api(libs.kotlinStdlib)
    implementation(libs.androidx.tracing)
    api(libs.jspecify)
}

android {
    buildFeatures {
        aidl = true
    }

    androidResources {
        noCompress += "ttf"
    }

    buildTypes.all {
        consumerProguardFiles("proguard-rules.pro")
    }

    defaultConfig.vectorDrawables.useSupportLibrary = true

    // AccessibilityNodeInfo.Selection / SelectionPosition became public in SDK 37.1
    @Suppress("UnstableApiUsage")
    compileSdkMinor = 1

    namespace = "androidx.core"

}

extra.set(
    "pomInfo", mapOf(
        "name" to "SESL Core",
        "description" to "SESL variant of androidx.core:core module. " +
            "Provides backward-compatible implementations of Android platform APIs and " +
            "features."
    )
)
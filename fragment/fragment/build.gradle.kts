plugins {
    alias(libs.plugins.androidLibrary)
    id("com.android.legacy-kapt")
}

android {
    buildTypes.configureEach {
        consumerProguardFiles("proguard-rules.pro")
    }

    namespace = "androidx.fragment"
}

dependencies {
    api(libs.androidx.annotation)
    api(libs.androidx.collection)
    api(libs.androidx.loader)
    api(libs.androidx.activity)
    api(libs.androidx.lifecycle.runtime)
    api(libs.androidx.lifecycle.livedata.core)
    api(libs.androidx.lifecycle.viewmodel)
    api(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.tracing)
    api(libs.androidx.savedstate)
    api(libs.androidx.annotation.experimental)
    api(libs.kotlinStdlib)

    api(libs.sesl.androidx.core)
    api(libs.sesl.androidx.viewpager)

    lintPublish(project(":fragment-lint"))

    constraints {
        implementation(libs.androidx.fragment.ktx) {
            version { require("1.8.8") }
        }
    }
}

extra.set(
    "pomInfo", mapOf(
        "name" to "SESL Fragment",
        "description" to "SESL variant of androidx.fragment:fragment module.  The Support Library is a static " +
            "library that you can add to your Android application in order to use APIs that are either not available " +
            "for older platform versions or utility APIs that aren't a part of the framework APIs." +
            " Compatible on devices running API 19 or later.")
)

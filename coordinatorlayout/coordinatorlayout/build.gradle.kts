plugins {
    alias(libs.plugins.androidLibrary)
}

android {
    sourceSets {
        named("main") {
            res.directories.addAll(listOf("src/main/res", "src/main/res-public"))
        }
    }

    buildTypes.configureEach {
        consumerProguardFiles("proguard-rules.pro")
    }

    defaultConfig.vectorDrawables.useSupportLibrary = true

    namespace = "androidx.coordinatorlayout"

}

dependencies {
    api(libs.jspecify)
    api(libs.androidx.annotation)
    implementation(libs.androidx.collection)

    api(libs.sesl.androidx.core)
    api(libs.sesl.androidx.customview)
}

extra.set(
    "pomInfo", mapOf(
        "name" to "SESL Coordinator Layout",
        "description" to "SESL variant of androidx.coordinatorlayout:coordinatorlayout module." +
            "The Support Library is a static library that you can add to your Android application " +
            "in order to use APIs that are either not available for older platform versions or " +
            "utility APIs that aren't a part of the framework APIs."
    )
)
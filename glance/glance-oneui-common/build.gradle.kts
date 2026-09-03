plugins {
    alias(libs.plugins.androidLibrary)
}

android {
    namespace = "androidx.glance.oneui.common"

    defaultConfig {
        vectorDrawables.useSupportLibrary = true
    }
}

dependencies {
    api(libs.androidx.annotation)
    api(libs.androidx.glance)
    api(libs.androidx.glance.appwidget)
    implementation(libs.kotlinStdlib)
    api(libs.sesl.androidx.core)
}

extra.set(
    "pomInfo", mapOf(
        "name" to "SESL Glance One UI Common",
        "description" to "SESL variant of androidx.glance:glance-oneui-common module. " +
            "Provides One UI specific extensions and utilities for Jetpack Glance."
    )
)

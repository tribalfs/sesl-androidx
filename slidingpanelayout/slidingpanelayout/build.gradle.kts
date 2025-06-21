plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinKapt)
}

dependencies {
    api(libs.androidx.annotation)
    implementation(libs.androidx.window)
    implementation(libs.androidx.transition)

    api(libs.sesl.androidx.appcompat)
    api(libs.sesl.androidx.customview)
    implementation(libs.sesl.androidx.coreKtx)
    implementation(libs.sesl.androidx.coordinatorlayout)
}

android {
    defaultConfig.vectorDrawables.useSupportLibrary = true

    namespace = "androidx.slidingpanelayout"
}

extra.set(
    "pomInfo", mapOf(
        "name" to "SESL Sliding Pane Layout",
        "description" to  "SESL variant of androidx.slidingpanepayout:slidingpanepayout module. " +
            "SlidingPaneLayout offers a responsive, two pane layout that automatically switches " +
            "between overlapping panes on smaller devices to a side by side view on larger devices.")
)
plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.kotlinAndroid)
}

dependencies {
    api(libs.androidx.annotation)

    implementation(libs.androidx.core.viewtree)
    implementation(libs.androidx.collection)
    implementation(libs.androidx.customview.poolingcontainer)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.compose.ui)

    api(libs.sesl.androidx.core)
    api(libs.sesl.androidx.customview)
    implementation(libs.sesl.androidx.appcompat)
}

android {
    sourceSets.named("main") {
        res.srcDirs("res", "res-public")
    }

    buildTypes.configureEach {
        consumerProguardFiles("proguard-rules.pro")
    }

    namespace = "androidx.recyclerview"

}

extra.set(
    "pomInfo", mapOf(
        "name" to "SESL Recyclerview",
        "description" to "SESL variant of androidx.recyclerview:recyclerview module- Android Support RecyclerView")
)

plugins {
    alias(libs.plugins.androidLibrary)
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
    sourceSets {
        named("main") {
            res.directories.addAll(listOf("res", "res-public"))
        }
    }

    namespace = "androidx.recyclerview"

}

extra.set(
    "pomInfo", mapOf(
        "name" to "SESL Recyclerview",
        "description" to "SESL variant of androidx.recyclerview:recyclerview module- Android Support RecyclerView")
)

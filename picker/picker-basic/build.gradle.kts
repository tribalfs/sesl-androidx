plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.mavenPublish)
}

android {
    namespace = "androidx.picker"

    sourceSets.named("main") {
        resources.srcDir("build/javaResources")
        res.srcDirs("src/main/res", "src/main/res-public")
    }
}

dependencies {
    api(libs.androidx.annotation)
    api(libs.androidx.dynamicanimation)
    implementation(libs.androidx.constraintLayout)
    api(libs.jspecify)

    api(libs.sesl.androidx.appcompat)
    api(libs.sesl.androidx.core)
    api(libs.sesl.androidx.viewpager)
}

extra.set(
    "pomInfo", mapOf(
        "name" to "SESL Picker Basic",
        "description" to "SESL androidx.picker:picker-basic Library.")
)

plugins {
    alias(libs.plugins.androidLibrary)
}

dependencies {
    api(libs.androidx.annotation)
    implementation(libs.androidx.collection.ktx)
    api(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx) {
        exclude(group = "androidx.fragment", module = "fragment")
    }

    api(libs.sesl.androidx.appcompat)
    api(libs.sesl.androidx.core)
    api(libs.sesl.androidx.fragment)
    api(libs.sesl.androidx.recyclerview)
}

android {
    sourceSets {
        named("main") {
            res.directories.clear()
            res.directories.addAll(listOf("res", "res-public"))
        }
    }

    defaultConfig.vectorDrawables.useSupportLibrary = true

    buildTypes. configureEach {
        consumerProguardFiles("proguard-rules.pro")
    }

    namespace = "androidx.preference"
}

extra.set(
    "pomInfo", mapOf(
        "name" to "SESL Preference",
        "description" to "SESL variant of androidx.preference:preference module.")
)
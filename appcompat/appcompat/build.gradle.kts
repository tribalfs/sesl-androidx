plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.mavenPublish)
}

dependencies {
    api(libs.sesl.androidx.core)
    api(libs.sesl.androidx.coreKtx)
    api(libs.sesl.androidx.fragment)
    api(libs.sesl.androidx.drawerlayout)

    api(libs.jspecify)
    api(libs.androidx.annotation)
    implementation(libs.kotlinStdlib)
    implementation(libs.androidx.emoji2)
    implementation(libs.androidx.emoji2.views.helper)
    implementation(libs.androidx.collection)
    api(libs.androidx.cursoradapter)
    api(libs.androidx.activity)
    api(libs.androidx.appcompat.resources)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.resourceinspection.annotation)
    api(libs.androidx.savedstate)

    lintPublish(project(":appcompat-lint"))
}

android {
    defaultConfig {
        // Disables the build tools' automatic vector -> PNG generation
        resourceConfigurations.clear()
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    sourceSets.named("main") {
        res.srcDirs("src/main/res", "src/main/res-public")
    }

    androidResources {
        additionalParameters.add("--no-version-vectors")
        noCompress += "ttf"
    }

    buildTypes.configureEach {
        consumerProguardFiles("proguard-rules.pro")
    }

    namespace = "androidx.appcompat"
}

extra.set(
    "pomInfo", mapOf(
        "name" to "SESL AppCompat",
        "description" to "SESL variant of androidx.appcompat:appcompat module." +
            " - Provides backwards-compatible implementations of UI-related Android SDK " +
            "functionality, including dark mode and Material theming."
    )
)
/**
 * Global project properties and versions metadata for SESL AndroidX modules.
 */

extra.apply {
    set(
        "moduleInfo", mapOf(
            // [<base androidx version or sesl if purely sesl>, <sesl version>, <internal version>, <minsdk>, <targetSdk>]
            "androidx.core" to listOf("1.16.0", "1.0.16-sesl7", "rev0", 21, 35),
            "androidx.core-ktx" to listOf("1.16.0", "1.0.0-sesl7", "rev0", 21, 35),
            "androidx.customview" to listOf("1.2.0-rc01", "1.0.0-sesl7", "rev0", 21, 35),
            "androidx.drawerlayout" to listOf("1.2.0", "1.0.0-sesl7", "rev0", 21, 35),
            "androidx.viewpager" to listOf("1.1.0-beta01", "1.0.1-sesl7", "rev0", 21, 35),
            "androidx.coordinatorlayout" to listOf("1.3.0", "1.0.0-sesl7", "rev0", 21, 35),
            "androidx.appcompat" to listOf("1.7.1", "1.0.47000-sesl7", "rev0", 21, 35),
            "androidx.fragment" to listOf("1.8.8", "1.0.9-sesl7", "rev0", 21, 35),
            "androidx.recyclerview" to listOf("1.4.0", "1.0.33-sesl7", "rev3", 21, 35),
            "androidx.preference" to listOf("1.2.1", "1.0.12-sesl7", "rev0", 21, 35),
            "androidx.slidingpanelayout" to listOf("1.2.0", "1.0.5-sesl7", "rev1", 21, 35),
            "androidx.viewpager2" to listOf("1.1.0", "1.0.4-sesl7", "rev0", 21, 35),
            "androidx.swiperefreshlayout" to listOf("1.2.0-alpha01", "1.0.1-sesl7", "rev0", 21, 35),
            "androidx.indexscroll" to listOf("1.0.6", "1.0.6-sesl7", "rev3", 21, 35),
            "androidx.picker-basic" to listOf("1.0.16", "1.0.16-sesl7", "rev0", 21, 35),
            "androidx.picker-color" to listOf("1.0.6", "1.0.6-sesl7", "rev0", 21, 35),
            "androidx.picker-app" to listOf("1.0.21", "1.0.21-sesl7", "rev0", 21, 35),
            "androidx.apppickerview" to listOf("1.0.1", "1.0.1-sesl7", "rev1", 21, 35),
        )
    )

    set(
        "pomInfo", mapOf(
            "inceptionYear" to "2024",
            "packaging" to "aar",
            "url" to "https://github.com/tribalfs/sesl-androidx",
            "scmUrl" to "https://github.com/tribalfs/sesl-androidx",
            "scmConnection" to "scm:git@github.com:tribalfs/sesl-androidx.git",
            "devConnection" to "scm:git@github.com:tribalfs/sesl-androidx.git",
            "licenceName" to "Apache-2.0 License",
            "licenseUrl" to "https://github.com/tribalfs/sesl-androidx/blob/sesl-androidx-main/LICENSE.txt",
            "licenceDist" to "repo",
            "developerId" to "tribalfs",
            "developName" to "Tribalfs",
            "developerUrl" to "https://github.com/tribalfs",
        )
    )

}

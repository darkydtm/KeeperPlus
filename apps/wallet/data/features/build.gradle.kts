plugins {
    id("target.android.library")
}

android {
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    api(projects.lib.features)
    implementation(projects.lib.log)
    implementation(projects.lib.features)
}

plugins {
    id("target.android.library")
    id("kotlin-parcelize")
}

dependencies {
    implementation(libs.androidx.biometric)
    implementation(libs.ton.crypto)
    implementation(libs.koin.core)

    implementation(projects.lib.sqlite)
    implementation(projects.lib.security)
    implementation(projects.lib.extensions)
}



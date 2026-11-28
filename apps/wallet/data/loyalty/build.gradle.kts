plugins {
    id("target.android.library")
}

dependencies {
	testImplementation(libs.junit)
	testImplementation(libs.mockk)
    implementation(libs.koin.core)
    implementation(libs.kotlinx.coroutines.core)

    implementation(projects.apps.wallet.api)
    implementation(projects.apps.wallet.data.core)
    implementation(projects.apps.wallet.data.features)
    implementation(projects.apps.wallet.data.settings)

    implementation(projects.lib.log)
}

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import java.util.Properties

plugins {
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.android.baselineprofile) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.multiplatform.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}

//buildscript {
//    buildscript.configurations.configureEach {
//        resolutionStrategy.activateDependencyLocking()
//    }
//
//    dependencyLocking {
//        lockAllConfigurations()
////        lockMode.set(LockMode.STRICT)
//    }
//}

val bcprovVersion = libs.versions.bcprov.get()

allprojects {
    configurations.all {
        exclude(group = "org.bouncycastle", module = "bcprov-jdk15on")
        exclude(group = "org.bouncycastle", module = "bcprov-jdk15to18")
        resolutionStrategy {
            force("org.bouncycastle:bcprov-jdk18on:$bcprovVersion")
            force("org.bouncycastle:bcpkix-jdk18on:$bcprovVersion")
            force("org.bouncycastle:bcutil-jdk18on:$bcprovVersion")
        }
    }

    tasks.register("resolveAllDependencies") {
        notCompatibleWithConfigurationCache("Resolves configurations at execution time")
        doLast {
            configurations
                .filter { it.isCanBeResolved }
                .forEach {
                    runCatching { it.resolve() }
                        .onFailure { e -> logger.warn("Skip ${it.name}: ${e.message}") }
                }
        }
    }

    // `--quiet` otherwise reports only "There were failing tests", without the test or the assertion.
    tasks.withType<Test>().configureEach {
        testLogging.quiet {
            events(TestLogEvent.FAILED)
            exceptionFormat = TestExceptionFormat.FULL
        }
    }
}


allprojects {
    pluginManager.withPlugin("android") {
        configure<ApplicationExtension> {
            val localProperties = Properties().apply {
                rootProject.file("local.properties")
                    .takeIf { it.exists() }
                    ?.inputStream()
                    ?.use { load(it) }
            }

            fun findProperty(name: String): String? =
                localProperties[name]?.toString() ?: project.findProperty(name)?.toString()

            signingConfigs {
                create("release") {
                    val environment = listOf("KEYSTORE", "STORE_PASSWORD", "KEY_ALIAS", "KEY_PASSWORD")
                        .map { System.getenv("ANDROID_RELEASE_$it") }
                    if (environment.any { it != null }) {
                        require(environment.all { !it.isNullOrBlank() }) { "Incomplete release signing configuration" }
                        storeFile = file(environment[0]!!)
                        storePassword = environment[1]
                        keyAlias = environment[2]
                        keyPassword = environment[3]
                    } else {
                        findProperty("android.injected.signing.store.file")?.let { storeFile = file(it) }
                        storePassword = findProperty("android.injected.signing.store.password")
                        keyAlias = findProperty("android.injected.signing.key.alias")
                        keyPassword = findProperty("android.injected.signing.key.password")
                    }
                }

                getByName("debug") {
                    val environment = listOf("KEYSTORE", "STORE_PASSWORD", "KEY_ALIAS", "KEY_PASSWORD")
                        .map { System.getenv("ANDROID_DEBUG_$it") }
                    if (environment.any { it != null }) {
                        require(environment.all { !it.isNullOrBlank() }) { "Incomplete debug signing configuration" }
                        storeFile = file(environment[0]!!)
                        storePassword = environment[1]
                        keyAlias = environment[2]
                        keyPassword = environment[3]
                    } else {
                        val debugFilePath = findProperty("android.injected.signing.debug.file")
                        if (debugFilePath != null) {
                            storeFile = file(debugFilePath)
                            storePassword = findProperty("android.injected.signing.debug.password")
                            keyAlias = findProperty("android.injected.signing.debug.key.alias")
                            keyPassword = findProperty("android.injected.signing.debug.key.password")
                        }
                    }
                }
            }
        }
    }
}

tasks.register<Delete>("clean") {
    delete(getLayout().buildDirectory)
}

// Point git at the repository's shared hooks; the public mirror has no .githooks/.
if (rootDir.resolve("../.githooks").isDirectory) {
    providers.exec { commandLine("git", "config", "core.hooksPath", ".githooks") }.result.get()
}

extensions.findByName("buildScan")?.withGroovyBuilder {
    setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
    setProperty("termsOfServiceAgree", "yes")
}

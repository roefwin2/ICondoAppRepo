import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    kotlin("plugin.serialization")
}

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_1_8)
                }
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    cocoapods {
        summary = "ICondo Shared Module - Compose Multiplatform"
        homepage = "https://github.com/idsolution/icondo"
        version = "2.0"
        ios.deploymentTarget = "16.0"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "shared"
            isStatic = true
            export("androidx.datastore:datastore-preferences-core:1.1.2")
        }
        // Note: Linphone SDK for iOS is handled directly in the iosApp Podfile
        // The Kotlin iOS VoIP implementation (IOSVoipService) uses NativeVoipHandler
        // interface which is implemented in Swift using the Linphone SDK
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)
            implementation(libs.timber)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.linphone.sdk.android)

            // ExoPlayer with RTSP support (primary player for RTSP streams)
            implementation("androidx.media3:media3-exoplayer:1.8.0")
            implementation("androidx.media3:media3-exoplayer-rtsp:1.8.0")
            implementation("androidx.media3:media3-ui:1.8.0")
            implementation("androidx.media3:media3-common:1.8.0")

            // Note: VLC removed due to 16KB page size incompatibility on Android 15+
            // ExoPlayer handles RTSP streams natively

            // Media for audio focus
            implementation("androidx.media:media:1.7.0")
        }

        commonMain.dependencies {
            implementation(project.dependencies.platform(libs.androidx.compose.bom))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.materialIconsExtended)
            implementation(libs.koin.compose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.koin.compose.viewmodel)
            api(libs.koin.core)
            implementation(libs.bundles.ktor)
            implementation(libs.lifecycle.viewmodel)
            implementation(libs.jetbrains.compose.navigation)
            api(libs.datastore.preferences)
            api(libs.datastore)
            implementation(libs.kotlinx.serialization.json.v160)
            implementation(libs.calf.permissions)
            implementation(libs.socket.io)
            implementation(libs.uuid)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

android {
    namespace = "com.idsolution.icondoapp"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    packaging {
        jniLibs.pickFirsts.addAll(
            listOf(
                "lib/x86/libc++_shared.so",
                "lib/x86_64/libc++_shared.so",
                "lib/armeabi-v7a/libc++_shared.so",
                "lib/arm64-v8a/libc++_shared.so"
            )
        )
    }
}

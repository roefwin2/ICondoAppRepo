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
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        ios.deploymentTarget = "16.0"
        podfile = project.file("../iosApp/Podfile")
        framework {
            baseName = "shared"
            isStatic = true
            export("androidx.datastore:datastore-preferences-core:1.1.2")
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)
            //timber
            implementation(libs.timber)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.linphone.sdk.android)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
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
            //implementation(libs.webrtc)
            implementation(libs.calf.permissions)
            implementation(libs.webrtc.kmp)
            implementation(libs.socket.io)
            implementation(libs.uuid)

            commonTest.dependencies {
                implementation(libs.kotlin.test)
            }
            iosMain.dependencies {
                implementation(libs.ktor.client.darwin)
                // Vous pouvez ajouter ici des dépendances spécifiques si nécessaires pour Linphone
            }
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
dependencies {
    implementation(project(":voip"))
    // Latest version is 5.0.x, using + to get the latest available
    implementation(libs.linphone.sdk.android)
    implementation(libs.firebase.messaging.ktx)
}

import java.net.URI

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven {
            url = URI.create("https://linphone.org/maven_repository")
        }
    }
}

rootProject.name = "ICondo"
include(":androidApp")
include(":shared")
include(":voip")

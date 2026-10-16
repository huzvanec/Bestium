rootProject.name = "Bestium"

include("core", "api", "hooks:bettermodel")

dependencyResolutionManagement {
    repositories {
        maven("https://repo.xenondevs.xyz/releases/")
    }
    versionCatalogs {
        create("origamiLibs") {
            from("xyz.xenondevs.origami:origami-catalog:0.6.1")
        }
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.xenondevs.xyz/releases/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

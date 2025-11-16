rootProject.name = "Bestium"

include("core", "api")

dependencyResolutionManagement {
    repositories {
        maven("https://repo.xenondevs.xyz/releases/")
    }
    versionCatalogs {
        create("origamiLibs") {
            from("xyz.xenondevs.origami:origami-catalog:0.3.1")
        }
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.xenondevs.xyz/releases/")
    }
}
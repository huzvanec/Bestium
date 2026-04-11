rootProject.name = "Bestium"

include("core", "api", "hooks:bettermodel")

dependencyResolutionManagement {
    repositories {
        maven("https://repo.xenondevs.xyz/releases/")
    }
    versionCatalogs {
        create("origamiLibs") {
            from("xyz.xenondevs.origami:origami-catalog:0.3.3")
        }
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.xenondevs.xyz/releases/")
    }
}
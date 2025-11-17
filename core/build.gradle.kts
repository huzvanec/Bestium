plugins {
    id("kotlin-conventions")
    id("plugin-conventions")
    alias(origamiLibs.plugins.origami)
}

dependencies {
    implementation(project(":api"))
    compileOnly(origamiLibs.mixin)
    compileOnly(origamiLibs.mixinextras)
}

origami {
    paperDevBundle(libs.versions.paper.get())
    pluginId = rootProject.name.lowercase()
}

// Include java mixin sources
sourceSets {
    main {
        java {
            setSrcDirs(listOf("src/main/kotlin"))
        }
    }
}

tasks {
    processResources {
        val props = mapOf(
            "version" to project.version,
            "minecraftVersion" to paperToMinecraftVersion(libs.versions.paper.get()),
        )
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }
}
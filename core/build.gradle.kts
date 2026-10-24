plugins {
    id("kotlin-conventions")
    alias(origamiLibs.plugins.origami)
}

dependencies {
    implementation(project(":api"))
    implementation(project(":hooks:bettermodel"))
    compileOnly(origamiLibs.mixin)
    compileOnly(origamiLibs.mixinextras)
}

origami {
    paperDevBundle("${libs.versions.minecraft.get()}.build.+")
}

tasks {
    processResources {
        val props = mapOf(
            "version" to project.version,
            "minecraftVersion" to libs.versions.minecraft.get(),
            "bettermodelVersion" to libs.versions.bettermodel.get(),
        )
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching(listOf("paper-plugin.yml", "versions.properties")) {
            expand(props)
        }
    }
}
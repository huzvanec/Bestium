val minecraftVersion = project.properties["minecraftVersion"] as String

dependencies {
    implementation(project(":api"))
    paperweight.paperDevBundle("$minecraftVersion-R0.1-SNAPSHOT")
}

tasks {
    processResources {
        val props = mapOf(
            "version" to version,
            "minecraftVersion" to minecraftVersion,
        )
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }
}
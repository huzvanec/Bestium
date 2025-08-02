plugins {
    id("kotlin-conventions")
    id("plugin-conventions")
}

dependencies {
    implementation(project(":api"))
    implementation(libs.bytebase)
    implementation(libs.bytebase.runtime)
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
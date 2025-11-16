plugins {
    id("java-conventions")
    alias(libs.plugins.shadow)
    alias(libs.plugins.run.paper)
    alias(origamiLibs.plugins.origami)
}

origami {
    paperDevBundle(libs.versions.paper.get())
    pluginId = rootProject.name.lowercase()
    targetConfigurations = emptySet()
}

allprojects {
    group = "cz.jeme"
    version = "3.2.0"
}

dependencies {
    implementation(project(":api"))
    implementation(project(":core", configuration = "default"))
}

runPaper {
    disablePluginJarDetection()
}

tasks {
    origamiJar {
        destinationDirectory = layout.buildDirectory.dir("tmp/origamiJar")
    }

    shadowJar {
        with(getByName<Jar>("origamiJar"))
        manifest.from(getByName<Jar>("origamiJar").manifest)

        archiveClassifier = ""

        dependencies {
            exclude(dependency("org.jetbrains:annotations:.*"))
        }

        // fun shade(pattern: String) = relocate(pattern, "${project.group}.${project.name.lowercase()}.shaded.$pattern")
    }

    assemble {
        dependsOn(shadowJar)
    }

    val jarName = "${rootProject.name}-${project.version}.jar"

    register<Copy>("copyPlugin") {
        dependsOn(shadowJar)
        from("build/libs/$jarName")
        into("run/plugins/")
    }

    runServer {
        dependsOn("copyPlugin")
        downloadPlugins {
            modrinth("bettermodel", libs.versions.bettermodel.get())
//            modrinth("nova-framework", "0.21.0-alpha.5")
        }
        jvmArgs("-javaagent:plugins/$jarName")
        minecraftVersion(paperToMinecraftVersion(libs.versions.paper.get()))
    }

    register("printVersion") {
        doLast { print(version) }
    }
}

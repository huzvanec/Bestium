plugins {
    id("java-conventions")
    alias(libs.plugins.shadow)
    alias(libs.plugins.run.paper)
}

allprojects {
    group = "cz.jeme"
    version = "3.3.1"
}

dependencies {
    implementation(project(":api"))
    implementation(project(":core", configuration = "default"))
}

runPaper {
    disablePluginJarDetection()
}

tasks {
    shadowJar {
        val origamiJar = project(":core").tasks.getByName<Jar>("origamiJar")
        with(origamiJar)
        manifest.from(origamiJar.manifest)

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
        }
        jvmArgs("-javaagent:plugins/$jarName")
        minecraftVersion(paperToMinecraftVersion(libs.versions.paper.get()))
    }

    register("printVersion") {
        doLast { print(version) }
    }
}

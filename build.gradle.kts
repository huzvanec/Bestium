plugins {
    id("java-conventions")
    alias(libs.plugins.shadow)
    alias(libs.plugins.run.paper)
}

allprojects {
    group = "cz.jeme"
    version = "2.1.0"
}

dependencies {
    implementation(project(":api"))
    implementation(project(":core"))
}

tasks {
    shadowJar {
        archiveClassifier = ""

        dependencies {
            exclude(dependency("org.jetbrains:annotations:.*"))
        }

        fun shade(pattern: String) = relocate(pattern, "${project.group}.${project.name.lowercase()}.shaded.$pattern")

        shade("kotlin")
    }

    assemble {
        dependsOn(shadowJar)
    }

    runServer {
        downloadPlugins {
            modrinth("bettermodel", libs.versions.bettermodel.get())
        }
        minecraftVersion(paperToMinecraftVersion(libs.versions.paper.get()))
    }

    register("printVersion") {
        doLast { print(version) }
    }
}

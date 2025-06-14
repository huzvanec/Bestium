plugins {
    `java-library`
    kotlin("jvm") version "2.1.21"
    id("com.gradleup.shadow") version "9.0.0-beta16"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17" apply false
}

val targetJavaVersion = (project.properties["targetJavaVersion"] as String).toInt()

allprojects {
    group = "cz.jeme"
    version = "1.0.0"


    apply {
        plugin("kotlin")
        plugin("java")
    }

    kotlin {
        jvmToolchain(targetJavaVersion)
    }

    java {
        toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    }

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://oss.sonatype.org/content/groups/public/")
    }

    dependencies {
        compileOnly("io.github.toxicity188:BetterModel:1.6.1")
    }
}

subprojects {
    apply {
        plugin("io.papermc.paperweight.userdev")
    }
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
            modrinth("bettermodel", "1.5.2")
        }
        minecraftVersion("1.21.4")
    }
}

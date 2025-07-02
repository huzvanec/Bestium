plugins {
    `java-library`
    kotlin("jvm") version "2.2.0"
    id("com.gradleup.shadow") version "9.0.0-rc1"
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
        val version = JavaVersion.toVersion(targetJavaVersion)
        sourceCompatibility = version
        targetCompatibility = version
        if (JavaVersion.current() < version)
            toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    }

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://oss.sonatype.org/content/groups/public/")
    }

    dependencies {
        compileOnly("io.github.toxicity188:BetterModel:1.8.0")
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
            val bmVer = project.configurations["compileOnly"].dependencies
                .first { it.group == "io.github.toxicity188" && it.name == "BetterModel" }
                .version!!

            modrinth("bettermodel", /* bmVer */"1.7.1-SNAPSHOT-196")
        }
        minecraftVersion(project.properties["minecraftVersion"] as String)
    }
}

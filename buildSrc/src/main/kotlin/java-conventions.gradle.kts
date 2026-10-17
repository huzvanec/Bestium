import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    `java-library`
}

val libs = the<LibrariesForLibs>()
val targetJavaVersion = libs.versions.java.get().toInt()

repositories {
    mavenCentral()
    maven("https://repo.xenondevs.xyz/releases/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(targetJavaVersion)
        vendor = JvmVendorSpec.ADOPTIUM
    }
}
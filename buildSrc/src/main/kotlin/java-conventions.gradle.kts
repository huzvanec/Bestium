import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    `java-library`
}

val libs = the<LibrariesForLibs>()
val targetJavaVersion = libs.versions.java.get().toInt()

repositories {
    mavenCentral()
}

java {
    val version = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = version
    targetCompatibility = version
    if (JavaVersion.current() < version)
        toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
}
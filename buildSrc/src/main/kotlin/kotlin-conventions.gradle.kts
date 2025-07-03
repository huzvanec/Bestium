import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("java-conventions")
    kotlin("jvm")
}

val libs = the<LibrariesForLibs>()
val targetJavaVersion = libs.versions.java.get().toInt()

kotlin {
    jvmToolchain(targetJavaVersion)
}
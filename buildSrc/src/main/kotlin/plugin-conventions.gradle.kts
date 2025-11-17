import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("java-conventions")
}

val libs = the<LibrariesForLibs>()

dependencies {
    compileOnly(libs.bettermodel)
}
import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("io.papermc.paperweight.userdev")
}

val libs = the<LibrariesForLibs>()

dependencies {
    compileOnly(libs.bettermodel)
    paperweight.paperDevBundle(libs.versions.paper.get())
}
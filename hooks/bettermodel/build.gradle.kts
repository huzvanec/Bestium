plugins {
    id("kotlin-conventions")
    id("io.papermc.paperweight.userdev")
}

dependencies {
    compileOnly(project(":api"))
    compileOnly(libs.bundles.bettermodel)
    paperweight.paperDevBundle(libs.versions.paper.get())
}

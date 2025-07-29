import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar

plugins {
    id("java-conventions")
    id("plugin-conventions")
    alias(libs.plugins.maven.publish)
}

mavenPublishing {
    configure(
        JavaLibrary(
            javadocJar = JavadocJar.Javadoc(),
            sourcesJar = true
        )
    )

    publishToMavenCentral(automaticRelease = false)
    signAllPublications()

    coordinates(
        project.group as String,
        "bestium",
        rootProject.version as String
    )

    pom {
        name = rootProject.name
        description = "A powerful library plugin for creating Minecraft entities with custom behavior"
        inceptionYear = "2025"
        url = "https://github.com/huzvanec/Bestium"
        licenses {
            license {
                name = "GNU General Public License v3.0"
                url = "https://www.gnu.org/licenses/gpl-3.0.en.html"
                distribution = "https://www.gnu.org/licenses/gpl-3.0.txt"
            }
        }
        developers {
            developer {
                id = "huzvanec"
                name = "Hužva"
                url = "https://github.com/huzvanec"
            }
        }
        scm {
            url = "https://github.com/huzvanec/Bestium"
            connection = "scm:git:git://github.com/huzvanec/Bestium.git"
            developerConnection = "scm:git:ssh://git@github.com/huzvanec/Bestium.git"
        }
    }
}

tasks {
    javadoc {
        (options as? StandardJavadocDocletOptions)?.apply {
            docTitle = "Bestium API v$version"
            windowTitle = docTitle
            links(
                "https://docs.oracle.com/en/java/javase/${libs.versions.java.get()}/docs/api/",
                "https://jd.papermc.io/paper/${paperToMinecraftVersion(libs.versions.paper.get())}/",
                "https://jspecify.dev/docs/api/",
                "https://jd.advntr.dev/api/latest/",
                "https://jd.advntr.dev/key/latest/",
                "https://jd.advntr.dev/text-minimessage/latest/",
                "https://jd.advntr.dev/text-serializer-gson/latest/",
                "https://jd.advntr.dev/text-serializer-legacy/latest/",
                "https://jd.advntr.dev/text-serializer-plain/latest/",
                "https://jd.advntr.dev/text-logger-slf4j/latest/"
            )
        }
    }
}
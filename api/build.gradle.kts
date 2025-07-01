import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar

plugins {
    id("com.vanniktech.maven.publish") version "0.33.0"
}

dependencies {
    paperweight.paperDevBundle("${project.properties["minecraftVersion"]}-R0.1-SNAPSHOT")
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
        rootProject.group as String,
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
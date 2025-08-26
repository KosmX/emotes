import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.maven
import org.w3c.dom.Element


fun asCurseForgeVersion(full: String, release: String): String {
    if ("w" in full) { // snapshots
        return "$release-Snapshot" // release is unknown for snapshot, so use argument
    } else if ("-" in release) { // rc and pre
        val majorMinorPatch = release.split("-").first() // extract release
        return "$majorMinorPatch-Snapshot"
    } else return release // release version
}

/**
 * Removes -pre and -rc suffixes to keep only release version
 */
fun removePreRc(mcVer: String): String {
    return mcVer.split("-").first()
}

private fun runCommand(cmd: String): Pair<Int, String> {
    val p = Runtime.getRuntime().exec(cmd.split(" ").toTypedArray())
    return p.waitFor() to p.inputReader().readText().trim()
}

fun getGitShortRevision(): String {
    return runCommand("git rev-parse --verify --short HEAD").second
}

fun getGitRevision(): String {
    return runCommand("git rev-parse --verify HEAD").second
}

fun MavenPublication.removeDependencies(artifactIds: List<String>) {
    pom.withXml {
        val dependenciesNode = asElement().getElementsByTagName("dependency")
        val dependenciesToRemove = mutableListOf<org.w3c.dom.Node>()

        for (i in 0 until dependenciesNode.length) {
            val dependency = dependenciesNode.item(i)
            val artifactIdNode = dependency.childNodes.let { nodes ->
                (0 until nodes.length)
                    .map { nodes.item(it) }
                    .find { it.nodeName == "artifactId" }
            }

            if (artifactIdNode != null && artifactIdNode.textContent in artifactIds) {
                dependenciesToRemove.add(dependency)
            }
        }

        dependenciesToRemove.forEach { it.parentNode.removeChild(it) }
    }
}

fun Element.getOrCreateChild(tagName: String): Element {
    val existingNodes = this.getElementsByTagName(tagName)
    if (existingNodes.length > 0) {
        return existingNodes.item(0) as Element
    }

    // Create a new child element and append it
    val newChild = ownerDocument.createElement(tagName)
    this.appendChild(newChild)

    // Return the updated NodeList
    return this.getElementsByTagName(tagName).item(0) as Element
}

fun Element.appendChild(name: String, text: String?) {
    val el = ownerDocument.createElement(name)
    el.textContent = text ?: ""
    appendChild(el)
}

fun Element.addNode(name: String, content: (Element.() -> Unit)) {
    val node = ownerDocument.createElement(name)
    node.content()
    appendChild(node)
}

fun MavenPublication.addDeps(project: Project, configuration: NamedDomainObjectProvider<Configuration>, scope: String) {
    addDeps(project, configuration.get(), scope)
}

fun MavenPublication.addDeps(project: Project, configuration: Configuration, scope: String) {
    pom.withXml {
        val dependenciesNode = asElement().getOrCreateChild("dependencies")

        val set = configuration.dependencies.toMutableSet()
        configuration.extendsFrom.forEach { set.addAll(configuration.dependencies.toSet()) }
        for (dep in set) {
            var group = dep.group
            var artifactId = dep.name
            if (dep is DefaultProjectDependency) {
                val p = project.project(dep.path)
                val publishing = p.extensions.findByType(PublishingExtension::class.java)
                if (publishing == null) continue
                for (pub in publishing.publications) {
                    if (pub !is MavenPublication) continue
                    group = pub.groupId
                    artifactId = pub.artifactId
                    break
                }
            }
            dependenciesNode.addNode("dependency") {
                appendChild("groupId", group)
                appendChild("artifactId", artifactId)
                appendChild("version", dep.version)
                appendChild("scope", scope)
                val exclude = dep is ModuleDependency && !dep.isTransitive
                if (!exclude) return@addNode
                addNode("exclusions") {
                    addNode("exclusion") {
                        appendChild("groupId", "*")
                        appendChild("artifactId", "*")
                    }
                }
            }
        }
    }

}

/**
 * Adds a maven.kosmx.dev repository with username and password set from [project]
 */
fun RepositoryHandler.kosmxRepo(project: Project): MavenArtifactRepository {
    return maven("https://maven.kosmx.dev/") {
        credentials {
            username = "kosmx"
            password = project.providers.environmentVariable("KOSMX_TOKEN").get()
        }
    }
}

fun MavenPublication.withCustomPom(name: String, desc: String) {
    pom {
        this.name = name
        description = desc
        url = "https://github.com/KosmX/emotes"
        developers {
            developer {
                id = "kosmx"
                this.name = "KosmX"
                email = "kosmx.mc@gmail.com"
            }
            developer {
                id = "dima_dencep"
                this.name = "dima_dencep"
                email = "dima_dencep@redlance.org"
            }
        }

        licenses {
            license {
                this.name = "GPL3"
                url = "https://github.com/KosmX/emotes/blob/HEAD/LICENSE"
            }
        }

        scm {
            connection = "scm:git:github.com/kosmx/emotes.git"
            developerConnection = "scm:git:github.com/kosmx/emotes.git"
            url = "https://github.com/KosmX/emotes"
        }
    }
}
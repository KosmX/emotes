import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.internal.extensions.core.extra

val ENV: Map<String, String> by lazy { System.getenv() }

operator fun Project.get(name: String): String = findProperty(name) as String

var Project.isRelease: Boolean
    get() = rootProject.extra.get("isRelease") as Boolean
    set(v) = rootProject.extra.set("isRelease", v)

var Project.changes: String
    get() = rootProject.extra.get("changes") as String
    set(v) = rootProject.extra.set("changes", v)

var Project.shouldPublishMaven: Boolean
    get() = rootProject.extra.get("shouldPublishMaven")!! as Boolean
    set(v) = rootProject.extra.set("shouldPublishMaven", v)

var Project.mod_version
    get() = rootProject.extra.get("mod_version").toString()
    set(v) = rootProject.extra.set("mod_version", v)

val Project.release_minecraft_versions: List<String>
    get() = this["minecraft_release_versions"].split(",")

val Project.curseforge_minecraft_versions: List<String>
    get() = release_minecraft_versions.stream()
        .map { asCurseForgeVersion(it) }
        .toList()

val Project.java_version: JavaVersion
    get() = JavaVersion.toVersion(this["java_version"])

/**
 * Can be `stable`, `beta`, `alpha`
 */
var Project.releaseType
    get() = rootProject.extra["releaseType"]!! as String
    set(v) = rootProject.extra.set("releaseType", v)

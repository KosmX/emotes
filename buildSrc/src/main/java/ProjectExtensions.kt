import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.TaskProvider
import org.gradle.internal.extensions.core.extra
import org.gradle.kotlin.dsl.register

val ENV: Map<String, String> by lazy { System.getenv() }

operator fun Project.get(name: String): String =
    properties[name] as String

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

val Project.version_base
    get() = properties["version_base"] as String

val Project.minecraft_version
    get() = properties["minecraft_version"] as String

val Project.release_minecraft_versions: List<String>
    get() = (properties["minecraft_release_versions"] as String).split(",")

val Project.curseforge_minecraft_versions: List<String>
    get() = release_minecraft_versions.stream()
        .map { asCurseForgeVersion(minecraft_version, it) }
        .toList()

val Project.parchment_version
    get() = properties["parchment_version"] as String

val Project.mod_description
    get() = properties["mod_description"] as String

val Project.fabric_loader_version
    get() = properties["fabric_loader_version"] as String

val Project.neoforge_version
    get() = properties["neoforge_version"] as String

val Project.java_version: JavaVersion
    get() = JavaVersion.toVersion(properties["java_version"] as String)

/**
 * Can be `stable`, `beta`, `alpha`
 */
var Project.releaseType
    get() = rootProject.extra["releaseType"]!! as String
    set(v) = rootProject.extra.set("releaseType", v)

val Project.archives_base_name
    get() = properties["archives_base_name"] as String

fun Project.publishDiscord(name: String = "publishDiscord", block: PublishDiscordTask.() -> Unit): TaskProvider<PublishDiscordTask> {
    return tasks.register<PublishDiscordTask>(name) {
        block()
        validate()
    }
}

fun Project.publishResult(platformName: String): RegularFileProperty {
    return tasks.withType(me.modmuss50.mpp.PublishModTask::class.java).first { it.platform.name == platformName }.result
}

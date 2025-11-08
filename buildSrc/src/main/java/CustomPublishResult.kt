import me.modmuss50.mpp.PublishResult
import java.io.File
import java.io.FileNotFoundException

val CURSEFORGE_EMOJI = Emoji("curseforge", "1136405235187847198")
val MODRINTH_EMOJI = Emoji("modrinth", "1136404935374798878")
val HANGAR_EMOJI = Emoji("hangar", "1407387843931672647")
val GITHUB_EMOJI = Emoji("github", "1136406913542795364")

fun Emoji.Companion.fromPlatform(platform: String): Emoji? {
    return when (platform) {
        "modrinth" -> MODRINTH_EMOJI
        "curseforge" -> CURSEFORGE_EMOJI
        "hangar" -> HANGAR_EMOJI
        "github" -> GITHUB_EMOJI
        else -> null
    }
}

interface ICustomPublishResult {
    val title: String
    val link: String
    val emoji: Emoji?
}

class CustomPublishResult(override val title: String,
                          override val link: String,
                          override val emoji: Emoji? = null) : ICustomPublishResult {
    companion object {
        fun from(result: PublishResult): CustomPublishResult {
            return CustomPublishResult(result.title, result.link, Emoji.fromPlatform(result.type))
        }
    }
}

class LatePublishResult(val file: File, private val emojiOverride: Emoji? = null, private val titleOverride: String? = null) : ICustomPublishResult {
    val loaded by lazy {
        val p = try {
            PublishResult.fromJson(file.readText())
        } catch (e: FileNotFoundException) {
            throw IllegalStateException("Missing publish result file. " +
                    "Possibly some publish task is failed", e)
        }
        CustomPublishResult.from(p)
    }
    override val title: String
        get() = titleOverride ?: loaded.title
    override val link: String
        get() = loaded.link
    override val emoji: Emoji?
        get() = emojiOverride ?: loaded.emoji

}

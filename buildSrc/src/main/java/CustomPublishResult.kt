import me.modmuss50.mpp.PublishResult
import java.io.File
import java.io.FileNotFoundException

val CURSEFORGE_EMOJI = Emoji("curseforge", "1136405235187847198")
val MODRINTH_EMOJI = Emoji("modrinth", "1136404935374798878")
val HANGAR_EMOJI = Emoji("hangar", "1407105220122509514")

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
            val emoji = when (result.type) {
                "modrinth" -> MODRINTH_EMOJI
                "curseforge" -> CURSEFORGE_EMOJI
                else -> null
            }
            return CustomPublishResult(result.title, result.link, emoji)
        }
    }
}

class LatePublishResult(val file: File, override val emoji: Emoji? = null) : ICustomPublishResult {
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
        get() = loaded.title
    override val link: String
        get() = loaded.link

}
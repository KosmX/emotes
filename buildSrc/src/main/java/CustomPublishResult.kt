import me.modmuss50.mpp.PublishResult
import java.io.File
import java.io.FileNotFoundException

const val CURSEFORGE_ICON = "<:curseforge:1136405235187847198>"
const val MODRINTH_ICON = "<:modrinth:1136404935374798878>"
const val HANGAR_ICON = "<:hangar:1407105220122509514>"

interface ICustomPublishResult {
    val title: String
    val link: String
}

class CustomPublishResult(override val title: String, override val link: String) : ICustomPublishResult {
    companion object {
        fun from(result: PublishResult): CustomPublishResult {
            val prefix = when (result.type) {
                "modrinth" -> MODRINTH_ICON
                "curseforge" -> CURSEFORGE_ICON
                else -> null
            }
            val title = prefix?.let { it+" "+result.title } ?: result.title
            return CustomPublishResult(title, result.link)
        }
    }
}

class LatePublishResult(val file: File) : ICustomPublishResult {
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
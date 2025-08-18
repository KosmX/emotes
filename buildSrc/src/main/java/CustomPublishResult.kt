import me.modmuss50.mpp.PublishResult
import java.io.File

const val CF_ICON = "💥"
const val MODRINTH_ICON = "🐸"
const val HANGAR_LOGO = "🚪"

interface ICustomPublishResult {
    val title: String
    val link: String
}

class CustomPublishResult(override val title: String, override val link: String) : ICustomPublishResult {
    companion object {
        fun from(result: PublishResult): CustomPublishResult {
            val prefix = when (result.type) {
                "modrinth" -> MODRINTH_ICON
                "curseforge" -> CF_ICON
                else -> null
            }
            val title = prefix?.let { it+" "+result.title } ?: result.title
            return CustomPublishResult(title, result.link)
        }
    }
}

class LatePublishResult(val file: File) : ICustomPublishResult {
    val loaded by lazy {
        val p = PublishResult.fromJson(file.readText())
        CustomPublishResult.from(p)
    }
    override val title: String
        get() = loaded.title
    override val link: String
        get() = loaded.title

}
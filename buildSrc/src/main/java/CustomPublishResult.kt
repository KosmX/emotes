import me.modmuss50.mpp.PublishResult

const val CF_ICON = "💥"
const val MODRINTH_ICON = "🐸"
const val HANGAR_LOGO = "🚪"

class CustomPublishResult(val title: String, val link: String) {
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
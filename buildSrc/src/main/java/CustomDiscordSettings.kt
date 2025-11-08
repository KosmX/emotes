import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.modmuss50.mpp.platforms.discord.DiscordAPI
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import java.time.format.DateTimeFormatter

class DownloadLinks {
    val rows = mutableListOf<List<ICustomPublishResult>>()
    private val currentRow = mutableListOf<ICustomPublishResult>()

    fun nextRow() {
        if (currentRow.isEmpty()) return
        val rws = currentRow.chunked(5)
        rows.addAll(rws)
        currentRow.clear()
    }

    fun add(result: ICustomPublishResult) {
        currentRow.add(result)
    }

    fun RegularFileProperty.toLate(emoji: Emoji?, title: String?): LatePublishResult {
        val file = this.get().asFile
        return LatePublishResult(file, emoji, title)
    }

    /**
     * Adds publish result for [platform] from mod-publish-plugin in [project]
     */
    fun Project.from(project: String, platform: String, emoji: Emoji? = null, title: String? = null) {
        from(project(project), platform, emoji, title)
    }

    /**
     * Adds publish result for [platform] from mod-publish-plugin in [project]
     */
    fun from(project: Project, platform: String, emoji: Emoji? = null, title: String? = null) {
        add(project.publishResult(platform).toLate(emoji, title))
    }

    fun custom(title: String, link: String, emoji: Emoji?) {
        add(CustomPublishResult(title, link, emoji))
    }
}

fun ICustomPublishResult.createButton(): ButtonComponent {
    return ButtonComponent(title, link, emoji)
}

@Serializable
data class Webhook(
    val content: String? = null,
    val username: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    val tts: Boolean? = null,
    val embeds: List<DiscordAPI.Embed>? = null,
    val components: List<Component>? = null,
    val flags: Int? = null,
    @SerialName("thread_name")
    val threadName: String? = null,
)

@Serializable
sealed class Component {
    protected abstract val type: Int
}

@Serializable
data class ActionRow(
    val components: List<Component>? = null,
) : Component() {
    override val type: Int = 1
}

@Serializable
data class Emoji(
    val name: String,
    val id: String
)

@Serializable
data class ButtonComponent(
    val label: String? = null,
    val url: String? = null,
    val emoji: Emoji? = null,
) : Component() {
    override val type: Int = 2
    @Suppress("unused")
    val style: Int = 5 // link
}

class EmbedBuilder {
    var title: String? = null
    var type: String? = null
    var description: String? = null
    var url: String? = null

    /**
     * ISO8601 timestamp
     */
    var timestamp: String? = null
    var color: Int? = null

    var footer: DiscordAPI.EmbedFooter? = null
    var image: DiscordAPI.EmbedImage? = null
    var thumbnail: DiscordAPI.EmbedThumbnail? = null
    var video: DiscordAPI.EmbedVideo? = null
    var provider: DiscordAPI.EmbedProvider? = null
    var author: DiscordAPI.EmbedAuthor? = null
    var fields: List<DiscordAPI.EmbedField>? = null

    fun thumbnail(url: String) {
        this.thumbnail = DiscordAPI.EmbedThumbnail(url = url)
    }

    fun timestamp(ms: Long?) {
        timestamp = ms?.let {
            val inst = java.time.Instant.ofEpochMilli(ms)
            DateTimeFormatter.ISO_INSTANT.format(inst)
        }
    }

    fun build(): DiscordAPI.Embed =
        DiscordAPI.Embed(
            title = title,
            type = type,
            description = description,
            url = url,
            timestamp = timestamp,
            color = color,
            footer = footer,
            image = image,
            thumbnail = thumbnail,
            video = video,
            provider = provider,
            author = author,
            fields = fields
        )
}

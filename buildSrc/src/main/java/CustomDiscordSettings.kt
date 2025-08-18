import me.modmuss50.mpp.PublishResult
import me.modmuss50.mpp.platforms.discord.DiscordAPI
import org.gradle.api.file.RegularFileProperty


class DownloadLinks {
    val rows = mutableListOf<List<ICustomPublishResult>>()
    private val _row = mutableListOf<ICustomPublishResult>()

    fun nextRow() {
        if (_row.isEmpty()) return
        val rws = _row.chunked(5)
        rows.addAll(rws)
        _row.clear()
    }

    operator fun CustomPublishResult.unaryPlus() {
        _row.add(this)
    }

    operator fun PublishResult.unaryPlus() {
        _row.add(CustomPublishResult.from(this))
    }

    operator fun RegularFileProperty.unaryPlus() {
        val file = this.get().asFile
        _row.add(LatePublishResult(file))
    }
}

fun ICustomPublishResult.createButton(): DiscordAPI.ButtonComponent {
    return DiscordAPI.ButtonComponent(title, link)
}

class EmbedBuilder {
    var title: String? = null
    var type: String? = null
    var description: String? = null
    var url: String? = null
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



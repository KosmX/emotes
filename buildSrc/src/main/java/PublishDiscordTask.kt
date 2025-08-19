import me.modmuss50.mpp.HttpUtils
import me.modmuss50.mpp.platforms.discord.DiscordAPI.json
import okhttp3.RequestBody.Companion.toRequestBody
import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault
abstract class PublishDiscordTask : DefaultTask() {
    private val httpUtils = HttpUtils()
    private val headers: Map<String, String> = mapOf("Content-Type" to "application/json")

    @get:Input
    @get:Optional
    abstract val content: Property<String>

    @get:Input
    abstract val url: Property<String>

    @get:Input
    @get:Optional
    abstract val username: Property<String>

    @get:Input
    @get:Optional
    abstract val embeds: ListProperty<EmbedBuilder>

    @get:Input
    @get:Optional
    abstract val links: Property<DownloadLinks>

    init {
        embeds.convention(emptyList())
        links.convention(DownloadLinks())
        group = "publishing"
    }

    fun embed(block: EmbedBuilder.() -> Unit) {
        val e = EmbedBuilder()
        e.block()
        embeds.add(e)
    }

    fun links(block: DownloadLinks.() -> Unit) {
        val l = DownloadLinks()
        l.block()
        links.set(l)
    }

    @TaskAction
    fun publish() {
        validate()
        val dl = links.get()

        val components = mutableListOf<Component>()
        dl.nextRow() // complete current row
        for (row in dl.rows) {
            val rc = row.map { it.createButton() }
            components.add(ActionRow(rc))
        }
        val wh = Webhook(content.get(),
            username.get(),
            embeds = embeds.get().map { it.build() },
            components = components.ifEmpty { null }
        )
        try {
            val body = json.encodeToString(wh).toRequestBody()
            httpUtils.post<String>(url.get(), body, headers)
        } catch (e: Exception) {
            throw RuntimeException("Failed to post a webhook", e)
        }
    }

    fun validate() {
        require(url.orNull?.isNotBlank() == true) {
            "Missing webhook URL"
        }
        require(content.isPresent || embeds.get().isNotEmpty()) {
            "Message must contain at least message or embed"
        }
    }
}

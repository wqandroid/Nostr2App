package nostr.postr.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.reflect.TypeToken
import nostr.postr.events.Event


@Entity(tableName = "feed_info")
class FeedItem(
    @PrimaryKey
    val id: String,
    val pubkey: String,
    val created_at: Long,
    val content: String,
    val tags: String? = null
) {

    private fun parseTags(): List<List<String>> {
        if (tags.isNullOrEmpty())return mutableListOf()
        return Event.gson.fromJson(tags, object : TypeToken<List<List<String>>>() {}.getType())
    }

    fun getReplyTos(): List<String> {
        return parseTags().filter { it.firstOrNull() == "e" }.mapNotNull { it.getOrNull(1) }
    }

    fun getMentions(): List<String> {
        return parseTags().filter { it.firstOrNull() == "p" }.mapNotNull { it.getOrNull(1) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FeedItem

        if (id != other.id) return false
        if (content != other.content) return false
        if (tags != other.tags) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + (tags?.hashCode() ?: 0)
        return result
    }


}
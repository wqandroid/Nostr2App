package nostr.postr.db

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.reflect.TypeToken
import kotlinx.parcelize.Parcelize
import nostr.postr.events.Event

@Parcelize
@Entity(tableName = "feed_info")
class FeedItem(
    @PrimaryKey
    val id: String,
    val pubkey: String,
    val created_at: Long,
    val content: String,
    val tags: String? = null
) : Parcelable {

    private fun parseTags(): List<List<String>> {
        if (tags.isNullOrEmpty()) return mutableListOf()
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

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }


}
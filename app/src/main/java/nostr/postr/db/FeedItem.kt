package nostr.postr.db

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "feed_info")
class FeedItem(
    @PrimaryKey
    val id: String, val pubkey: String, val created_at: Long, val content: String
) {

}
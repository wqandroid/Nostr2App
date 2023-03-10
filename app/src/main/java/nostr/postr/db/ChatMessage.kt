package nostr.postr.db

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity("chat")
class ChatMessage(
    @PrimaryKey val msgId: String,
    val roomId: String,
    val content: String,
    val createAt: Long,
    val createPubKey: String,
    val success: Boolean = true
)

@Entity("chat_room")
class ChatRoom(
    @PrimaryKey val roomId: String,
    val sendTo: String,
    var content: String,
    var lastUpdate: Long,
    var hasUnread: Boolean,
) {

    @Ignore
    var profile: UserProfile? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChatRoom

        if (roomId != other.roomId) return false

        return true
    }

    override fun hashCode(): Int {
        return roomId.hashCode()
    }


    fun getUserAvatar(): String {
        return profile?.picture ?: "https://robohash.org/${sendTo}.png"
    }

}
package nostr.postr.db

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity("chat")
class Chat(
    @PrimaryKey val msgId: String,
    val roomId: String,
    val content: String,
    val createAt: Long,
    val isRead: Boolean
)

@Entity("chat_room")
class ChatRoom(
    @PrimaryKey val roomId: String,
    val sendTo: String,
    val content: String,
    val lastUpdate: Long
) {
    @Ignore
    var profile: UserProfile? = null

}
package nostr.postr.db

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import nostr.postr.bechToBytes

@Parcelize
@Entity(tableName = "user_profile")
class UserProfile(
    @PrimaryKey
    val pubkey: String
) :Parcelable{

    var about: String? = null
    var banner: String? = null
    var display_name: String? = null
    var lud06: String? = null
    var lud16: String? = null
    var name: String? = null
    var nip05: String? = null
    var picture: String? = null
    var website: String? = null


    fun bestName(): String {
        return display_name ?: name ?: pubkey
    }
    fun getUserAvatar(): String {
        return picture ?: "https://robohash.org/${pubkey}.png"
    }
}

@Entity(tableName = "block_user2")
class BlockUser(@PrimaryKey val pubkey: String) {
    var contentMD5: String? = null
}

@Entity(tableName = "follow_user")
class FollowUserKey(@PrimaryKey val pubkey: String,var status: Int = 0) {
    var name: String? = null
}
package nostr.postr.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import nostr.postr.bechToBytes


@Entity(tableName = "user_profile")
class UserProfile(
    @PrimaryKey
    val pubkey: String
) {

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
}

@Entity(tableName = "block_user2")
class BlockUser(@PrimaryKey val pubkey: String) {
    var contentMD5: String? = null
}

@Entity(tableName = "follow_user")
class FollowUserKey(@PrimaryKey val pubkey: String,var status: Int = 0) {
    var name: String? = null
}
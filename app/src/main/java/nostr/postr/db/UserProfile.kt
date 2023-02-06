package nostr.postr.db

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "user_profile")
class UserProfile(@PrimaryKey
                  val pubkey:String) {

    var about: String? = null
    var banner: String? = null
    var display_name: String? = null
    var lud06: String? = null
    var lud16: String? = null
    var name: String? = null
    var nip05: String? = null
    var picture: String? = null
    var website: String? = null
}
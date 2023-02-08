package nostr.postr

data class ContactMetaData(
    val name: String,
    val picture: String,
    val about: String,
    val display_name:String?,
    val nip05: String?,
    val banner:String?,
    val lud16:String?,
    val website:String?
)

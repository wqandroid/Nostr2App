package nostr.postr

data class ContactMetaData(
    val name: String,
    val picture: String,
    val about: String,
    val display_name:String?,
    val nip05: String?,
    val banner:String?,
    val lud16:String?,
    val website:String?,
    val nip05valid:Boolean?
)


//{
//    "name": "wolf",
//    "username": "wolf",
//    "display_name": "Amanda Wolf",
//    "displayName": "Amanda Wolf",
//    "picture": "https://pbs.twimg.com/profile_images/1623398566952861696/LIfkB13r.jpg",
//    "banner": "https://pbs.twimg.com/profile_banners/1100386194859413505/1675247669/1080x360",
//    "website": "",
//    "about": "Knowledge is power and I'm pretty powerless. ChatGPT is my PA.n bleakcement68@walletofsatoshi.com",
//    "lud16": "LNURL1DP68GURN8GHJ7AMPD3KX2AR0VEEKZAR0WD5XJTNRDAKJ7TNHV4KXCTTTDEHHWM30D3H82UNVWQHKYMR9V94KXETDV4H8GD3CQEF5T7",
//    "nip05": "wolf@Nostr-Check.com",
//    "nip05valid": true
//}
package nostr.postr

object Constants {
    val defaultRelays = listOf(
//        Relay("wss://relay.damus.io", read = true, write = true),
//        Relay("wss://relay.nostr.info", read = true, write = true),
        Relay("wss://nostr-pub.wellorder.net", read = true, write = true),
//        Relay("wss://nostr-pub.semisol.dev", read = true, write = true),
        Relay("wss://nostr.bitcoiner.social", read = true, write = true),
        Relay("wss://nostr.zebedee.cloud", read = true, write = true),
    )
}

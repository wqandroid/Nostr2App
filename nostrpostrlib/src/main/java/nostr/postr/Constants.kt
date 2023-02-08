package nostr.postr

object Constants {
    val defaultRelays = listOf(
        Relay("wss://relay.damus.io", read = true, write = true),
        Relay("wss://nostr-relay.untethr.me", read = true, write = false),
        Relay("wss://nostr-relay.freeberty.net", read = true, write = false),
        Relay("wss://nostr.bitcoiner.social", read = true, write = false),
        Relay("wss://nostr-relay.wlvs.space", read = true, write = false),
        Relay("wss://nostr-pub.wellorder.net", read = true, write = true),
        Relay("wss://nostr.rocks", read = false, write = true),
        Relay("wss://nostr.onsats.org", read = false, write = true)
    )
}

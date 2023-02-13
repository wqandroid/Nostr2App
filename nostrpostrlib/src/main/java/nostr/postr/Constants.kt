package nostr.postr

object Constants {
    val defaultRelays = listOf(
        Relay("wss://relay.nostr.info", read = true, write = true),
        Relay("wss://nostr-pub.wellorder.net", read = true, write = true),
//        Relay("wss://nostr-pub.semisol.dev", read = true, write = true),
        Relay("wss://nostr.bitcoiner.social", read = true, write = true),
        Relay("wss://nostr.zebedee.cloud", read = true, write = true),
        Relay("wss://nostr.bitcoiner.social", read = true, write = true),
        Relay("wss://relay.damus.io", read = true, write = true),
        Relay("wss://nostr.mom", read = true, write = true),
    )



    fun getUserRelays():List<Relay>{
        return defaultRelays
    }



//    RelaySetupInfo("wss://nostr.bitcoiner.social", read = true, write = true, feedTypes = activeTypes),
//    RelaySetupInfo("wss://relay.nostr.bg", read = true, write = true, feedTypes = activeTypes),
//    RelaySetupInfo("wss://relay.snort.social", read = true, write = true, feedTypes = activeTypes),
//    RelaySetupInfo("wss://relay.damus.io", read = true, write = true, feedTypes = activeTypes),
//    RelaySetupInfo("wss://nostr.oxtr.dev", read = true, write = true, feedTypes = activeTypes),
//    RelaySetupInfo("wss://nostr-pub.wellorder.net", read = true, write = true, feedTypes = activeTypes),
//    RelaySetupInfo("wss://nostr.mom", read = true, write = true, feedTypes = activeTypes),
//    RelaySetupInfo("wss://no.str.cr", read = true, write = true, feedTypes = activeTypes),
//    RelaySetupInfo("wss://nos.lol", read = true, write = true, feedTypes = activeTypes),
}

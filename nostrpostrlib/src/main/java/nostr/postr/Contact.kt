package nostr.postr

data class Contact(val pubKeyHex: String, val relayUri: String?){
    override fun toString(): String {
        return "Contact(pubKeyHex='$pubKeyHex', relayUri=$relayUri)"
    }
}
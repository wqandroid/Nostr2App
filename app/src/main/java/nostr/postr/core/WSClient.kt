package nostr.postr.core

import nostr.postr.*
import nostr.postr.events.Event
import java.util.*

object WSClient {


    private val relays: List<Relay> = Constants.getUserRelays()


    fun startConnection() {
        this.relays.forEach {
            it.justConnection()
        }
    }


    fun requestAndWatch(
        subscriptionId: String = UUID.randomUUID().toString().substring(0..10),
        filters: MutableList<JsonFilter> = mutableListOf(JsonFilter())
    ) {
        this.relays.forEach {
            it.requestAndWatch(subscriptionId = subscriptionId, filters)
        }
    }

    fun send(signedEvent: Event) {
        this.relays.forEach {
            it.send(signedEvent)
        }
    }

    fun close(subscriptionId: String) {
        RelayPool.close(subscriptionId)
        this.relays.forEach {
            it.close(subscriptionId)
        }
    }

    fun disconnect() {
        this.relays.forEach {
            it.disconnect()
        }
    }

    fun subscribe(listener: Client.Listener, allRelay: Boolean = true) {
        if (allRelay) {
            this.relays.forEach {
                it.register(listener)
            }
        } else {
            this.relays.firstOrNull { it.isOpen }?.register(listener)
        }
    }

    fun unsubscribe(listener: Client.Listener): Boolean {
        this.relays.forEach {
            it.unregister(listener)
        }
        return true
    }


}
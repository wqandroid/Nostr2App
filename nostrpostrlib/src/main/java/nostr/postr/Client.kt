package nostr.postr

import nostr.postr.events.Event
import java.util.UUID

/**
 * The Nostr Client manages multiple personae the user may switch between. Events are received and
 * published through multiple relays.
 * Events are stored with their respective persona.
 */
object Client {
    /**
     * Lenient mode:
     *
     * true: For maximum compatibility. If you want to play ball with sloppy counterparts, use
     *       this.
     * false: For developers who want to make protocol compliant counterparts. If your software
     *        produces events that fail to deserialize in strict mode, you should probably fix
     *        something.
     **/
    var lenient: Boolean = false

    //    private val listeners = HashSet<Listener>()
    internal var relays = Constants.defaultRelays.filter { it.enable }



    fun connect(
        relays: List<Relay> = Constants.defaultRelays
    ) {
//        synchronized(this) {
//            val temp = relays.filter {
//                it.enable
//            }.toList()
//            RelayPool.register(this)
//            RelayPool.loadRelays(temp)
//            this.relays = temp
//        }
    }

    fun requestAndWatch(
        subscriptionId: String = UUID.randomUUID().toString().substring(0..10),
        filters: MutableList<JsonFilter> = mutableListOf(JsonFilter())
    ) {

        synchronized(this) {
//            RelayPool.requestAndWatch(subscriptionId)
            relays.forEach {
                it.requestAndWatch(subscriptionId = subscriptionId,filters)
            }
        }
    }

    fun send(signedEvent: Event) {
        synchronized(this) {
            relays.forEach {
                it.send(signedEvent)
            }
        }
    }

    fun close(subscriptionId: String) {
        RelayPool.close(subscriptionId)
        relays.forEach {
            it.close(subscriptionId)
        }
    }

    fun disconnect() {
        relays.forEach {
            it.disconnect()
        }
    }

    fun subscribe(listener: Listener) {
//        listeners.add(listener)
        relays.forEach {
            it.register(listener)
        }
    }

    fun unsubscribe(listener: Listener): Boolean {
        relays.forEach {
            it.unregister(listener)
        }
        return true
    }


    abstract class Listener {
        /**
         * A new message was received
         */
        open fun onEvent(event: Event, subscriptionId: String, relay: Relay) = Unit


        /**
         * A new or repeat message was received
         */

        open fun onError(error: Error, subscriptionId: String, relay: Relay) = Unit

        /**
         * Connected to or disconnected from a relay
         */
        open fun onRelayStateChange(type: Relay.Type, relay: Relay) = Unit

        open fun onOK(relay: Relay) = Unit
    }
}
package nostr.postr.core

import android.util.Log
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import nostr.postr.Client
import nostr.postr.Relay
import nostr.postr.events.*
import java.util.*

abstract class WsViewModel : ViewModel() {
    val scope = CoroutineScope(Job() + Dispatchers.IO)

    var comDis = CompositeDisposable()

    private val clientListener = object : Client.Listener() {

        override fun onOK(relay: Relay) {
            super.onOK(relay)
            onOk(relay)
        }

        override fun onEvent(event: Event, subscriptionId: String, relay: Relay) {
            when (event.kind) {
                PrivateDmEvent.kind -> {
                    onRecPrivateDmEvent(subscriptionId, event as PrivateDmEvent)
                }
                ContactListEvent.kind -> {
                    onRecContactListEvent(subscriptionId, event as ContactListEvent)
                }
                MetadataEvent.kind -> {
                    onRecMetadataEvent(subscriptionId, event as MetadataEvent)
                }
                TextNoteEvent.kind -> {
                    onRecTextNoteEvent(subscriptionId, event as TextNoteEvent)
                }
                RecommendRelayEvent.kind -> {
                    Log.d("RecommendRelayEvent--->", event.toJson())
                }
            }
        }

        override fun onError(error: Error, subscriptionId: String, relay: Relay) {
            Log.e("RELAY", "onError: Relay ${relay.url}: ${error.message}")
            onRecError(error, subscriptionId, relay)
        }

        override fun onRelayStateChange(type: Relay.Type, relay: Relay) {
            Log.e(
                "RELAY", "Relay ${relay.url} ${
                    when (type) {
                        Relay.Type.CONNECT -> "connected."
                        Relay.Type.DISCONNECT -> "disconnected."
                        Relay.Type.EOSE -> "sent all events it had stored."
                    }
                }"
            )
        }
    }

    val wsClient = lazy {
        WSClient.also {
            it.subscribe(clientListener)
        }
    }

    open fun onRecMetadataEvent(subscriptionId: String, event: MetadataEvent) {

    }

    open fun onRecContactListEvent(subscriptionId: String, event: ContactListEvent) {

    }

    open fun onRecPrivateDmEvent(subscriptionId: String, event: PrivateDmEvent) {

    }

    open fun onRecTextNoteEvent(subscriptionId: String, event: TextNoteEvent) {

    }

    open fun onRecError(error: Error, subscriptionId: String, relay: Relay) {

    }

    open fun onOk(relay: Relay) {

    }

    fun getRand5(): String {
        return UUID.randomUUID().toString().substring(0..5)
    }

    override fun onCleared() {
        super.onCleared()
        wsClient.value.unsubscribe(clientListener)
//        wsClient.value.disconnect()
        comDis.clear()
        Log.e(
            "RELAY", "手动------------》disconnect"
        )
    }
}
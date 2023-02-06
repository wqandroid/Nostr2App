package nostr.postr.feed

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import nostr.postr.Client
import nostr.postr.JsonFilter
import nostr.postr.MyApplication
import nostr.postr.Relay
import nostr.postr.db.NostrDB
import nostr.postr.events.*

class FeedViewModel : ViewModel() {
    val feedLiveData = MutableLiveData<List<nostr.postr.db.FeedItem>>()


    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    private var count = 0

    private val clientListener = object : Client.Listener() {
        override fun onNewEvent(event: Event, subscriptionId: String) {
            when (event.kind) {
                MetadataEvent.kind, // 0
                TextNoteEvent.kind, // 1
                RecommendRelayEvent.kind, // 2
                ContactListEvent.kind, // 3
                PrivateDmEvent.kind, // 4
                DeletionEvent.kind, // 5
                in listOf(6, 7, 17, 30, 40, 7357) -> Unit
//                else -> Log.d("UNHANDLED_EVENT", event.toJson())
            }
            if (event.kind == TextNoteEvent.kind) {
                Log.d("TextNoteEvent--->", event.toJson())

                scope.launch {

                    val textEvent=event as TextNoteEvent

                    var feed = nostr.postr.db.FeedItem(
                        textEvent.id.toString(),
                        textEvent.pubKey.toString(),
                        textEvent.createdAt,
                        textEvent.content
                    )
                    NostrDB.getDatabase(MyApplication.getInstance())
                        .feedDao().insertFeed(feed)
                    Log.d("TextNoteEvent--->", "插入成功")
                    count++
                    if (count % 20 == 0) {
                        loadFeedFromDB()
                    }
                }

            } else if (event.kind == RecommendRelayEvent.kind) {
                Log.d("RecommendRelayEvent--->", event.toJson())
            }
        }

        override fun onError(error: Error, subscriptionId: String, relay: Relay) {
            Log.e("ERROR", "Relay ${relay.url}: ${error.message}")
        }

        override fun onRelayStateChange(type: Relay.Type, relay: Relay) {
            Log.d(
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


    fun reqFeed() {
        Client.subscribe(clientListener)
        Client.lenient = true

        val filter = JsonFilter(
            since = 1675666229,
//            until = 1675667229,
            limit = 20,
//            authors = mutableListOf("npub1n6tkfw2ptvhlpcj8x03lu6zeytnekjqjc6k5z257q4z8z5lstjas26nls2")
//                .apply {
//                    this.add("b6d43020a8edc6f7f66e63e592456e1fc6eb2d5ed426247b1346f994168d2dc5")
////                   this.add("npub1n6tkfw2ptvhlpcj8x03lu6zeytnekjqjc6k5z257q4z8z5lstjas26nls2")
//                }
        )
//        Client.connect()
//        Client.requestAndWatch(filters = mutableListOf(filter))


    }

    fun loadFeedFromDB() {
        scope.launch {
            val list = NostrDB.getDatabase(MyApplication._instance)
                .feedDao().getAll().sortedByDescending {
                    it.created_at
                }
            withContext(Dispatchers.Main) {
                feedLiveData.value = list
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Client.unsubscribe(listener = clientListener)
    }

}
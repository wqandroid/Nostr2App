package nostr.postr.feed

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import nostr.postr.*
import nostr.postr.db.NostrDB
import nostr.postr.db.UserProfile
import nostr.postr.events.*

class FeedViewModel : ViewModel() {
    val feedLiveData = MutableLiveData<List<Feed>>()


    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    private var count = 0

    private var profileList = mutableListOf<String>()

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
            when (event.kind) {
                MetadataEvent.kind -> {
                    val metadataEvent = event as MetadataEvent
                    Log.e("MetadataEvent--->", event.toJson())

                    metadataEvent.contactMetaData?.let {
                        val userProfile = UserProfile(event.pubKey.toHex()).apply {
                            this.name = it.name
                            this.about = it.about
                            this.picture = it.picture
                            this.nip05 = it.nip05
                        }
                        Log.e("MetadataEvent--->", "---->${userProfile.name}")
                        scope.launch {
                            NostrDB.getDatabase(MyApplication.getInstance())
                                .profileDao().insertUser(userProfile)
                        }
                    }

                }
                TextNoteEvent.kind -> {

                    scope.launch {

                        val textEvent = event as TextNoteEvent

                        var feed = nostr.postr.db.FeedItem(
                            textEvent.id.toString(),
                            textEvent.pubKey.toHex(),
                            textEvent.createdAt,
                            textEvent.content
                        )
                        NostrDB.getDatabase(MyApplication.getInstance())
                            .feedDao().insertFeed(feed)
                        count++
                        if (count % 20 == 0) {
                            loadFeedFromDB()
                        }

                        val userProfile = NostrDB.getDatabase(MyApplication._instance).profileDao()
                            .getUserInfo(feed.pubkey)
                        if (userProfile == null) {
                            profileList.add(feed.pubkey)
                            if (profileList.size >= 20) {
                                reqProfile()
                            }
                        }

                    }

                }
                RecommendRelayEvent.kind -> {
                    Log.d("RecommendRelayEvent--->", event.toJson())
                }
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

        scope.launch {
            val sinceTime = NostrDB.getDatabase(MyApplication._instance)
                .feedDao().getLast()?.created_at ?: System.currentTimeMillis() / 1000

            Client.subscribe(clientListener)
            Client.lenient = true

            val filter = JsonFilter(
                since = sinceTime,
                limit = 20,
            )
            Client.connect()
            Client.requestAndWatch(filters = mutableListOf(filter))

        }


    }

    fun reqProfile() {
        scope.launch {
            val temp = mutableListOf<String>()
            temp.addAll(profileList)
            profileList.clear()
//                Client.subscribe(clientListener)
            val filter = JsonFilter(
                kinds = mutableListOf(0),
                since = 1675748168,
                limit = 20,
                authors = temp
            )
//                Client.connect()
            Client.requestAndWatch(filters = mutableListOf(filter))
        }
    }


    fun loadFeedFromDB() {
        scope.launch {
            val list = NostrDB.getDatabase(MyApplication._instance)
                .feedDao().getAll()

            val feedList = mutableListOf<Feed>()
            list.forEach {
                val userProfile = NostrDB.getDatabase(MyApplication._instance).profileDao()
                    .getUserInfo(it.pubkey)
                feedList.add(Feed(it, userProfile))
            }

            withContext(Dispatchers.Main) {
                feedLiveData.value = feedList
            }
//            list.forEach {
//                val userProfile = NostrDB.getDatabase(MyApplication._instance).profileDao()
//                    .getUserInfo(it.pubkey)
//                if (userProfile == null) {
//                    profileList.add(it.pubkey)
//                }
//            }
//            reqProfile()
        }
    }

    override fun onCleared() {
        super.onCleared()
        Client.unsubscribe(listener = clientListener)
    }

}
package nostr.postr.ui.user

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import nostr.postr.*
import nostr.postr.core.AccountManger
import nostr.postr.db.BlockUser
import nostr.postr.db.NostrDB
import nostr.postr.db.UserProfile
import nostr.postr.events.*
import nostr.postr.ui.feed.Feed
import nostr.postr.util.MD5

class UserViewModel : ViewModel() {

    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    lateinit var pubKey: String

    val user = MutableLiveData<UserProfile>()
    val feedLiveData = MutableLiveData<Feed>()
    val flowResult=MutableLiveData(false)


    private val clientListener = object : Client.Listener() {

        override fun onOK(relay: Relay) {
            super.onOK(relay)
            flowResult.postValue(true)
        }

        override fun onNewEvent(event: Event, subscriptionId: String) {
            when (event.kind) {
                MetadataEvent.kind -> {
                    val metadataEvent = event as MetadataEvent
//                    Log.e("account--->", event.toJson())
                    if (pubKey != event.pubKey.toHex()) return
                    metadataEvent.contactMetaData?.let {
                        val userProfile = UserProfile(event.pubKey.toHex()).apply {
                            this.name = it.name
                            this.about = it.about
                            this.picture = it.picture
                            this.nip05 = it.nip05
                            this.display_name = it.display_name
                            this.banner = it.banner
                            this.website = it.website
                            this.lud16 = it.lud16
                        }
                        Log.e("account--->", "---->${userProfile.name}")
                        scope.launch {
                            NostrDB.getDatabase(MyApplication.getInstance())
                                .profileDao().insertUser(userProfile)
                        }
                    }

                }
                TextNoteEvent.kind -> {
                    val textEvent = event as TextNoteEvent

                    if (pubKey != event.pubKey.toHex()) return

                    var feed = nostr.postr.db.FeedItem(
                        textEvent.id.toString(),
                        textEvent.pubKey.toHex(),
                        textEvent.createdAt,
                        textEvent.content
                    )
                    feedLiveData.postValue(Feed(feed, user.value))
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


    fun reqProfile(pubKey: String) {
        scope.launch {

            NostrDB.getDatabase(MyApplication._instance)
                .profileDao().getUserInfo(pubKey)?.let {
                    user.postValue(it)
                }
            Client.subscribe(clientListener)
            val filter = JsonFilter(
                kinds = mutableListOf(0).apply {
                    this.add(1)
                },
//                since=System.currentTimeMillis()/1000-3*24*3600,
                authors = mutableListOf(pubKey)
            )
            Client.requestAndWatch(filters = mutableListOf(filter))
        }
    }


    fun addFlow(pubKey: String) {

        val list = mutableListOf<Contact>()
        list.add(Contact(pubKeyHex = pubKey, relayUri = null))

        val relayUser = mutableMapOf<String, ContactListEvent.ReadWrite>()
        Constants.defaultRelays.filter {
            it.enable
        }.forEach {
            relayUser[it.url]=ContactListEvent.ReadWrite(it.read, it.write)
        }
        val event = ContactListEvent.create(list, relayUser, privateKey = AccountManger.getPrivateKey())
        Client.send(event)
    }

    override fun onCleared() {
        super.onCleared()
        Client.unsubscribe(listener = clientListener)
    }

}
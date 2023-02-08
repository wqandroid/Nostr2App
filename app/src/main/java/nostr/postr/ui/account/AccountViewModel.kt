package nostr.postr.ui.account

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

class AccountViewModel : ViewModel() {

    private val scope = CoroutineScope(Job() + Dispatchers.IO)


    val user = MutableLiveData<UserProfile>()

    private val blockList = mutableListOf<String>()

    val pubKey = lazy {
        AccountManger.getPublicKey()
    }


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
                    Log.e("account--->", event.toJson())

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

                            if (userProfile.pubkey == pubKey.value){
                                NostrDB.getDatabase(MyApplication.getInstance())
                                    .profileDao().insertUser(userProfile)
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


    fun reqProfile(pubKey: String) {
        scope.launch {
            Client.subscribe(clientListener)
            val filter = JsonFilter(
                kinds = mutableListOf(0),
                authors = mutableListOf(pubKey))
            Client.connect()
            Client.requestAndWatch(filters = mutableListOf(filter))
        }
    }


    fun loadSelfProfile() {
        if (AccountManger.isLogin()) {
            scope.launch {
                val selfPubKey = AccountManger.getPublicKey()?.let {
                    NostrDB.getDatabase(MyApplication._instance)
                        .profileDao().getUserInfo(it)?.let {
                            user.postValue(it)
                        }
                }
                Log.e("account", "自己的公钥：$selfPubKey")
            }
        }

    }

    override fun onCleared() {
        super.onCleared()
        Client.unsubscribe(listener = clientListener)
    }

}
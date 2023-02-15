package nostr.postr.ui.user

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.acinq.secp256k1.Hex
import kotlinx.coroutines.*
import nostr.postr.*
import nostr.postr.core.AccountManger
import nostr.postr.core.WSClient
import nostr.postr.core.WsViewModel
import nostr.postr.db.BlockUser
import nostr.postr.db.FollowUserKey
import nostr.postr.db.NostrDB
import nostr.postr.db.UserProfile
import nostr.postr.events.*
import nostr.postr.ui.dashboard.FollowInfo
import nostr.postr.ui.feed.Feed
import nostr.postr.util.MD5
import java.util.*

class UserViewModel : WsViewModel() {


    lateinit var pubKey: String

    val user = MutableLiveData<UserProfile>()
    val feedLiveData = MutableLiveData<Feed>()
    val flowResult = MutableLiveData(false)


    val contactMetaDataLiveData = MutableLiveData<ContactMetaData>()

    private val subID = "user_info_detail_${UUID.randomUUID().toString().substring(0..5)}"


    private val idSet = hashSetOf<String>()


    override fun onRecMetadataEvent(subscriptionId: String, metadataEvent: MetadataEvent) {
        super.onRecMetadataEvent(subscriptionId, metadataEvent)
        if (subID != subscriptionId) return
        Log.e("account--->", metadataEvent.toJson())
        if (pubKey != metadataEvent.pubKey.toHex()) return
        metadataEvent.contactMetaData?.let {

            contactMetaDataLiveData.postValue(it)

            val userProfile = UserProfile(metadataEvent.pubKey.toHex()).apply {
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


    override fun onRecTextNoteEvent(subscriptionId: String, event: TextNoteEvent) {
        super.onRecTextNoteEvent(subscriptionId, event)
        if (subID != subscriptionId) return
        if (!idSet.contains(event.id.toHex())) {
            Log.e("textEvent--->${event.id.toHex()}", event.toJson())
            idSet.add(event.id.toHex())
            var feed = nostr.postr.db.FeedItem(
                event.id.toString(),
                event.pubKey.toHex(),
                event.createdAt,
                event.content,
                event.tag2JsonString()
            )
            feedLiveData.postValue(Feed(feed, user.value))
        }
    }

    val followList = MutableLiveData<List<FollowInfo>>()
    private val tempFollowList = mutableListOf<FollowInfo>()
    override fun onRecContactListEvent(subscriptionId: String, event: ContactListEvent) {
        super.onRecContactListEvent(subscriptionId, event)

        if (subscriptionId == subID) {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    tempFollowList.addAll(event.follows
                        .map {
                            FollowInfo(
                                it.pubKeyHex,
                                it.relayUri,
                                null,
                            )
                        })
                    followList.postValue(tempFollowList)
                }
            }
        }
    }


    fun reqProfile(pubKey: String) {
        scope.launch {

            NostrDB.getDatabase(MyApplication._instance)
                .profileDao().getUserInfo(pubKey)?.let {
                    user.postValue(it)
                }

            val filters = mutableListOf(
                JsonFilter(
                    authors = mutableListOf(pubKey),
                    kinds = mutableListOf(0, 3),
                    limit = 1
                ),
//                JsonFilter(
//                    kinds = mutableListOf(3),
//                    tags = mutableMapOf<String, List<String>>()
//                        .apply {
//                            this["p"] = listOf(pubKey)
//                        }
//                ),
                JsonFilter(
                    authors = mutableListOf(pubKey),
                    kinds = mutableListOf(1),
                    limit = 60
                )
            )
            wsClient.value.requestAndWatch(subID, filters = filters)
        }
    }


    fun addFlow(pubKey: String, follow: MutableList<String>) {


        scope.launch {

            if (follow.contains(pubKey)) {
                //unfollow
                follow.remove(pubKey)
            } else {
                follow.add(pubKey)
            }

            val list = follow.map { Contact(it, null) }

//        list.add(Contact(pubKeyHex = pubKey, relayUri = null))

            val relayUser = mutableMapOf<String, ContactListEvent.ReadWrite>()
            Constants.defaultRelays.filter {
                it.enable
            }.forEach {
                relayUser[it.url] = ContactListEvent.ReadWrite(it.read, it.write)
            }
            val event =
                ContactListEvent.create(list, relayUser, privateKey = AccountManger.getPrivateKey())
            wsClient.value.send(event)
        }
    }

    override fun onCleared() {
        wsClient.value.close(subID)
        super.onCleared()

    }

}
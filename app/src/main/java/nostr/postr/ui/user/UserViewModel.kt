package nostr.postr.ui.user

import android.util.Log
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import nostr.postr.*
import nostr.postr.core.AccountManger
import nostr.postr.core.WsViewModel
import nostr.postr.db.NostrDB
import nostr.postr.db.UserProfile
import nostr.postr.events.*
import nostr.postr.ui.feed.Feed
import java.util.*

class UserViewModel : WsViewModel() {


    lateinit var pubKey: String

    val user = MutableLiveData<UserProfile>()
    val feedLiveData = MutableLiveData<Feed>()
    val flowResult = MutableLiveData(false)


    private val subID = "user_info_detail_${UUID.randomUUID().toString().substring(0..5)}"

    private val subUserFollows = "user_followers_${UUID.randomUUID().toString().substring(0..5)}"
    private val subUserFollowers = "user_followers_${UUID.randomUUID().toString().substring(0..5)}"

    private val idSet = hashSetOf<String>()


    val followList = MutableLiveData<MutableSet<String>>()
    val followersList = MutableLiveData<MutableSet<String>>()



    override fun onRecMetadataEvent(subscriptionId: String, metadataEvent: MetadataEvent) {
        super.onRecMetadataEvent(subscriptionId, metadataEvent)
        if (subID != subscriptionId) return
        Log.e("account--->", metadataEvent.toJson())
        if (pubKey != metadataEvent.pubKey.toHex()) return
        metadataEvent.contactMetaData?.let {

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
                event.id.toHex(),
                event.pubKey.toHex(),
                event.createdAt,
                event.content,
                event.tag2JsonString()
            )
            feedLiveData.postValue(Feed(feed, user.value))
        }
    }


    override fun onRecContactListEvent(subscriptionId: String, event: ContactListEvent) {
        super.onRecContactListEvent(subscriptionId, event)



        if (subscriptionId == subUserFollows) {

            val set = followList.value ?: mutableSetOf()

            Log.e("关注列表","data:${event.follows.size}---${event.relayUse?.size}")

            event.follows.forEach {
                set.add(it.pubKeyHex)
            }

            followList.postValue(set)
        } else if (subscriptionId == subUserFollowers) {
            //粉丝
            Log.e("关注列表","data222:${event.follows.size}---${event.relayUse?.size}")
            val set = followersList.value ?: mutableSetOf()

            event.follows.forEach {
                set.add(it.pubKeyHex)
            }

            followersList.postValue(set)
        }
//        Log.e("关注列表", "$subscriptionId--->${followList.value?.size}----${followersList.value?.size}")
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
                    kinds = mutableListOf(0,2),
                ),
                JsonFilter(
                    authors = mutableListOf(pubKey),
                    kinds = mutableListOf(1),
                    limit = 60
                )
            )
            wsClient.value.requestAndWatch(subID, filters = filters)
        }
        reqFollowers()
    }

    //多少人关注了Ta
     fun reqFollowers() {
        wsClient.value.requestAndWatch(
            subUserFollows, mutableListOf(
                JsonFilter(
                    authors = mutableListOf(pubKey),
                    kinds = mutableListOf(3),
                    limit = 1
                )
            )
        )

        wsClient.value.requestAndWatch(
            subUserFollowers, mutableListOf(
                JsonFilter(
                    kinds = mutableListOf(3),
//                    authors = mutableListOf(pubKey),
                    tags = mutableMapOf<String, List<String>>()
                        .apply {
                            this["e"] = listOf(pubKey)
                        },
                    limit = 1
                )
            )
        )
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
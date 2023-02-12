package nostr.postr.ui.home

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import nostr.postr.*
import nostr.postr.core.AccountManger
import nostr.postr.core.WSClient
import nostr.postr.core.WsViewModel
import nostr.postr.db.FollowUserKey
import nostr.postr.db.NostrDB
import nostr.postr.db.UserProfile
import nostr.postr.events.*
import nostr.postr.ui.feed.Feed
import java.util.*

class HomeViewModel : WsViewModel() {

    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    //自己的用户信息
    var pubKey: String = ""

    private val mainSubscriptionId = "mainUser_${UUID.randomUUID().toString().substring(0..5)}"

    private val followSubscriptionId="follow_feed${UUID.randomUUID().toString().substring(0..5)}"

    val feedCountLiveData = MutableLiveData<Int>(0)
    val feedLiveData = MutableLiveData<List<Feed>>()


    private var count = 0
    private var profileList = mutableListOf<String>()

    private val setIds= mutableSetOf<String>()

    override fun onRecPrivateDmEvent(subscriptionId: String, event: PrivateDmEvent) {
        super.onRecPrivateDmEvent(subscriptionId, event)
        if (subscriptionId != mainSubscriptionId) return
        var even = event as PrivateDmEvent
//                    var pubkeyToUse = event.pubKey
//                    val recepientPK = event.recipientPubKey

        if (event.pubKey.toHex() == pubKey) {
//                        Log.e("msg_from", "self --message")
//                        Log.e(
//                            "msg_from--->",
//                            "${subscriptionId}-->\n私信:${
//                                even.plainContent(
//                                    AccountManger.getPrivateKey(),
//                                    even.recipientPubKey!!
//                                )
//                            }"
//                        )
        } else {
//                        Log.e(
//                            "msg_from--->",
//                            "${subscriptionId}-->\n私信:${
//                                even.plainContent(
//                                    AccountManger.getPrivateKey(),
//                                    even.pubKey
//                                )
//                            }"
//                        )

        }
//

    }

    override fun onRecContactListEvent(subscriptionId: String, event: ContactListEvent) {
        super.onRecContactListEvent(subscriptionId, event)
        if (subscriptionId != mainSubscriptionId) return
        scope.launch {

            Log.e("onRecContactListEvent--->", event.toJson())
            event.follows.forEach {
                NostrDB.getDatabase(MyApplication._instance)
                    .followUserKeyDao()
                    .insertFollow(FollowUserKey(it.pubKeyHex, status = 1).apply {
                        this.name = it.relayUri
                    })
            }

            event.follows.map { it.pubKeyHex }.also { stringList ->
                stringList.toMutableList().also {
                    AccountManger.follows=it
                    reqFollowFeed(it)
                }
            }
        }
    }

    override fun onRecMetadataEvent(subscriptionId: String, event: MetadataEvent) {
        super.onRecMetadataEvent(subscriptionId, event)
        if (event.pubKey.toHex() == pubKey) {
            Log.e("MetadataEvent--->", event.toJson())
        }

        event.contactMetaData.let {
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
            //                        Log.e("MetadataEvent--->", "---->${userProfile.name}")
            scope.launch {
                NostrDB.getDatabase(MyApplication.getInstance())
                    .profileDao().insertUser(userProfile)
            }
        }

    }

    override fun onRecTextNoteEvent(subscriptionId: String, textEvent: TextNoteEvent) {
        super.onRecTextNoteEvent(subscriptionId, textEvent)
//        if (!followSet.map { it.pubkey }.contains(textEvent.pubKey.toHex())){
//                        Log.e("no_follow", "拦截---->${textEvent.content}")
//                        return
//                    }
//                    if (subscriptionId != mainSubscriptionId) return
        scope.launch {

//                        if (blockList.contains(textEvent.pubKey.toHex()) || blockContentList.contains(
//                                MD5.md5(textEvent.content)
//                            )
//                        ) return@launch

            if (!textEvent.isFeed() || subscriptionId != followSubscriptionId) return@launch

            if (setIds.contains(textEvent.id.toHex()))return@launch
            setIds.add(textEvent.id.toHex())
            var feed = nostr.postr.db.FeedItem(
                textEvent.id.toString(),
                textEvent.pubKey.toHex(),
                textEvent.createdAt,
                textEvent.content
            )
            NostrDB.getDatabase(MyApplication.getInstance())
                .feedDao().insertFeed(feed)
            count++
//                        if (count % 20 == 0) {
//                            loadFeedFromDB()
//                        }
            Log.e("TextNoteEvent--->$count", textEvent.toJson())

            withContext(Dispatchers.Main) {
                feedCountLiveData.value = count
            }
            val userProfile = NostrDB.getDatabase(MyApplication._instance).profileDao()
                .getUserInfo(feed.pubkey)
            if (userProfile == null) {
                profileList.add(feed.pubkey)
                if (profileList.size >= 40) {
                    profileList.clear()
                    reqProfile(profileList)
                }
            }
        }
    }


    fun reqMainUserInfo() {

        if (!AccountManger.isLogin()) return
        pubKey = AccountManger.getPublicKey()
        val map = mutableMapOf<String, List<String>>()
            .apply {
                this["p"] = listOf(pubKey)
            }
        val filter = mutableListOf(

            JsonFilter(
                authors = mutableListOf(pubKey),
                kinds = mutableListOf(0),
                limit = 1
            ),
            JsonFilter(
                authors = mutableListOf(pubKey),
                kinds = mutableListOf(3),
                limit = 1
            ),
//            JsonFilter(
//                kinds = mutableListOf(1),//6
//                limit = 20,
//                since = System.currentTimeMillis()/1000
//            ),
//            JsonFilter(
//                authors = listOf(AccountManger.getPublicKey()),
//                kinds = mutableListOf(1984)
//            )
        )
        wsClient.value.requestAndWatch(subscriptionId = mainSubscriptionId, filters = filter)
    }

    private fun reqFollowFeed(list: MutableList<String>) {
//        val map = mutableMapOf<String, List<String>>()
//        map["p"] = list
        list.add(AccountManger.getPublicKey())
        wsClient.value.requestAndWatch(
            followSubscriptionId,
            filters = mutableListOf(
                JsonFilter(
                    kinds = listOf(TextNoteEvent.kind),
//                    tags = map,
                    authors = list,
                    since = NostrDB.getDatabase(MyApplication._instance)
                        .feedDao().getLast()?.created_at ?: (System.currentTimeMillis() / 1000),
                    limit = 200
                )
            )
        )
    }


    fun reqProfile(list: List<String>) {
        scope.launch {
            val filter = JsonFilter(
                kinds = mutableListOf(0),
//                since = 1675748168,
//                limit = 20,
                authors = list
            )
            val profileSubscriptionId = "profile_${UUID.randomUUID().toString().substring(0..5)}"
            wsClient.value.requestAndWatch(
                subscriptionId = profileSubscriptionId,
                filters = mutableListOf(filter)
            )
        }
    }


    fun loadFeedFromDB() {

        scope.launch {
            val list = NostrDB.getDatabase(MyApplication._instance)
                .feedDao().getAll()

            val feedList = mutableListOf<Feed>()
            list.forEach {
                //拉黑的不展示
                val userProfile = NostrDB.getDatabase(MyApplication._instance).profileDao()
                    .getUserInfo(it.pubkey)
                feedList.add(Feed(it, userProfile))
            }


            withContext(Dispatchers.Main) {
                feedLiveData.value = feedList
                feedCountLiveData.value = 0
                count = 0
            }

            //req profile
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
        wsClient.value.close(mainSubscriptionId)
        super.onCleared()
    }

}
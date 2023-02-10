package nostr.postr.ui.feed

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
import nostr.postr.ui.dashboard.FollowInfo
import nostr.postr.util.MD5
import java.util.*

class FeedViewModel : ViewModel() {

    private val scope = CoroutineScope(Job() + Dispatchers.IO)


    private var filterList = mutableListOf("棋牌", "S9", "广告机器人", "全球华人", "抖音快手", "Amethyst", "您的Pi")

    val feedCountLiveData = MutableLiveData<Int>(0)
    val feedLiveData = MutableLiveData<List<Feed>>()

    val followList = MutableLiveData<List<FollowInfo>>()

    private val blockList = mutableListOf<String>()
    private val blockContentList = mutableListOf<String>()

    private var count = 0
    private var profileList = mutableListOf<String>()

    private var followSet = mutableSetOf<FollowInfo>()

    private var profileSubscriptionId: String = ""
    private val mainSubscriptionId = "mainUser_${UUID.randomUUID().toString().substring(0..5)}"

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
                ContactListEvent.kind -> {
                    val event = event as ContactListEvent
                    Log.e(
                        "ContactListEvent--->",
                        "${subscriptionId}-->\n${Thread.currentThread().name}\n--->$event"
                    )
//                    if (subscriptionId == mainSubscriptionId) {
                    scope.launch {
                        event.follows.forEach {

                            val profile =
                                NostrDB.getDatabase(MyApplication._instance).profileDao()
                                    .getUserInfo(it.pubKeyHex)

                            followSet.add(
                                FollowInfo(
                                    it.pubKeyHex,
                                    it.relayUri,
                                    profile
                                )
                            )
                            followList.postValue(followSet.toList())
                            if (profile == null) {
                                reqProfile(listOf(it.pubKeyHex))
                            }

                        }
                        reqFollowFeed(event.follows.map { it.pubKeyHex })
                    }

                }
                MetadataEvent.kind -> {
                    val metadataEvent = event as MetadataEvent
//                    Log.e("MetadataEvent--->", event.toJson())

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
//                        Log.e("MetadataEvent--->", "---->${userProfile.name}")
                        scope.launch {
                            NostrDB.getDatabase(MyApplication.getInstance())
                                .profileDao().insertUser(userProfile)
                        }
                    }
                }
                TextNoteEvent.kind -> {

                    val textEvent = event as TextNoteEvent

                    if (blockList.contains(textEvent.pubKey.toHex()) || blockContentList.contains(
                            MD5.md5(textEvent.content)
                        )
                    ) return

                    if (filterList.any { textEvent.content.contains(it) }) {
                        Log.e("block", "拦截---->${textEvent.content}")
                        return
                    }
                    scope.launch {

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
                        feedCountLiveData.postValue(count)
                        val userProfile = NostrDB.getDatabase(MyApplication._instance).profileDao()
                            .getUserInfo(feed.pubkey)
                        if (userProfile == null) {
                            profileList.add(feed.pubkey)
                            if (profileList.size >= 40) {
                                reqProfile(profileList)
                                profileList.clear()
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
//            Log.e("ERROR", "Relay ${relay.url}: ${error.message}")
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


    fun addBlock(pubKey: String, contentMD5: String) {
        scope.launch {

            NostrDB.getDatabase(MyApplication._instance)
                .blockUserDao().insertBlockUser(BlockUser(pubKey).apply {
                    this.contentMD5 = MD5.md5(contentMD5)
                })
//            loadBlockUser()

            blockList.add(pubKey)
            blockContentList.add(contentMD5)

            //删除该用户下面的所有动态
            NostrDB.getDatabase(MyApplication._instance)
                .feedDao().getFeedByPublicKey(pubKey)
                .forEach {
                    NostrDB.getDatabase(MyApplication._instance).feedDao()
                        .delete(it)
                }
            //重新加载 动态
            loadFeedFromDB()
        }
    }

    fun loadBlockUser() {
        scope.launch {
            blockList.clear()
            blockContentList.clear()
            val list = NostrDB.getDatabase(MyApplication._instance)
                .blockUserDao().getAllBlock()

            blockList.addAll(
                list.map { it.pubkey }
            )
            list.forEach {
                if (it.contentMD5.isNullOrEmpty()) {
                    blockContentList.add(it.contentMD5!!)
                }
            }
        }
    }

    fun reqFeed() {

        scope.launch {
            val sinceTime =
//                NostrDB.getDatabase(MyApplication._instance)
//                .feedDao().getLast()?.created_at ?:
                System.currentTimeMillis() / 1000

            Client.subscribe(clientListener)

//            val filter = JsonFilter(
//                since = sinceTime,
//                kinds = mutableListOf(1, 2),
//                limit = 20,
//            )
//
//            Client.reqSend(filters = mutableListOf(filter))

//            Client.requestAndWatch(
//                filters = mutableListOf(
//                    JsonFilter(
//                        kinds = listOf(ContactListEvent.kind),
//                        authors = listOf(AccountManger.getPublicKey())
//                    )
//                )
//            )
        }


    }

    fun reqFollowFeed(list: List<String>) {

        val map = mutableMapOf<String, List<String>>()
        map["p"] = list
        Client.requestAndWatch(
            filters = mutableListOf(
                JsonFilter(
                    kinds = listOf(TextNoteEvent.kind),
                    tags = map,
                    limit = 200
                )
            )
        )
    }


    fun reqMainUserInfo() {
        //    ["REQ","mainUser 8563",{"#p":["9e9764b9415b2ff0e24733e3fe685922e79b4812c6ad412a9e05447153f05cbb"],"kinds":[1,3,4],"limit":5000},{"authors":["9e9764b9415b2ff0e24733e3fe685922e79b4812c6ad412a9e05447153f05cbb"],"kinds":[0,1,2,3,4]}]

        if (!AccountManger.isLogin()) return
        Client.subscribe(clientListener)
        val pubKey = AccountManger.getPublicKey()
        val map = mutableMapOf<String, List<String>>()
            .apply {
                this["p"] = listOf(pubKey)
            }
        val filter = mutableListOf(
            JsonFilter(
                kinds = mutableListOf(1, 3, 4),
                tags = map
            ),
            JsonFilter(
                kinds = mutableListOf(0, 1, 2, 3, 4),
                authors = listOf(AccountManger.getPublicKey()),
            )
        )
        Client.requestAndWatch(subscriptionId = mainSubscriptionId, filters = filter)
    }


    fun stopSubFeed() {
        Client.unsubscribe(listener = clientListener)
    }

    fun reqProfile(list: List<String>) {
        scope.launch {
            Client.subscribe(clientListener)
            val filter = JsonFilter(
                kinds = mutableListOf(0),
                since = 1675748168,
                limit = 20,
                authors = list
            )
            profileSubscriptionId = "profile_${UUID.randomUUID().toString().substring(0..5)}"
            Client.requestAndWatch(
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
                if (!blockList.contains(it.pubkey) && !blockContentList.contains(MD5.md5(it.content))) {
                    val userProfile = NostrDB.getDatabase(MyApplication._instance).profileDao()
                        .getUserInfo(it.pubkey)
                    feedList.add(Feed(it, userProfile))
                }
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
        super.onCleared()
        stopSubFeed()
    }

}
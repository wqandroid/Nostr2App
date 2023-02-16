package nostr.postr.ui.home

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.*
import nostr.postr.*
import nostr.postr.core.AccountManger
import nostr.postr.core.WsViewModel
import nostr.postr.db.*
import nostr.postr.events.*
import nostr.postr.ui.feed.Feed
import java.util.*

class HomeViewModel : WsViewModel() {


    //自己的用户信息
    var pubKey: String = ""

    private val mainSubscriptionId = "mainUser_${UUID.randomUUID().toString().substring(0..5)}"

    private val followSubscriptionId = "follow_feed${UUID.randomUUID().toString().substring(0..5)}"

    val feedCountLiveData = MutableLiveData<Int>(0)
    val feedLiveData = MutableLiveData<List<Feed>>()


    private var count = 0
    private var profileList = mutableListOf<String>()

    private val setIds = mutableSetOf<String>()

//    private val map = mutableMapOf<String, MutableList<PrivateDmEvent>>()

    override fun onRecPrivateDmEvent(subscriptionId: String, event: PrivateDmEvent) {
        super.onRecPrivateDmEvent(subscriptionId, event)
//        if (subscriptionId != mainSubscriptionId) return
        if (event.pubKey.toHex() == pubKey || event.recipientPubKey?.toHex() == pubKey) {

            val isSelf = event.pubKey.toHex() == pubKey
            val chatRoomID =
                if (isSelf) {
                    "${event.recipientPubKey?.toHex()}-${event.pubKey.toHex()}"
                } else {
                    "${event.pubKey.toHex()}-${event.recipientPubKey?.toHex()}"
                }

            val content = if (isSelf) {
                event.plainContent(
                    AccountManger.getPrivateKey(),
                    event.recipientPubKey ?: event.pubKey
                )
            } else {
                event.plainContent(
                    AccountManger.getPrivateKey(),
                    event.pubKey
                )
            } ?: return

            val sendTo =
                if (isSelf) event.recipientPubKey?.toHex() ?: pubKey else event.pubKey.toHex()

            scope.launch {
                var room = getDB().chatDao().getChatRoomById(chatRoomID)
                if (room == null) {
                    room = ChatRoom(
                        roomId = chatRoomID,
                        sendTo,
                        content,
                        event.createdAt,
                        true
                    )
                } else {
                    room.hasUnread = true
                    room.content = content
                    room.lastUpdate = event.createdAt
                }
                getDB().chatDao().createChatRoom(room)
                ChatMessage(
                    event.id.toHex(),
                    chatRoomID,
                    content,
                    event.createdAt,
                    if (isSelf) pubKey else sendTo,
                    success = true,
                ).also {
                    getDB()
                        .chatDao().insertMsg(it)
                    Log.e(
                        "msg_room--->", "${chatRoomID}\n:-->私信群组:$isSelf   ${it.content}"
                    )
                }
            }


        } else {
            Log.e(
                "msg_room--->", "非我的消息？？??${event.toJson()}"
            )
        }
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
                    AccountManger.follows = it
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

            if (subscriptionId != followSubscriptionId) return@launch

            if (setIds.contains(textEvent.id.toHex())) return@launch
            setIds.add(textEvent.id.toHex())
            var feed = FeedItem(
                textEvent.id.toString(),
                textEvent.pubKey.toHex(),
                textEvent.createdAt,
                textEvent.content,
                textEvent.tag2JsonString()
            )
            NostrDB.getDatabase(MyApplication.getInstance())
                .feedDao().insertFeed(feed)
            count++
            if (count % 2 == 0) {
                loadFeedFromDB()
            }
//            Log.e("TextNoteEvent--->$count", textEvent.toJson())
//            withContext(Dispatchers.Main) {
//                feedCountLiveData.value = count
//            }
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


    fun reqGlobalFeed() {

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
            JsonFilter(
//from me
                kinds = mutableListOf(4),//6
                authors = mutableListOf(pubKey),
                since = NostrDB.getDatabase(MyApplication._instance)
                    .chatDao().getLast()?.createAt ?: (System.currentTimeMillis() / 10000),
            ),
            JsonFilter(//to me
                kinds = mutableListOf(4),//6
                since = NostrDB.getDatabase(MyApplication._instance)
                    .chatDao().getLast()?.createAt ?: (System.currentTimeMillis() / 10000),
                tags = map
            ),
//            JsonFilter(
//                authors = listOf(AccountManger.getPublicKey()),
//                kinds = mutableListOf(1984)
//            )
        )
        wsClient.value.requestAndWatch(subscriptionId = mainSubscriptionId, filters = filter)
    }

    fun subChat() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = NostrDB.getDatabase(MyApplication._instance)
                .chatDao().getTotalChatRoom().map {
                    it.sendTo
                }
            wsClient.value.requestAndWatch(
                subscriptionId = "req_chat_${getRand5()}", filters = mutableListOf(
                    JsonFilter(
                        kinds = mutableListOf(4),//6
                        authors = list,
                    )
                )
            )
        }

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
                        .feedDao().getLast()?.created_at
                        ?: (System.currentTimeMillis() / 10000),
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
            val profileSubscriptionId =
                "profile_${UUID.randomUUID().toString().substring(0..5)}"
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
package nostr.postr.ui.feed.global

import android.util.Log
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.launch
import nostr.postr.JsonFilter
import nostr.postr.MyApplication
import nostr.postr.core.WsViewModel
import nostr.postr.db.FeedItem
import nostr.postr.db.NostrDB
import nostr.postr.db.UserProfile
import nostr.postr.events.MetadataEvent
import nostr.postr.events.TextNoteEvent
import nostr.postr.toHex
import nostr.postr.ui.feed.Feed
import java.util.*

class GlobalFeedViewModel : WsViewModel() {


    private val mainSubscriptionId = "global_feed${UUID.randomUUID().toString().substring(0..5)}"


    val feedLiveData = MutableLiveData<Feed>()


    //this is filter words
    private val tempList = mutableListOf<String>()

    private val userMap = mutableMapOf<String, UserProfile>()

    private val set= mutableSetOf<String>("同城","服务好","破处直播","交流群","推广","成人抖阴","小机器人","腾讯","BELONS")

    private val blocKey= setOf<String>("35e8d69e112de24d753014477d92b2634de4f7657a3ea1f5dac6dd812ce4dfeb")


    override fun onRecMetadataEvent(subscriptionId: String, event: MetadataEvent) {
        super.onRecMetadataEvent(subscriptionId, event)

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
            scope.launch {
                NostrDB.getDatabase(MyApplication.getInstance())
                    .profileDao().insertUser(userProfile)
            }
            userMap[userProfile.pubkey] = userProfile
        }

    }

    override fun onRecTextNoteEvent(subscriptionId: String, textEvent: TextNoteEvent) {
        super.onRecTextNoteEvent(subscriptionId, textEvent)
        if (subscriptionId != mainSubscriptionId) return
        if (blocKey.contains(textEvent.pubKey.toHex()))return
        scope.launch {
//                        if (blockList.contains(textEvent.pubKey.toHex()) || blockContentList.contains(
//                                MD5.md5(textEvent.content)
//                            )
//                        ) return@launch



            val startChart = if (textEvent.content.length > 7) textEvent.content.substring(
                0,
                6
            ) else textEvent.content
            if (!tempList.contains(startChart)) {
                tempList.add(startChart)

                val laji=set.any { textEvent.content.contains(it) }
                if (laji)return@launch
                var feed = FeedItem(
                    textEvent.id.toString(),
                    textEvent.pubKey.toHex(),
                    textEvent.createdAt,
                    textEvent.content,
                    textEvent.tag2JsonString()
                )
                postFeedList(feed)
            } else {
//                Log.e("TextNoteEvent--->${tempList.size}", textEvent.toJson())
            }
        }
    }


    private fun postFeedList(item: FeedItem) {
        feedLiveData.postValue(Feed(feedItem = item, userMap[item.pubkey]))
    }



    fun reqGlobalFeed() {
        wsClient.value.requestAndWatch(
            mainSubscriptionId,
            filters = mutableListOf(
                JsonFilter(
                    kinds = listOf(TextNoteEvent.kind),
                    since = (System.currentTimeMillis() / 1000),
                    limit = 20
                )
            )
        )
    }


    private suspend fun reqProfile(list: List<String>) {
        val filter = JsonFilter(
            kinds = mutableListOf(0),
            authors = list
        )
        val profileSubscriptionId = "profile_${UUID.randomUUID().toString().substring(0..5)}"
        wsClient.value.requestAndWatch(
            subscriptionId = profileSubscriptionId,
            filters = mutableListOf(filter)
        )
    }


    override fun onCleared() {
        wsClient.value.close(mainSubscriptionId)
        super.onCleared()
    }

}
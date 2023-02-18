package nostr.postr.ui.feed

import android.util.Log
import fr.acinq.secp256k1.Hex
import kotlinx.coroutines.*
import nostr.postr.*
import nostr.postr.core.WsViewModel
import nostr.postr.db.*
import nostr.postr.events.*
import java.util.*

class FeedDetailViewModel : WsViewModel() {


    //自己的用户信息
    var pubKey: String = ""

    private val mainSubscriptionId = "feed_detail_${UUID.randomUUID().toString().substring(0..5)}"

    private val followSubscriptionId = "follow_feed${UUID.randomUUID().toString().substring(0..5)}"


    private var profileList = mutableListOf<String>()

    private val setIds = mutableSetOf<String>()


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

        }

    }

    override fun onRecTextNoteEvent(subscriptionId: String, textEvent: TextNoteEvent) {
        super.onRecTextNoteEvent(subscriptionId, textEvent)
        scope.launch {
            if (subscriptionId != mainSubscriptionId) return@launch
            Log.e("onRecTextNoteEvent--->", textEvent.toJson())
            if (setIds.contains(textEvent.id.toHex())) return@launch
            setIds.add(textEvent.id.toHex())
            var feed = FeedItem(
                textEvent.id.toString(),
                textEvent.pubKey.toHex(),
                textEvent.createdAt,
                textEvent.content,
                textEvent.tag2JsonString()
            )

        }
    }

    fun reqFeedInfo(id: String="0433d88da8fa203b20932cbdd73345a34b2bcff2c49e125087d67a2b4299ac42") {
        val map = mutableMapOf<String, List<String>>()
            .apply {
                this["e"] = listOf(id)
//                this["p"] = listOf(id)
            }
        val filter = mutableListOf(
            JsonFilter(
                kinds = mutableListOf(1),
                tags = map
            ),
        )
        wsClient.value.requestAndWatch(subscriptionId = mainSubscriptionId, filters = filter)
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


    override fun onCleared() {
        wsClient.value.close(mainSubscriptionId)
        super.onCleared()
    }

}
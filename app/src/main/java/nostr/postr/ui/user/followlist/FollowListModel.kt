package nostr.postr.ui.user.followlist

import android.util.Log
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import nostr.postr.*
import nostr.postr.core.AccountManger
import nostr.postr.core.WsViewModel
import nostr.postr.db.NostrDB
import nostr.postr.db.UserProfile
import nostr.postr.events.*
import java.util.*

class FollowListModel : WsViewModel() {


    lateinit var pubKey: String

    val user = MutableLiveData<UserProfile>()
    val flowResult = MutableLiveData(false)

    private val subID = "user_info_detail_${UUID.randomUUID().toString().substring(0..5)}"

    private val subUserFollows = "user_followers_${UUID.randomUUID().toString().substring(0..5)}"

    private val idSet = hashSetOf<String>()

    val followUserList = MutableLiveData<MutableList<FollowInfo>>()

    private val followList = mutableSetOf<String>()

    private val userMap = mutableMapOf<String, UserProfile>()

    override fun onRecMetadataEvent(subscriptionId: String, metadataEvent: MetadataEvent) {
        super.onRecMetadataEvent(subscriptionId, metadataEvent)
        if (subID != subscriptionId) return
        Log.e("account--->", metadataEvent.toJson())
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
            userMap[userProfile.pubkey] = userProfile
            updateUserProfileUI()
        }
    }


    override fun onRecContactListEvent(subscriptionId: String, event: ContactListEvent) {
        super.onRecContactListEvent(subscriptionId, event)

        if (subscriptionId == subUserFollows) {
            Log.e("关注列表", "data:${event.follows.size}---${event.relayUse?.size}")
            val subProfile = mutableListOf<String>()
            val list = followUserList.value ?: mutableListOf()
            var isChange = false
            event.follows.forEach {
                if (!followList.contains(it.pubKeyHex)) {
                    followList.add(it.pubKeyHex)
                    val profile = NostrDB.getDatabase(MyApplication._instance)
                        .profileDao().getUserInfo2(it.pubKeyHex)
                    if (profile == null) {
                        subProfile.add(it.pubKeyHex)
                    }
                    list.add(FollowInfo(it.pubKeyHex, it.relayUri, profile))
                    isChange = true
                }
            }
            if (isChange) {
                followUserList.postValue(list)
                if (subProfile.isNotEmpty()){
                    reqProfile(subProfile)
                }
            }
        }
//        Log.e("关注列表", "$subscriptionId--->${followList.value?.size}----${followersList.value?.size}")
    }


    private fun updateUserProfileUI() {
        followUserList.value!!.forEach {
            it.userProfile = userMap[it.pubkey]
        }


    }

    private fun reqProfile(pubKey: List<String>) {
        scope.launch {
            val filters = mutableListOf(
                JsonFilter(
                    authors = pubKey,
                    kinds = mutableListOf(0),
                    limit = 1
                ),
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

    }


    fun addFlow(pubKey: String, follow: MutableList<String> =AccountManger.follows) {

        scope.launch {

            if (follow.contains(pubKey)) {
                //unfollow
                follow.remove(pubKey)
            } else {
                follow.add(pubKey)
            }

            val list = follow.map { Contact(it, null) }

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
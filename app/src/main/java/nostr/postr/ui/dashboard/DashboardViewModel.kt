package nostr.postr.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nostr.postr.JsonFilter
import nostr.postr.MyApplication
import nostr.postr.core.WsViewModel
import nostr.postr.db.NostrDB
import nostr.postr.events.ContactListEvent

class DashboardViewModel : WsViewModel() {

    val followList = MutableLiveData<List<FollowInfo>>()
    var mainSubscriptionId: String="Follow_list_${getRand5()}"
    override fun onRecContactListEvent(subscriptionId: String, event: ContactListEvent) {
        super.onRecContactListEvent(subscriptionId, event)

        if (subscriptionId == mainSubscriptionId) {

            viewModelScope.launch {

                withContext(Dispatchers.IO) {

                    var list = event.follows
                        .map {
                            FollowInfo(
                                it.pubKeyHex,
                                it.relayUri,
                                NostrDB.getDatabase(MyApplication._instance).profileDao()
                                    .getUserInfo2(it.pubKeyHex)
                            )
                        }
                    followList.postValue(list)
                }
            }
        }
    }

    fun subFollows(pubKey: String) {

        wsClient.value.requestAndWatch(
            mainSubscriptionId, mutableListOf(
                JsonFilter(
                    authors = mutableListOf(pubKey),
                    kinds = mutableListOf(3),
                    limit = 1
                )
            )
        )
    }

    override fun onCleared() {
        wsClient.value.close(mainSubscriptionId)
        super.onCleared()
    }

}
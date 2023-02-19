package nostr.postr.ui.feed

import android.util.Log
import androidx.lifecycle.MutableLiveData
import nostr.postr.Relay
import nostr.postr.core.AccountManger
import nostr.postr.core.WsViewModel
import nostr.postr.events.TextNoteEvent

class PublishViewModel : WsViewModel() {


    val sendLiveDta = MutableLiveData<Int>(0)


    override fun onOk(relay: Relay) {
        super.onOk(relay)
        Log.e("publish", "___${relay.url}")
        sendLiveDta.postValue(1)
    }


    fun sendPost(text: String, replyTos: List<String>?, mentions: List<String>?) {
        val event = TextNoteEvent.create(
            text,
            replyTos,
            mentions,
            AccountManger.getPrivateKey()
        )
        sendLiveDta.value = 0
        wsClient.value.send(event)
    }

}
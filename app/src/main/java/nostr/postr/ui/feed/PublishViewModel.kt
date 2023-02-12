package nostr.postr.ui.feed

import android.util.Log
import androidx.lifecycle.MutableLiveData
import nostr.postr.Relay
import nostr.postr.core.AccountManger
import nostr.postr.core.WsViewModel
import nostr.postr.events.TextNoteEvent

class PublishViewModel : WsViewModel() {


    val sendLiveDta= MutableLiveData<Int>(0)


    override fun onOk(relay: Relay) {
        super.onOk(relay)
        Log.e("publish","___${relay.url}")
        sendLiveDta.postValue(1)
    }


    fun connection(){
        wsClient.value.justConn()
    }


    fun sendPost(text:String) {
        val event = TextNoteEvent.create(
            text,
            mutableListOf(),
            mutableListOf(),
            AccountManger.getPrivateKey()
        )

        sendLiveDta.value=0
        wsClient.value.send(event)
    }

}
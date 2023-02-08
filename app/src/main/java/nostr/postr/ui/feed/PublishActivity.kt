package nostr.postr.ui.feed

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import nostr.postr.*
import nostr.postr.core.AccountManger
import nostr.postr.databinding.ActivityPublishBinding
import nostr.postr.events.Event
import nostr.postr.events.TextNoteEvent

class PublishActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPublishBinding

    private val listener = object : Client.Listener() {
        override fun onNewEvent(event: Event, subscriptionId: String) {
            if (event.pubKey.toHex() == AccountManger.getPublicKey()) {
                Log.e("publish", event.toJson())
                stop()
                finish()
            } else {
                Log.e("publish", "Why do we get this event? ${event.id}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPublishBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.mbtToolbar)


        binding.mbtSend.setOnClickListener {

            val event = TextNoteEvent.create(
                binding.edText.text.toString(),
                mutableListOf(),
                mutableListOf(),
                AccountManger.getPrivateKey()
            )

            Client.send(event)
        }

        binding.mbtToolbar.setNavigationOnClickListener {
            finish()
        }

    }


    private fun stop() {
        Client.unsubscribe(listener)
    }
}
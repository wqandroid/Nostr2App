package nostr.postr.ui.feed

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import nostr.postr.*
import nostr.postr.core.AccountManger
import nostr.postr.core.WSClient
import nostr.postr.databinding.ActivityPublishBinding
import nostr.postr.events.Event
import nostr.postr.events.TextNoteEvent

class PublishActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPublishBinding


    private val viewModel by viewModels<PublishViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPublishBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.mbtToolbar)


        viewModel.connection()
        binding.mbtSend.setOnClickListener {
            viewModel.sendPost(binding.edText.text.toString())
        }

        binding.mbtToolbar.setNavigationOnClickListener {
            finish()
        }

        viewModel.sendLiveDta.observe(this) {
            if (it == 1) {
                Toast.makeText(this, "发送成功", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    }

}
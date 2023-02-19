package nostr.postr.ui.feed

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import nostr.postr.*
import nostr.postr.core.AccountManger
import nostr.postr.core.BaseAct
import nostr.postr.core.WSClient
import nostr.postr.databinding.ActivityPublishBinding
import nostr.postr.events.Event
import nostr.postr.events.TextNoteEvent
import nostr.postr.ui.user.UserDetailActivity
import nostr.postr.util.UIUtils.makeVisibility
import nostr.postr.util.buildSpannableString

class PublishActivity : BaseAct() {

    private lateinit var binding: ActivityPublishBinding


    private val viewModel by viewModels<PublishViewModel>()

    private var feed: Feed? = null
    private var replyList: MutableList<String> = mutableListOf()
    private var mentionsList: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPublishBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.mbtToolbar)
        showBack()
        intent.getParcelableExtra<Feed>("feed")?.let {
            feed = it
            binding.tvReply.makeVisibility()
            binding.tvReply.buildSpannableString {
                addText("replying to ") {
                    setColor(MyApplication._instance.getColor(R.color.md_theme_onSurfaceVariant))
                }

                replyList = it.feedItem.getReplyTos().toMutableList()
                replyList.add(0, feed!!.feedItem.id)

                mentionsList = it.feedItem.getMentions().toMutableList()
                    .apply {
                        add(0, it.feedItem.pubkey)
                    }

                replyList.forEach { key ->
                    addText("@${key.substring(0, 6)}") {
                        setColor(MyApplication._instance.getColor(R.color.md_theme_primary))
                    }
                }
            }
        }


        binding.mbtSend.setOnClickListener {
            viewModel.sendPost(binding.edText.text.toString(), replyList,mentionsList)
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


    companion object {
        fun start(con: Context, feed: Feed, isReply: Boolean) {
            con.startActivity(Intent(con, PublishActivity::class.java).apply {
                putExtra("feed", feed)
                putExtra("isReply", isReply)
            })
        }
    }


}
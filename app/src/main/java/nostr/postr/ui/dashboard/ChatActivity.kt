package nostr.postr.ui.dashboard

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import nostr.postr.MyApplication
import nostr.postr.R
import nostr.postr.databinding.ActivityChatBinding
import nostr.postr.db.ChatMessage
import nostr.postr.db.ChatRoom
import nostr.postr.db.NostrDB

class ChatActivity : AppCompatActivity() {

    lateinit var binding: ActivityChatBinding


    private val list = mutableListOf<ChatMessage>()
    private lateinit var msgAdapter: ChatMsgAdapter

    private val viewMdodel by viewModels<PrivateChatViewModel>()
    private lateinit var pubKey: String
    private lateinit var chatRoom: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        msgAdapter = ChatMsgAdapter(list)
        binding.recyclerView
            .apply {
                layoutManager = LinearLayoutManager(this@ChatActivity)
                adapter = msgAdapter
            }
        pubKey = intent?.getStringExtra("pubKey") ?: return
        chatRoom=intent.getStringExtra("chat_room_id")?:return
        chatRoom.let {
            NostrDB.getDatabase(MyApplication._instance)
                .chatDao().getChatGroupMessage(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    list.clear()
                    list.addAll(it.sortedBy { it.createAt })
                    msgAdapter.notifyDataSetChanged()
                    binding.recyclerView.scrollToPosition(0)
                }
        }

        binding.edContent.doAfterTextChanged {
            binding.mbtSend.isEnabled = it?.isNotEmpty() == true
        }

        binding.mbtSend.setOnClickListener {
            viewMdodel.sendChat(binding.edContent.text.toString(),pubKey,chatRoom)
            binding.edContent.setText("")
        }
    }

    companion object {

        fun startChat(activity: Activity, pubKey: String, roomId: String) {
            activity.startActivity(Intent(activity, ChatActivity::class.java).apply {
                putExtra("pubKey", pubKey)
                putExtra("chat_room_id", roomId)
            })
        }
    }

}
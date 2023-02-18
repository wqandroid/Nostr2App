package nostr.postr.ui.chat

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import nostr.postr.MyApplication
import nostr.postr.core.AccountManger
import nostr.postr.core.BaseAct
import nostr.postr.databinding.ActivityChatBinding
import nostr.postr.db.ChatMessage
import nostr.postr.db.NostrDB
import nostr.postr.ui.chat.viewmodel.PrivateChatViewModel

class ChatActivity : BaseAct() {

    lateinit var binding: ActivityChatBinding


    private val list = mutableListOf<ChatMessage>()
    private lateinit var msgAdapter: ChatMsgAdapter

    private val viewModel by viewModels<PrivateChatViewModel>()
    private lateinit var pubKey: String
    private lateinit var chatRoom: String
    private lateinit var layoutManager: LinearLayoutManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        showBack()
        msgAdapter = ChatMsgAdapter(list)
        binding.recyclerView
            .apply {
                layoutManager = LinearLayoutManager(this@ChatActivity).also {
                    it.stackFromEnd = true
                    this@ChatActivity.layoutManager=it
                }
                adapter = msgAdapter
            }
        pubKey = intent?.getStringExtra("pubKey") ?: return
        chatRoom = intent.getStringExtra("chat_room_id") ?: return
        viewModel.makeAllMessageAsRead(chatRoom)
        chatRoom.let {
            NostrDB.getDatabase(MyApplication._instance)
                .chatDao().getChatGroupMessage(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    list.clear()
                    list.addAll(it.sortedBy { it.createAt })
                    msgAdapter.notifyDataSetChanged()
                    layoutManager.scrollToPositionWithOffset(
                        msgAdapter.itemCount-1,
                        Integer.MIN_VALUE
                    )
                    viewModel.makeAllMessageAsRead(chatRoom)
                }
        }

        binding.edContent.doAfterTextChanged {
            binding.mbtSend.isEnabled = it?.isNotEmpty() == true
        }

        binding.mbtSend.setOnClickListener {
            viewModel.sendChat(binding.edContent.text.toString(), pubKey, chatRoom)
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
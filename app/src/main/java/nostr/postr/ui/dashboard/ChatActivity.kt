package nostr.postr.ui.dashboard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import nostr.postr.MyApplication
import nostr.postr.R
import nostr.postr.databinding.ActivityChatBinding
import nostr.postr.db.ChatMessage
import nostr.postr.db.NostrDB

class ChatActivity : AppCompatActivity() {

    lateinit var binding: ActivityChatBinding


    private val list = mutableListOf<ChatMessage>()
    private lateinit var msgAdapter: ChatMsgAdapter

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

        val chatRoom = intent.getStringExtra("chat_room_id")?.let {
            NostrDB.getDatabase(MyApplication._instance)
                .chatDao().getChatGroupMessage(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    list.addAll(it)
                    msgAdapter.notifyDataSetChanged()
                }
        }


    }


}
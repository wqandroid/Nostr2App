package nostr.postr.ui.user

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import fr.acinq.secp256k1.Hex
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nostr.postr.*
import nostr.postr.core.AccountManger
import nostr.postr.core.BaseAct
import nostr.postr.databinding.ActivityUserDetailBinding
import nostr.postr.db.ChatRoom
import nostr.postr.db.NostrDB
import nostr.postr.db.UserProfile
import nostr.postr.ui.chat.ChatActivity
import nostr.postr.ui.feed.Feed
import nostr.postr.ui.feed.FeedAdapter
import nostr.postr.ui.user.followlist.UserFollowsListAct
import nostr.postr.util.UIUtils.makeGone
import nostr.postr.util.UIUtils.makeVisibility

class UserDetailActivity : BaseAct() {

    private lateinit var binding: ActivityUserDetailBinding

    private lateinit var pubkey: String

    private val userViewModel by viewModels<UserViewModel>()
    private var list = mutableListOf<Feed>()
    private val adapter by lazy { FeedAdapter(false, list) }
    private val set = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityUserDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        pubkey = intent.getStringExtra("pubkey")!!

        userViewModel.pubKey = pubkey
        userViewModel.reqProfile(pubkey)
        binding.toolbar.title = Hex.decode(pubkey).toNpub()
        userViewModel.user.observe(this) {
            showUser(it)
        }



        binding.rvFeed.adapter = adapter
        binding.rvFeed.layoutManager = LinearLayoutManager(this)
        binding.rvFeed.hasFixedSize()

        userViewModel.flowResult.observe(this) {
            if (it) {
                binding.mbtFollow.text = "Following"
                Toast.makeText(this, "关注成功", Toast.LENGTH_SHORT).show()
            }
        }

        userViewModel.feedLiveData.observe(this) {
            set.add(it.feedItem.id)
            list.add(it)
            list.sortByDescending { it.feedItem.created_at }
            adapter.notifyDataSetChanged()
            binding.progressHorizontal.makeGone()
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.mbtFollow.isVisible = pubkey != AccountManger.getPublicKey()
        binding.mbtChat.isVisible = pubkey != AccountManger.getPublicKey()


        userViewModel.followList.observe(this) {
            binding.tvFollows.text = "${it.size} Follows"
            binding.tvFollows.setOnClickListener {
                startActivity(Intent(this, UserFollowsListAct::class.java).apply {
                    putExtra("publicKey", pubkey)
                })
            }
        }
//        userViewModel.followersList.observe(this) {
//            binding.tvFollowers.text = "${it.size} Followers"
//        }
        AccountManger.follows.let {
            if (AccountManger.follows.contains(pubkey)) {
                binding.mbtFollow.text = "UnFollow"
            } else {
                binding.mbtFollow.text = "Follow"
            }
            binding.mbtFollow.isEnabled = true
            binding.mbtFollow.setOnClickListener { v ->
                userViewModel.addFlow(
                    pubkey,
                    it
                )
            }
        }


        binding.mbtChat.setOnClickListener {

            userViewModel.viewModelScope.launch(Dispatchers.IO) {
                val roomid = "${pubkey}-${AccountManger.getPublicKey()}"

                val chatRoom = NostrDB.getDatabase(MyApplication._instance)
                    .chatDao().getChatRoomById(roomid)

                if (chatRoom == null) {
                    ChatRoom(roomId = roomid, pubkey, "", System.currentTimeMillis() / 1000, false)
                        .also {
                            NostrDB.getDatabase(MyApplication._instance)
                                .chatDao().createChatRoom(it)
                        }
                }

                withContext(Dispatchers.Main) {
                    ChatActivity.startChat(this@UserDetailActivity, pubkey, roomid)
                }
            }
        }

        comDis.add(NostrDB.getDatabase(MyApplication._instance)
            .profileDao().getUserInfoRx(pubkey)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                it?.let {
                    showUser(it)
                }
            })
    }


    private fun updateFollows() {

    }


    private fun showUser(it: UserProfile) {
        Glide.with(this).load(it.getUserAvatar())
            .into(binding.ivAvatar)
        Glide.with(this).load(it.getUserAvatar())
            .into(binding.ivAvatar2)

        it.display_name?.let {
            binding.toolbar.title = it
        }
        it.name?.let {
            binding.toolbar.subtitle = "@$it"
        }
        if (!it.about.isNullOrEmpty()) {
            binding.tvDesc.makeVisibility()
            binding.tvDesc.text = "${it.about} ${it.website}"
        }
        if (it.nip05?.isNotEmpty() == true) {
            binding.tvNip05.makeVisibility()
            binding.tvNip05.text = it.nip05
        }
        if (it.lud16?.isNotEmpty() == true) {
            binding.nip16.makeVisibility()
            binding.nip16.text = it.lud16
        }
        it.banner.let {
            Glide.with(this).load(it)
                .into(binding.ivBanner)
        }
        binding.llContent.requestLayout()
        binding.llContent.invalidate()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.user_info, menu)
        return true
    }


    override fun onDestroy() {
        super.onDestroy()
    }

}
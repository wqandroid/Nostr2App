package nostr.postr.ui.user

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.map
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import fr.acinq.secp256k1.Hex
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import nostr.postr.MyApplication
import nostr.postr.R
import nostr.postr.bechToBytes
import nostr.postr.core.AccountManger
import nostr.postr.core.BaseAct
import nostr.postr.databinding.ActivityUserDetailBinding
import nostr.postr.db.NostrDB
import nostr.postr.db.UserProfile
import nostr.postr.toNpub
import nostr.postr.ui.AppViewModel
import nostr.postr.ui.feed.Feed
import nostr.postr.ui.feed.FeedAdapter
import nostr.postr.util.UIUtils.makeGone
import nostr.postr.util.UIUtils.makeVisibility

class UserDetailActivity : BaseAct() {

    private lateinit var binding: ActivityUserDetailBinding

    private lateinit var pubkey: String

    private val userViewModel by viewModels<UserViewModel>()


    private lateinit var adapter: FeedAdapter
    private var list = mutableListOf<Feed>()
    private val set = mutableSetOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityUserDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
//        window.statusBarColor = getColor(R.color.md_theme_primary)

        pubkey = intent.getStringExtra("pubkey")!!

        userViewModel.pubKey = pubkey
        binding.toolbar.title=pubkey
        userViewModel.reqProfile(pubkey)
        binding.toolbar.title = Hex.decode(pubkey).toNpub()
        userViewModel.user.observe(this) {
            showUser(it)
        }

        adapter = FeedAdapter(mutableListOf())

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
            if (!set.contains(it.feedItem.id)) {
                set.add(it.feedItem.id)
                list.add(it)
                list.sortByDescending { it.feedItem.created_at }
                adapter.updateData(list)
                binding.progressHorizontal.makeGone()
            }


        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.mbtFollow.isVisible = pubkey != AccountManger.getPublicKey()


        userViewModel.followList.observe(this) {
            val isFollowing = it.any { it.pubkey == AccountManger.getPublicKey() }
            if (isFollowing) {
                binding.mbtFollow.text = "Following(${list.size})"
            } else {
                binding.mbtFollow.text = "Follow(${list.size})"
            }
        }

        AccountManger.follows.let {
            binding.mbtFollow.setOnClickListener { v ->
                userViewModel.addFlow(
                    pubkey,
                    it
                )
            }
        }

//        comDis.add(NostrDB.getDatabase(MyApplication.getInstance())
//            .followUserKeyDao().getAll()
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe { list ->
//                val isFollowing = list.any { it.pubkey == pubkey }
//                binding.mbtFollow.isEnabled = true
//                if (isFollowing) {
//                    binding.mbtFollow.text = "Following(${list.size})"
//                } else {
//                    binding.mbtFollow.text = "Follow(${list.size})"
//                }
//                binding.mbtFollow.setOnClickListener {
//
//                    userViewModel.addFlow(
//                        pubkey,
//                        list.map { it.pubkey }.toMutableList()
//                    )
//                }
//            })

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

    private fun showUser(it: UserProfile) {
        Glide.with(this).load(it.picture)
            .into(binding.ivAvatar)

        it.display_name?.let {
            binding.toolbar.title = it
        }
        it.name?.let {
            binding.toolbar.subtitle="@$it"
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


    override fun onDestroy() {
        super.onDestroy()
    }

}
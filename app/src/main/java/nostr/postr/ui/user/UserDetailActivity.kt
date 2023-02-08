package nostr.postr.ui.user

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import nostr.postr.R
import nostr.postr.databinding.ActivityUserDetailBinding
import nostr.postr.ui.feed.Feed
import nostr.postr.ui.feed.FeedAdapter

class UserDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserDetailBinding

    private lateinit var pubkey: String

    private val userViewModel by viewModels<UserViewModel>()


    private lateinit var adapter: FeedAdapter
    private var list= mutableListOf<Feed>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        pubkey = intent.getStringExtra("pubkey")!!

        userViewModel.pubKey=pubkey

        userViewModel.reqProfile(pubkey)

        userViewModel.user.observe(this) {

            Glide.with(this).load(it.picture)
                .into(binding.ivAvatar)
            binding.tvName.text = it.display_name ?: it.name
            binding.tvDesc.text = it.about

        }


        adapter = FeedAdapter(mutableListOf())

        binding.rvFeed.adapter = adapter
        binding.rvFeed.layoutManager = LinearLayoutManager(this)
        binding.rvFeed.hasFixedSize()


        userViewModel.feedLiveData.observe(this) {
            list.add(it)
            list.sortByDescending { it.feedItem.created_at }
            adapter.updateData(list)
        }
    }


}
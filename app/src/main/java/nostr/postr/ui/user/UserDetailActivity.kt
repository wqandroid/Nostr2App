package nostr.postr.ui.user

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import fr.acinq.secp256k1.Hex
import nostr.postr.R
import nostr.postr.databinding.ActivityUserDetailBinding
import nostr.postr.toNpub
import nostr.postr.ui.feed.Feed
import nostr.postr.ui.feed.FeedAdapter
import nostr.postr.util.UIUtils.makeVisibility

class UserDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserDetailBinding

    private lateinit var pubkey: String

    private val userViewModel by viewModels<UserViewModel>()


    private lateinit var adapter: FeedAdapter
    private var list = mutableListOf<Feed>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityUserDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        setSupportActionBar(binding.toolbar)

        window.statusBarColor = getColor(R.color.md_theme_primary)

        pubkey = intent.getStringExtra("pubkey")!!

        userViewModel.pubKey = pubkey

        userViewModel.reqProfile(pubkey)

        binding.toolbar.title = Hex.decode(pubkey).toNpub()
        userViewModel.user.observe(this) {

            Glide.with(this).load(it.picture)
                .into(binding.ivAvatar)
            binding.toolbar.title = it.display_name ?: it.name
            binding.tvDesc.text = "${it.about} ${it.website} ${it.lud16}"
            if (it.nip05?.isNotEmpty() == true) {
                binding.tvNip05.makeVisibility()
                binding.tvNip05.text = it.nip05
            }
            if (it.banner?.isNotEmpty()==true){

                Glide.with(this).load(it.banner)
                    .into(binding.ivBanner)
            }

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

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

    }


}
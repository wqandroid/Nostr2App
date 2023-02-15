package nostr.postr.ui.feed.global

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import nostr.postr.R
import nostr.postr.databinding.ActivityGlobalFeedBinding
import nostr.postr.ui.ImageDetailActivity
import nostr.postr.ui.feed.Feed
import nostr.postr.ui.feed.FeedAdapter
import nostr.postr.ui.user.UserDetailActivity
import nostr.postr.util.MD5

class GlobalFeedActivity : AppCompatActivity(), FeedAdapter.ItemChildClickListener {

    private val viewModel by viewModels<GlobalFeedViewModel>()
    private lateinit var binding: ActivityGlobalFeedBinding

    private val list= mutableListOf<Feed>()

    private val adapter by lazy {
        FeedAdapter(false,list).also {
            it.clickListener = this@GlobalFeedActivity
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGlobalFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@GlobalFeedActivity)
            adapter = this@GlobalFeedActivity.adapter
        }

        viewModel.feedLiveData.observe(this) {
//            Log.e("feed_","global:${it.size}")
            list.add(0,it)
            if (list.size<5){
                adapter.notifyDataSetChanged()
            }else if (list.size%5==0){
                adapter.notifyDataSetChanged()
            }
        }
        viewModel.reqGlobalFeed()
    }

    override fun onClick(feed: Feed, itemView: View) {
        when (itemView.id) {
            R.id.iv_more -> {
                Log.e("account", "block${MD5.md5(feed.feedItem.content)}")
                //            feedViewModel.addBlock(feed.feedItem.pubkey,feed.feedItem.content)
            }
            R.id.iv_avatar -> {
                startActivity(
                    Intent(this, UserDetailActivity::class.java)
                    .apply {
                        putExtra("pubkey", feed.feedItem.pubkey)
                    })
            }
            R.id.tv_reply -> {
                startActivity(
                    Intent(this, UserDetailActivity::class.java)
                    .apply {
                        putExtra("pubkey", feed.feedItem.getReplyTos()!![0])
                    })
            }
            R.id.iv_content_img->{
                val intent= Intent(this, ImageDetailActivity::class.java)
                intent.putExtra("img_url",feed.findImageUrl())
                startActivity(
                    intent,
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this,
                        itemView,
                        "search"
                    ).toBundle()
                )
            }
        }
    }


}
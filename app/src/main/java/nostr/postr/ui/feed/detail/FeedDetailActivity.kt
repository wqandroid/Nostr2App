package nostr.postr.ui.feed.detail

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import fr.acinq.secp256k1.Hex
import nostr.postr.MyApplication
import nostr.postr.R
import nostr.postr.core.BaseAct
import nostr.postr.databinding.ActivityFeedDetailBinding
import nostr.postr.databinding.FragmentFeedItemBinding
import nostr.postr.db.NostrDB
import nostr.postr.toNpub
import nostr.postr.ui.ImageDetailActivity
import nostr.postr.ui.feed.Feed
import nostr.postr.ui.feed.PublishActivity
import nostr.postr.ui.user.UserDetailActivity
import nostr.postr.util.UIUtils
import nostr.postr.util.UIUtils.makeGone
import nostr.postr.util.UIUtils.makeVisibility
import nostr.postr.util.buildSpannableString

class FeedDetailActivity : BaseAct(), FeedCommentAdapter.ItemChildClickListener {

    private val viewModel by viewModels<FeedDetailViewModel>()

    private lateinit var binding: ActivityFeedDetailBinding

    private val data = mutableListOf<Feed>()

    private val adapter by lazy {
        FeedCommentAdapter(data).also {
            it.clickListener = this@FeedDetailActivity
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        showBack()

        val feed = intent.getParcelableExtra<Feed>("feed") ?: return


        binding.recyclerView.apply {
            this.layoutManager = LinearLayoutManager(this@FeedDetailActivity)
            this.adapter = this@FeedDetailActivity.adapter
        }

        showFeedInfo(binding.feed, feed)

        viewModel.reqFeedInfo(feed.feedItem.id)

        viewModel.feedLiveData.observe(this) {
            data.clear()
            data.addAll(it)
            adapter.notifyDataSetChanged()
        }
    }


    private fun showFeedInfo(binding: FragmentFeedItemBinding, item: Feed) {

//        if (item.userProfile == null) {
            item.userProfile = NostrDB.getDatabase(MyApplication._instance).profileDao()
                .getUserInfo2(item.feedItem.pubkey)
//        }

        binding.tvContent.text = item.feedItem.content
        binding.tvTime.text = UIUtils.parseTime(item.feedItem.created_at)

        if (item.feedItem.getReplyTos().isEmpty()) {
            binding.tvReply.makeGone()
        } else {

            binding.tvReply.makeVisibility()
            binding.tvReply.buildSpannableString {
                addText("replying to") {
                    setColor(MyApplication._instance.getColor(R.color.md_theme_primary))
                }
                item.feedItem.getMentions().forEach { key ->
                    addText("@${key.substring(0, 6)}") {
                        setColor(MyApplication._instance.getColor(R.color.md_theme_primary))
                        onClick {
                            startActivity(
                                Intent(
                                    this@FeedDetailActivity,
                                    UserDetailActivity::class.java
                                ).apply {
                                    putExtra("pubkey", key)
                                }
                            )
                        }
                    }
                }
            }
        }

        if (item.userProfile == null) {
            binding.ivLn6.isVisible = false
            binding.tvName.text = ""
            binding.tvDisplayName.text = Hex.decode(item.feedItem.pubkey).toNpub()
        } else {
            binding.tvDisplayName.text = item.userProfile!!.display_name
            binding.tvName.text = "@${item.userProfile!!.name}"
            binding.ivLn6.isVisible = item.userProfile!!.lud16?.isNotEmpty() == true
        }


        Glide.with(binding.ivAvatar).load(item.getUserAvatar())
            .into(
                binding.ivAvatar
            )

        val listUrls = item.findImageUrl()
        if (listUrls.isEmpty()) {
//            binding.llContentImg.visibility = View.GONE
            binding.llContentImg.removeAllViews()
        } else {
//            binding.llContentImg.makeVisibility()
            binding.llContentImg.removeAllViews()

            var newContent = item.feedItem.content

            listUrls.forEach { url ->

                val imageView = ImageView(this@FeedDetailActivity)
                imageView.maxHeight = 720
                imageView.maxWidth = 720
//                imageView.scaleType=ImageView.ScaleType.CENTER_INSIDE
                val p = LinearLayoutCompat.LayoutParams(
                    LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                    LinearLayoutCompat.LayoutParams.WRAP_CONTENT
                )
                binding.llContentImg
                    .addView(imageView, p)


                Glide.with(binding.ivAvatar).load(url).into(
                    imageView
                )
                imageView.setOnClickListener {
                    val intent = Intent(this@FeedDetailActivity, ImageDetailActivity::class.java)
                    intent.putExtra("img_url", url)
                    startActivity(
                        intent,
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                            this@FeedDetailActivity,
                            it,
                            "search"
                        ).toBundle()
                    )
                }

                newContent = newContent.replace(url, "")
                newContent = newContent.replace("\n", "")
            }
            binding.tvContent.text = newContent
        }


    }

    override fun onClick(feed: Feed, itemView: View) {
        when (itemView.id) {
            R.id.iv_comment -> {
                PublishActivity.start(this, feed, true)
            }
            R.id.iv_avatar -> {
                startActivity(Intent(this, UserDetailActivity::class.java)
                    .apply {
                        putExtra("pubkey", feed.feedItem.pubkey)
                    })
            }
        }
    }


}
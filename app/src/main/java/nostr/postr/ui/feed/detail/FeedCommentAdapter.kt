package nostr.postr.ui.feed.detail

import android.app.Activity
import android.content.Intent
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import fr.acinq.secp256k1.Hex
import kotlinx.parcelize.Parcelize
import nostr.postr.MyApplication
import nostr.postr.R
import nostr.postr.databinding.FragmentFeedItemBinding
import nostr.postr.databinding.ItemFeedContentBinding
import nostr.postr.db.FeedItem
import nostr.postr.db.UserProfile
import nostr.postr.toNpub
import nostr.postr.ui.ImageDetailActivity
import nostr.postr.ui.feed.Feed
import nostr.postr.ui.user.UserDetailActivity
import nostr.postr.util.UIUtils
import nostr.postr.util.UIUtils.makeGone
import nostr.postr.util.UIUtils.makeVisibility
import nostr.postr.util.buildSpannableString
import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URL
import java.util.regex.Pattern


class FeedCommentAdapter(val data: List<Feed> = mutableListOf()) :
    RecyclerView.Adapter<FeedCommentAdapter.FeedViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {

        val binding = ItemFeedContentBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return FeedViewHolder(binding)
    }


    var clickListener: ItemChildClickListener? = null


    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {

        val item: Feed = data[position]

        holder.binding.tvContent.text = item.feedItem.content
        holder.binding.tvTime.text = UIUtils.parseTime(item.feedItem.created_at)

        if (item.feedItem.getReplyTos().isNullOrEmpty()) {
            holder.binding.tvReply.makeGone()
        } else {
            holder.binding.tvReply.makeVisibility()
            holder.binding.tvReply.buildSpannableString {
                addText("replying to") {
                    setColor(MyApplication._instance.getColor(R.color.md_theme_primary))
                }
                item.feedItem.getMentions().forEach { key ->
                    addText("@${key.substring(0, 6)}") {
                        setColor(MyApplication._instance.getColor(R.color.md_theme_primary))
                        onClick {
                            holder.itemView.context.startActivity(
                                Intent(
                                    holder.itemView.context,
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
            holder.binding.tvDisplayName.text = Hex.decode(item.feedItem.pubkey).toNpub()
        } else {
            holder.binding.tvDisplayName.text =
                "${item.userProfile?.display_name}@${item.userProfile?.name}"
        }


        Glide.with(holder.binding.ivAvatar).load(item.getUserAvatar())
            .into(
                holder.binding.ivAvatar
            )

        val listUrls = item.findImageUrl()
        if (listUrls.isEmpty()) {
//            holder.binding.llContentImg.visibility = View.GONE
            holder.binding.llContentImg.removeAllViews()
        } else {
//            holder.binding.llContentImg.makeVisibility()
            holder.binding.llContentImg.removeAllViews()

            var newContent = item.feedItem.content

            listUrls.forEach { url ->

                val imageView = ImageView(holder.itemView.context)
                imageView.maxHeight = 720
                imageView.maxWidth = 720
//                imageView.scaleType=ImageView.ScaleType.CENTER_INSIDE
                val p = LinearLayoutCompat.LayoutParams(
                    LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                    LinearLayoutCompat.LayoutParams.WRAP_CONTENT
                )
                holder.binding.llContentImg
                    .addView(imageView, p)


                Glide.with(holder.binding.ivAvatar).load(url).into(
                    imageView
                )
                imageView.setOnClickListener {
                    val intent = Intent(holder.itemView.context, ImageDetailActivity::class.java)
                    intent.putExtra("img_url", url)
                    holder.itemView.context.startActivity(
                        intent,
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                            holder.itemView.context as Activity,
                            it,
                            "search"
                        ).toBundle()
                    )
                }

                newContent = newContent.replace(url, "")
                newContent = newContent.replace("\n", "")
            }
            holder.binding.tvContent.text = newContent
        }

        holder.binding.root.setOnClickListener {
            clickListener?.onClick(item, it)
        }
        holder.binding.ivAvatar.setOnClickListener {
            clickListener?.onClick(item, it)
        }
        holder.binding.ivComment.setOnClickListener {
            clickListener?.onClick(item, it)
        }
        holder.setIsRecyclable(true)
    }


    override fun getItemCount() = data.size

    inner class FeedViewHolder(val binding: ItemFeedContentBinding) :
        RecyclerView.ViewHolder(binding.root)


    interface ItemChildClickListener {
        fun onClick(feed: Feed, itemView: View)
    }
}
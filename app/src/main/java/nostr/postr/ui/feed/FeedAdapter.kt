package nostr.postr.ui.feed

import android.app.Activity
import android.content.Intent
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
import nostr.postr.MyApplication
import nostr.postr.R
import nostr.postr.databinding.FragmentFeedItemBinding
import nostr.postr.db.FeedItem
import nostr.postr.db.UserProfile
import nostr.postr.toNpub
import nostr.postr.ui.ImageDetailActivity
import nostr.postr.ui.user.UserDetailActivity
import nostr.postr.util.UIUtils
import nostr.postr.util.UIUtils.makeGone
import nostr.postr.util.UIUtils.makeVisibility
import nostr.postr.util.buildSpannableString
import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URL
import java.util.regex.Pattern


val imageExtension: Pattern = Pattern.compile("(.*/)*.+\\.(png|jpg|gif|bmp|jpeg|webp|svg)$")
val videoExtension: Pattern = Pattern.compile("(.*/)*.+\\.(mp4|avi|wmv|mpg|amv|webm)$")


data class Feed(val feedItem: FeedItem, val userProfile: UserProfile?) {


    private fun isValidURL(url: String?): Boolean {
        return try {
            URL(url).toURI()
            true
        } catch (e: MalformedURLException) {
            false
        } catch (e: URISyntaxException) {
            false
        }
    }

    fun findImageUrl(): List<String> {
        val urs = mutableListOf<String>()
        feedItem.content.split('\n').forEach { word ->
            if (isValidURL(word)) {
                val m = imageExtension.matcher(word)
                if (m.find()) {
                    urs.add(m.group())
                }

                val m2 = videoExtension.matcher(word)
                if (m2.find()) {
                    urs.add(m2.group())
                }
            }
        }
        return urs
    }

    fun getUserAvatar(): String {
        return userProfile?.picture ?: "https://robohash.org/${feedItem.pubkey}.png"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Feed

        if (feedItem != other.feedItem) return false

        return true
    }

    override fun hashCode(): Int {
        return feedItem.hashCode()
    }


}

class FeedAdapter(val isUseDiff: Boolean = true, val data: List<Feed> = mutableListOf()) :
    RecyclerView.Adapter<FeedAdapter.FeedViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {

        val binding = FragmentFeedItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return FeedViewHolder(binding)
    }


    var clickListener: ItemChildClickListener? = null


    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {

        val item: Feed = if (isUseDiff) differ.currentList[position] else data[position]

        holder.binding.tvContent.text = item.feedItem.content
        holder.binding.tvTime.text = UIUtils.parseTime(item.feedItem.created_at)

        if (item.feedItem.getReplyTos().isNullOrEmpty()) {
            holder.binding.tvReply.makeGone()
        } else {

            holder.binding.tvReply.makeVisibility()
            holder.binding.tvReply.buildSpannableString {
                addText("reply") {
                    setColor(MyApplication._instance.getColor(R.color.md_theme_primary))
                }
                item.feedItem.getReplyTos().forEach { key ->
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
            holder.binding.ivLn6.isVisible = false
            holder.binding.tvName.text = ""
            holder.binding.tvDisplayName.text = Hex.decode(item.feedItem.pubkey).toNpub()
        } else {
            holder.binding.tvDisplayName.text = item.userProfile.display_name
            holder.binding.tvName.text = "@${item.userProfile.name}"
            holder.binding.ivLn6.isVisible = item.userProfile.lud16?.isNotEmpty() == true
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
                imageView.maxHeight=720
                imageView.maxWidth=720
//                imageView.scaleType=ImageView.ScaleType.CENTER_INSIDE
                val p= LinearLayoutCompat.LayoutParams(
                    LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                    LinearLayoutCompat.LayoutParams.WRAP_CONTENT
                )
                holder.binding.llContentImg
                    .addView(imageView,p)


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

                newContent= newContent.replace(url, "")
                newContent=newContent.replace("\n","")
            }
            holder.binding.tvContent.text = newContent
        }

        holder.binding.root.setOnClickListener {
            clickListener?.onClick(item, it)
        }
        holder.binding.ivMore.setOnClickListener {
            clickListener?.onClick(item, it)
        }
        holder.binding.ivAvatar.setOnClickListener {
            clickListener?.onClick(item, it)
        }
        holder.setIsRecyclable(true)
    }


    override fun getItemCount() = if (isUseDiff) differ.currentList.size else data.size

    inner class FeedViewHolder(val binding: FragmentFeedItemBinding) :
        RecyclerView.ViewHolder(binding.root)


    private val diffCallBack = object : DiffUtil.ItemCallback<Feed>() {
        override fun areItemsTheSame(oldItem: Feed, newItem: Feed): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Feed, newItem: Feed): Boolean {
            return oldItem.feedItem.content == newItem.feedItem.content
                    || oldItem.feedItem.getReplyTos() == newItem.feedItem.getReplyTos()
                    || oldItem.feedItem.getMentions() == newItem.feedItem.getMentions()

        }
    }

    val differ = AsyncListDiffer(this, diffCallBack)

    interface ItemChildClickListener {
        fun onClick(feed: Feed, itemView: View)
    }
}
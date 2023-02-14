package nostr.postr.ui.feed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import fr.acinq.secp256k1.Hex
import nostr.postr.databinding.FragmentFeedItemBinding
import nostr.postr.db.FeedItem
import nostr.postr.db.UserProfile
import nostr.postr.toNpub
import nostr.postr.util.UIUtils
import nostr.postr.util.UIUtils.makeGone
import nostr.postr.util.UIUtils.makeVisibility
import java.util.regex.Pattern


data class Feed(val feedItem: FeedItem, val userProfile: UserProfile?) {

    val imageExtension = Pattern.compile("(.*/)*.+\\.(png|jpg|gif|bmp|jpeg|webp|svg)$")
    val videoExtension = Pattern.compile("(.*/)*.+\\.(mp4|avi|wmv|mpg|amv|webm)$")


    var replyTos: List<String>? = null
    var mentions: List<String>? = null

    fun findImageUrl(): String? {
        val m = imageExtension.matcher(feedItem.content)
        val m2 = videoExtension.matcher(feedItem.content)
        if (m.find() || m2.find()) {
           return m.group()
        }
        return null
    }

}

class FeedAdapter() :
    RecyclerView.Adapter<FeedAdapter.FeedViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {

        val binding = FragmentFeedItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return FeedViewHolder(binding)
    }


    var clickListener: ItemChildClickListener? = null


    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        val item: Feed = differ.currentList[position]
        holder.binding.tvContent.text = item.feedItem.content
        holder.binding.tvTime.text = UIUtils.parseTime(item.feedItem.created_at)

        if (item.replyTos.isNullOrEmpty()) {
            holder.binding.tvReply.makeGone()
        } else {
            holder.binding.tvReply.makeVisibility()
            holder.binding.tvReply.text = "@reply${item.replyTos!![0]}"
            holder.binding.tvReply.setOnClickListener {
                clickListener?.onClick(item, it)
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


        Glide.with(holder.binding.ivAvatar).load(item.userProfile?.picture).into(
            holder.binding.ivAvatar
        )

        if (item.findImageUrl().isNullOrEmpty()) {
            holder.binding.ivContentImg.visibility = View.GONE
        } else {
//            Log.e("matches", "--->${m.group()}---${item.feedItem.content}")
            holder.binding.ivContentImg.visibility = View.VISIBLE
            Glide.with(holder.binding.ivAvatar).load(item.findImageUrl()!!).into(
                holder.binding.ivContentImg
            )
            holder.binding.ivContentImg.setOnClickListener {
                clickListener?.onClick(item,it)
            }
        }

        holder.binding.ivMore.setOnClickListener {
            clickListener?.onClick(item, it)
        }
        holder.binding.ivAvatar.setOnClickListener {
            clickListener?.onClick(item, it)
        }
        holder.setIsRecyclable(true)
    }


    override fun getItemCount() = differ.currentList.size

    inner class FeedViewHolder(val binding: FragmentFeedItemBinding) :
        RecyclerView.ViewHolder(binding.root)


    private val diffCallBack = object : DiffUtil.ItemCallback<Feed>() {
        override fun areItemsTheSame(oldItem: Feed, newItem: Feed): Boolean {
            return oldItem.feedItem.id == newItem.feedItem.id
        }

        override fun areContentsTheSame(oldItem: Feed, newItem: Feed): Boolean {
            return oldItem.feedItem.content == newItem.feedItem.content
        }

    }

    val differ = AsyncListDiffer(this, diffCallBack)

    interface ItemChildClickListener {
        fun onClick(feed: Feed, itemView: View)
    }
}
package nostr.postr.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import fr.acinq.secp256k1.Hex
import nostr.postr.databinding.FragmentFeedItemBinding
import nostr.postr.databinding.ItemFollowUserBinding
import nostr.postr.db.FeedItem
import nostr.postr.db.UserProfile
import nostr.postr.toNpub
import nostr.postr.util.UIUtils.makeGone
import nostr.postr.util.UIUtils.makeVisibility
import java.util.regex.Pattern

data class FollowInfo(val pubkey: String, val name: String?, var userProfile: UserProfile?){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FollowInfo

        if (pubkey != other.pubkey) return false

        return true
    }

    override fun hashCode(): Int {
        return pubkey.hashCode()
    }
}

class FollowAdapter(var listData: MutableList<FollowInfo>) :
    RecyclerView.Adapter<FollowAdapter.FollowViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowViewHolder {

        val binding = ItemFollowUserBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return FollowViewHolder(binding)
    }

    var clickListener: ItemChildClickListener? = null


    override fun onBindViewHolder(holder: FollowViewHolder, position: Int) {
        val item: FollowInfo = listData[position]

        holder.binding.tvName.text =
            if (item.userProfile == null) item.name ?: item.pubkey else item.userProfile?.bestName()


        if (item.userProfile?.nip05?.isNotEmpty() == true) {
            holder.binding.tvNip05.makeVisibility()
            holder.binding.tvNip05.text = item.userProfile?.nip05
        } else {
            holder.binding.tvNip05.makeGone()
        }

        Glide.with(holder.binding.ivAvatar).load(item.userProfile?.picture).into(
            holder.binding.ivAvatar
        )

        holder.binding.ivAvatar.setOnClickListener {
            clickListener?.onClick(item, it)
        }
    }


    override fun getItemCount() = listData.size

    inner class FollowViewHolder(val binding: ItemFollowUserBinding) :
        RecyclerView.ViewHolder(binding.root)


    interface ItemChildClickListener {
        fun onClick(feed: FollowInfo, itemView: View)
    }
}
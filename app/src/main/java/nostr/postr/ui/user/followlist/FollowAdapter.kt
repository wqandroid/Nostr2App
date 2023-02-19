package nostr.postr.ui.user.followlist

import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.parcelize.Parcelize
import nostr.postr.core.AccountManger
import nostr.postr.databinding.ItemFollowUserBinding
import nostr.postr.db.UserProfile

@Parcelize
data class FollowInfo(val pubkey: String, val name: String?, var userProfile: UserProfile?):Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FollowInfo

        if (pubkey != other.pubkey) return false

        return true
    }

    fun getAvatar(): String {
        return if (userProfile == null) {
            "https://robohash.org/${pubkey}.png"
        } else {
            return userProfile!!.getUserAvatar()
        }
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


        Glide.with(holder.binding.ivAvatar).load(item.getAvatar()).into(
            holder.binding.ivAvatar
        )

        holder.binding.ivAvatar.setOnClickListener {
            clickListener?.onClick(item, it)
        }

        if (AccountManger.follows.contains(item.pubkey)) {
            holder.binding.mbtFollow.text = "UnFollow"
        } else {
            holder.binding.mbtFollow.text = "Follow"
        }
        holder.binding.mbtFollow.setOnClickListener {
            clickListener?.onClick(item,it)
        }

    }


    override fun getItemCount() = listData.size

    inner class FollowViewHolder(val binding: ItemFollowUserBinding) :
        RecyclerView.ViewHolder(binding.root)


    interface ItemChildClickListener {
        fun onClick(feed: FollowInfo, itemView: View)
    }
}
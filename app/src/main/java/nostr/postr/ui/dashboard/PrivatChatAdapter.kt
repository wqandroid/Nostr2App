package nostr.postr.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import fr.acinq.secp256k1.Hex
import nostr.postr.databinding.FragmentFeedItemBinding
import nostr.postr.databinding.ItemChatGroupBinding
import nostr.postr.databinding.ItemFollowUserBinding
import nostr.postr.db.Chat
import nostr.postr.db.ChatRoom
import nostr.postr.db.FeedItem
import nostr.postr.db.UserProfile
import nostr.postr.toNpub
import nostr.postr.util.UIUtils.makeGone
import nostr.postr.util.UIUtils.makeVisibility
import java.util.regex.Pattern


class ChatAdapter(var listData: MutableList<ChatRoom>) :
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ItemChatGroupBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    var clickListener: ItemChildClickListener? = null


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: ChatRoom = listData[position]

        holder.binding.tvName.text =
            if (item.profile == null) item.roomId else item.profile?.bestName()

        holder.binding.tvContent.text=item.content

        Glide.with(holder.binding.ivAvatar).load(item.profile?.picture).into(
            holder.binding.ivAvatar
        )

    }


    override fun getItemCount() = listData.size

    inner class ViewHolder(val binding: ItemChatGroupBinding) :
        RecyclerView.ViewHolder(binding.root)


    interface ItemChildClickListener {
        fun onClick(feed: ChatRoom, itemView: View)
    }
}
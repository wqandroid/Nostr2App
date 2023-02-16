package nostr.postr.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import nostr.postr.databinding.ItemChatGroupBinding
import nostr.postr.db.ChatRoom


class ChatListAdapter :
    RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ItemChatGroupBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    var clickListener: ItemChildClickListener? = null


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: ChatRoom = differ.currentList[position]

        holder.binding.tvName.text =
            if (item.profile == null) item.roomId.substring(0,8) else item.profile?.bestName()

        holder.binding.tvContent.text = item.content

        Glide.with(holder.binding.ivAvatar).load(item.getUserAvatar()).into(
            holder.binding.ivAvatar
        )

        holder.binding.ivHasUnRead.isVisible=item.hasUnread

        holder.binding.clRoot.setOnClickListener {
            clickListener?.onClick(item,it)
        }
        holder.binding.ivAvatar.setOnClickListener {
            clickListener?.onClick(item,it)
        }
    }


    override fun getItemCount() = differ.currentList.size

    inner class ViewHolder(val binding: ItemChatGroupBinding) :
        RecyclerView.ViewHolder(binding.root)


    private val diffCallback = object : DiffUtil.ItemCallback<ChatRoom>() {
        override fun areItemsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean {
            return oldItem.content == newItem.content
        }

    }
    val differ = AsyncListDiffer(this, diffCallback)

    interface ItemChildClickListener {
        fun onClick(feed: ChatRoom, itemView: View)
    }
}
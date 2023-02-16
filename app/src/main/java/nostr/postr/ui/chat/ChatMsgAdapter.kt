package nostr.postr.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import nostr.postr.R
import nostr.postr.core.AccountManger
import nostr.postr.databinding.ItemChatReceiveBinding
import nostr.postr.databinding.ItemChatToBinding
import nostr.postr.db.ChatMessage
import nostr.postr.util.UIUtils
import nostr.postr.util.UIUtils.makeGone
import nostr.postr.util.UIUtils.makeVisibility

class ChatMsgAdapter(
    val list: MutableList<ChatMessage>,
    val pubKey: String = AccountManger.getPublicKey()
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatMsgAdapter.ChatHolder {

        val view =
            if (viewType == 0) {
                ItemChatToBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            } else {
                ItemChatReceiveBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            }
        return ChatHolder(view.root)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        list[position].apply {
            holder.itemView.findViewById<TextView>(R.id.tv_content).text = content
            holder.itemView.findViewById<TextView>(R.id.tv_time).text =
                UIUtils.parseTime(createAt)
            //send
            if (createPubKey == pubKey) {
                if (!success) {
                    if (System.currentTimeMillis() / 1000 - createAt < 60) {
                        holder.itemView.findViewById<ImageView>(R.id.iv_send_error).makeGone()
                        holder.itemView.findViewById<ImageView>(R.id.pb_send).makeVisibility()
                    } else {
                        holder.itemView.findViewById<ImageView>(R.id.iv_send_error).makeVisibility()
                        holder.itemView.findViewById<ImageView>(R.id.pb_send).makeGone()
                    }
                } else {
                    holder.itemView.findViewById<ImageView>(R.id.iv_send_error).makeGone()
                    holder.itemView.findViewById<ImageView>(R.id.pb_send).makeGone()
                }
            }

        }

    }


    override fun getItemViewType(position: Int): Int {
        return if (list[position].createPubKey == pubKey) 0 else 1
    }

    override fun getItemCount(): Int {
        return list.size
    }


    inner class ChatHolder(val binding: View) :
        RecyclerView.ViewHolder(binding)


}
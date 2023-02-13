package nostr.postr.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import nostr.postr.R
import nostr.postr.core.AccountManger
import nostr.postr.databinding.ItemChatReceiveBinding
import nostr.postr.databinding.ItemChatToBinding
import nostr.postr.db.ChatMessage
import nostr.postr.util.UIUtils

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
        holder.itemView.findViewById<TextView>(R.id.tv_content).text = list[position].content
        holder.itemView.findViewById<TextView>(R.id.tv_time).text =
        UIUtils.parseTime(list[position].createAt)

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
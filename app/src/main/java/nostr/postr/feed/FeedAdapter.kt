package nostr.postr.feed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import nostr.postr.R
import nostr.postr.db.FeedItem


class FeedAdapter(var listData: MutableList<FeedItem>) :
    RecyclerView.Adapter<FeedAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem: View = layoutInflater.inflate(R.layout.fragment_feed_item, parent, false)
        return ViewHolder(listItem)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item: FeedItem = listData[position]
        holder.tv_content.text = item.content
    }

    fun updateData( list: List<FeedItem>){
        listData.clear()
        listData.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemCount() = listData.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        var imageView: ImageView
        var tv_content: TextView

        init {
//            imageView = itemView.findViewById(R.id.imageView) as ImageView
            tv_content = itemView.findViewById(R.id.tv_content)
        }
    }
}
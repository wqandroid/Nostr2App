package nostr.postr.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import nostr.postr.R
import nostr.postr.databinding.FragmentDashboardBinding
import nostr.postr.ui.feed.Feed
import nostr.postr.ui.feed.FeedViewModel
import nostr.postr.ui.user.UserDetailActivity
import nostr.postr.util.MD5

class DashboardFragment : Fragment(), FollowAdapter.ItemChildClickListener {

    lateinit var binding: FragmentDashboardBinding

    private lateinit var feedModel: FeedViewModel

    private lateinit var adapter: FollowAdapter
    private val list = mutableListOf<FollowInfo>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        feedModel =
            ViewModelProvider(requireActivity())[FeedViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.adapter = FollowAdapter(list).also {
            binding.recyclerView.adapter = it
        }
        this.adapter.clickListener=this
        binding.recyclerView.apply {
            this.layoutManager = LinearLayoutManager(requireContext())
        }

        feedModel.reqMainUserInfo()

        feedModel.followList.observe(viewLifecycleOwner) {
            list.clear()
            list.addAll(it)
            adapter.notifyDataSetChanged()
        }

    }


    override fun onClick(feed: FollowInfo, itemView: View) {
        if (itemView.id == R.id.iv_avatar) {
            startActivity(
                Intent(requireContext(), UserDetailActivity::class.java)
                    .apply {
                        putExtra("pubkey", feed.pubkey)
                    })
        }
    }

}
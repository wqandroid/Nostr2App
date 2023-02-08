package nostr.postr.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import nostr.postr.R
import nostr.postr.databinding.FragmentFeedBinding
import nostr.postr.databinding.FragmentHomeBinding
import nostr.postr.feed.Feed
import nostr.postr.feed.FeedAdapter
import nostr.postr.feed.FeedViewModel
import nostr.postr.ui.user.UserDetailActivity
import nostr.postr.util.MD5
import nostr.postr.util.UIUtils.makeGone
import nostr.postr.util.UIUtils.makeVisibility

class HomeFragment : Fragment(), FeedAdapter.ItemChildClickListener {
    private lateinit var binding: FragmentFeedBinding
    private lateinit var feedViewModel: FeedViewModel

    private lateinit var adapter: FeedAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        feedViewModel =
            ViewModelProvider(this)[FeedViewModel::class.java]
        binding = FragmentFeedBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = FeedAdapter(mutableListOf())

        binding.rvFeed.adapter = adapter
        binding.rvFeed.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFeed.hasFixedSize()

        feedViewModel.feedLiveData.observe(viewLifecycleOwner) {
            adapter.updateData(it)
        }
        feedViewModel.loadBlockUser()
        feedViewModel.loadFeedFromDB()

        adapter.clickListener = this

        feedViewModel.feedCountLiveData.observe(viewLifecycleOwner){

            if (it>1){
                binding.mbtFeedLoad.makeVisibility()
                binding.mbtFeedLoad.text="新增动态（$it）点击加载"
            }else{
                binding.mbtFeedLoad.makeGone()
            }
        }

        binding.mbtFeedLoad.setOnClickListener {
            feedViewModel.loadFeedFromDB()
            binding.mbtFeedLoad.makeGone()
        }

        binding.toolbar.setOnClickListener {
            requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)
                .openDrawer(GravityCompat.START)
        }

    }

    override fun onClick(feed: Feed, itemView: View) {
        if (itemView.id == R.id.iv_more) {
            Log.e("account","block${MD5.md5(feed.feedItem.content)}")
            feedViewModel.addBlock(feed.feedItem.pubkey,feed.feedItem.content)
        }else if (itemView.id == R.id.iv_avatar){
            startActivity(Intent(requireContext(),UserDetailActivity::class.java)
                .apply {
                    putExtra("pubkey",feed.feedItem.pubkey)
                })
        }
    }

    override fun onResume() {
        super.onResume()
        feedViewModel.reqFeed()
    }

    override fun onPause() {
        super.onPause()
        feedViewModel.stopSubFeed()
    }


}
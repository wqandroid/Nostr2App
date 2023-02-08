package nostr.postr.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import nostr.postr.R
import nostr.postr.databinding.FragmentFeedBinding
import nostr.postr.databinding.FragmentHomeBinding
import nostr.postr.feed.Feed
import nostr.postr.feed.FeedAdapter
import nostr.postr.feed.FeedViewModel
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
//        feedViewModel.reqFeed()
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
        }
    }

    override fun onClick(feed: Feed, itemView: View) {
        if (itemView.id == R.id.iv_more) {
            feedViewModel.addBlock(feed.feedItem.pubkey)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }


}
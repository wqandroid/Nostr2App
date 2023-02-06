package nostr.postr.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import nostr.postr.databinding.FragmentFeedBinding
import nostr.postr.db.FeedItem

class FeedFragment : Fragment() {

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
        feedViewModel.reqFeed()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter= FeedAdapter(mutableListOf())

        binding.rvFeed.adapter=adapter
        binding.rvFeed.layoutManager=LinearLayoutManager(requireContext())
        binding.rvFeed.hasFixedSize()

        feedViewModel.feedLiveData.observe(viewLifecycleOwner){
          adapter.updateData(it)
        }

        feedViewModel.loadFeedFromDB()

    }

}
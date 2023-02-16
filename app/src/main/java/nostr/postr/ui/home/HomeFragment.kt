package nostr.postr.ui.home

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import nostr.postr.R
import nostr.postr.databinding.FragmentFeedBinding
import nostr.postr.ui.ImageDetailActivity
import nostr.postr.ui.feed.Feed
import nostr.postr.ui.feed.FeedAdapter
import nostr.postr.ui.feed.PublishActivity
import nostr.postr.ui.feed.global.GlobalFeedActivity
import nostr.postr.ui.user.UserDetailActivity
import nostr.postr.util.MD5
import nostr.postr.util.UIUtils.makeGone
import nostr.postr.util.UIUtils.makeVisibility

class HomeFragment : Fragment(), FeedAdapter.ItemChildClickListener {
    private lateinit var binding: FragmentFeedBinding
    private val feedViewModel by viewModels<HomeViewModel>()

    private val adapter by lazy { FeedAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeedBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initViewModel()
    }

    private fun initView() {
        binding.rvFeed.adapter = adapter
        adapter.clickListener = this
        binding.rvFeed.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFeed.hasFixedSize()



        binding.mbtFeedLoad.setOnClickListener {
            feedViewModel.loadFeedFromDB()
            binding.mbtFeedLoad.makeGone()
        }

        binding.toolbar.setOnClickListener {
            requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)
                .openDrawer(GravityCompat.START)
        }

        binding.ivAdd.setOnClickListener {
            startActivity(Intent(requireContext(), PublishActivity::class.java))
        }
        binding.ivPublicFeed.setOnClickListener {
            startActivity(Intent(requireContext(), GlobalFeedActivity::class.java))
        }


        binding.emptyLayout.makeVisibility()
        binding.btEmptyClick.setOnClickListener {
            startActivity(Intent(requireContext(), GlobalFeedActivity::class.java))
        }

        anim()
    }

    private fun anim() {
        val animation = RotateAnimation(0f,360f,1,0.5f, 1,0.5f)
        animation.startOffset = 0 // 开始动画延迟
        animation.duration = 1800 // 动画持续时间
        animation.repeatCount = -1 // 重放次数（所以动画的播放次数=重放次数+1），为infinite时无限重复(-1也能实现无限)
        val mLinearInterpolator = LinearInterpolator() // 插值器
        animation.interpolator = mLinearInterpolator // 插值器，即影响动画的播放速度
        binding.ivPublicFeed.startAnimation(animation)
    }

    private fun initViewModel() {
        feedViewModel.feedLiveData.observe(viewLifecycleOwner) {
            adapter.differ.submitList(it)
            if (it.isNotEmpty()) {
                binding.emptyLayout.makeGone()
            }
        }
        //        feedViewModel.loadBlockUser()
        feedViewModel.loadFeedFromDB()

        feedViewModel.feedCountLiveData.observe(viewLifecycleOwner) {

            if (it > 1) {
                binding.mbtFeedLoad.makeVisibility()
                binding.mbtFeedLoad.text = "新增动态（$it）点击加载"
            } else {
                binding.mbtFeedLoad.makeGone()
            }
        }
        feedViewModel.reqMainUserInfo()
    }

    override fun onClick(feed: Feed, itemView: View) {
        when (itemView.id) {
            R.id.iv_more -> {
                Log.e("account", "block${MD5.md5(feed.feedItem.content)}")
                //            feedViewModel.addBlock(feed.feedItem.pubkey,feed.feedItem.content)
            }
            R.id.iv_avatar -> {
                startActivity(Intent(requireContext(), UserDetailActivity::class.java)
                    .apply {
                        putExtra("pubkey", feed.feedItem.pubkey)
                    })
            }
            R.id.tv_reply -> {
                startActivity(Intent(requireContext(), UserDetailActivity::class.java)
                    .apply {
                        putExtra("pubkey", feed.feedItem.getReplyTos()!![0])
                    })
            }
            R.id.iv_content_img -> {
                val intent = Intent(requireActivity(), ImageDetailActivity::class.java)
                intent.putExtra("img_url", feed.findImageUrl())
                startActivity(
                    intent,
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        requireActivity(),
                        itemView,
                        "search"
                    ).toBundle()
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
//        feedViewModel.reqFeed()
    }

    override fun onPause() {
        super.onPause()
//        feedViewModel.stopSubFeed()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}
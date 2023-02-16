package nostr.postr.ui.user.followlist

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import nostr.postr.core.AccountManger
import nostr.postr.core.BaseAct
import nostr.postr.databinding.ActivityUserFollowsListBinding

class UserFollowsListAct : BaseAct(),FollowAdapter.ItemChildClickListener {


    private val viewModel by viewModels<FollowListModel>()

    private val list = mutableListOf<FollowInfo>()

    private lateinit var binding: ActivityUserFollowsListBinding

    private val adapter by lazy {
        FollowAdapter(list).also {
            it.clickListener=this
        }
    }

    lateinit var publicKey: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserFollowsListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        showBack()
        publicKey = intent.getStringExtra("publicKey") ?: return
        viewModel.pubKey=publicKey
        binding.recyclerView.apply {
            this.adapter = this@UserFollowsListAct.adapter
            this.layoutManager = LinearLayoutManager(this@UserFollowsListAct)
        }

        viewModel.reqFollowers()

        viewModel.followUserList.observe(this) {
            list.clear()
            list.addAll(it)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onClick(feed: FollowInfo, itemView: View) {
        viewModel.addFlow(feed.pubkey)
    }


}
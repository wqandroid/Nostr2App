package nostr.postr.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nostr.postr.MyApplication
import nostr.postr.R
import nostr.postr.core.AccountManger
import nostr.postr.databinding.FragmentDashboardBinding
import nostr.postr.db.ChatRoom
import nostr.postr.db.NostrDB
import nostr.postr.ui.AppViewModel
import nostr.postr.ui.user.UserDetailActivity

class DashboardFragment : Fragment(), ChatAdapter.ItemChildClickListener {

    lateinit var binding: FragmentDashboardBinding


    private lateinit var adapter: ChatAdapter
    private val list = mutableListOf<ChatRoom>()
    private val viewModel by viewModels<DashboardViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.adapter = ChatAdapter(list).also {
            binding.recyclerView.adapter = it
        }
        this.adapter.clickListener = this
        binding.recyclerView.apply {
            this.layoutManager = LinearLayoutManager(requireContext())
        }
        this.viewModel.chatRoomLiveDat.observe(viewLifecycleOwner) {
            list.clear()
            list.addAll(it)
            adapter.notifyDataSetChanged()
        }

        viewModel.followList.observe(viewLifecycleOwner) {
            binding.tvFollowCount.text = "已关注(${it.count()})"
        }

        viewModel.subFollows(AccountManger.getPublicKey())
        viewModel.loadChat()
    }


    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onClick(feed: ChatRoom, itemView: View) {
        if (itemView.id == R.id.iv_avatar) {
            startActivity(
                Intent(requireContext(), UserDetailActivity::class.java)
                    .apply {
                        putExtra("pubkey", feed.sendTo)
                    })
        }
    }
}
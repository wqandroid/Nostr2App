package nostr.postr.ui.chat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import nostr.postr.R
import nostr.postr.core.AccountManger
import nostr.postr.databinding.FragmentDashboardBinding
import nostr.postr.db.ChatRoom
import nostr.postr.ui.chat.viewmodel.PrivateChatViewModel
import nostr.postr.ui.user.UserDetailActivity
import nostr.postr.util.UIUtils.makeGone
import nostr.postr.util.UIUtils.makeVisibility

class PrivateChatFragment : Fragment(), ChatListAdapter.ItemChildClickListener {

    lateinit var binding: FragmentDashboardBinding

    private val chatAdapter by lazy { ChatListAdapter() }
    private val viewModel by viewModels<PrivateChatViewModel>()

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

        binding.recyclerView.apply {
            this.layoutManager = LinearLayoutManager(requireContext())
            this.adapter = chatAdapter.also {
                it.clickListener = this@PrivateChatFragment
            }
        }
        this.viewModel.chatRoomLiveDat.observe(viewLifecycleOwner) { rooms ->
            binding.tvTitleChat.text = "Chat(${rooms.size})"
            chatAdapter.differ.submitList(rooms.filter {
                it.sendTo != AccountManger.getPublicKey() &&
                        it.content.isNotEmpty()
            }.sortedByDescending { it.lastUpdate })
        }

        viewModel.followList.observe(viewLifecycleOwner) {
            it.filter { it.pubkey == AccountManger.getPublicKey() }
            binding.tvFollowCount.text = "已关注(${it.count()})"
            if (it.isNotEmpty()) {
                binding.hz.makeVisibility()
                binding.llFollow.removeAllViews()
                it.sortedByDescending { it.userProfile?.picture }.forEach { follow ->
                    val root = LayoutInflater.from(requireContext())
                        .inflate(R.layout.view_avatar_follow, null)
                    val imageView = root.findViewById<ImageView>(R.id.iv_avatar)
                    binding.llFollow.addView(root)
                    Glide.with(this).load(follow.userProfile?.picture)
                        .into(imageView)

                    imageView.setOnClickListener {
                        startActivity(Intent(requireContext(), UserDetailActivity::class.java)
                            .apply {
                                putExtra("pubkey", follow.pubkey)
                            })
                    }
                }
            } else {
                binding.hz.makeGone()
            }
        }

        viewModel.subFollows(AccountManger.getPublicKey())

    }


    override fun onResume() {
        super.onResume()
        viewModel.loadChat()
    }

    override fun onClick(chatRoom: ChatRoom, itemView: View) {
        if (itemView.id == R.id.cl_root) {
            ChatActivity.startChat(requireActivity(), chatRoom.sendTo, chatRoom.roomId)
        } else if (itemView.id == R.id.iv_avatar) {
            startActivity(Intent(requireContext(), UserDetailActivity::class.java)
                .apply {
                    putExtra("pubkey", chatRoom.sendTo)
                })
        }
    }
}
package nostr.postr.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import nostr.postr.Utils
import nostr.postr.core.AccountManger
import nostr.postr.databinding.FmDialogLoginLayoutBinding
import nostr.postr.toNsec
import nostr.postr.ui.AppViewModel
import nostr.postr.util.UIUtils
import nostr.postr.util.UIUtils.makeGone
import nostr.postr.util.UIUtils.makeVisibility

class LoginBottomDialog : BottomSheetDialogFragment() {

    private lateinit var binding: FmDialogLoginLayoutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FmDialogLoginLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mdTips.makeGone()

        binding.tvInput.setEndIconOnClickListener {
            UIUtils.clipText(binding.edKey, requireContext())
        }

        binding.mbtAccess.setOnClickListener {
            if (Utils.verifyKey(binding.edKey.editableText.toString())) {
                AccountManger.login(binding.edKey.editableText.toString())
                (requireParentFragment() as AccountDrawFragment).updateLoginStatus()
                dismiss()
//                ViewModelProvider(requireActivity()).get(AppViewModel::class.java)
//                    .reqMainUserInfo()
            } else {
                Toast.makeText(requireContext(), "请输入正确的key", Toast.LENGTH_SHORT).show()
            }
        }
        binding.mbtCreateKey.setOnClickListener {
            binding.edKey.setText(Utils.privkeyCreate().toNsec())
            binding.mdTips.makeVisibility()
        }
    }


}
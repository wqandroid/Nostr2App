package nostr.postr.ui.account

import android.os.Binder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import nostr.postr.Utils
import nostr.postr.core.AccountManger
import nostr.postr.databinding.FmDialogLoginLayoutBinding
import nostr.postr.toHex
import nostr.postr.toNsec
import nostr.postr.util.UIUtils
import nostr.postr.util.UIUtils.makeGone
import nostr.postr.util.UIUtils.makeVisibility

class LoginBottomDialog:BottomSheetDialogFragment() {

    private lateinit var binding: FmDialogLoginLayoutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FmDialogLoginLayoutBinding.inflate(inflater,container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mdTips.makeGone()

        binding.tvInput.setEndIconOnClickListener {
            UIUtils.clipText(binding.edKey,requireContext())
        }

        binding.mbtAccess.setOnClickListener {
            AccountManger.login(binding.edKey.editableText.toString())
            (requireParentFragment() as AccountDrawFragment).updateLoginStatus()
            dismiss()
        }

        binding.mbtCreateKey.setOnClickListener {
            binding.edKey.setText(Utils.privkeyCreate().toNsec())
            binding.mdTips.makeVisibility()
        }

    }


}
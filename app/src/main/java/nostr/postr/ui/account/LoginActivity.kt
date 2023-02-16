package nostr.postr.ui.account

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import nostr.postr.MainActivity
import nostr.postr.Utils
import nostr.postr.core.AccountManger
import nostr.postr.core.BaseAct
import nostr.postr.databinding.FmDialogLoginLayoutBinding
import nostr.postr.toNsec
import nostr.postr.ui.user.UserViewModel
import nostr.postr.util.UIUtils
import nostr.postr.util.UIUtils.makeGone
import nostr.postr.util.UIUtils.makeVisibility

class LoginActivity : BaseAct() {

    private lateinit var binding: FmDialogLoginLayoutBinding

    private val viewModel by viewModels<UserViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FmDialogLoginLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.mdTips.makeGone()

        binding.tvInput.setEndIconOnClickListener {
            UIUtils.clipText(binding.edKey, this)
        }

        binding.mbtAccess.setOnClickListener {
            if (Utils.verifyKey(binding.edKey.editableText.toString())) {
                AccountManger.login(binding.edKey.editableText.toString())
//                viewModel.addFlow("9e9764b9415b2ff0e24733e3fe685922e79b4812c6ad412a9e05447153f05cbb",
//                    mutableListOf()
//                )
                startActivity(Intent(this,MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "请输入正确的key", Toast.LENGTH_SHORT).show()
            }
        }

        binding.mbtCreateKey.setOnClickListener {
            binding.edKey.setText(Utils.privkeyCreate().toNsec())
            binding.mdTips.makeVisibility()
        }
    }


}
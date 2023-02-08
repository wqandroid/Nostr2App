package nostr.postr.ui.account

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import nostr.postr.core.AccountManger
import nostr.postr.databinding.FragmentDrawLayoutBinding

class AccountDrawFragment : Fragment() {

    private lateinit var binding: FragmentDrawLayoutBinding

    private val viewModel by viewModels<AccountViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDrawLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mbtLogin.setOnClickListener {
            if (AccountManger.isLogin()) {
                AccountManger.logout()
                binding.mbtLogin.text = "Login"
                viewModel.user.value=null
            } else {
                AccountManger.login("nsec1cd3c5gaymh5xvqspwvcjpcv8p0neh7arah3rv767038sua48mdds8a3svd")
                binding.mbtLogin.text = "Logout"
//                Log.e("account","---->${AccountManger.getPublicKey()}")
                viewModel.reqProfile(AccountManger.getPublicKey())
                //c3638a23a4dde8660201733120e1870be79bfba3ede2367b5e7c4f0e76a7db5b

                LoginBottomDialog().show(childFragmentManager,"")
            }
        }

        binding.mswitch.setOnClickListener {
            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//                AppConfig.getInstance().saveNightModel(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//                AppConfig.getInstance().saveNightModel(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }

        viewModel.user.observe(viewLifecycleOwner) {
           if (it!=null){
               Glide.with(this).load(it.picture)
                   .into(binding.ivAvatar)
               binding.tvName.text = it.display_name ?: it.name
               binding.tvDesc.text=it.about
           }else{
               binding.tvName.text="未登录"
           }
        }

        if (AccountManger.isLogin()) {
//            viewModel.reqProfile(AccountManger.getPublicKey())
            viewModel.loadSelfProfile()
        }

    }

    override fun onResume() {
        super.onResume()

        if (AccountManger.isLogin()) {
            binding.mbtLogin.text = "Logout"
            viewModel.reqProfile(AccountManger.getPublicKey())
        } else {
            binding.mbtLogin.text = "Login"
        }
    }

}
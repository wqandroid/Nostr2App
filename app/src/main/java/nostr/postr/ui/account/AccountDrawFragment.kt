package nostr.postr.ui.account

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import nostr.postr.MyApplication
import nostr.postr.R
import nostr.postr.core.AccountManger
import nostr.postr.databinding.FragmentDrawLayoutBinding
import nostr.postr.db.NostrDB
import nostr.postr.db.UserProfile
import nostr.postr.ui.user.UserDetailActivity

class AccountDrawFragment : Fragment() {

    private lateinit var binding: FragmentDrawLayoutBinding


    val dis = CompositeDisposable()

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
                updateLoginStatus()
            } else {
                LoginBottomDialog().show(childFragmentManager, "")
            }
        }

        binding.ivDay.setOnClickListener {
            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                binding.ivDay.setImageResource(R.drawable.baseline_light_mode_24)
            } else {
                binding.ivDay.setImageResource(R.drawable.baseline_mode_night_24)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }

        updateLoginStatus()

        binding.ivAvatar.setOnClickListener {
            if (AccountManger.isLogin()) {
                startActivity(
                    Intent(requireContext(), UserDetailActivity::class.java)
                        .apply {
                            putExtra("pubkey", AccountManger.getPublicKey())
                        })
            } else {
                LoginBottomDialog().show(childFragmentManager, "")
            }
        }


    }

    private fun showProfile(it: UserProfile?) {
        if (AccountManger.isLogin() && it != null) {
            Glide.with(this).load(it.picture)
                .into(binding.ivAvatar)
            binding.tvName.text = it.display_name ?: it.name
            binding.tvDesc.text = it.about
        }
    }

    fun updateLoginStatus() {

        if (AccountManger.isLogin()) {
            binding.mbtLogin.text = "Logout"
            binding.tvName.text = ""
            binding.tvDesc.text = ""
            dis.add(
                    NostrDB.getDatabase(MyApplication._instance).profileDao()
                        .getUserInfoRx(AccountManger.getPublicKey())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            showProfile(it)
                        }

                )
        } else {
            binding.mbtLogin.text = "Login"
            binding.tvName.text = "未登录"
            binding.tvDesc.text = "未登录"
            binding.ivAvatar.setImageResource(R.mipmap.ic_launcher)
        }
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onDestroy() {
        super.onDestroy()
        dis.clear()
    }

}
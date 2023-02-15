package nostr.postr

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.color.DynamicColors
import com.tencent.mmkv.MMKV
import nostr.postr.core.WSClient
import nostr.postr.ui.AppViewModel

class MyApplication : Application() {


    override fun onCreate() {
        super.onCreate()
        _instance = this
        MMKV.initialize(this)
//        DynamicColors.applyToActivitiesIfAvailable(this)

        //链接所有ws
        WSClient.startConnection()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }



    companion object {
        lateinit var _instance: MyApplication
        fun getInstance(): MyApplication {
            return _instance
        }
        fun getViewModel(): AppViewModel {
            return ViewModelProvider.AndroidViewModelFactory(_instance).create(AppViewModel::class.java)
        }

    }

}
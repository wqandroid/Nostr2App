package nostr.postr

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.tencent.mmkv.MMKV

class MyApplication: Application() {


    override fun onCreate() {
        super.onCreate()
        _instance=this
        MMKV.initialize(this)
//        DynamicColors.applyToActivitiesIfAvailable(this)

        //链接所有ws
        Client.connect()
    }

    companion object{
        lateinit var _instance: MyApplication
        fun getInstance():MyApplication{
            return  _instance
        }
    }

}
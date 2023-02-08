package nostr.postr

import android.app.Application
import com.tencent.mmkv.MMKV

class MyApplication: Application() {


    override fun onCreate() {
        super.onCreate()
        _instance=this
        MMKV.initialize(this)
    }

    companion object{
        lateinit var _instance: MyApplication
        fun getInstance():MyApplication{
            return  _instance
        }
    }

}
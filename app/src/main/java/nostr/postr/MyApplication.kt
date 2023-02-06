package nostr.postr

import android.app.Application

class MyApplication: Application() {


    override fun onCreate() {
        super.onCreate()
        _instance=this
    }

    companion object{
        lateinit var _instance: MyApplication
        fun getInstance():MyApplication{
            return  _instance
        }
    }

}
package nostr.postr.core

import android.util.Log
import com.tencent.mmkv.MMKV
import nostr.postr.*

object AccountManger {


    private val mmkv = lazy {
        MMKV.defaultMMKV(1, "asd_wq")
    }

    fun isLogin(): Boolean {
        return mmkv.value.decodeString("pri_key","")!!.isNotEmpty()
    }


    fun getPublicKey(): String {
        return Utils.pubkeyCreate(mmkv.value.decodeString("pri_key")!!.bechToBytes()).toHex()
    }
//
//
//    fun loadpubKey(){
//        val or=Utils.pubkeyCreate(mmkv.value.decodeString("pri_key")!!.bechToBytes())
//        Log.e("account","o--$or")
//        Log.e("account","1--${or.toHex()}")
//        Log.e("account","2--${or.toNpub()}")
//        Log.e("account","3--${or.toNsec()}")
//
//    }

    fun login(priKey: String) {
        mmkv.value.encode("pri_key", priKey)
    }

    fun logout() {
        mmkv.value.encode("pri_key","")
    }
}
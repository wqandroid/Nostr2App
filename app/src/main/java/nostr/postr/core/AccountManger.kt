package nostr.postr.core

import android.util.Log
import com.tencent.mmkv.MMKV
import nostr.postr.*

object AccountManger {

    private val pKey="pri_key_2"

    private val mmkv = lazy {
        MMKV.defaultMMKV(1, "asd_wq")
    }

    fun isLogin(): Boolean {
        return mmkv.value.decodeString(pKey,"")!!.isNotEmpty()
    }


    fun getPublicKey(): String {
        if (isLogin()){
            return Utils.pubkeyCreate(mmkv.value.decodeString(pKey)!!.bechToBytes()).toHex()
        }else{
            throw  java.lang.IllegalStateException("please login")
        }
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
        mmkv.value.encode(pKey, priKey)
    }

    fun logout() {
        mmkv.value.encode(pKey,"")
    }
}
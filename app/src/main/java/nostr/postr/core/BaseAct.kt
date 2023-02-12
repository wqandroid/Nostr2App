package nostr.postr.core

import androidx.appcompat.app.AppCompatActivity
import io.reactivex.rxjava3.disposables.CompositeDisposable

open class BaseAct : AppCompatActivity() {


    var comDis = CompositeDisposable()


    override fun onDestroy() {
        super.onDestroy()
        comDis.clear()
    }

}
package nostr.postr.core

import android.R
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.rxjava3.disposables.CompositeDisposable

open class BaseAct : AppCompatActivity() {


    var comDis = CompositeDisposable()


    open fun showBack() {
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
        }
        title = ""
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        comDis.clear()
    }

}
package nostr.postr.util

import android.view.View

object UIUtils {
    open fun View.makeGone() {
        visibility = View.GONE
    }

    open fun View.makeVisibility() {
        visibility = View.VISIBLE
    }
}
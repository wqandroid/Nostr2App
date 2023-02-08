package nostr.postr.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.widget.EditText

object UIUtils {
    open fun View.makeGone() {
        visibility = View.GONE
    }

    open fun View.makeVisibility() {
        visibility = View.VISIBLE
    }


    fun copyStringText(text: CharSequence?, context: Context) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        // 将文本内容放到系统剪贴板里。
        cm.setPrimaryClip(ClipData.newPlainText(text, text))
    }
    fun clipText(editText: EditText, context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val bool = clipboard.hasPrimaryClip()
        if (bool) {
            clipboard.primaryClip?.getItemAt(0)?.let {
                if (it.text.isNotEmpty()) {
                    editText.setText(it.text)
                }
            }
        }
    }
}
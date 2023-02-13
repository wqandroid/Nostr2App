package nostr.postr.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.widget.EditText
import androidx.fragment.app.Fragment

object UIUtils {
    open fun View.makeGone() {
        visibility = View.GONE
    }

    open fun View.makeVisibility() {
        visibility = View.VISIBLE
    }

    fun Fragment.dp2px(context: Context, dipValue: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
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

     fun parseTime(time: Long): String {
        val now = System.currentTimeMillis() / 1000
        val du = now - time
        return if (du < 60) {
            "$du 秒钟前"
        } else if (du < 60 * 60) {
            "${du / 60} 分钟前"
        } else if (du < 24 * 60 * 60) {
            "${du / 3600} 小时前"
        } else {
            "${du / (3600 * 24)} 天前"
        }

    }

}
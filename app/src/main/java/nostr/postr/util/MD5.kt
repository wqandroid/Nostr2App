package nostr.postr.util

import java.security.MessageDigest

/**
 * 说明：MD5处理
 * 创建人：FH Q313596790
 * 修改时间：2014年9月20日
 */
object MD5 {
    fun md5(str: String): String {
        var str = str
        try {
            val md = MessageDigest.getInstance("MD5")
            md.update(str.toByteArray())
            val b = md.digest()
            var i: Int
            val buf = StringBuffer("")
            for (offset in b.indices) {
                i = b[offset].toInt()
                if (i < 0) i += 256
                if (i < 16) buf.append("0")
                buf.append(Integer.toHexString(i))
            }
            str = buf.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return str
    }

    fun hexdigest(paramArrayOfByte: ByteArray?): String {
        val hexDigits = charArrayOf(
            48.toChar(),
            49.toChar(),
            50.toChar(),
            51.toChar(),
            52.toChar(),
            53.toChar(),
            54.toChar(),
            55.toChar(),
            56.toChar(),
            57.toChar(),
            97.toChar(),
            98.toChar(),
            99.toChar(),
            100.toChar(),
            101.toChar(),
            102.toChar()
        )
        try {
            val localMessageDigest = MessageDigest.getInstance("MD5")
            localMessageDigest.update(paramArrayOfByte)
            val arrayOfByte = localMessageDigest.digest()
            val arrayOfChar = CharArray(32)
            var i = 0
            var j = 0
            while (true) {
                if (i >= 16) {
                    return String(arrayOfChar)
                }
                val k = arrayOfByte[i].toInt()
                arrayOfChar[j] = hexDigits[0xF and (k ushr 4)]
                arrayOfChar[++j] = hexDigits[k and 0xF]
                i++
                j++
            }
        } catch (e: Exception) {
        }
        return ""
    }
}
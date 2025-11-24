package io.github.scola.qart

import android.graphics.Bitmap
import android.graphics.Color
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Writer

/**
 * Created by shaozheng on 2016/9/1.
 */
object Util {
    fun saveBitmap(bitmap: Bitmap, path: String) {
        try {
            FileOutputStream(path).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun calculateColorGrayValue(color: Int): Int {
        val GS_RED = 0.299
        val GS_GREEN = 0.587
        val GS_BLUE = 0.114
        return (Color.red(color) * GS_RED + Color.green(color) * GS_GREEN + Color.blue(color) * GS_BLUE).toInt()
    }

    @Throws(IOException::class)
    fun copy(src: File, dst: File) {
        src.copyTo(dst, overwrite = true)
    }

    @Throws(IOException::class)
    fun saveConfig(dst: File, config: String) {
        dst.writeText(config)
    }
}

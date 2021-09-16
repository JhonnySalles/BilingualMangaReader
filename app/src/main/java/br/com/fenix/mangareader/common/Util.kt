package br.com.fenix.mangareader.common

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.or

class Util {
    companion object Storage {
        fun getScreenDpWidth(context: Context): Int {
            val displayMetrics = context.resources.displayMetrics
            return Math.round(displayMetrics.widthPixels / displayMetrics.density)
        }

        fun getHeapSize(context: Context): Int {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val isLargeHeap = context.applicationInfo.flags and ApplicationInfo.FLAG_LARGE_HEAP != 0
            var memoryClass = am.memoryClass
            if (isLargeHeap)
                memoryClass = am.largeMemoryClass

            return 1024 * memoryClass
        }

        fun calculateBitmapSize(bitmap: Bitmap): Int {
            val sizeInBytes: Int
            sizeInBytes = bitmap.byteCount
            return sizeInBytes / 1024
        }

        fun isImage(filename: String): Boolean {
            return filename.lowercase(Locale.getDefault())
                .matches(Regex(".*\\.(jpg|jpeg|bmp|gif|png|webp)$"))
        }

        fun isZip(filename: String): Boolean {
            return filename.lowercase(Locale.getDefault()).matches(Regex(".*\\.(zip|cbz)$"))
        }

        fun isRar(filename: String): Boolean {
            return filename.lowercase(Locale.getDefault()).matches(Regex(".*\\.(rar|cbr)$"))
        }

        fun isTarball(filename: String): Boolean {
            return filename.lowercase(Locale.getDefault()).matches(Regex(".*\\.(cbt)$"))
        }

        fun isSevenZ(filename: String): Boolean {
            return filename.lowercase(Locale.getDefault()).matches(Regex(".*\\.(cb7|7z)$"))
        }

        fun isArchive(filename: String): Boolean {
            return isZip(filename) || isRar(filename) || isTarball(filename) || isSevenZ(filename)
        }

        fun getDeviceWidth(context: Context): Int {
            val displayMetrics = context.resources.displayMetrics
            return Math.round(displayMetrics.widthPixels / displayMetrics.density)
        }

        fun getDeviceHeight(context: Context): Int {
            val displayMetrics = context.resources.displayMetrics
            return Math.round(displayMetrics.heightPixels / displayMetrics.density)
        }

        fun MD5(string: String?): String? {
            return try {
                val strBytes = string?.toByteArray()
                val messageDigest = MessageDigest.getInstance("MD5")
                val digest = messageDigest.digest(strBytes)
                val sb = StringBuffer()
                for (i in digest.indices)
                    sb.append(
                        Integer.toHexString((digest[i] and 0xFF.toByte() or 0x100.toByte()).toInt())
                            .substring(1, 3)
                    )

                sb.toString()
            } catch (e: NoSuchAlgorithmException) {
                string?.replace("/", ".")
            }
        }

        fun calculateInSampleSize(
            options: BitmapFactory.Options,
            reqWidth: Int,
            reqHeight: Int
        ): Int {
            val height = options.outHeight
            val width = options.outWidth
            var inSampleSize = 1
            if (height > reqHeight || width > reqWidth) {
                val halfHeight = height / 2
                val halfWidth = width / 2

                while (halfHeight / inSampleSize > reqHeight
                    && halfWidth / inSampleSize > reqWidth
                ) {
                    inSampleSize *= 2
                }
            }
            return inSampleSize
        }

        fun calculateMemorySize(context: Context, percentage: Int): Int {
            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryClass = activityManager.largeMemoryClass
            return 1024 * 1024 * memoryClass / percentage
        }

        fun getCacheFile(context: Context, identifier: String?): File? {
            return File(context.externalCacheDir, this.MD5(identifier))
        }

        fun toByteArray(`is`: InputStream): ByteArray? {
            val output = ByteArrayOutputStream()
            return try {
                val b = ByteArray(4096)
                var n = 0
                while (`is`.read(b).also { n = it } != -1) {
                    output.write(b, 0, n)
                }
                output.toByteArray()
            } finally {
                output.close()
            }
        }

        fun imateToByteArray(image: Bitmap): ByteArray? {
            val output = ByteArrayOutputStream()
            return output.use { output ->
                image.compress(Bitmap.CompressFormat.JPEG, 100, output)
                output.toByteArray()
            }
        }

        fun encodeImageBase64(image: Bitmap): String {
            return android.util.Base64.encodeToString(
                imateToByteArray(image),
                android.util.Base64.DEFAULT
            )
        }

        fun decodeImageBase64(image: String): Bitmap {
            val imageBytes = android.util.Base64.decode(image, android.util.Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        }
    }
}
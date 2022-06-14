package br.com.fenix.bilingualmangareader.util.helpers

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import br.com.fenix.bilingualmangareader.service.parses.Parse
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import kotlin.experimental.and

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
            val sizeInBytes: Int = bitmap.byteCount
            return sizeInBytes / 1024
        }

        fun isJson(filename: String): Boolean {
            return filename.lowercase(Locale.getDefault())
                .matches(Regex(".*\\.(json)$"))
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

        fun MD5(string: String): String {
            return try {
                val md = MessageDigest.getInstance("MD5")
                return BigInteger(1, md.digest(string.toByteArray())).toString(16).padStart(32, '0')
            } catch (e: NoSuchAlgorithmException) {
                string.replace("/", ".")
            }
        }

        fun MD5(image: InputStream): String {
            return try {
                val buffer = ByteArray(1024)
                val digest = MessageDigest.getInstance("MD5")
                var numRead = 0
                while (numRead != -1) {
                    numRead = image.read(buffer)
                    if (numRead > 0) digest.update(buffer, 0, numRead)
                }
                val md5Bytes = digest.digest()
                var returnVal = ""
                for (element in md5Bytes)
                    returnVal += Integer.toString((element and 0xff.toByte()) + 0x100, 16).substring(1)

                returnVal
            } catch (e: Exception) {
                ""
            } finally {
                closeInputStream(image)
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

        fun toByteArray(`is`: InputStream): ByteArray? {
            val output = ByteArrayOutputStream()
            return try {
                val b = ByteArray(4096)
                var n: Int
                while (`is`.read(b).also { n = it } != -1) {
                    output.write(b, 0, n)
                }
                output.toByteArray()
            } finally {
                output.close()
            }
        }

        fun imageToByteArray(image: Bitmap): ByteArray? {
            val output = ByteArrayOutputStream()
            return output.use { otp ->
                image.compress(Bitmap.CompressFormat.JPEG, 100, otp)
                otp.toByteArray()
            }
        }

        fun encodeImageBase64(image: Bitmap): String {
            return android.util.Base64.encodeToString(
                imageToByteArray(image),
                android.util.Base64.DEFAULT
            )
        }

        fun decodeImageBase64(image: String): Bitmap {
            val imageBytes = android.util.Base64.decode(image, android.util.Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        }

        fun imageToInputStream(image: Bitmap): InputStream {
            val output = ByteArrayOutputStream()
            return output.use { otp ->
                image.compress(Bitmap.CompressFormat.JPEG, 100, otp)
                ByteArrayInputStream(output.toByteArray())
            }
        }

        fun closeInputStream(input: InputStream?) {
            if (input != null) {
                try {
                    input.close()
                } catch (e: Exception) {
                }
            }
        }

        fun destroyParse(parse: Parse?) {
            if (parse != null) {
                try {
                    parse.destroy()
                } catch (e: Exception) {
                }
            }
        }

        fun getNameFromPath(path: String): String {
            return if (path.contains('/'))
                path.substringAfterLast("/")
            else if (path.contains('\\'))
                path.substringAfterLast('\\')
            else
                path
        }

        fun normalizeFilePath(path: String): String {
            var folder: String = path

            if (folder.contains("primary"))
                folder = folder.replaceFirst("primary", "emulated/0" )

            if (folder.contains("/tree"))
                folder = folder.replace("/tree", "/storage").replace(":", "/")
            else if (folder.contains("/document"))
                folder = folder.replace("/document", "/storage").replace(":", "/")

            return folder
        }

        fun getChapterFromPath(path: String): Float {
            if (path.isEmpty()) return -1f

            var folder = if (path.contains('/', true))
                path.replaceAfterLast('/', "").replace("/", "", false).lowercase()
            else
                path.replaceAfterLast('\\', "").replace("\\", "", false).lowercase()

            folder = if (folder.contains("capitulo", true))
                folder.substringAfterLast("capitulo").replace("capitulo", "", true)
            else if (folder.contains("capítulo", true))
                folder.substringAfterLast("capítulo").replace("capítulo", "", true)
            else folder

            return folder.toFloatOrNull() ?: -1f
        }

    }
}
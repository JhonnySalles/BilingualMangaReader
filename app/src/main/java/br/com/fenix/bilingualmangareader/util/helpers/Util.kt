package br.com.fenix.bilingualmangareader.util.helpers

import android.app.ActivityManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.DisplayMetrics
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Library
import br.com.fenix.bilingualmangareader.model.enums.Languages
import br.com.fenix.bilingualmangareader.model.enums.Libraries
import br.com.fenix.bilingualmangareader.service.parses.Parse
import br.com.fenix.bilingualmangareader.util.constants.DataBaseConsts
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.experimental.and
import kotlin.math.roundToInt


class Util {
    companion object Utils {
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

        fun dpToPx(context: Context, dp: Int): Int {
            val displayMetrics = context.resources.displayMetrics
            return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
        }

        fun pxToDp(context: Context, px: Int): Int {
            val displayMetrics = context.resources.displayMetrics
            return (px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
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

        fun getNameWithoutExtensionFromPath(path: String): String {
            var name = if (path.contains('/'))
                path.substringAfterLast("/")
            else if (path.contains('\\'))
                path.substringAfterLast('\\')
            else
                path

            name = if (name.contains('.'))
                name.substringBefore(".")
            else
                name

            return name
        }

        fun getExtensionFromPath(path: String): String {
            return if (path.contains('.'))
                path.substringAfterLast(".")
            else
                path
        }

        fun normalizeNameCache(name: String): String {
            val normalize = if (name.contains("-"))
                name.substringBefore("-")
            else if (name.contains(" "))
                name.substringBefore(" ")
            else name

            val random = (0..1000).random()
            return normalize.replace("[^\\w\\d]".toRegex(), "").trim().plus(random).lowercase()
        }

        fun normalizeFilePath(path: String): String {
            var folder: String = path

            if (folder.contains("primary"))
                folder = folder.replaceFirst("primary", "emulated/0")

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

        fun getFolderFromPath(path: String): String {
            // Two validations are needed, because the rar file only has the base values, with the beginning already in the folder when it exists
            val folder = if (path.contains('/'))
                path.replaceAfterLast('/', "").substring(0, path.lastIndexOf('/'))
            else if (path.contains('\\'))
                path.replaceAfterLast('\\', "").substring(0, path.lastIndexOf('\\'))
            else
                path

            return if (folder.contains('/'))
                folder.replaceBeforeLast('/', "").replaceFirst("/", "")
            else if (folder.contains('\\'))
                folder.replaceBeforeLast('\\', "").replaceFirst("/", "")
            else
                folder
        }

        var googleLang: String = ""
        private var mapLanguages: HashMap<String, Languages>? = null
        fun getLanguages(context: Context): HashMap<String, Languages> {
            return if (mapLanguages != null)
                mapLanguages!!
            else {
                val languages = context.resources.getStringArray(R.array.languages)
                googleLang = languages[3]
                mapLanguages = hashMapOf(
                    languages[0] to Languages.PORTUGUESE,
                    languages[1] to Languages.ENGLISH,
                    languages[2] to Languages.JAPANESE,
                    languages[3] to Languages.PORTUGUESE_GOOGLE
                )
                mapLanguages!!
            }
        }

        fun stringToLanguage(context: Context, language: String): Languages? {
            val mapLanguages = getLanguages(context)
            return if (mapLanguages.containsKey(language)) mapLanguages[language] else null
        }

        fun languageToString(context: Context, language: Languages): String {
            val mapLanguages = getLanguages(context)
            return if (mapLanguages.containsValue(language))
                mapLanguages.filter { language == it.value }.keys.first()
            else
                ""
        }

        fun choiceLanguage(
            context: Context,
            theme: Int = R.style.AppCompatMaterialAlertDialogStyle,
            ignoreGoogle: Boolean = true,
            setLanguage: (language: Languages) -> (Unit)
        ) {
            val mapLanguage = getLanguages(context)
            val items = if (ignoreGoogle)
                mapLanguage.keys.filterNot { it == googleLang }.toTypedArray()
            else
                mapLanguage.keys.toTypedArray()

            MaterialAlertDialogBuilder(context, theme)
                .setTitle(context.resources.getString(R.string.languages_choice))
                .setItems(items) { _, selected ->
                    val language = mapLanguage[items[selected]]
                    if (language != null)
                        setLanguage(language)
                }
                .show()
        }

        fun getNameFromMangaTitle(text: String): String {
            return text.substringBeforeLast("Volume").replace(" - ", "").trim()
        }

        fun formatterDate(context: Context, date: Date?): String {
            if (date == null)
                return context.getString(R.string.date_format_unknown)
            val preferences = GeneralConsts.getSharedPreferences(context)
            val pattern = preferences.getString(GeneralConsts.KEYS.SYSTEM.FORMAT_DATA, "yyyy-MM-dd")
            return SimpleDateFormat(pattern, Locale.getDefault()).format(date)
        }

        fun setBold(text: String): String =
            "<b>$text</b>"

        fun getDivideStrings(text: String, delimiter: Char = '\n', occurrences: Int = 10): Pair<String, String> {
            var postion = text.length
            var occurence = 0
            for ((i, c) in text.withIndex()) {
                if (c == delimiter) {
                    occurence++
                    postion = i
                }
                if (occurence >= occurrences)
                    break
            }

            val string1 = text.substring(0, postion)
            val string2 = if (postion >= text.length) "" else text.substring(postion, text.length)

            return Pair(string1, string2)
        }
    }
}

class FileUtil(val context: Context) {

    /**
     * Copies an asset file from assets to phone internal storage, if it doesn't already exist
     * Will be copied to path <prefix> + <assetName> in files directory
     * Returns true if copied, false otherwise (including if file already exists)
     */
    fun copyAssetToFilesIfNotExist(prefix: String, assetName: String, dir: String = ""): Boolean {
        val directory = dir.ifEmpty { context.filesDir.absolutePath }
        val file = File(directory, prefix + assetName)
        if (file.exists())
            return false

        val inputStream: InputStream = context.assets.open(assetName)
        File(directory, prefix).mkdirs()
        // Copy in 10mb chunks to avoid going oom for larger files
        inputStream.copyTo(file.outputStream(), 10000)
        inputStream.close()
        return true
    }

}

class LibraryUtil {
    companion object LibraryUtils {
        fun getDefault(context: Context): Library {
            val preference: SharedPreferences = GeneralConsts.getSharedPreferences(context)
            val path = preference.getString(GeneralConsts.KEYS.LIBRARY.FOLDER, "") ?: ""
            return Library(-1, Libraries.DEFAULT.name, path)
        }
    }
}
package br.com.fenix.bilingualmangareader.util.helpers

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.*
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Library
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.model.enums.Languages
import br.com.fenix.bilingualmangareader.model.enums.Themes
import br.com.fenix.bilingualmangareader.service.parses.Parse
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.*
import java.lang.Math.abs
import java.math.BigInteger
import java.nio.channels.FileChannel
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.experimental.and
import kotlin.math.max
import kotlin.math.min
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
            val displayMetrics = Resources.getSystem().displayMetrics
            return Math.round(displayMetrics.widthPixels / displayMetrics.density)
        }

        fun getDeviceHeight(context: Context): Int {
            val displayMetrics = Resources.getSystem().displayMetrics
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

        fun toOutputStream(`is`: InputStream): OutputStream {
            val output = ByteArrayOutputStream()
            val b = ByteArray(4096)
            var n: Int
            while (`is`.read(b).also { n = it } != -1) {
                output.write(b, 0, n)
            }
            return output
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

        fun closeOutputStream(output: OutputStream?) {
            if (output != null) {
                try {
                    output.close()
                } catch (e: Exception) {
                }
            }
        }

        fun destroyParse(parse: Parse?, isClearCache: Boolean = true) {
            if (parse != null) {
                try {
                    parse.destroy(isClearCache)
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

        fun getNameWithoutVolumeAndChapter(manga: String): String {
            if (manga.isEmpty()) return manga

            var name = manga

            if (name.contains(" - "))
                name = name.substringBeforeLast(" - ")

            name = if (name.contains("volume", true))
                name.substringBeforeLast("volume", "").replace("volume", "", true)
            else if (name.contains("capitulo", true))
                name.substringBeforeLast("capitulo").replace("capitulo", "", true)
            else if (name.contains("capítulo", true))
                name.substringBeforeLast("capítulo").replace("capítulo", "", true)
            else name

            return name
        }

        fun getExtensionFromPath(path: String): String {
            return if (path.contains('.'))
                path.substringAfterLast(".")
            else
                path
        }

        fun normalizeNameCache(name: String, prefix: String = "", isRandom: Boolean = true): String {
            val normalize = if (name.contains("-"))
                name.substringBefore("-")
            else if (name.contains(" "))
                name.substringBefore(" ")
            else name

            val random = if (isRandom) (0..1000).random() else ""
            return prefix + normalize.replace("[^\\w\\d ]".toRegex(), "").trim().plus(random).lowercase()
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


        private fun getNumberAtEnd(str: String): String {
            var numbers = ""
            val m: Matcher = Pattern.compile("\\d+$|\\d+\\w$|\\d+\\.\\d+$|(\\(|\\{|\\[)\\d+(\\)|\\]|\\})$").matcher(str)
            while (m.find())
                numbers = m.group()

            return numbers
        }

        private fun getPadding(name: String, numbers: String): String {
            return if (name.contains(Regex("\\d+$")))
                numbers.padStart(10, '0')
            else if (name.contains(Regex("\\d+\\w\$")))
                numbers.replace(Regex("\\w\$"), "").padStart(10, '0') + numbers.replace(Regex("\\d+"), "")
            else if (name.contains(Regex("\\d+\\.\\d+\$")))
                numbers.replace(Regex("\\.\\d+\$"), "").padStart(10, '0') + '.' + numbers.replace(Regex("\\d+\\."), "")
            else if (name.contains(Regex("(\\(|\\{|\\[)\\d+(\\)|\\]|\\})$")))
                numbers.replace(Regex("[^0-9]"), "").padStart(10, '0')
            else
                numbers
        }

        fun getNormalizedNameOrdering(path: String): String {
            val name: String = getNameWithoutExtensionFromPath(path)
            val numbers = getNumberAtEnd(name)
            return if (numbers.isEmpty())
                getNameFromPath(path)
            else
                name.substring(0, name.lastIndexOf(numbers)) + getPadding(name, numbers) + getExtensionFromPath(path)
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

        private var mapThemes: HashMap<String, Themes>? = null
        fun getThemes(context: Context): HashMap<String, Themes> {
            return if (mapThemes != null)
                mapThemes!!
            else {
                val themes = context.resources.getStringArray(R.array.themes)
                mapThemes = hashMapOf(
                    themes[0] to Themes.ORIGINAL,
                    themes[1] to Themes.BLOOD_RED,
                    themes[2] to Themes.BLUE,
                    themes[3] to Themes.FOREST_GREEN,
                    themes[4] to Themes.GREEN,
                    themes[5] to Themes.NEON_BLUE,
                    themes[6] to Themes.NEON_GREEN,
                    themes[7] to Themes.OCEAN_BLUE,
                    themes[8] to Themes.PINK,
                    themes[9] to Themes.RED,
                )
                mapThemes!!
            }
        }

        fun themeDescription(context: Context, themes: Themes): String {
            val mapThemes = getThemes(context)
            return if (mapThemes.containsValue(themes))
                mapThemes.filter { themes == it.value }.keys.first()
            else
                ""
        }

        fun choiceLanguage(
            context: Context,
            theme: Int = R.style.AppCompatMaterialAlertList,
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

        fun setVerticalText(text: String): String {
            var vertical: String = ""
            for (c in text)
                vertical += c + "\n"

            return vertical
        }

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

        @ColorInt
        fun Context.getColorFromAttr(
            @AttrRes attrColor: Int,
            typedValue: TypedValue = TypedValue(),
            resolveRefs: Boolean = true
        ): Int {
            theme.resolveAttribute(attrColor, typedValue, resolveRefs)
            return typedValue.data
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

    fun copyFile(fromFile: FileInputStream, toFile: FileOutputStream) {
        var fromChannel: FileChannel? = null
        var toChannel: FileChannel? = null
        try {
            fromChannel = fromFile.channel
            toChannel = toFile.channel
            fromChannel.transferTo(0, fromChannel.size(), toChannel)
        } finally {
            try {
                fromChannel?.close()
            } finally {
                toChannel?.close()
            }
        }
    }

    fun copyName(file: File) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied Text", file.name)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(
            context,
            context.getString(R.string.action_copy_name, file.name),
            Toast.LENGTH_LONG
        ).show()
    }

    fun copyName(manga: Manga) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied Text", manga.fileName)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(
            context,
            context.getString(R.string.action_copy_name, manga.fileName),
            Toast.LENGTH_LONG
        ).show()
    }

}

class MsgUtil {
    companion object MsgUtil {
        fun validPermission(grantResults: IntArray): Boolean {
            var permiss = true
            for (grant in grantResults)
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    permiss = false
                    break
                }
            return permiss
        }

        fun validPermission(context: Context, grantResults: IntArray) {
            if (!validPermission(grantResults))
                AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle)
                    .setTitle(context.getString(R.string.alert_permission_files_access_denied_title))
                    .setMessage(context.getString(R.string.alert_permission_files_access_denied))
                    .setPositiveButton(R.string.action_neutral) { _, _ -> }.create().show()
        }

        inline fun alert(
            context: Context,
            title: String,
            message: String,
            theme: Int = R.style.AppCompatMaterialAlertDialog,
            crossinline action: (dialog: DialogInterface, which: Int) -> Unit
        ) {
            MaterialAlertDialogBuilder(context, theme)
                .setTitle(title).setMessage(message)
                .setPositiveButton(
                    R.string.action_positive
                ) { dialog, which ->
                    action(dialog, which)
                }
                .create().show()
        }

        inline fun alert(
            context: Context,
            title: String,
            message: String,
            theme: Int = R.style.AppCompatMaterialAlertDialog,
            crossinline positiveAction: (dialog: DialogInterface, which: Int) -> Unit,
            crossinline negativeAction: (dialog: DialogInterface, which: Int) -> Unit
        ) {
            MaterialAlertDialogBuilder(context, theme)
                .setTitle(title).setMessage(message)
                .setPositiveButton(
                    R.string.action_positive
                ) { dialog, which ->
                    positiveAction(dialog, which)
                }
                .setNegativeButton(
                    R.string.action_negative
                ) { dialog, which ->
                    negativeAction(dialog, which)
                }
                .create().show()
        }

        inline fun error(
            context: Context,
            title: String,
            message: String,
            theme: Int = R.style.AppCompatMaterialErrorDialogStyle,
            crossinline action: (dialog: DialogInterface, which: Int) -> Unit
        ) {
            MaterialAlertDialogBuilder(context, theme)
                .setTitle(title).setMessage(message)
                .setPositiveButton(
                    R.string.action_positive
                ) { dialog, which ->
                    action(dialog, which)
                }
                .create().show()
        }
    }
}

class LibraryUtil {
    companion object LibraryUtils {
        fun getDefault(context: Context): Library {
            val preference: SharedPreferences = GeneralConsts.getSharedPreferences(context)
            val path = preference.getString(GeneralConsts.KEYS.LIBRARY.FOLDER, "") ?: ""
            return Library(GeneralConsts.KEYS.LIBRARY.DEFAULT, context.getString(R.string.library_default), path)
        }
    }
}

class ImageUtil {
    companion object ImageUtils {
        private var initTouchDown = 0L
        private var initPos: PointF = PointF(0f, 0f)
        private var mScaleFactor = 1.0f

        @SuppressLint("ClickableViewAccessibility")
        fun setZoomPinch(context: Context, image: ImageView, oneClick: () -> Unit) {
            val mScaleListener = object : SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    mScaleFactor *= detector.scaleFactor
                    mScaleFactor = max(1.0f, min(mScaleFactor, 5.0f))
                    image.scaleX = mScaleFactor
                    image.scaleY = mScaleFactor
                    return true
                }
            }
            val mScaleGestureDetector = ScaleGestureDetector(context, mScaleListener)
            image.setOnTouchListener { view: View, event: MotionEvent ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initTouchDown = System.currentTimeMillis()
                        initPos = PointF(event.x, event.y)
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        image.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(300L)
                            .setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator?) {
                                    super.onAnimationEnd(animation)
                                    mScaleFactor = 1.0f
                                    image.scaleX = mScaleFactor
                                    image.scaleY = mScaleFactor
                                }
                            }).start()

                        val isTouchDuration = System.currentTimeMillis() - initTouchDown < 300
                        val isTouchLength = abs(event.x - initPos.x) + abs(event.y - initPos.y) < 10

                        if (isTouchLength && isTouchDuration)
                            view.performClick()
                    }
                    else -> {
                        mScaleGestureDetector.onTouchEvent(event)
                    }
                }

                true
            }

            image.setOnClickListener { oneClick() }
        }
    }
}
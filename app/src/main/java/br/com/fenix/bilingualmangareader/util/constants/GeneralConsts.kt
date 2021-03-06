package br.com.fenix.bilingualmangareader.util.constants

import android.content.Context
import android.content.SharedPreferences
import br.com.fenix.bilingualmangareader.MainActivity
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class GeneralConsts private constructor() {
    companion object {
        fun getCacheDir(context: Context): File? {
            return context.externalCacheDir
        }

        fun getSharedPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences(KEYS.PREFERENCE_NAME, Context.MODE_PRIVATE)
        }

        fun formatterDate(context: Context, dateTime: LocalDateTime): String {
            val preferences = getSharedPreferences(context)
            val pattern = preferences.getString(KEYS.SYSTEM.FORMAT_DATA, "yyyy-MM-dd")
            return dateTime.format(DateTimeFormatter.ofPattern(pattern))
        }

        fun formatterDateTime(context: Context, dateTime: LocalDateTime): String {
            val preferences = getSharedPreferences(context)
            val pattern = preferences.getString(KEYS.SYSTEM.FORMAT_DATA, "yyyy-MM-dd") + " hh:mm:ss a"
            return dateTime.format(DateTimeFormatter.ofPattern(pattern))
        }
    }

    object KEYS {
        const val PREFERENCE_NAME = "SHARED_PREFS"

        object LIBRARY {
            const val FOLDER = "LIBRARY_FOLDER"
            const val ORDER = "LIBRARY_ORDER"
            const val ORIENTATION = "LAST_ORIENTATION"
            const val LIBRARY_TYPE = "LAST_LIBRARY_TYPE"
        }

        object SUBTITLE {
            const val FOLDER = "SUBTITLE_FOLDER"
            const val LANGUAGE = "SUBTITLE_LANGUAGE"
            const val TRANSLATE = "SUBTITLE_TRANSLATE"
        }

        object READER {
            const val READER_MODE = "READER_MODE"
            const val PAGE_MODE = "READER_PAGE_MODE"
        }

        object SYSTEM {
            const val LANGUAGE = "SYSTEM_LANGUAGE"
            const val FORMAT_DATA = "SYSTEM_FORMAT_DATA"
        }

        object MANGA {
            const val NAME = "MANGA_NAME"
            const val MARK = "MANGA_MARK"
            const val PAGE_NUMBER = "PAGE_NUMBER"
        }

        object OBJECT {
            const val MANGA = "MANGA_OBJECT"
            const val FILE = "FILE_OBJECT"
            const val PAGELINK = "PAGE_LINK"
        }

        object COLOR_FILTER {
            const val CUSTOM_FILTER = "CUSTOM_FILTER"
            const val GRAY_SCALE = "GRAY_SCALE"
            const val INVERT_COLOR = "INVERT_COLOR"
            const val COLOR_RED = "COLOR_RED"
            const val COLOR_BLUE = "COLOR_BLUE"
            const val COLOR_GREEN = "COLOR_GREEN"
            const val COLOR_ALPHA = "COLOR_ALPHA"
            const val SEPIA = "SEPIA"
            const val BLUE_LIGHT = "BLUE_LIGHT"
            const val BLUE_LIGHT_ALPHA = "BLUE_LIGHT_ALPHA"
        }

        object PAGE_LINK {
            const val USE_IN_SEARCH_TRANSLATE = "USE_PAGE_LINK_IN_SEARCH_TRANSLATE"
            const val USE_DUAL_PAGE_CALCULATE = "USE_DUAL_PAGE_CALCULATE"
            const val USE_PAGE_PATH_FOR_LINKED = "USE_PAGE_PATH_FOR_LINKED"
        }
    }

    object TAG {
        const val LOG = "MangaReader"
        const val STACKTRACE = "[STACKTRACE] "

        object DATABASE {
            const val INSERT = "$LOG - [DATABASE] INSERT"
            const val SELECT = "$LOG - [DATABASE] SELECT"
            const val DELETE = "$LOG - [DATABASE] DELETE"
            const val LIST = "$LOG - [DATABASE] LIST"
        }
    }

    object CONFIG {
        val DATA_FORMAT = listOf("dd/MM/yyyy", "MM/dd/yy", "dd/MM/yy", "yyyy-MM-dd")
    }

    object CACHE_FOLDER {
        const val TESSERACT = "tesseract"
        const val RAR = "RarTemp"
        const val COVERS = "Covers"
        const val LINKED = "Linked"
        const val IMAGE = "Image"
        const val A = "a"
        const val B = "b"
        const val C = "c"
        const val D = "d"
    }

    object SCANNER {
        const val POSITION = "POSITION"
        const val MESSAGE_MANGA_UPDATE_FINISHED = 0
        const val MESSAGE_MANGA_UPDATED_ADD = 1
        const val MESSAGE_MANGA_UPDATED_REMOVE = 2
    }

    object MANGA_DETAIL {
        const val REQUEST_ENDED = 999
    }

    object REQUEST {
        const val PERMISSION_DRAW_OVERLAYS = 505
        const val PERMISSION_DRAW_OVERLAYS_FLOATING_OCR = 506
        const val PERMISSION_DRAW_OVERLAYS_FLOATING_SUBTITLE = 507
        const val PERMISSION_DRAW_OVERLAYS_FLOATING_BUTTONS = 508
        const val OPEN_JSON = 205
        const val OPEN_PAGE_LINK = 206
        const val OPEN_MANGA_FOLDER = 105
        const val PERMISSION_FILES_ACCESS = 101
    }

}
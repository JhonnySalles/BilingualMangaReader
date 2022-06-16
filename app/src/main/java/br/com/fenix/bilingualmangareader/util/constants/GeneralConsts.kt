package br.com.fenix.bilingualmangareader.util.constants

import android.content.Context
import android.content.SharedPreferences
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class GeneralConsts private constructor() {
    companion object {
        private lateinit var mContext: Context
        fun setContext(context: Context) {
            mContext = context
        }

        fun getCacheDir(): File? {
            return mContext.externalCacheDir
        }

        fun getSharedPreferences(context: Context): SharedPreferences {
            mContext = context
            return mContext.getSharedPreferences(KEYS.PREFERENCE_NAME, Context.MODE_PRIVATE)
        }

        fun getSharedPreferences(): SharedPreferences {
            return mContext.getSharedPreferences(KEYS.PREFERENCE_NAME, Context.MODE_PRIVATE)
        }

        fun formatterDate(dateTime: Date): String {
            val preferences = getSharedPreferences()
            val pattern = preferences.getString(KEYS.SYSTEM.FORMAT_DATA, "yyyy-MM-dd")
            return SimpleDateFormat(pattern, Locale.getDefault()).format(dateTime)
        }

        fun formatterDateTime(dateTime: Date): String {
            val preferences = getSharedPreferences()
            val pattern = preferences.getString(KEYS.SYSTEM.FORMAT_DATA, "yyyy-MM-dd") + " hh:mm:ss a"
            return SimpleDateFormat(pattern, Locale.getDefault()).format(dateTime)
        }
    }

    object DATE {
        const val DATE_TIME_PATTERN = "yyyy-MM-dd hh:mm:ss"
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
        }
    }

    object TAG {
        const val LOG = "MangaReader"

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

    object CACHEFOLDER {
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
        const val MESSAGE_MANGA_UPDATED = 1
        const val MESSAGE_COVER_UPDATE_FINISHED = 2
    }

    object REQUEST {
        const val PERMISSION_DRAW_OVERLAYS = 505
        const val OPEN_JSON = 205
        const val OPEN_PAGE_LINK = 206
        const val OPEN_MANGA_FOLDER = 105
    }

}
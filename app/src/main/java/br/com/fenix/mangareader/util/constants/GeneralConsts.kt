package br.com.fenix.mangareader.util.constants

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class GeneralConsts private constructor() {
    companion object {
        private lateinit var mContext : Context
        fun setContext(context: Context) {
            mContext = context
        }

        fun getSharedPreferences(context: Context): SharedPreferences? {
            mContext = context
            return mContext.getSharedPreferences(KEYS.PREFERENCE_NAME, Context.MODE_PRIVATE)
        }

        fun getSharedPreferences(): SharedPreferences? {
            return mContext.getSharedPreferences(KEYS.PREFERENCE_NAME, Context.MODE_PRIVATE)
        }

        fun formaterDate(dateTime: LocalDateTime) : String {
            val preferences = getSharedPreferences()
            val pattern = preferences?.getString(KEYS.SYSTEM.FORMAT_DATA, "yyyy-MM-dd")
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
        }

        object OBJECT {
            const val MANGA = "MANGA_OBJECT"
            const val FILE = "FILE_OBJECT"
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

    object SCANNER {
        const val MESSAGE_MEDIA_UPDATE_FINISHED = 0
        const val MESSAGE_MEDIA_UPDATED = 1
    }

}
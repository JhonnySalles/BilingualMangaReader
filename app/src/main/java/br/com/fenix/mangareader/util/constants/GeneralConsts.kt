package br.com.fenix.mangareader.util.constants

import android.content.Context
import android.content.SharedPreferences

class GeneralConsts private constructor() {
    companion object {
        fun getSharedPreferences(context: Context): SharedPreferences? {
            return context.getSharedPreferences(KEYS.PREFERENCE_NAME, Context.MODE_PRIVATE)
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

        object SYSTEM {
            const val LANGUAGE = "SYSTEM_LANGUAGE"
            const val FORMAT_DATA = "SYSTEM_FORMAT_DATA"
        }

        object BOOK {
            const val NAME = "BOOK_NAME"
            const val MARK = "BOOK_MARK"
        }

        object OBJECT {
            const val BOOK = "BOOK_OBJECT"
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
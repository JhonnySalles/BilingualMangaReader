package br.com.fenix.mangareader.constants

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

        object BOOK {
            const val PATH = "BOOK_PATH"
            const val MARK = "BOOK_MARK"
        }
    }

    object TAG {
        const val LOG = "MangaReader"
    }
}
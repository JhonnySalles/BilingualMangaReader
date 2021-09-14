package br.com.fenix.mangareader.model

import android.content.Context
import android.content.SharedPreferences

class Consts {
    companion object {
        fun getTagLog(): String {
            return "MangaReader"
        }

        fun getSharedPreferences(context: Context): SharedPreferences? {
            return context.getSharedPreferences(getKeySharedPrefs(), Context.MODE_PRIVATE)
        }

        fun getKeySharedPrefs(): String {
            return "SHARED_PREFS";
        }

        fun getKeyLibraryFolder(): String {
            return "LIBRARY_FOLDER";
        }

        fun getKeyLibraryOrder(): String {
            return "LIBRARY_ORDER";
        }

        fun getKeyLastOrientation(): String {
            return "LAST_ORIENTATION";
        }

        fun getKeyLastLibraryType(): String {
            return "LIBRARY_TYPE";
        }

        fun getKeyBookPath(): String {
            return "BOOK_PATH";
        }

        fun getKeyBookMark(): String {
            return "BOOK_MARK";
        }
    }

}
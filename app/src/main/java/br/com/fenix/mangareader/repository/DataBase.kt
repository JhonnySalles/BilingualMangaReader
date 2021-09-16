package br.com.fenix.mangareader.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import br.com.fenix.mangareader.common.Converters
import br.com.fenix.mangareader.model.Book
import br.com.fenix.mangareader.model.Cover

@Database(entities = [Book::class, Cover::class], version = 1)
@TypeConverters(Converters::class)
abstract class DataBase : RoomDatabase() {

    abstract fun getBookDao() : BookDAO
    abstract fun getCoverDao() : CoverDAO

    // Singleton - One database initialize only
    companion object {
        private const val DATABASE_NAME = "BilingualMangaReader.db"

        private lateinit var INSTANCE : DataBase
        fun getDataBase(context : Context) : DataBase {
            if (!::INSTANCE.isInitialized)
                synchronized(DataBase::class.java) { // Used for a two or many cores
                    INSTANCE = Room.databaseBuilder(context, DataBase::class.java, DATABASE_NAME)
                        .allowMainThreadQueries().build() // MainThread uses another thread in db conection
                }
            return INSTANCE
        }
    }
}
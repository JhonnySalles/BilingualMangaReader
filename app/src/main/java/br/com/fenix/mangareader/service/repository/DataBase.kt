package br.com.fenix.mangareader.service.repository

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import br.com.fenix.mangareader.model.entity.*
import br.com.fenix.mangareader.util.constants.DataBaseConsts
import br.com.fenix.mangareader.util.constants.GeneralConsts
import br.com.fenix.mangareader.util.helpers.Converters
import java.io.BufferedReader
import java.io.InputStream

@Database(entities = [Manga::class, Cover::class, SubTitle::class, KanjiJLPT::class, Kanjax::class], version = 1)
@TypeConverters(Converters::class)
abstract class DataBase : RoomDatabase() {

    abstract fun getMangaDao(): MangaDAO
    abstract fun getCoverDao(): CoverDAO
    abstract fun getSubTitleDao(): SubTitleDAO
    abstract fun getKanjiJLPTDao(): KanjiJLPTDAO
    abstract fun getKanjaxDao(): KanjaxDAO

    // Singleton - One database initialize only
    companion object {
        private const val DATABASE_NAME = "BilingualMangaReader.db"

        lateinit var mAssets : AssetManager
        private lateinit var INSTANCE: DataBase
        fun getDataBase(context: Context): DataBase {
            if (!::INSTANCE.isInitialized)
                mAssets = context.assets
                synchronized(DataBase::class.java) { // Used for a two or many cores
                    INSTANCE = Room.databaseBuilder(context, DataBase::class.java, DATABASE_NAME)
                        .addCallback(rdc)
                        .addMigrations(Migrations.MIGRATION_1_2)
                        .allowMainThreadQueries()
                        .build() // MainThread uses another thread in db conection
                }
            return INSTANCE
        }

        private var rdc: Callback = object : Callback() {
            override fun onCreate(database: SupportSQLiteDatabase) {
                Log.i(GeneralConsts.TAG.LOG, "Iniciando os dados iniciais do banco.")
                val kanji = mAssets.open("kanji.sql").bufferedReader().use(BufferedReader::readText)
                database.execSQL(Migrations.SQL_INITIAL.KANJI + kanji)
                val kanjax = mAssets.open("kanjax.sql").bufferedReader().use(BufferedReader::readText)
                database.execSQL(Migrations.SQL_INITIAL.KANJAX + kanjax)
            }
        }
    }
}
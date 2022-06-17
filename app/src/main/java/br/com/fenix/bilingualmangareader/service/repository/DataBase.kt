package br.com.fenix.bilingualmangareader.service.repository

import android.content.Context
import android.content.res.AssetManager
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import br.com.fenix.bilingualmangareader.model.entity.*
import br.com.fenix.bilingualmangareader.util.helpers.Converters
import org.slf4j.LoggerFactory
import java.io.BufferedReader

@Database(version = 4, entities = [Manga::class, Cover::class, SubTitle::class, KanjiJLPT::class, Kanjax::class, FileLink::class, PageLink::class])
@TypeConverters(Converters::class)
abstract class DataBase : RoomDatabase() {

    abstract fun getMangaDao(): MangaDAO
    abstract fun getCoverDao(): CoverDAO
    abstract fun getSubTitleDao(): SubTitleDAO
    abstract fun getKanjiJLPTDao(): KanjiJLPTDAO
    abstract fun getKanjaxDao(): KanjaxDAO
    abstract fun getFileLinkDao(): FileLinkDAO
    abstract fun getPageLinkDao(): PageLinkDAO

    // Singleton - One database initialize only
    companion object {
        private val mLOGGER = LoggerFactory.getLogger(DataBase::class.java)
        private const val DATABASE_NAME = "BilingualMangaReader.db"

        lateinit var mAssets: AssetManager
        private lateinit var INSTANCE: DataBase
        fun getDataBase(context: Context): DataBase {
            if (!::INSTANCE.isInitialized)
                mAssets = context.assets
            synchronized(DataBase::class.java) { // Used for a two or many cores
                INSTANCE = Room.databaseBuilder(context, DataBase::class.java, DATABASE_NAME)
                    .addCallback(rdc)
                    .addMigrations(Migrations.MIGRATION_1_2, Migrations.MIGRATION_2_3, Migrations.MIGRATION_3_4)
                    .allowMainThreadQueries()
                    .build() // MainThread uses another thread in db conection
            }
            return INSTANCE
        }

        private var rdc: Callback = object : Callback() {
            override fun onCreate(database: SupportSQLiteDatabase) {
                mLOGGER.info("Create initial database data....")

                val kanji = mAssets.open("kanji.sql").bufferedReader().use(BufferedReader::readText)
                database.execSQL(Migrations.SQLINITIAL.KANJI + kanji)
                val kanjax = mAssets.open("kanjax.sql").bufferedReader().use(BufferedReader::readText)
                database.execSQL(Migrations.SQLINITIAL.KANJAX + kanjax)

                mLOGGER.info("Completed initial database data.")
            }
        }
    }
}
package br.com.fenix.bilingualmangareader.service.repository

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.net.Uri
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import br.com.fenix.bilingualmangareader.MainActivity
import br.com.fenix.bilingualmangareader.model.entity.*
import br.com.fenix.bilingualmangareader.util.helpers.BackupError
import br.com.fenix.bilingualmangareader.util.helpers.Converters
import br.com.fenix.bilingualmangareader.util.helpers.ErrorRestoreDatabase
import de.raphaelebner.roomdatabasebackup.core.RoomBackup
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.File


@Database(
    version = 9,
    entities = [Manga::class, SubTitle::class, KanjiJLPT::class, Kanjax::class, FileLink::class, PageLink::class,
        Vocabulary::class, Library::class, VocabularyManga::class]
)
@TypeConverters(Converters::class)
abstract class DataBase : RoomDatabase() {

    abstract fun getMangaDao(): MangaDAO
    abstract fun getSubTitleDao(): SubTitleDAO
    abstract fun getKanjiJLPTDao(): KanjiJLPTDAO
    abstract fun getKanjaxDao(): KanjaxDAO
    abstract fun getFileLinkDao(): FileLinkDAO
    abstract fun getPageLinkDao(): PageLinkDAO
    abstract fun getVocabularyDao(): VocabularyDAO
    abstract fun getLibrariesDao(): LibrariesDAO

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
                    .addMigrations(
                        Migrations.MIGRATION_1_2, Migrations.MIGRATION_2_3, Migrations.MIGRATION_3_4, Migrations.MIGRATION_4_5,
                        Migrations.MIGRATION_5_6, Migrations.MIGRATION_6_7, Migrations.MIGRATION_7_8, Migrations.MIGRATION_8_9,
                        Migrations.MIGRATION_9_10
                    )
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


        // Backup and restore
        fun backupDatabase(context: Context, saveFile: File) {
            val backup = RoomBackup(context)
            backup
                .database(INSTANCE)
                .enableLogDebug(true)
                .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_EXTERNAL)
                .backupLocationCustomFile(saveFile)
                .maxFileCount(5)
                .backupIsEncrypted(false)
                .onCompleteListener { success, msg ->
                    mLOGGER.error("Backup database. success: $success, msg: $msg.")
                    if (success)
                        backup.restartApp(Intent(context, MainActivity::class.java))
                    else {
                        mLOGGER.error("Error when backup database", msg)
                        throw BackupError("Error when backup database")
                    }
                }.backup()
        }

        fun autoBackupDatabase(context: Context) {
            val backup = RoomBackup(context)
            backup
                .database(INSTANCE)
                .enableLogDebug(true)
                .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_INTERNAL)
                .maxFileCount(5)
                .backupIsEncrypted(false)
                .onCompleteListener { success, msg ->
                    mLOGGER.error("Backup database. success: $success, msg: $msg.")
                    if (success)
                        backup.restartApp(Intent(context, MainActivity::class.java))
                    else {
                        mLOGGER.error("Error when backup database", msg)
                        throw BackupError("Error when backup database")
                    }
                }.backup()
        }

        fun restoreDatabase(context: Context, savedFile: File) {
            val backup = RoomBackup(context)
            backup
                .database(INSTANCE)
                .enableLogDebug(true)
                .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_EXTERNAL)
                .backupLocationCustomFile(savedFile)
                .backupIsEncrypted(false)
                .onCompleteListener { success, msg ->
                    mLOGGER.error("Restore backup database. success: $success, msg: $msg.")
                    if (success)
                        backup.restartApp(Intent(context, MainActivity::class.java))
                    else {
                        mLOGGER.error("Error when restore backup database", msg)
                        throw ErrorRestoreDatabase("Error for restore file.")
                    }
                }.restore()
        }

        fun validDatabaseFile(context: Context, file: Uri): Boolean {
            val cr: ContentResolver = context.contentResolver
            val mime = cr.getType(file)
            return "application/octet-stream" == mime
        }
    }
}
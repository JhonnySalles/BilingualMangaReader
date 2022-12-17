package br.com.fenix.bilingualmangareader.service.repository

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.net.Uri
import android.widget.Toast
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import br.com.fenix.bilingualmangareader.MainActivity
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.*
import br.com.fenix.bilingualmangareader.util.helpers.BackupError
import br.com.fenix.bilingualmangareader.util.helpers.Converters
import br.com.fenix.bilingualmangareader.util.helpers.ErrorRestoreDatabase
import de.raphaelebner.roomdatabasebackup.core.RoomBackup
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.File


@Database(
    version = 13,
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
                        Migrations.MIGRATION_9_10, Migrations.MIGRATION_10_11, Migrations.MIGRATION_11_12, Migrations.MIGRATION_12_13
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
        fun backupDatabase(context: Context, file: File) {
            val backup = RoomBackup(context)
            backup
                .database(INSTANCE)
                .enableLogDebug(true)
                .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_FILE)
                .backupLocationCustomFile(File(file.path))
                .customBackupFileName(file.name)
                .backupIsEncrypted(false)
                .apply {
                    onCompleteListener { success, message, exitCode ->
                        mLOGGER.error("Backup database. success: $success, msg: $message, code: $exitCode.")
                        if (success)
                            backup.restartApp(Intent(context, MainActivity::class.java))
                        else {
                            mLOGGER.error("Error when backup database", message)
                            throw BackupError("Error when backup database")
                        }
                    }
                }.backup()
        }

        fun autoBackupDatabase(context: Context) {
            val backup = RoomBackup(context)
            backup
                .database(INSTANCE)
                .enableLogDebug(true)
                .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_EXTERNAL)
                .maxFileCount(5)
                .backupIsEncrypted(false)
                .apply {
                    onCompleteListener { success, message, exitCode ->
                        mLOGGER.error("Backup database. success: $success, msg: $message, code: $exitCode.")
                        if (success){
                            Toast.makeText(
                                context,
                                context.getString(R.string.config_database_backup_success),
                                Toast.LENGTH_LONG
                            ).show()
                            backup.restartApp(Intent(context, MainActivity::class.java))
                        } else {
                            mLOGGER.error("Error when backup database", message)
                            throw BackupError("Error when backup database")
                        }
                    }
                }.backup()
        }

        fun restoreDatabase(context: Context, file: File) {
            val backup = RoomBackup(context)
            backup
                .database(INSTANCE)
                .enableLogDebug(true)
                .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_FILE)
                .backupLocationCustomFile(File(file.path))
                .backupIsEncrypted(false)
                .apply {
                    onCompleteListener { success, message, exitCode ->
                        mLOGGER.error("Restore backup database. success: $success, msg: $message, code: $exitCode.")
                        if (success) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.config_database_restore_success),
                                Toast.LENGTH_LONG
                            ).show()
                            backup.restartApp(Intent(context, MainActivity::class.java))
                        } else {
                            mLOGGER.error("Error when restore backup database", message)
                            throw ErrorRestoreDatabase("Error when restore file.")
                        }
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
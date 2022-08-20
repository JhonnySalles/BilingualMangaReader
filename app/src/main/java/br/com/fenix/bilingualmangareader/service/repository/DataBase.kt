package br.com.fenix.bilingualmangareader.service.repository

import android.content.ContentResolver
import android.content.Context
import android.content.res.AssetManager
import android.net.Uri
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import br.com.fenix.bilingualmangareader.model.entity.*
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.util.helpers.*
import org.slf4j.LoggerFactory
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


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
                        Migrations.MIGRATION_5_6, Migrations.MIGRATION_6_7, Migrations.MIGRATION_7_8, Migrations.MIGRATION_8_9
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
            if (INSTANCE.isOpen)
                INSTANCE.close()

            val dbFile: File = context.getDatabasePath(DATABASE_NAME)
            val path = File(saveFile.path)
            if (!path.exists())
                path.mkdirs()

            if (saveFile.exists()) {
                mLOGGER.warn("File exists. Deleting it and then creating new file.")
                saveFile.delete()
            }

            try {
                if (saveFile.createNewFile()) {
                    val bufferSize = 8 * 1024
                    val buffer = ByteArray(bufferSize)
                    var bytes = bufferSize
                    val saveDB: OutputStream = FileOutputStream(saveFile)
                    val inputDB: InputStream = FileInputStream(dbFile)
                    while (inputDB.read(buffer, 0, bufferSize).also { bytes = it } > 0) {
                        saveDB.write(buffer, 0, bytes)
                    }
                    saveDB.flush()
                    inputDB.close()
                    saveDB.close()
                }
            } catch (e: Exception) {
                mLOGGER.error("Error when backup database", e)
                throw BackupError("Error when backup database")
            }

            GeneralConsts.getSharedPreferences(context).edit().putString(
                GeneralConsts.KEYS.DATABASE.LAST_BACKUP,
                SimpleDateFormat(GeneralConsts.PATTERNS.DATE_TIME_PATTERN, Locale.getDefault()).format(Date())
            ).apply()
        }

        fun restoreDatabase(context: Context, inputNewDB: InputStream) {
            if (INSTANCE.isOpen)
                INSTANCE.close()

            GeneralConsts.getSharedPreferences(context).edit().putBoolean(GeneralConsts.KEYS.DATABASE.RESTORE_DATABASE, true).apply()

            deleteRestoreBackupFile(context)
            backupDatabaseForRestore(context)

            val oldDB: File = context.getDatabasePath(DATABASE_NAME)
            try {
                FileUtil(context).copyFile(inputNewDB as FileInputStream, FileOutputStream(oldDB))
                validateDatabase(context)
            } catch (e: IOException) {
                restoreDatabase(context)
                mLOGGER.warn("Error to read a file.", e)
                throw ErrorRestoreDatabase("Error for restore file.")
            } catch (e: Exception) {
                restoreDatabase(context)
                throw e
            }
        }

        fun restoreDatabase(context: Context): Boolean {
            if (INSTANCE.isOpen)
                INSTANCE.close()

            val backupFile = getBackupPath(context)
            val database: File = context.getDatabasePath(DATABASE_NAME)
            try {
                FileUtil(context).copyFile(FileInputStream(backupFile), FileOutputStream(database))
                if(!INSTANCE.isOpen){
                    INSTANCE.openHelper.writableDatabase
                }
                return true
            } catch (e: IOException) {
                mLOGGER.warn("Error when restore a backup file.", e)
                flushDB(context)
            }

            return false
        }

        private fun backupDatabaseForRestore(context: Context) {
            val database: File = context.getDatabasePath(DATABASE_NAME)
            val backupDir = getDatabaseBackupPath(context)
            if (!backupDir.exists())
                backupDir.mkdirs()

            val newFile = getBackupPath(context)
            if (newFile.exists()) {
                mLOGGER.warn("Backup Restore - File exists. Deleting it and then creating new file.")
                newFile.delete()
            }

            try {
                if (newFile.createNewFile()) {
                    val bufferSize = 8 * 1024
                    val buffer = ByteArray(bufferSize)
                    var bytes = bufferSize
                    val saveDB: OutputStream = FileOutputStream(newFile)
                    val inputDB: InputStream = FileInputStream(database)
                    while (inputDB.read(buffer, 0, bufferSize).also { bytes = it } > 0) {
                        saveDB.write(buffer, 0, bytes)
                    }
                    saveDB.flush()
                    inputDB.close()
                    saveDB.close()
                }
            } catch (e: java.lang.Exception) {
                mLOGGER.error("Error for restore file.", e)
                throw ErrorRestoreDatabase("Error for restore file.")
            }
        }

        fun validDatabaseFile(context: Context, file: Uri): Boolean {
            val cr: ContentResolver = context.contentResolver
            val mime = cr.getType(file)
            return "application/octet-stream" == mime
        }

        private fun validateDatabase(context: Context): Boolean {
            val sharedPreferences = GeneralConsts.getSharedPreferences(context)

            if(!INSTANCE.isOpen){
                INSTANCE.openHelper.writableDatabase
            }

            if (sharedPreferences.getBoolean(GeneralConsts.KEYS.DATABASE.RESTORE_DATABASE, false)) {
                sharedPreferences.edit().putBoolean(GeneralConsts.KEYS.DATABASE.RESTORE_DATABASE, false).apply()

                if (getDataBase(context).getMangaDao().getMangaCount() <= 0) {
                    if (restoreDatabase(context)) {
                        deleteRestoreBackupFile(context)
                        throw InvalidDatabase("Invalid DataBase File")
                    } else {
                        flushDB(context)
                        throw RestoredNewDatabase("Corrupted old database, restored a new database")
                    }
                }
            } else {
                if (getDataBase(context).getMangaDao().getMangaCount() <= 0) {
                    flushDB(context)
                    deleteRestoreBackupFile(context)
                    backupDatabaseForRestore(context)
                    throw RestoredNewDatabase("Created a new database")
                }
            }
            return true
        }

        private fun deleteRestoreBackupFile(context: Context) {
            val restoreFile = getBackupPath(context)

            if (restoreFile.exists())
                restoreFile.delete()
        }

        private fun getDatabaseBackupPath(context: Context): File {
            val database: File = context.getDatabasePath(DATABASE_NAME)
            return File(database.parent, "backup")
        }

        private fun getBackupPath(context: Context): File {
            val database = getDatabaseBackupPath(context)
            return File(database.path + File.separator + GeneralConsts.KEYS.DATABASE.BACKUP_RESTORE_ROLLBACK_FILE_NAME)
        }

        private fun flushDB(context: Context) {
            val database = context.getDatabasePath(DATABASE_NAME)
            if (database.exists())
                database.delete()

            if(!INSTANCE.isOpen){
                INSTANCE.openHelper.writableDatabase
            }
        }

    }
}
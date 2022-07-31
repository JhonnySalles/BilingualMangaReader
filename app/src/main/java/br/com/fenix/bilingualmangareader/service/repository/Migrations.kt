package br.com.fenix.bilingualmangareader.service.repository

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import br.com.fenix.bilingualmangareader.model.enums.Languages
import br.com.fenix.bilingualmangareader.util.constants.DataBaseConsts
import org.slf4j.LoggerFactory
import java.io.BufferedReader

class Migrations {
    object SQLINITIAL {
        const val KANJI: String = "INSERT INTO " + DataBaseConsts.JLPT.TABLE_NAME +
                " (" + DataBaseConsts.JLPT.COLUMNS.KANJI + ", " + DataBaseConsts.JLPT.COLUMNS.LEVEL + ") VALUES "

        const val KANJAX: String = "INSERT INTO " + DataBaseConsts.KANJAX.TABLE_NAME +
                " (" + DataBaseConsts.KANJAX.COLUMNS.ID + ", " + DataBaseConsts.KANJAX.COLUMNS.KANJI + ", " +
                DataBaseConsts.KANJAX.COLUMNS.KEYWORD + ", " + DataBaseConsts.KANJAX.COLUMNS.MEANING + ", " +
                DataBaseConsts.KANJAX.COLUMNS.KOOHII + ", " + DataBaseConsts.KANJAX.COLUMNS.KOOHII2 + ", " +
                DataBaseConsts.KANJAX.COLUMNS.ONYOMI + ", " + DataBaseConsts.KANJAX.COLUMNS.KUNYOMI + ", " +
                DataBaseConsts.KANJAX.COLUMNS.ONWORDS + ", " + DataBaseConsts.KANJAX.COLUMNS.KUNWORDS + ", " +
                DataBaseConsts.KANJAX.COLUMNS.JLPT + ", " + DataBaseConsts.KANJAX.COLUMNS.GRADE + ", " +
                DataBaseConsts.KANJAX.COLUMNS.FREQUENCE + ", " + DataBaseConsts.KANJAX.COLUMNS.STROKES + ", " +
                DataBaseConsts.KANJAX.COLUMNS.VARIANTS + ", " + DataBaseConsts.KANJAX.COLUMNS.RADICAL + ", " +
                DataBaseConsts.KANJAX.COLUMNS.PARTS + ", " + DataBaseConsts.KANJAX.COLUMNS.UTF8 + ", " +
                DataBaseConsts.KANJAX.COLUMNS.SJIS + ", " + DataBaseConsts.KANJAX.COLUMNS.KEYWORDS_PT + ", " +
                DataBaseConsts.KANJAX.COLUMNS.MEANING_PT + ") VALUES "

        const val VOCABULARY: String = "INSERT INTO " + DataBaseConsts.VOCABULARY.TABLE_NAME +
                " (" + DataBaseConsts.VOCABULARY.COLUMNS.WORD + ", " + DataBaseConsts.VOCABULARY.COLUMNS.BASIC_FORM + ", " +
                DataBaseConsts.VOCABULARY.COLUMNS.READING + ", " + DataBaseConsts.VOCABULARY.COLUMNS.MEANING + ") VALUES "

    }

    companion object {
        private val mLOGGER = LoggerFactory.getLogger(Migrations::class.java)

        // Migration version 2.
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                mLOGGER.info("Start migration 1 - 2...")

                database.execSQL("ALTER TABLE " + DataBaseConsts.MANGA.TABLE_NAME + " ADD COLUMN " + DataBaseConsts.MANGA.COLUMNS.EXCLUDED + " INTEGER DEFAULT 0 NOT NULL")

                database.execSQL(
                    "CREATE TABLE " + DataBaseConsts.FILELINK.TABLE_NAME + " (" +
                            DataBaseConsts.FILELINK.COLUMNS.ID + " INTEGER PRIMARY KEY, " +
                            DataBaseConsts.FILELINK.COLUMNS.FK_ID_MANGA + " INTEGER NOT NULL, " +
                            DataBaseConsts.FILELINK.COLUMNS.PAGES + " INTEGER NOT NULL, " +
                            DataBaseConsts.FILELINK.COLUMNS.FILE_PATH + " TEXT NOT NULL, " +
                            DataBaseConsts.FILELINK.COLUMNS.FILE_NAME + " TEXT NOT NULL, " +
                            DataBaseConsts.FILELINK.COLUMNS.FILE_TYPE + " TEXT NOT NULL, " +
                            DataBaseConsts.FILELINK.COLUMNS.FILE_FOLDER + " TEXT NOT NULL," +
                            DataBaseConsts.FILELINK.COLUMNS.DATE_CREATE + " TEXT, " +
                            DataBaseConsts.FILELINK.COLUMNS.LAST_ACCESS + " TEXT)"
                )

                database.execSQL(
                    "CREATE INDEX index_" + DataBaseConsts.FILELINK.TABLE_NAME
                            + "_" + DataBaseConsts.FILELINK.COLUMNS.FK_ID_MANGA + "_" + DataBaseConsts.FILELINK.COLUMNS.FILE_NAME
                            + " ON " + DataBaseConsts.FILELINK.TABLE_NAME +
                            "(" + DataBaseConsts.FILELINK.COLUMNS.FK_ID_MANGA + ", " + DataBaseConsts.FILELINK.COLUMNS.FILE_NAME + ")"
                )

                database.execSQL(
                    "CREATE TABLE " + DataBaseConsts.PAGESLINK.TABLE_NAME + " (" +
                            DataBaseConsts.PAGESLINK.COLUMNS.ID + " INTEGER PRIMARY KEY, " +
                            DataBaseConsts.PAGESLINK.COLUMNS.FK_ID_FILE + " INTEGER, " +
                            DataBaseConsts.PAGESLINK.COLUMNS.MANGA_PAGE + " INTEGER NOT NULL, " +
                            DataBaseConsts.PAGESLINK.COLUMNS.MANGA_PAGES + " INTEGER NOT NULL, " +
                            DataBaseConsts.PAGESLINK.COLUMNS.FILE_LINK_PAGE + " INTEGER NOT NULL, " +
                            DataBaseConsts.PAGESLINK.COLUMNS.FILE_LINK_PAGES + " INTEGER NOT NULL, " +
                            DataBaseConsts.PAGESLINK.COLUMNS.MANGA_PAGE_NAME + " TEXT NOT NULL, " +
                            DataBaseConsts.PAGESLINK.COLUMNS.FILE_LINK_PAGE_NAME + " TEXT NOT NULL, " +
                            DataBaseConsts.PAGESLINK.COLUMNS.NOT_LINKED + " INTEGER DEFAULT 0 NOT NULL)"
                )

                database.execSQL(
                    "CREATE INDEX index_" + DataBaseConsts.PAGESLINK.TABLE_NAME
                            + "_" + DataBaseConsts.PAGESLINK.COLUMNS.FK_ID_FILE + " ON " + DataBaseConsts.PAGESLINK.TABLE_NAME +
                            "(" + DataBaseConsts.PAGESLINK.COLUMNS.FK_ID_FILE + ")"
                )

                mLOGGER.info("Completed migration 1 - 2.")
            }
        }

        // Migration version 3.
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                mLOGGER.info("Start migration 2 - 3...")

                database.execSQL("ALTER TABLE " + DataBaseConsts.PAGESLINK.TABLE_NAME + " ADD COLUMN " + DataBaseConsts.PAGESLINK.COLUMNS.FILE_RIGHT_LINK_PAGE + " INTEGER DEFAULT -1 NOT NULL")
                database.execSQL("ALTER TABLE " + DataBaseConsts.PAGESLINK.TABLE_NAME + " ADD COLUMN " + DataBaseConsts.PAGESLINK.COLUMNS.FILE_RIGHT_LINK_PAGE_NAME + " TEXT DEFAULT '' NOT NULL")
                database.execSQL("ALTER TABLE " + DataBaseConsts.PAGESLINK.TABLE_NAME + " ADD COLUMN " + DataBaseConsts.PAGESLINK.COLUMNS.DUAL_IMAGE + " INTEGER DEFAULT 0 NOT NULL")

                mLOGGER.info("Completed migration 2 - 3.")
            }
        }

        // Migration version 4.
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                mLOGGER.info("Start migration 3 - 4...")

                database.execSQL("ALTER TABLE " + DataBaseConsts.FILELINK.TABLE_NAME + " ADD COLUMN " + DataBaseConsts.FILELINK.COLUMNS.LANGUAGE + " TEXT DEFAULT '" + Languages.PORTUGUESE + "' NOT NULL")

                mLOGGER.info("Completed migration 3 - 4.")
            }
        }

        // Migration version 5.
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                mLOGGER.info("Start migration 4 - 5...")

                database.execSQL("DROP TABLE IF EXISTS " + DataBaseConsts.COVER.TABLE_NAME)

                mLOGGER.info("Completed migration 4 - 5.")
            }
        }

        // Migration version 6.
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                mLOGGER.info("Start migration 5 - 6...")

                database.execSQL("ALTER TABLE " + DataBaseConsts.PAGESLINK.TABLE_NAME + " ADD COLUMN " + DataBaseConsts.PAGESLINK.COLUMNS.MANGA_DUAL_PAGE + " INTEGER DEFAULT 0 NOT NULL")
                database.execSQL("ALTER TABLE " + DataBaseConsts.PAGESLINK.TABLE_NAME + " ADD COLUMN " + DataBaseConsts.PAGESLINK.COLUMNS.FILE_LEFT_DUAL_PAGE + " INTEGER DEFAULT 0 NOT NULL")
                database.execSQL("ALTER TABLE " + DataBaseConsts.PAGESLINK.TABLE_NAME + " ADD COLUMN " + DataBaseConsts.PAGESLINK.COLUMNS.FILE_RIGHT_DUAL_PAGE + " INTEGER DEFAULT 0 NOT NULL")

                mLOGGER.info("Completed migration 5 - 6.")
            }
        }

        // Migration version 7.
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                mLOGGER.info("Start migration 6 - 7...")

                database.execSQL("ALTER TABLE " + DataBaseConsts.PAGESLINK.TABLE_NAME + " ADD COLUMN " + DataBaseConsts.PAGESLINK.COLUMNS.MANGA_PAGE_PATH + " TEXT DEFAULT '' NOT NULL")
                database.execSQL("ALTER TABLE " + DataBaseConsts.PAGESLINK.TABLE_NAME + " ADD COLUMN " + DataBaseConsts.PAGESLINK.COLUMNS.FILE_LINK_PAGE_PATH + " TEXT DEFAULT '' NOT NULL")
                database.execSQL("ALTER TABLE " + DataBaseConsts.PAGESLINK.TABLE_NAME + " ADD COLUMN " + DataBaseConsts.PAGESLINK.COLUMNS.FILE_RIGHT_LINK_PAGE_PATH + " TEXT DEFAULT '' NOT NULL")

                mLOGGER.info("Completed migration 6 - 7.")
            }
        }

        // Migration version 8.
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                mLOGGER.info("Start migration 7 - 8...")

                database.execSQL("ALTER TABLE " + DataBaseConsts.MANGA.TABLE_NAME + " ADD COLUMN " + DataBaseConsts.MANGA.COLUMNS.LAST_ALTERATION + " TEXT")
                database.execSQL("ALTER TABLE " + DataBaseConsts.FILELINK.TABLE_NAME + " ADD COLUMN " + DataBaseConsts.FILELINK.COLUMNS.LAST_ALTERATION + " TEXT")
                database.execSQL("ALTER TABLE " + DataBaseConsts.SUBTITLES.TABLE_NAME + " ADD COLUMN " + DataBaseConsts.SUBTITLES.COLUMNS.LAST_ALTERATION + " TEXT")

                mLOGGER.info("Completed migration 7 - 8.")
            }
        }

        // Migration version 9.
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                mLOGGER.info("Start migration 8 - 9...")

                database.execSQL("ALTER TABLE " + DataBaseConsts.MANGA.TABLE_NAME + " ADD COLUMN " + DataBaseConsts.MANGA.COLUMNS.FK_ID_LIBRARY + " INTEGER")

                database.execSQL(
                    "CREATE TABLE " + DataBaseConsts.VOCABULARY.TABLE_NAME + " (" +
                            DataBaseConsts.VOCABULARY.COLUMNS.ID + " INTEGER PRIMARY KEY, " +
                            DataBaseConsts.VOCABULARY.COLUMNS.WORD + " TEXT NOT NULL, " +
                            DataBaseConsts.VOCABULARY.COLUMNS.BASIC_FORM + " TEXT, " +
                            DataBaseConsts.VOCABULARY.COLUMNS.READING + " TEXT NOT NULL, " +
                            DataBaseConsts.VOCABULARY.COLUMNS.MEANING + " TEXT NOT NULL, " +
                            DataBaseConsts.VOCABULARY.COLUMNS.REVISED + " INTEGER DEFAULT 0 NOT NULL)"
                )

                database.execSQL(
                    "CREATE INDEX index_" + DataBaseConsts.VOCABULARY.TABLE_NAME
                            + "_" + DataBaseConsts.VOCABULARY.COLUMNS.WORD + "_" + DataBaseConsts.VOCABULARY.COLUMNS.BASIC_FORM
                            + " ON " + DataBaseConsts.VOCABULARY.TABLE_NAME +
                            "(" + DataBaseConsts.VOCABULARY.COLUMNS.WORD + ", " + DataBaseConsts.VOCABULARY.COLUMNS.BASIC_FORM + ")"
                )

                database.execSQL(
                    "CREATE TABLE " + DataBaseConsts.MANGA_VOCABULARY.TABLE_NAME + " (" +
                            DataBaseConsts.MANGA_VOCABULARY.COLUMNS.ID_MANGA + " INTEGER, " +
                            DataBaseConsts.MANGA_VOCABULARY.COLUMNS.ID_VOCABULARY + " INTEGER," +
                            " FOREIGN KEY(" + DataBaseConsts.MANGA_VOCABULARY.COLUMNS.ID_MANGA +") REFERENCES " + DataBaseConsts.MANGA.TABLE_NAME + "(" + DataBaseConsts.MANGA.COLUMNS.ID + ")," +
                            " FOREIGN KEY(" + DataBaseConsts.MANGA_VOCABULARY.COLUMNS.ID_VOCABULARY +") REFERENCES " + DataBaseConsts.LIBRARIES.TABLE_NAME + "(" + DataBaseConsts.LIBRARIES.COLUMNS.ID + "))"
                )

                database.execSQL(
                    "CREATE TABLE " + DataBaseConsts.LIBRARIES.TABLE_NAME + " (" +
                            DataBaseConsts.LIBRARIES.COLUMNS.ID + " INTEGER PRIMARY KEY, " +
                            DataBaseConsts.LIBRARIES.COLUMNS.TITLE + " TEXT NOT NULL, " +
                            DataBaseConsts.LIBRARIES.COLUMNS.PATH + " TEXT NOT NULL, " +
                            DataBaseConsts.LIBRARIES.COLUMNS.TYPE + " TEXT NOT NULL, " +
                            DataBaseConsts.LIBRARIES.COLUMNS.ENABLED + " INTEGER DEFAULT 0 NOT NULL, " +
                            DataBaseConsts.LIBRARIES.COLUMNS.EXCLUDED + " INTEGER DEFAULT 0 NOT NULL)"
                )

                database.execSQL(
                    "CREATE INDEX index_" + DataBaseConsts.LIBRARIES.TABLE_NAME
                            + "_" + DataBaseConsts.LIBRARIES.COLUMNS.TITLE + " ON " + DataBaseConsts.LIBRARIES.TABLE_NAME +
                            "(" + DataBaseConsts.LIBRARIES.COLUMNS.TITLE + ")"
                )

                mLOGGER.info("Insert initial vocabulary data...")

                val kanji = DataBase.mAssets.open("vocabulary.sql").bufferedReader().use(BufferedReader::readText)
                database.execSQL(SQLINITIAL.VOCABULARY + kanji)
                database.execSQL( "UPDATE " + DataBaseConsts.VOCABULARY.TABLE_NAME + " SET " + DataBaseConsts.VOCABULARY.COLUMNS.REVISED + " = 1"  )

                mLOGGER.info("Completed migration 8 - 9.")
            }
        }
    }
}
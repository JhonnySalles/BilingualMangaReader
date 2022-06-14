package br.com.fenix.bilingualmangareader.service.repository

import android.util.Log
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import br.com.fenix.bilingualmangareader.model.enums.Languages
import br.com.fenix.bilingualmangareader.util.constants.DataBaseConsts
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts

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

    }

    companion object {
        // Migration version 2.
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.i(GeneralConsts.TAG.LOG, "Iniciando o migration 1 - 2")

                database.execSQL("ALTER TABLE " + DataBaseConsts.MANGA.TABLE_NAME + " ADD COLUMN " + DataBaseConsts.MANGA.COLUMNS.EXCLUDED + " INTEGER DEFAULT 0 NOT NULL")

                database.execSQL( "CREATE TABLE " + DataBaseConsts.FILELINK.TABLE_NAME + " (" +
                        DataBaseConsts.FILELINK.COLUMNS.ID + " INTEGER PRIMARY KEY, " +
                        DataBaseConsts.FILELINK.COLUMNS.FK_ID_MANGA + " INTEGER NOT NULL, " +
                        DataBaseConsts.FILELINK.COLUMNS.PAGES + " INTEGER NOT NULL, " +
                        DataBaseConsts.FILELINK.COLUMNS.FILE_PATH + " TEXT NOT NULL, " +
                        DataBaseConsts.FILELINK.COLUMNS.FILE_NAME + " TEXT NOT NULL, " +
                        DataBaseConsts.FILELINK.COLUMNS.FILE_TYPE + " TEXT NOT NULL, " +
                        DataBaseConsts.FILELINK.COLUMNS.FILE_FOLDER + " TEXT NOT NULL," +
                        DataBaseConsts.FILELINK.COLUMNS.DATE_CREATE + " TEXT, " +
                        DataBaseConsts.FILELINK.COLUMNS.LAST_ACCESS + " TEXT)")

                database.execSQL( "CREATE INDEX index_" + DataBaseConsts.FILELINK.TABLE_NAME
                        + "_" + DataBaseConsts.FILELINK.COLUMNS.FK_ID_MANGA + "_" + DataBaseConsts.FILELINK.COLUMNS.FILE_NAME
                        + " ON " +  DataBaseConsts.FILELINK.TABLE_NAME +
                        "(" + DataBaseConsts.FILELINK.COLUMNS.FK_ID_MANGA + ", " + DataBaseConsts.FILELINK.COLUMNS.FILE_NAME + ")")

                database.execSQL( "CREATE TABLE " + DataBaseConsts.PAGESLINK.TABLE_NAME + " (" +
                        DataBaseConsts.PAGESLINK.COLUMNS.ID + " INTEGER PRIMARY KEY, " +
                        DataBaseConsts.PAGESLINK.COLUMNS.FK_ID_FILE + " INTEGER, " +
                        DataBaseConsts.PAGESLINK.COLUMNS.MANGA_PAGE + " INTEGER NOT NULL, " +
                        DataBaseConsts.PAGESLINK.COLUMNS.MANGA_PAGES + " INTEGER NOT NULL, " +
                        DataBaseConsts.PAGESLINK.COLUMNS.FILE_LINK_PAGE + " INTEGER NOT NULL, " +
                        DataBaseConsts.PAGESLINK.COLUMNS.FILE_LINK_PAGES + " INTEGER NOT NULL, " +
                        DataBaseConsts.PAGESLINK.COLUMNS.MANGA_PAGE_NAME + " TEXT NOT NULL, " +
                        DataBaseConsts.PAGESLINK.COLUMNS.FILE_LINK_PAGE_NAME + " TEXT NOT NULL, " +
                        DataBaseConsts.PAGESLINK.COLUMNS.NOT_LINKED + " INTEGER DEFAULT 0 NOT NULL)")

                database.execSQL( "CREATE INDEX index_" + DataBaseConsts.PAGESLINK.TABLE_NAME
                        + "_" + DataBaseConsts.PAGESLINK.COLUMNS.FK_ID_FILE + " ON " +  DataBaseConsts.PAGESLINK.TABLE_NAME +
                        "(" + DataBaseConsts.PAGESLINK.COLUMNS.FK_ID_FILE + ")")

                Log.i(GeneralConsts.TAG.LOG, "Concluido a migration 1 - 2")
            }
        }

        // Migration version 3.
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE " + DataBaseConsts.PAGESLINK.TABLE_NAME + " ADD COLUMN " + DataBaseConsts.PAGESLINK.COLUMNS.FILE_RIGHT_LINK_PAGE + " INTEGER DEFAULT -1 NOT NULL")
                database.execSQL("ALTER TABLE " + DataBaseConsts.PAGESLINK.TABLE_NAME + " ADD COLUMN " + DataBaseConsts.PAGESLINK.COLUMNS.FILE_RIGHT_LINK_PAGE_NAME + " TEXT DEFAULT '' NOT NULL")
                database.execSQL("ALTER TABLE " + DataBaseConsts.PAGESLINK.TABLE_NAME + " ADD COLUMN " + DataBaseConsts.PAGESLINK.COLUMNS.DUAL_IMAGE + " INTEGER DEFAULT 0 NOT NULL")

                Log.i(GeneralConsts.TAG.LOG, "Iniciando o migration 2 - 3")
            }
        }

        // Migration version 4.
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE " + DataBaseConsts.FILELINK.TABLE_NAME + " ADD COLUMN " + DataBaseConsts.FILELINK.COLUMNS.LANGUAGE + " TEXT DEFAULT '" + Languages.PORTUGUESE + "' NOT NULL")
                Log.i(GeneralConsts.TAG.LOG, "Iniciando o migration 3 - 4")
            }
        }
    }
}
package br.com.fenix.bilingualmangareader.service.repository

import android.util.Log
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import br.com.fenix.bilingualmangareader.util.constants.DataBaseConsts
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts

class Migrations {
    object SQL_INITIAL {
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
        // Migration version 1.
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.i(GeneralConsts.TAG.LOG, "Iniciando o migration 0 - 1")

            }
        }
    }
}
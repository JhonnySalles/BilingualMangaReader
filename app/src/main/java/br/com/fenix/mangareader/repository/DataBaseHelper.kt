package br.com.fenix.mangareader.repository

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import br.com.fenix.mangareader.constants.DataBaseConsts

class DataBaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "BilingualMangaReader.db"

        private const val CREATE_TABLE_BOOK = ("create table " +
                DataBaseConsts.BOOK.TABLE_NAME + " (" +
                DataBaseConsts.BOOK.COLUMNS.ID + " integer primary key autoincrement, " +
                DataBaseConsts.BOOK.COLUMNS.TITLE + " text, " +
                DataBaseConsts.BOOK.COLUMNS.SUB_TITLE + " text, " +
                DataBaseConsts.BOOK.COLUMNS.PAGES + " integer, " +
                DataBaseConsts.BOOK.COLUMNS.BOOK_MARK + " integer, " +
                DataBaseConsts.BOOK.COLUMNS.FILE_PATH + " text, " +
                DataBaseConsts.BOOK.COLUMNS.FILE_NAME + " text, " +
                DataBaseConsts.BOOK.COLUMNS.FILE_TYPE + " text, " +
                DataBaseConsts.BOOK.COLUMNS.FAVORITE + " integer, " +
                DataBaseConsts.BOOK.COLUMNS.DATE_CREATE + " text); "
                )

        private const val CREATE_TABLE_COVER = ("create table " +
                DataBaseConsts.COVER.TABLE_NAME + " (" +
                DataBaseConsts.COVER.COLUMNS.ID + " integer primary key autoincrement, " +
                DataBaseConsts.COVER.COLUMNS.FK_ID_BOOK + " integer, " +
                DataBaseConsts.COVER.COLUMNS.NAME + " text, " +
                DataBaseConsts.COVER.COLUMNS.SIZE + " integer, " +
                DataBaseConsts.COVER.COLUMNS.TYPE + " text, " +
                DataBaseConsts.COVER.COLUMNS.IMAGE + " blob, " +
                "FOREIGN KEY(" + DataBaseConsts.COVER.COLUMNS.FK_ID_BOOK + " REFERENCES " +
                DataBaseConsts.BOOK.TABLE_NAME + "(" + DataBaseConsts.BOOK.COLUMNS.ID +")) ); "
                )

        private const val CREATE_TABLE_SUB_TITLE = ("create table " +
                DataBaseConsts.SUBTITLES.TABLE_NAME + " (" +
                DataBaseConsts.SUBTITLES.COLUMNS.ID + " integer primary key autoincrement, " +
                DataBaseConsts.SUBTITLES.COLUMNS.FK_ID_BOOK + " integer, " +
                DataBaseConsts.SUBTITLES.COLUMNS.NAME + " text, " +
                DataBaseConsts.SUBTITLES.COLUMNS.FOLDER + " text, " +
                DataBaseConsts.SUBTITLES.COLUMNS.ROOT + " integer, " +
                DataBaseConsts.SUBTITLES.COLUMNS.DATE_CREATE + " text, " +
                "FOREIGN KEY(" + DataBaseConsts.SUBTITLES.COLUMNS.FK_ID_BOOK + " REFERENCES " +
                DataBaseConsts.BOOK.TABLE_NAME + "(" + DataBaseConsts.BOOK.COLUMNS.ID +")) ); "
                )
    }


    override fun onCreate(db : SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_BOOK)
        db.execSQL(CREATE_TABLE_COVER)
        db.execSQL(CREATE_TABLE_SUB_TITLE)
    }

    override fun onUpgrade(db : SQLiteDatabase, initial : Int, final: Int) {

    }

}
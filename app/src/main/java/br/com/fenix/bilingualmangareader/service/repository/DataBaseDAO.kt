package br.com.fenix.bilingualmangareader.service.repository

import androidx.room.*
import br.com.fenix.bilingualmangareader.model.entity.*
import br.com.fenix.bilingualmangareader.util.constants.DataBaseConsts

interface DataBaseDAO<T> {

    @Insert
    fun save(obj: T): Long

    @Insert
    fun save(entities: List<T>)

    @Update
    fun update(obj: T): Int

    @Update
    fun update(entities: List<T>)

    @Delete
    fun delete(obj: T)

}


@Dao
abstract class MangaDAO : DataBaseDAO<Manga> {
    @Query("SELECT * FROM " + DataBaseConsts.MANGA.TABLE_NAME + " WHERE " + DataBaseConsts.MANGA.COLUMNS.EXCLUDED + " = 0 ")
    abstract fun list(): List<Manga>

    @Query(
        "SELECT * FROM ( " +
                " SELECT ${DataBaseConsts.MANGA.COLUMNS.ID}, ${DataBaseConsts.MANGA.COLUMNS.TITLE}, ${DataBaseConsts.MANGA.COLUMNS.SUB_TITLE}, ${DataBaseConsts.MANGA.COLUMNS.FILE_PATH}, " +
                "        ${DataBaseConsts.MANGA.COLUMNS.FILE_FOLDER}, ${DataBaseConsts.MANGA.COLUMNS.FILE_NAME}, ${DataBaseConsts.MANGA.COLUMNS.FILE_TYPE}, ${DataBaseConsts.MANGA.COLUMNS.PAGES}, " +
                "        ${DataBaseConsts.MANGA.COLUMNS.BOOK_MARK}, ${DataBaseConsts.MANGA.COLUMNS.FAVORITE}, ${DataBaseConsts.MANGA.COLUMNS.DATE_CREATE}, ${DataBaseConsts.MANGA.COLUMNS.LAST_ACCESS}, " +
                "        ${DataBaseConsts.MANGA.COLUMNS.EXCLUDED} AS excluded, ${DataBaseConsts.MANGA.COLUMNS.LAST_ACCESS} AS sort " +
                " FROM " + DataBaseConsts.MANGA.TABLE_NAME +
                " WHERE " + DataBaseConsts.MANGA.COLUMNS.LAST_ACCESS + " is not null " +
                "UNION" +
                " SELECT null AS ${DataBaseConsts.MANGA.COLUMNS.ID}, '' AS ${DataBaseConsts.MANGA.COLUMNS.TITLE}, " +
                "        '' AS ${DataBaseConsts.MANGA.COLUMNS.SUB_TITLE}, '' AS ${DataBaseConsts.MANGA.COLUMNS.FILE_PATH}, '' AS ${DataBaseConsts.MANGA.COLUMNS.FILE_FOLDER}, " +
                "        '' AS ${DataBaseConsts.MANGA.COLUMNS.FILE_NAME}, '' AS ${DataBaseConsts.MANGA.COLUMNS.FILE_TYPE}, 0 AS ${DataBaseConsts.MANGA.COLUMNS.PAGES}, " +
                "        0 AS ${DataBaseConsts.MANGA.COLUMNS.BOOK_MARK}, 0 AS ${DataBaseConsts.MANGA.COLUMNS.FAVORITE}, null AS ${DataBaseConsts.MANGA.COLUMNS.DATE_CREATE}, " +
                "        Substr(${DataBaseConsts.MANGA.COLUMNS.LAST_ACCESS}, 0, 12) || '00:00:00.000' AS ${DataBaseConsts.MANGA.COLUMNS.LAST_ACCESS}, ${DataBaseConsts.MANGA.COLUMNS.EXCLUDED} AS excluded, " +
                "        Substr(${DataBaseConsts.MANGA.COLUMNS.LAST_ACCESS}, 0, 12) || '25:60:60.000' AS sort " +
                " FROM  " + DataBaseConsts.MANGA.TABLE_NAME +
                " WHERE " + DataBaseConsts.MANGA.COLUMNS.LAST_ACCESS + " is not null " +
                " GROUP  BY Substr(${DataBaseConsts.MANGA.COLUMNS.LAST_ACCESS}, 0, 11)) " +
                "ORDER  BY sort DESC "
    )
    abstract fun listHistory(): List<Manga>

    @Query("SELECT * FROM " + DataBaseConsts.MANGA.TABLE_NAME + " WHERE " + DataBaseConsts.MANGA.COLUMNS.EXCLUDED + " = 0 AND " + DataBaseConsts.MANGA.COLUMNS.ID + " = :id")
    abstract fun get(id: Long): Manga

    @Query("SELECT * FROM " + DataBaseConsts.MANGA.TABLE_NAME + " WHERE " + DataBaseConsts.MANGA.COLUMNS.EXCLUDED + " = 0 AND " + DataBaseConsts.MANGA.COLUMNS.FILE_NAME + " = :name")
    abstract fun get(name: String): Manga

    @Query("SELECT * FROM " + DataBaseConsts.MANGA.TABLE_NAME + " WHERE " + DataBaseConsts.MANGA.COLUMNS.EXCLUDED + " = 0 AND " + DataBaseConsts.MANGA.COLUMNS.FILE_FOLDER + " = :folder ")
    abstract fun listByFolder(folder: String): List<Manga>

    @Query("UPDATE " + DataBaseConsts.MANGA.TABLE_NAME + " SET " + DataBaseConsts.MANGA.COLUMNS.BOOK_MARK + " = :marker " + " WHERE " + DataBaseConsts.MANGA.COLUMNS.ID + " = :id ")
    abstract fun updateBookMark(id: Long, marker: Int)

    @Query("UPDATE " + DataBaseConsts.MANGA.TABLE_NAME + " SET " + DataBaseConsts.MANGA.COLUMNS.LAST_ACCESS + " = null, " + DataBaseConsts.MANGA.COLUMNS.BOOK_MARK + " = 0, " + DataBaseConsts.MANGA.COLUMNS.FAVORITE + " = false" + " WHERE " + DataBaseConsts.MANGA.COLUMNS.ID + " = :id ")
    abstract fun clearHistory(id: Long)

    @Query("UPDATE " + DataBaseConsts.MANGA.TABLE_NAME + " SET " + DataBaseConsts.MANGA.COLUMNS.LAST_ACCESS + " = null, " + DataBaseConsts.MANGA.COLUMNS.BOOK_MARK + " = 0, " + DataBaseConsts.MANGA.COLUMNS.FAVORITE + " = false")
    abstract fun clearHistory()

    @Query("UPDATE " + DataBaseConsts.MANGA.TABLE_NAME + " SET " + DataBaseConsts.MANGA.COLUMNS.EXCLUDED + " = 1 WHERE " + DataBaseConsts.MANGA.COLUMNS.ID + " = :id")
    abstract fun delete(id: Long)

    @Query("SELECT * FROM " + DataBaseConsts.MANGA.TABLE_NAME + " WHERE " + DataBaseConsts.MANGA.COLUMNS.EXCLUDED + " = 1")
    abstract fun listDeleted(): List<Manga>

    @Query("UPDATE " + DataBaseConsts.MANGA.TABLE_NAME + " SET " + DataBaseConsts.MANGA.COLUMNS.EXCLUDED + " = 0 WHERE " + DataBaseConsts.MANGA.COLUMNS.ID + " = :id")
    abstract fun clearDelete(id: Long)
}


@Dao
abstract class CoverDAO : DataBaseDAO<Cover> {

    @Query("SELECT * FROM " + DataBaseConsts.COVER.TABLE_NAME + " WHERE " + DataBaseConsts.COVER.COLUMNS.FK_ID_MANGA + " = :idManga AND " + DataBaseConsts.COVER.COLUMNS.ID + " = :id")
    abstract fun get(idManga: Long, id: Long): Cover

    @Query("SELECT * FROM " + DataBaseConsts.COVER.TABLE_NAME + " WHERE " + DataBaseConsts.COVER.COLUMNS.FK_ID_MANGA + " = :idManga LIMIT 1")
    abstract fun findFirstByIdManga(idManga: Long): Cover

    @Query("SELECT * FROM " + DataBaseConsts.COVER.TABLE_NAME + " WHERE " + DataBaseConsts.COVER.COLUMNS.FK_ID_MANGA + " = :idManga")
    abstract fun listByIdManga(idManga: Long): List<Cover>

    @Query("DELETE FROM " + DataBaseConsts.COVER.TABLE_NAME + " WHERE " + DataBaseConsts.COVER.COLUMNS.FK_ID_MANGA + " = :idManga")
    abstract fun deleteAll(idManga: Long)

}

@Dao
abstract class SubTitleDAO : DataBaseDAO<SubTitle> {

    @Query("SELECT * FROM " + DataBaseConsts.SUBTITLES.TABLE_NAME + " WHERE " + DataBaseConsts.SUBTITLES.COLUMNS.FK_ID_MANGA + " = :idManga AND " + DataBaseConsts.SUBTITLES.COLUMNS.ID + " = :Id")
    abstract fun get(idManga: Long, Id: Long): SubTitle

    @Query("SELECT * FROM " + DataBaseConsts.SUBTITLES.TABLE_NAME + " WHERE " + DataBaseConsts.SUBTITLES.COLUMNS.FK_ID_MANGA + " = :idManga LIMIT 1")
    abstract fun findByIdManga(idManga: Long): SubTitle

    @Query("SELECT * FROM " + DataBaseConsts.SUBTITLES.TABLE_NAME + " WHERE " + DataBaseConsts.SUBTITLES.COLUMNS.FK_ID_MANGA + " = :idManga")
    abstract fun listByIdManga(idManga: Long): List<SubTitle>

    @Query("DELETE FROM " + DataBaseConsts.SUBTITLES.TABLE_NAME + " WHERE " + DataBaseConsts.SUBTITLES.COLUMNS.FK_ID_MANGA + " = :idManga")
    abstract fun deleteAll(idManga: Long)

}


@Dao
abstract class KanjiJLPTDAO : DataBaseDAO<KanjiJLPT> {

    @Query("SELECT * FROM " + DataBaseConsts.JLPT.TABLE_NAME + " WHERE " + DataBaseConsts.JLPT.COLUMNS.ID + " = :id")
    abstract fun get(id: Long): KanjiJLPT

    @Query("SELECT * FROM " + DataBaseConsts.JLPT.TABLE_NAME)
    abstract fun list(): List<KanjiJLPT>

}

@Dao
abstract class KanjaxDAO : DataBaseDAO<Kanjax> {

    @Query("SELECT * FROM " + DataBaseConsts.KANJAX.TABLE_NAME + " WHERE " + DataBaseConsts.KANJAX.COLUMNS.ID + " = :id")
    abstract fun get(id: Long): Kanjax

    @Query("SELECT * FROM " + DataBaseConsts.KANJAX.TABLE_NAME + " WHERE " + DataBaseConsts.KANJAX.COLUMNS.KANJI + " = :kanji")
    abstract fun get(kanji: String): Kanjax

    @Query("SELECT * FROM " + DataBaseConsts.KANJAX.TABLE_NAME)
    abstract fun list(): List<Kanjax>

}

@Dao
abstract class FileLinkDAO : DataBaseDAO<FileLink> {

    @Query("SELECT * FROM " + DataBaseConsts.FILELINK.TABLE_NAME + " WHERE " + DataBaseConsts.FILELINK.COLUMNS.FK_ID_MANGA + " = :idManga AND " + DataBaseConsts.FILELINK.COLUMNS.FILE_NAME + " = :fileName AND " + DataBaseConsts.FILELINK.COLUMNS.PAGES + " = :pages")
    abstract fun get(idManga: Long, fileName: String, pages: Int): FileLink?

    @Query("SELECT * FROM " + DataBaseConsts.FILELINK.TABLE_NAME + " WHERE " + DataBaseConsts.FILELINK.COLUMNS.FK_ID_MANGA + " = :idManga ORDER BY " + DataBaseConsts.FILELINK.COLUMNS.LAST_ACCESS + " DESC LIMIT 1")
    abstract fun getLastAccess(idManga: Long): FileLink?

    @Query("DELETE FROM " + DataBaseConsts.FILELINK.TABLE_NAME + " WHERE " + DataBaseConsts.FILELINK.COLUMNS.FK_ID_MANGA + " = :idManga AND " + DataBaseConsts.FILELINK.COLUMNS.FILE_NAME + " = :fileName")
    abstract fun delete(idManga: Long, fileName: String)

    @Query("DELETE FROM " + DataBaseConsts.FILELINK.TABLE_NAME + " WHERE " + DataBaseConsts.FILELINK.COLUMNS.FK_ID_MANGA + " = :idManga AND " + DataBaseConsts.FILELINK.COLUMNS.ID + " = :idFileLink")
    abstract fun delete(idManga: Long, idFileLink: Long)

    @Query("DELETE FROM " + DataBaseConsts.FILELINK.TABLE_NAME + " WHERE " + DataBaseConsts.FILELINK.COLUMNS.FK_ID_MANGA + " = :idManga")
    abstract fun deleteAllByManga(idManga: Long)

}

@Dao
abstract class PageLinkDAO : DataBaseDAO<PageLink> {

    @Query("SELECT * FROM " + DataBaseConsts.PAGESLINK.TABLE_NAME + " WHERE " + DataBaseConsts.PAGESLINK.COLUMNS.NOT_LINKED + " = 0 AND " + DataBaseConsts.PAGESLINK.COLUMNS.FK_ID_FILE + " = :idFile")
    abstract fun getPageLink(idFile: Long): List<PageLink>

    @Query("SELECT * FROM " + DataBaseConsts.PAGESLINK.TABLE_NAME + " WHERE " + DataBaseConsts.PAGESLINK.COLUMNS.NOT_LINKED + " = 1 AND " + DataBaseConsts.PAGESLINK.COLUMNS.FK_ID_FILE + " = :idFile")
    abstract fun getPageNotLink(idFile: Long): List<PageLink>

    @Query("DELETE FROM " + DataBaseConsts.PAGESLINK.TABLE_NAME + " WHERE " + DataBaseConsts.PAGESLINK.COLUMNS.FK_ID_FILE + " = :idFile")
    abstract fun deleteAll(idFile: Long)

    @Query("DELETE FROM " + DataBaseConsts.PAGESLINK.TABLE_NAME + " WHERE " + DataBaseConsts.PAGESLINK.COLUMNS.FK_ID_FILE + " in (SELECT " +  DataBaseConsts.FILELINK.COLUMNS.ID + " FROM " + DataBaseConsts.FILELINK.TABLE_NAME + " WHERE " + DataBaseConsts.FILELINK.COLUMNS.FK_ID_MANGA + " = :idManga)")
    abstract fun deleteAllByManga(idManga: Long)

}


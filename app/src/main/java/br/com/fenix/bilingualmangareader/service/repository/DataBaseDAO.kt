package br.com.fenix.bilingualmangareader.service.repository

import androidx.paging.PagingSource
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteOpenHelper
import br.com.fenix.bilingualmangareader.model.entity.*
import br.com.fenix.bilingualmangareader.model.enums.Libraries
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
    @Query("SELECT count(*) FROM " + DataBaseConsts.MANGA.TABLE_NAME)
    abstract fun getMangaCount(): Int

    @Query("SELECT * FROM " + DataBaseConsts.MANGA.TABLE_NAME + " WHERE " + DataBaseConsts.MANGA.COLUMNS.FK_ID_LIBRARY + " = :library AND " + DataBaseConsts.MANGA.COLUMNS.EXCLUDED + " = 0")
    abstract fun list(library: Long?): List<Manga>

    @Query("SELECT * FROM " + DataBaseConsts.MANGA.TABLE_NAME + " WHERE " + DataBaseConsts.MANGA.COLUMNS.FK_ID_LIBRARY + " = :library AND " + DataBaseConsts.MANGA.COLUMNS.EXCLUDED + " = 0 AND " + DataBaseConsts.MANGA.COLUMNS.LAST_ALTERATION + " >= datetime('now','-1 day')")
    abstract fun listRecentChange(library: Long?): List<Manga>

    @Query("SELECT * FROM " + DataBaseConsts.MANGA.TABLE_NAME + " WHERE " + DataBaseConsts.MANGA.COLUMNS.FK_ID_LIBRARY + " = :library AND " + DataBaseConsts.MANGA.COLUMNS.EXCLUDED + " = 1 AND " + DataBaseConsts.MANGA.COLUMNS.LAST_ALTERATION + " >= datetime('now','-1 day')")
    abstract fun listRecentDeleted(library: Long?): List<Manga>

    @Query(
        "SELECT * FROM ( " +
                " SELECT ${DataBaseConsts.MANGA.COLUMNS.ID}, ${DataBaseConsts.MANGA.COLUMNS.TITLE}, ${DataBaseConsts.MANGA.COLUMNS.SUB_TITLE}, ${DataBaseConsts.MANGA.COLUMNS.FILE_PATH}, " +
                "        ${DataBaseConsts.MANGA.COLUMNS.FILE_FOLDER}, ${DataBaseConsts.MANGA.COLUMNS.FILE_NAME}, ${DataBaseConsts.MANGA.COLUMNS.FILE_TYPE}, ${DataBaseConsts.MANGA.COLUMNS.PAGES}, " +
                "        ${DataBaseConsts.MANGA.COLUMNS.BOOK_MARK}, ${DataBaseConsts.MANGA.COLUMNS.FAVORITE}, ${DataBaseConsts.MANGA.COLUMNS.HAS_SUBTITLE}, ${DataBaseConsts.MANGA.COLUMNS.DATE_CREATE}, " +
                "        ${DataBaseConsts.MANGA.COLUMNS.LAST_ACCESS}, ${DataBaseConsts.MANGA.COLUMNS.FK_ID_LIBRARY}, ${DataBaseConsts.MANGA.COLUMNS.EXCLUDED} AS excluded, ${DataBaseConsts.MANGA.COLUMNS.LAST_ACCESS} AS sort " +
                " FROM " + DataBaseConsts.MANGA.TABLE_NAME +
                " WHERE " + DataBaseConsts.MANGA.COLUMNS.LAST_ACCESS + " is not null " +
                "UNION" +
                " SELECT null AS ${DataBaseConsts.MANGA.COLUMNS.ID}, '' AS ${DataBaseConsts.MANGA.COLUMNS.TITLE}, " +
                "        '' AS ${DataBaseConsts.MANGA.COLUMNS.SUB_TITLE}, '' AS ${DataBaseConsts.MANGA.COLUMNS.FILE_PATH}, '' AS ${DataBaseConsts.MANGA.COLUMNS.FILE_FOLDER}, " +
                "        '' AS ${DataBaseConsts.MANGA.COLUMNS.FILE_NAME}, '' AS ${DataBaseConsts.MANGA.COLUMNS.FILE_TYPE}, 0 AS ${DataBaseConsts.MANGA.COLUMNS.PAGES}, " +
                "        0 AS ${DataBaseConsts.MANGA.COLUMNS.BOOK_MARK}, 0 AS ${DataBaseConsts.MANGA.COLUMNS.FAVORITE}, ${DataBaseConsts.MANGA.COLUMNS.HAS_SUBTITLE},  null AS ${DataBaseConsts.MANGA.COLUMNS.DATE_CREATE}, " +
                "        Substr(${DataBaseConsts.MANGA.COLUMNS.LAST_ACCESS}, 0, 12) || '00:00:00.000' AS ${DataBaseConsts.MANGA.COLUMNS.LAST_ACCESS}," +
                "        ${DataBaseConsts.MANGA.COLUMNS.FK_ID_LIBRARY}, ${DataBaseConsts.MANGA.COLUMNS.EXCLUDED} AS excluded, " +
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

    @Query("SELECT * FROM " + DataBaseConsts.MANGA.TABLE_NAME + " WHERE " + DataBaseConsts.MANGA.COLUMNS.EXCLUDED + " = 0 AND " + DataBaseConsts.MANGA.COLUMNS.FILE_FOLDER + " = :folder ORDER BY " + DataBaseConsts.MANGA.COLUMNS.TITLE)
    abstract fun listByFolder(folder: String): List<Manga>

    @Query("SELECT * FROM " + DataBaseConsts.MANGA.TABLE_NAME + " WHERE " + DataBaseConsts.MANGA.COLUMNS.FK_ID_LIBRARY + " = :library AND " + DataBaseConsts.MANGA.COLUMNS.EXCLUDED + " = 0 ORDER BY " + DataBaseConsts.MANGA.COLUMNS.TITLE)
    abstract fun listOrderByTitle(library: Long?): List<Manga>

    @Query("UPDATE " + DataBaseConsts.MANGA.TABLE_NAME + " SET " + DataBaseConsts.MANGA.COLUMNS.BOOK_MARK + " = :marker " + " WHERE " + DataBaseConsts.MANGA.COLUMNS.ID + " = :id ")
    abstract fun updateBookMark(id: Long, marker: Int)

    @Query("UPDATE " + DataBaseConsts.MANGA.TABLE_NAME + " SET " + DataBaseConsts.MANGA.COLUMNS.EXCLUDED + " = 1 WHERE " + DataBaseConsts.MANGA.COLUMNS.ID + " = :id")
    abstract fun delete(id: Long)

    @Query("SELECT * FROM " + DataBaseConsts.MANGA.TABLE_NAME + " WHERE " + DataBaseConsts.MANGA.COLUMNS.FK_ID_LIBRARY + " = :library AND " + DataBaseConsts.MANGA.COLUMNS.EXCLUDED + " = 1")
    abstract fun listDeleted(library: Long?): List<Manga>

    @Query("UPDATE " + DataBaseConsts.MANGA.TABLE_NAME + " SET " + DataBaseConsts.MANGA.COLUMNS.EXCLUDED + " = 1 WHERE " + DataBaseConsts.MANGA.COLUMNS.FK_ID_LIBRARY + " = :library")
    abstract fun deleteLibrary(library: Long?)

    @Query("UPDATE " + DataBaseConsts.MANGA.TABLE_NAME + " SET " + DataBaseConsts.MANGA.COLUMNS.EXCLUDED + " = 0 WHERE " + DataBaseConsts.MANGA.COLUMNS.FK_ID_LIBRARY + " = :library AND " + DataBaseConsts.MANGA.COLUMNS.ID + " = :id")
    abstract fun clearDelete(library: Long?, id: Long)

    @Query("SELECT * FROM " + DataBaseConsts.MANGA.TABLE_NAME + " WHERE " + DataBaseConsts.MANGA.COLUMNS.EXCLUDED + " = 0 ORDER BY " + DataBaseConsts.MANGA.COLUMNS.LAST_ACCESS + " DESC LIMIT 2")
    abstract fun getLastOpen(): List<Manga>?

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

    @Query("UPDATE " + DataBaseConsts.MANGA.TABLE_NAME + " SET " + DataBaseConsts.MANGA.COLUMNS.HAS_SUBTITLE + " = :hasSubtitle" + " WHERE " + DataBaseConsts.MANGA.COLUMNS.ID + " = :idManga")
    abstract fun updateHasSubtitle(idManga: Long, hasSubtitle: Boolean)

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

    @Query("SELECT * FROM " + DataBaseConsts.FILELINK.TABLE_NAME + " WHERE " + DataBaseConsts.FILELINK.COLUMNS.FK_ID_MANGA + " = :idManga")
    abstract fun get(idManga: Long): List<FileLink>?

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

    @Query("DELETE FROM " + DataBaseConsts.PAGESLINK.TABLE_NAME + " WHERE " + DataBaseConsts.PAGESLINK.COLUMNS.FK_ID_FILE + " in (SELECT " + DataBaseConsts.FILELINK.COLUMNS.ID + " FROM " + DataBaseConsts.FILELINK.TABLE_NAME + " WHERE " + DataBaseConsts.FILELINK.COLUMNS.FK_ID_MANGA + " = :idManga)")
    abstract fun deleteAllByManga(idManga: Long)

}


@Dao
abstract class VocabularyDAO : DataBaseDAO<Vocabulary> {

    @Query(
        "SELECT V.*, (SELECT SUM(" + DataBaseConsts.MANGA_VOCABULARY.COLUMNS.APPEARS + ") FROM " + DataBaseConsts.MANGA_VOCABULARY.TABLE_NAME + " CT " +
                "     WHERE V." + DataBaseConsts.VOCABULARY.COLUMNS.ID + " = CT." + DataBaseConsts.MANGA_VOCABULARY.COLUMNS.ID_VOCABULARY + " ) count "+
                " FROM " + DataBaseConsts.VOCABULARY.TABLE_NAME + " V " +
                " WHERE CASE WHEN 1 = :favorite THEN " + DataBaseConsts.VOCABULARY.COLUMNS.FAVORITE + " = :favorite ELSE 1 > 0 END " +
                " GROUP BY " + DataBaseConsts.VOCABULARY.COLUMNS.WORD + " ORDER BY count DESC, " + DataBaseConsts.VOCABULARY.COLUMNS.WORD
    )
    abstract fun list(favorite: Boolean): PagingSource<Int, Vocabulary>

    @Query(
        "SELECT V.*, (SELECT SUM(" + DataBaseConsts.MANGA_VOCABULARY.COLUMNS.APPEARS + ") FROM " + DataBaseConsts.MANGA_VOCABULARY.TABLE_NAME + " CT " +
                "     WHERE V." + DataBaseConsts.VOCABULARY.COLUMNS.ID + " = CT." + DataBaseConsts.MANGA_VOCABULARY.COLUMNS.ID_VOCABULARY + " ) count "+
                " FROM " + DataBaseConsts.VOCABULARY.TABLE_NAME +  " V " +
                " WHERE (" + DataBaseConsts.VOCABULARY.COLUMNS.WORD + " LIKE '%' || :vocabulary || '%' OR " + DataBaseConsts.VOCABULARY.COLUMNS.BASIC_FORM + " LIKE '%' || :basicForm || '%' )" +
                " AND CASE WHEN 1 = :favorite THEN " + DataBaseConsts.VOCABULARY.COLUMNS.FAVORITE + " = :favorite ELSE 1 > 0 END " +
                " GROUP BY " + DataBaseConsts.VOCABULARY.COLUMNS.WORD + " ORDER BY count DESC, " + DataBaseConsts.VOCABULARY.COLUMNS.WORD
    )
    abstract fun list(vocabulary: String, basicForm: String, favorite: Boolean): PagingSource<Int, Vocabulary>

    @Query(
        "SELECT V.*, (SELECT SUM(" + DataBaseConsts.MANGA_VOCABULARY.COLUMNS.APPEARS + ") FROM " + DataBaseConsts.MANGA_VOCABULARY.TABLE_NAME + " CT " +
                "     WHERE V." + DataBaseConsts.VOCABULARY.COLUMNS.ID + " = CT." + DataBaseConsts.MANGA_VOCABULARY.COLUMNS.ID_VOCABULARY + " ) count "+
                " FROM " + DataBaseConsts.VOCABULARY.TABLE_NAME + " V " +
                " INNER JOIN " + DataBaseConsts.MANGA_VOCABULARY.TABLE_NAME + " MGV ON MGV." + DataBaseConsts.MANGA_VOCABULARY.COLUMNS.ID_VOCABULARY + " = V." + DataBaseConsts.VOCABULARY.COLUMNS.ID +
                " INNER JOIN " + DataBaseConsts.MANGA.TABLE_NAME + " MG ON MGV." + DataBaseConsts.MANGA_VOCABULARY.COLUMNS.ID_MANGA + " = MG." + DataBaseConsts.MANGA.COLUMNS.ID +
                " WHERE CASE WHEN LENGTH(:manga) <> 0 THEN MG." + DataBaseConsts.MANGA.COLUMNS.TITLE + " LIKE '%' || :manga || '%' ELSE 1 > 0 END " +
                " AND CASE WHEN 1 = :favorite THEN V." + DataBaseConsts.VOCABULARY.COLUMNS.FAVORITE + " = :favorite ELSE 1 > 0 END " +
                " GROUP BY " + DataBaseConsts.VOCABULARY.COLUMNS.WORD + " ORDER BY count DESC, " + DataBaseConsts.VOCABULARY.COLUMNS.WORD
    )
    abstract fun list(manga: String, favorite: Boolean): PagingSource<Int, Vocabulary>

    @Query(
        "SELECT V.*, (SELECT SUM(" + DataBaseConsts.MANGA_VOCABULARY.COLUMNS.APPEARS + ") FROM " + DataBaseConsts.MANGA_VOCABULARY.TABLE_NAME + " CT " +
                "     WHERE V." + DataBaseConsts.VOCABULARY.COLUMNS.ID + " = CT." + DataBaseConsts.MANGA_VOCABULARY.COLUMNS.ID_VOCABULARY + " ) count "+
                " FROM " + DataBaseConsts.VOCABULARY.TABLE_NAME + " V " +
                " INNER JOIN " + DataBaseConsts.MANGA_VOCABULARY.TABLE_NAME + " MGV ON MGV." + DataBaseConsts.MANGA_VOCABULARY.COLUMNS.ID_VOCABULARY + " = V." + DataBaseConsts.VOCABULARY.COLUMNS.ID +
                " INNER JOIN " + DataBaseConsts.MANGA.TABLE_NAME + " MG ON MGV." + DataBaseConsts.MANGA_VOCABULARY.COLUMNS.ID_MANGA + " = MG." + DataBaseConsts.MANGA.COLUMNS.ID +
                " WHERE CASE WHEN LENGTH(:manga) <> 0 THEN MG." + DataBaseConsts.MANGA.COLUMNS.TITLE + " LIKE '%' || :manga || '%' ELSE 1 > 0 END " +
                " AND (V." + DataBaseConsts.VOCABULARY.COLUMNS.WORD + " LIKE '%' || :vocabulary || '%' OR V." + DataBaseConsts.VOCABULARY.COLUMNS.BASIC_FORM + " LIKE '%' || :basicForm || '%' )" +
                " AND CASE WHEN 1 = :favorite THEN V." + DataBaseConsts.VOCABULARY.COLUMNS.FAVORITE + " = :favorite ELSE 1 > 0 END " +
                " GROUP BY " + DataBaseConsts.VOCABULARY.COLUMNS.WORD + " ORDER BY count DESC, " + DataBaseConsts.VOCABULARY.COLUMNS.WORD
    )
    abstract fun list(manga: String, vocabulary: String, basicForm: String, favorite: Boolean): PagingSource<Int, Vocabulary>

    @Query("SELECT * FROM " + DataBaseConsts.VOCABULARY.TABLE_NAME + " WHERE " + DataBaseConsts.VOCABULARY.COLUMNS.ID + " = :id")
    abstract fun get(id: Long): Vocabulary

    @Query("SELECT * FROM " + DataBaseConsts.VOCABULARY.TABLE_NAME + " WHERE " + DataBaseConsts.VOCABULARY.COLUMNS.WORD + " = :vocabulary AND " + DataBaseConsts.VOCABULARY.COLUMNS.BASIC_FORM + " = :basicForm LIMIT 1")
    abstract fun find(vocabulary: String, basicForm: String): Vocabulary?

    @Query("SELECT * FROM " + DataBaseConsts.VOCABULARY.TABLE_NAME + " WHERE " + DataBaseConsts.VOCABULARY.COLUMNS.WORD + " = :vocabulary OR " + DataBaseConsts.VOCABULARY.COLUMNS.BASIC_FORM + " = :vocabulary LIMIT 1")
    abstract fun find(vocabulary: String): Vocabulary?

    @Query("SELECT * FROM " + DataBaseConsts.VOCABULARY.TABLE_NAME + " WHERE " + DataBaseConsts.VOCABULARY.COLUMNS.WORD + " = :vocabulary OR " + DataBaseConsts.VOCABULARY.COLUMNS.BASIC_FORM + " = :vocabulary")
    abstract fun findAll(vocabulary: String): List<Vocabulary>

    @Query("SELECT * FROM " + DataBaseConsts.VOCABULARY.TABLE_NAME + " WHERE " + DataBaseConsts.VOCABULARY.COLUMNS.ID + " = :idManga ")
    abstract fun find(idManga: Long): List<Vocabulary>

    @Query(
        "SELECT * FROM " + DataBaseConsts.VOCABULARY.TABLE_NAME + " WHERE " + DataBaseConsts.VOCABULARY.COLUMNS.WORD + " = :vocabulary " +
                " AND CASE WHEN LENGTH(:basicForm) = 0 THEN " + DataBaseConsts.VOCABULARY.COLUMNS.BASIC_FORM + " IS NULL ELSE " + DataBaseConsts.VOCABULARY.COLUMNS.BASIC_FORM + " = :basicForm END LIMIT 1"
    )
    abstract fun exists(vocabulary: String, basicForm: String): Vocabulary?

    @Query("SELECT * FROM " + DataBaseConsts.MANGA_VOCABULARY.TABLE_NAME + " WHERE " + DataBaseConsts.MANGA_VOCABULARY.COLUMNS.ID_VOCABULARY + " = :idVocabulary GROUP BY " + DataBaseConsts.MANGA_VOCABULARY.COLUMNS.ID_MANGA)
    abstract fun findByVocabulary(idVocabulary: Long): List<VocabularyManga>

    @Query("SELECT * FROM " + DataBaseConsts.MANGA_VOCABULARY.TABLE_NAME + " WHERE " + DataBaseConsts.MANGA_VOCABULARY.COLUMNS.ID_MANGA + " = :idManga GROUP BY " + DataBaseConsts.MANGA_VOCABULARY.COLUMNS.ID_VOCABULARY)
    abstract fun findByManga(idManga: Long): List<VocabularyManga>

    @Query("SELECT * FROM " + DataBaseConsts.MANGA.TABLE_NAME + " WHERE " + DataBaseConsts.MANGA.COLUMNS.ID + " = :id")
    abstract fun getManga(id: Long): Manga

    fun insert(dbHelper: SupportSQLiteOpenHelper, idManga: Long, idVocabulary: Long, appears: Int) {
        val database = dbHelper.readableDatabase
        database.execSQL(
            "INSERT OR REPLACE INTO " + DataBaseConsts.MANGA_VOCABULARY.TABLE_NAME +
                    " (" + DataBaseConsts.MANGA_VOCABULARY.COLUMNS.ID_MANGA + ',' + DataBaseConsts.MANGA_VOCABULARY.COLUMNS.ID_VOCABULARY + ',' + DataBaseConsts.MANGA_VOCABULARY.COLUMNS.APPEARS +
                    ") VALUES ($idManga, $idVocabulary, $appears)"
        )
    }

}


@Dao
abstract class LibrariesDAO : DataBaseDAO<Library> {

    @Query("SELECT * FROM " + DataBaseConsts.LIBRARIES.TABLE_NAME + " WHERE " + DataBaseConsts.LIBRARIES.COLUMNS.EXCLUDED + " = 0")
    abstract fun list(): List<Library>

    @Query("SELECT * FROM " + DataBaseConsts.LIBRARIES.TABLE_NAME + " WHERE " + DataBaseConsts.LIBRARIES.COLUMNS.EXCLUDED + " = 0" + " AND " + DataBaseConsts.LIBRARIES.COLUMNS.ENABLED + " = 1")
    abstract fun listEnabled(): List<Library>

    @Query("SELECT * FROM " + DataBaseConsts.LIBRARIES.TABLE_NAME + " WHERE " + DataBaseConsts.LIBRARIES.COLUMNS.ID + " = :id")
    abstract fun get(id: Long): Library

    @Query("SELECT * FROM " + DataBaseConsts.LIBRARIES.TABLE_NAME + " WHERE " + DataBaseConsts.LIBRARIES.COLUMNS.TYPE + " = :type")
    abstract fun get(type: Libraries): Library

    @Query("UPDATE " + DataBaseConsts.LIBRARIES.TABLE_NAME + " SET " + DataBaseConsts.LIBRARIES.COLUMNS.EXCLUDED + " = 1 WHERE " + DataBaseConsts.LIBRARIES.COLUMNS.ID + " = :id")
    abstract fun delete(id: Long)

    @Query("UPDATE " + DataBaseConsts.LIBRARIES.TABLE_NAME + " SET " + DataBaseConsts.LIBRARIES.COLUMNS.EXCLUDED + " = 1 ")
    abstract fun deleteAll()

    @Query("SELECT * FROM " + DataBaseConsts.LIBRARIES.TABLE_NAME + " WHERE " + DataBaseConsts.LIBRARIES.COLUMNS.EXCLUDED + " = 1 AND " + DataBaseConsts.LIBRARIES.COLUMNS.PATH + " = :path")
    abstract fun findDeleted(path: String): Library?

    @Query("UPDATE " + DataBaseConsts.LIBRARIES.TABLE_NAME + " SET " + DataBaseConsts.LIBRARIES.COLUMNS.EXCLUDED + " = 1 WHERE " + DataBaseConsts.LIBRARIES.COLUMNS.PATH + " = :path")
    abstract fun removeDefault(path: String)

}


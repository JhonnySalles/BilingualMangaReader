package br.com.fenix.mangareader.service.repository

import androidx.room.*
import br.com.fenix.mangareader.model.entity.*
import br.com.fenix.mangareader.util.constants.DataBaseConsts
import java.time.LocalDateTime

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
    @Query("SELECT * FROM " + DataBaseConsts.MANGA.TABLE_NAME)
    abstract fun list(): List<Manga>

    @Query("SELECT * FROM " + DataBaseConsts.MANGA.TABLE_NAME + " WHERE " + DataBaseConsts.MANGA.COLUMNS.LAST_ACCESS + " > :dateInitial ORDER BY " + DataBaseConsts.MANGA.COLUMNS.LAST_ACCESS + " DESC")
    abstract fun listHistory(dateInitial: LocalDateTime? = LocalDateTime.MIN): List<Manga>

    @Query("SELECT * FROM " + DataBaseConsts.MANGA.TABLE_NAME + " WHERE " + DataBaseConsts.MANGA.COLUMNS.ID + " = :id")
    abstract fun get(id: Long): Manga

    @Query("SELECT * FROM " + DataBaseConsts.MANGA.TABLE_NAME + " WHERE " + DataBaseConsts.MANGA.COLUMNS.FILE_NAME + " = :name")
    abstract fun get(name: String): Manga

    @Query("SELECT * FROM " + DataBaseConsts.MANGA.TABLE_NAME + " WHERE " + DataBaseConsts.MANGA.COLUMNS.FILE_FOLDER + " = :folder ")
    abstract fun listByFolder(folder: String?): List<Manga>

    @Query("UPDATE " + DataBaseConsts.MANGA.TABLE_NAME + " SET " + DataBaseConsts.MANGA.COLUMNS.BOOK_MARK + " = :marker " + " WHERE " + DataBaseConsts.MANGA.COLUMNS.ID + " = :id ")
    abstract fun updateBookMark(id: Long, marker: Int)

    @Query("UPDATE " + DataBaseConsts.MANGA.TABLE_NAME + " SET " + DataBaseConsts.MANGA.COLUMNS.LAST_ACCESS + " = :access " + " WHERE " + DataBaseConsts.MANGA.COLUMNS.ID + " = :id ")
    abstract fun updateLastAccess(id: Long, access: String)

    @Query("UPDATE " + DataBaseConsts.MANGA.TABLE_NAME + " SET " + DataBaseConsts.MANGA.COLUMNS.LAST_ACCESS + " = :dateInitial, " + DataBaseConsts.MANGA.COLUMNS.BOOK_MARK + " = 0, " + DataBaseConsts.MANGA.COLUMNS.FAVORITE + " = false" + " WHERE " + DataBaseConsts.MANGA.COLUMNS.ID + " = :id ")
    abstract fun clearHistory(dateInitial: LocalDateTime? = LocalDateTime.MIN)

    @Query("UPDATE " + DataBaseConsts.MANGA.TABLE_NAME + " SET " + DataBaseConsts.MANGA.COLUMNS.LAST_ACCESS + " = :dateInitial, " + DataBaseConsts.MANGA.COLUMNS.BOOK_MARK + " = 0, " + DataBaseConsts.MANGA.COLUMNS.FAVORITE + " = false")
    abstract fun clearHistory(id: Long, dateInitial: LocalDateTime? = LocalDateTime.MIN)
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


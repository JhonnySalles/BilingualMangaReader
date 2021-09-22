package br.com.fenix.mangareader.service.repository

import androidx.room.*
import br.com.fenix.mangareader.model.entity.Book
import br.com.fenix.mangareader.model.entity.Cover
import br.com.fenix.mangareader.model.entity.SubTitle
import br.com.fenix.mangareader.util.constants.DataBaseConsts
import org.intellij.lang.annotations.Language

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
abstract class BookDAO : DataBaseDAO<Book> {

    @Query("SELECT * FROM " + DataBaseConsts.BOOK.TABLE_NAME)
    abstract fun list(): List<Book>

    @Query("SELECT * FROM " + DataBaseConsts.BOOK.TABLE_NAME + " WHERE " + DataBaseConsts.BOOK.COLUMNS.ID + " = :id")
    abstract fun get(id: Long): Book

    @Query("SELECT * FROM " + DataBaseConsts.BOOK.TABLE_NAME + " WHERE " + DataBaseConsts.BOOK.COLUMNS.FILE_NAME + " = :name")
    abstract fun get(name: String): Book

    @Query("SELECT * FROM " + DataBaseConsts.BOOK.TABLE_NAME + " WHERE " + DataBaseConsts.BOOK.COLUMNS.FILE_FOLDER + " = :folder ")
    abstract fun listByFolder(folder : String?): List<Book>

    @Query("UPDATE " + DataBaseConsts.BOOK.TABLE_NAME + " SET " + DataBaseConsts.BOOK.COLUMNS.BOOK_MARK + " = :marker " + " WHERE " + DataBaseConsts.BOOK.COLUMNS.ID + " = :id ")
    abstract fun updateBookMark(id: Long, marker:Int)

    @Query("UPDATE " + DataBaseConsts.BOOK.TABLE_NAME + " SET " + DataBaseConsts.BOOK.COLUMNS.LAST_ACCESS + " = :acess " + " WHERE " + DataBaseConsts.BOOK.COLUMNS.ID + " = :id ")
    abstract fun updateLastAcess(id: Long, acess:String)
}


@Dao
abstract class CoverDAO : DataBaseDAO<Cover> {

    @Query("SELECT * FROM " + DataBaseConsts.COVER.TABLE_NAME + " WHERE " + DataBaseConsts.COVER.COLUMNS.FK_ID_BOOK + " = :idBook AND " + DataBaseConsts.COVER.COLUMNS.ID + " = :id")
    abstract fun get(idBook: Long, id: Long): Cover

    @Query("SELECT * FROM " + DataBaseConsts.COVER.TABLE_NAME + " WHERE " + DataBaseConsts.COVER.COLUMNS.FK_ID_BOOK + " = :idBook LIMIT 1")
    abstract fun findFirstByIdBook(idBook: Long): Cover

    @Query("SELECT * FROM " + DataBaseConsts.COVER.TABLE_NAME + " WHERE " + DataBaseConsts.COVER.COLUMNS.FK_ID_BOOK + " = :idBook")
    abstract fun listByIdBook(idBook: Long): List<Cover>

    @Query("DELETE FROM " + DataBaseConsts.COVER.TABLE_NAME + " WHERE " + DataBaseConsts.COVER.COLUMNS.FK_ID_BOOK + " = :idBook")
    abstract fun deleteAll(idBook: Long)

}

@Dao
abstract class SubTitleDAO : DataBaseDAO<SubTitle> {

    @Query("SELECT * FROM " + DataBaseConsts.SUBTITLES.TABLE_NAME + " WHERE " + DataBaseConsts.SUBTITLES.COLUMNS.FK_ID_BOOK + " = :idBook AND " + DataBaseConsts.SUBTITLES.COLUMNS.ID + " = :id")
    abstract fun get(idBook: Long, id: Long): SubTitle

    @Query("SELECT * FROM " + DataBaseConsts.SUBTITLES.TABLE_NAME + " WHERE " + DataBaseConsts.SUBTITLES.COLUMNS.FK_ID_BOOK + " = :idBook")
    abstract fun listByIdBook(idBook: Long): List<SubTitle>

    @Query("DELETE FROM " + DataBaseConsts.SUBTITLES.TABLE_NAME + " WHERE " + DataBaseConsts.SUBTITLES.COLUMNS.FK_ID_BOOK + " = :idBook")
    abstract fun deleteAll(idBook: Long)

}


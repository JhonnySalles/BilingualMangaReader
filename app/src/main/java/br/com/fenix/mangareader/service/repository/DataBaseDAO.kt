package br.com.fenix.mangareader.service.repository

import androidx.room.*
import br.com.fenix.mangareader.model.entity.Book
import br.com.fenix.mangareader.model.entity.Cover
import br.com.fenix.mangareader.util.constants.DataBaseConsts

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

    @Query("SELECT * FROM " + DataBaseConsts.BOOK.TABLE_NAME + " WHERE " + DataBaseConsts.BOOK.COLUMNS.FILE_PATH + " = :path ")
    abstract fun listByPath(path : String?): List<Book>

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

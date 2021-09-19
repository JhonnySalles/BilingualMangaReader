package br.com.fenix.mangareader.service.repository

import android.content.Context
import android.util.Log
import br.com.fenix.mangareader.model.entity.Book
import br.com.fenix.mangareader.util.constants.GeneralConsts

class BookRepository(context: Context) {

    private val mCoverRepository: CoverRepository = CoverRepository(context)
    private var mDataBase = DataBase.getDataBase(context).getBookDao()

    fun save(obj: Book): Long {
        val id = mDataBase.save(obj)

        if (obj.thumbnail != null)
            mCoverRepository.save(obj.thumbnail!!)

        return id
    }

    fun update(obj: Book) {
        mDataBase.update(obj)

        if (obj.thumbnail != null && obj.thumbnail!!.update)
            mCoverRepository.save(obj.thumbnail!!)
    }

    fun updateBookMark(obj: Book) {
        if (obj.id != null)
            mDataBase.updateBookMark(obj.id!!, obj.bookMark)
    }

    fun delete(obj: Book) {
        mCoverRepository.deleteAll(obj.id!!)
        mDataBase.delete(obj)
    }

    fun list(withCover: Boolean): List<Book>? {
        return try {
            var list = mDataBase.list()

            if (withCover)
                for (book in list)
                    book.thumbnail = mCoverRepository.findFirstByIdBook(book.id!!)

            list
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.DATABASE.LIST, e.message.toString())
            null
        }
    }

    fun get(id: Long): Book? {
        return try {
            mDataBase.get(id)
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.DATABASE.SELECT, e.message.toString())
            null
        }
    }

    fun findByFileName(name: String): Book? {
        return try {
            mDataBase.get(name)
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.DATABASE.SELECT, e.message.toString())
            null
        }
    }

    fun findByFileFolder(folder: String): List<Book>? {
        return try {
            mDataBase.listByFolder(folder)
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.DATABASE.LIST, e.message.toString())
            null
        }
    }
}
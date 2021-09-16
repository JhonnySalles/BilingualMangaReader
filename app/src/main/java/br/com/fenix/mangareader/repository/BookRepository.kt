package br.com.fenix.mangareader.repository

import android.content.Context
import android.util.Log
import br.com.fenix.mangareader.constants.GeneralConsts
import br.com.fenix.mangareader.model.Book

class BookRepository(context: Context) {

    private val mCoverRepository: CoverRepository = CoverRepository(context)
    private var mDataBase = DataBase.getDataBase(context).getBookDao()

    fun save(obj: Book): Long {
        val id = mDataBase.save(obj)

        if (obj.tumbnail != null)
            mCoverRepository.save(obj.tumbnail!!)

        return id
    }

    fun update(obj: Book) {
        mDataBase.update(obj)

        if (obj.tumbnail != null && obj.tumbnail!!.update)
            mCoverRepository.save(obj.tumbnail!!)
    }

    fun delete(obj: Book) {
        mCoverRepository.deleteAll(obj.id)
        mDataBase.delete(obj)
    }

    fun list(): List<Book>? {
        return try {
            mDataBase.list()
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, e.message.toString())
            null
        }
    }

    fun get(id: Long): Book? {
        return try {
            mDataBase.get(id)
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, e.message.toString())
            null
        }
    }

    fun findByFileName(name: String): Book? {
        return try {
            mDataBase.get(name)
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, e.message.toString())
            null
        }
    }
}
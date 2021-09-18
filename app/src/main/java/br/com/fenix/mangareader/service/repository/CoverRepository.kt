package br.com.fenix.mangareader.service.repository

import android.content.Context
import android.util.Log
import br.com.fenix.mangareader.model.entity.Cover
import br.com.fenix.mangareader.util.constants.GeneralConsts

class CoverRepository(context: Context) {

    private var mDataBase = DataBase.getDataBase(context).getCoverDao()

    fun save(obj: Cover): Long {
        return mDataBase.save(obj)
    }

    fun update(obj: Cover) {
        mDataBase.update(obj)
    }

    fun delete(obj: Cover) {
        mDataBase.delete(obj)
    }

    fun deleteAll(idBook: Long) {
        mDataBase.deleteAll(idBook)
    }

    fun get(idBook: Long, id: Long): Cover? {
        return try {
            mDataBase.get(idBook, id)
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, e.message.toString())
            null
        }
    }

    fun findFirstByIdBook(idBook: Long): Cover? {
        return try {
            mDataBase.findFirstByIdBook(idBook)
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, e.message.toString())
            null
        }
    }

    fun listByIdBook(idBook: Long): List<Cover>? {
        return try {
            mDataBase.listByIdBook(idBook)
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, e.message.toString())
            null
        }
    }

}
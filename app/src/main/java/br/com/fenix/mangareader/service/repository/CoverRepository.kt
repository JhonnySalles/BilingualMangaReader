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

    fun deleteAll(idManga: Long) {
        mDataBase.deleteAll(idManga)
    }

    fun get(idManga: Long, id: Long): Cover? {
        return try {
            mDataBase.get(idManga, id)
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, e.message.toString())
            null
        }
    }

    fun findFirstByIdManga(idManga: Long): Cover? {
        return try {
            mDataBase.findFirstByIdManga(idManga)
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, e.message.toString())
            null
        }
    }

    fun listByIdManga(idManga: Long): List<Cover>? {
        return try {
            mDataBase.listByIdManga(idManga)
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, e.message.toString())
            null
        }
    }

}
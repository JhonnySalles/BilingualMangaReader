package br.com.fenix.mangareader.service.repository

import android.content.Context
import android.util.Log
import br.com.fenix.mangareader.model.entity.SubTitle
import br.com.fenix.mangareader.util.constants.GeneralConsts

class SubTitleRepository(context: Context) {

    private var mDataBase = DataBase.getDataBase(context).getSubTitleDao()

    fun save(obj: SubTitle): Long {
        return mDataBase.save(obj)
    }

    fun update(obj: SubTitle) {
        mDataBase.update(obj)
    }

    fun delete(obj: SubTitle) {
        mDataBase.delete(obj)
    }

    fun deleteAll(idManga: Long) {
        mDataBase.deleteAll(idManga)
    }

    fun get(idManga: Long, id: Long): SubTitle? {
        return try {
            mDataBase.get(idManga, id)
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, e.message.toString())
            null
        }
    }

    fun listByIdBook(idManga: Long): List<SubTitle>? {
        return try {
            mDataBase.listByIdManga(idManga)
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, e.message.toString())
            null
        }
    }

}
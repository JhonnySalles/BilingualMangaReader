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

    fun deleteAll(idBook: Long) {
        mDataBase.deleteAll(idBook)
    }

    fun get(idBook: Long, id: Long): SubTitle? {
        return try {
            mDataBase.get(idBook, id)
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, e.message.toString())
            null
        }
    }

    fun listByIdBook(idBook: Long): List<SubTitle>? {
        return try {
            mDataBase.listByIdBook(idBook)
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, e.message.toString())
            null
        }
    }

}
package br.com.fenix.bilingualmangareader.service.repository

import android.content.Context
import android.util.Log
import br.com.fenix.bilingualmangareader.model.entity.SubTitle
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts

class SubTitleRepository(context: Context) {

    private var mDataBase = DataBase.getDataBase(context).getSubTitleDao()

    fun save(obj: SubTitle): Long {
        deleteAll(obj.id_manga)
        return mDataBase.save(obj)
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

    fun findByIdManga(idManga: Long): SubTitle? {
        return try {
            mDataBase.findByIdManga(idManga)
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, e.message.toString())
            null
        }
    }

}
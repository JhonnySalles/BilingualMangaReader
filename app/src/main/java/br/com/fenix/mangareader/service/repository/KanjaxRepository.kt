package br.com.fenix.mangareader.service.repository

import android.content.Context
import android.util.Log
import br.com.fenix.mangareader.model.entity.Kanjax
import br.com.fenix.mangareader.util.constants.GeneralConsts

class KanjaxRepository(context: Context) {

    private var mDataBase = DataBase.getDataBase(context).getKanjaxDao()

    fun get(id: Long): Kanjax? {
        return try {
            mDataBase.get(id)
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, e.message.toString())
            null
        }
    }

    fun get(kanji: String): Kanjax? {
        return try {
            mDataBase.get(kanji)
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, e.message.toString())
            null
        }
    }

    fun list(): List<Kanjax>? {
        return try {
            mDataBase.list()
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, e.message.toString())
            null
        }
    }

}
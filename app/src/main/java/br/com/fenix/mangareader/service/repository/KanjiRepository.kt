package br.com.fenix.mangareader.service.repository

import android.content.Context
import android.util.Log
import br.com.fenix.mangareader.model.entity.KanjiJLPT
import br.com.fenix.mangareader.util.constants.GeneralConsts

class KanjiRepository(context: Context) {

    private var mDataBase = DataBase.getDataBase(context).getKanjiJLPTDao()

    fun get(id: Long): KanjiJLPT? {
        return try {
            mDataBase.get(id)
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, e.message.toString())
            null
        }
    }

    fun list(): List<KanjiJLPT>? {
        return try {
            mDataBase.list()
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, e.message.toString())
            null
        }
    }


    fun getHashMap(): Map<String, Int>? {
        return try {
            mDataBase.list().map { it.kanji to it.level }.toMap()
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, e.message.toString())
            null
        }
    }

}
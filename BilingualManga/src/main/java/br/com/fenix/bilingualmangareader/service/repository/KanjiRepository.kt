package br.com.fenix.bilingualmangareader.service.repository

import android.content.Context
import br.com.fenix.bilingualmangareader.model.entity.KanjiJLPT
import org.slf4j.LoggerFactory

class KanjiRepository(context: Context) {

    private val mLOGGER = LoggerFactory.getLogger(KanjiRepository::class.java)
    private var mDataBase = DataBase.getDataBase(context).getKanjiJLPTDao()

    fun get(id: Long): KanjiJLPT? {
        return try {
            mDataBase.get(id)
        } catch (e: Exception) {
            mLOGGER.error("Error when get KanjiJLPT: " + e.message, e)
            null
        }
    }

    fun list(): List<KanjiJLPT>? {
        return try {
            mDataBase.list()
        } catch (e: Exception) {
            mLOGGER.error("Error when list KanjiJLPT: " + e.message, e)
            null
        }
    }


    fun getHashMap(): Map<String, Int>? {
        return try {
            mDataBase.list().associate { it.kanji to it.level }
        } catch (e: Exception) {
            mLOGGER.error("Error when get HashMap: " + e.message, e)
            null
        }
    }

}
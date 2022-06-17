package br.com.fenix.bilingualmangareader.service.repository

import android.content.Context
import br.com.fenix.bilingualmangareader.model.entity.Kanjax
import mu.KotlinLogging

class KanjaxRepository(context: Context) {

    private val mLOGGER = KotlinLogging.logger {}
    private var mDataBase = DataBase.getDataBase(context).getKanjaxDao()

    fun get(id: Long): Kanjax? {
        return try {
            mDataBase.get(id)
        } catch (e: Exception) {
            mLOGGER.error { "Error when get Kanjax: " + e.message }
            null
        }
    }

    fun get(kanji: String): Kanjax? {
        return try {
            mDataBase.get(kanji)
        } catch (e: Exception) {
            mLOGGER.error { "Error when get Kanjax: " + e.message }
            null
        }
    }

    fun list(): List<Kanjax>? {
        return try {
            mDataBase.list()
        } catch (e: Exception) {
            mLOGGER.error { "Error when list Kanjax: " + e.message }
            null
        }
    }

}
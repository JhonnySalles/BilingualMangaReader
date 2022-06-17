package br.com.fenix.bilingualmangareader.service.repository

import android.content.Context
import br.com.fenix.bilingualmangareader.model.entity.Cover
import mu.KotlinLogging

class CoverRepository(context: Context) {

    private val mLOGGER = KotlinLogging.logger {}
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
            mLOGGER.error { "Error when get cover: " + e.message }
            null
        }
    }

    fun findFirstByIdManga(idManga: Long): Cover? {
        return try {
            mDataBase.findFirstByIdManga(idManga)
        } catch (e: Exception) {
            mLOGGER.error { "Error when find cover by manga: " + e.message }
            null
        }
    }

    fun listByIdManga(idManga: Long): List<Cover>? {
        return try {
            mDataBase.listByIdManga(idManga)
        } catch (e: Exception) {
            mLOGGER.error { "Error when list cover by manga: " + e.message }
            null
        }
    }

}
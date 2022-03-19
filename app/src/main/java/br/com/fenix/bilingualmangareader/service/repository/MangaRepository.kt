package br.com.fenix.bilingualmangareader.service.repository

import android.content.Context
import android.util.Log
import br.com.fenix.bilingualmangareader.model.entity.Cover
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts

class MangaRepository(context: Context) {

    private val mCoverRepository: CoverRepository = CoverRepository(context)
    private var mDataBase = DataBase.getDataBase(context).getMangaDao()

    fun save(obj: Manga): Long {
        val id = mDataBase.save(obj)

        if (obj.thumbnail != null)
            mCoverRepository.save(obj.thumbnail!!)

        return id
    }

    fun update(obj: Manga) {
        mDataBase.update(obj)

        if (obj.thumbnail != null && obj.thumbnail!!.update)
            mCoverRepository.save(obj.thumbnail!!)
    }

    fun updateBookMark(obj: Manga) {
        if (obj.id != null)
            mDataBase.updateBookMark(obj.id!!, obj.bookMark)
    }

    fun updateLastAcess(obj: Manga) {
        if (obj.id != null)
            mDataBase.update(obj)
    }

    fun delete(obj: Manga) {
        mCoverRepository.deleteAll(obj.id!!)
        mDataBase.delete(obj)
    }

    fun list(): List<Manga>? {
        return try {
            mDataBase.list()
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.DATABASE.LIST, e.message.toString())
            null
        }
    }

    fun listHistory(): List<Manga>? {
        return try {
            mDataBase.listHistory()
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.DATABASE.LIST, e.message.toString())
            null
        }
    }

    fun clearHistory(obj : Manga?) {
        try {
            if (obj != null)
                if (obj.id != null)
                    mDataBase.clearHistory(obj.id!!)
            else
                mDataBase.clearHistory()
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.DATABASE.LIST, e.message.toString())
        }
    }

    fun getThumbnail(idManga: Long): Cover? {
        return mCoverRepository.findFirstByIdManga(idManga)
    }

    fun get(id: Long): Manga? {
        return try {
            mDataBase.get(id)
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.DATABASE.SELECT, e.message.toString())
            null
        }
    }

    fun findByFileName(name: String): Manga? {
        return try {
            mDataBase.get(name)
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.DATABASE.SELECT, e.message.toString())
            null
        }
    }

    fun findByFileFolder(folder: String): List<Manga>? {
        return try {
            mDataBase.listByFolder(folder)
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.DATABASE.LIST, e.message.toString())
            null
        }
    }
}
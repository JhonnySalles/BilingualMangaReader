package br.com.fenix.bilingualmangareader.service.repository

import android.content.Context
import br.com.fenix.bilingualmangareader.model.entity.Cover
import br.com.fenix.bilingualmangareader.model.entity.Manga
import org.slf4j.LoggerFactory

class MangaRepository(context: Context) {

    private val mLOGGER = LoggerFactory.getLogger(MangaRepository::class.java)
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
        if (obj.id != null)
            mDataBase.delete(obj.id!!)
    }

    fun list(): List<Manga>? {
        return try {
            mDataBase.list()
        } catch (e: Exception) {
            mLOGGER.error("Error when list Manga: " + e.message, e)
            null
        }
    }

    fun listDeleted(): List<Manga>? {
        return try {
            mDataBase.listDeleted()
        } catch (e: Exception) {
            mLOGGER.error("Error when list Manga: " + e.message, e)
            null
        }
    }

    fun listHistory(): List<Manga>? {
        return try {
            mDataBase.listHistory()
        } catch (e: Exception) {
            mLOGGER.error("Error when list Manga History: " + e.message, e)
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
            mLOGGER.error("Error when clear Manga History: " + e.message, e)
        }
    }

    fun getThumbnail(idManga: Long): Cover? {
        return mCoverRepository.findFirstByIdManga(idManga)
    }

    fun get(id: Long): Manga? {
        return try {
            mDataBase.get(id)
        } catch (e: Exception) {
            mLOGGER.error("Error when get Manga: " + e.message, e)
            null
        }
    }

    fun findByFileName(name: String): Manga? {
        return try {
            mDataBase.get(name)
        } catch (e: Exception) {
            mLOGGER.error("Error when find Manga by file name: " + e.message, e)
            null
        }
    }

    fun findByFileFolder(folder: String): List<Manga>? {
        return try {
            mDataBase.listByFolder(folder)
        } catch (e: Exception) {
            mLOGGER.error("Error when find Manga by file folder: " + e.message, e)
            null
        }
    }
}
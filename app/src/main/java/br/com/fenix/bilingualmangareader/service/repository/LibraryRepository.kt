package br.com.fenix.bilingualmangareader.service.repository

import android.content.Context
import br.com.fenix.bilingualmangareader.model.entity.Library
import br.com.fenix.bilingualmangareader.model.enums.Libraries
import br.com.fenix.bilingualmangareader.model.enums.LibraryType
import org.slf4j.LoggerFactory

class LibraryRepository(context: Context) {

    private val mLOGGER = LoggerFactory.getLogger(LibraryRepository::class.java)
    private var mDataBase = DataBase.getDataBase(context).getLibrariesDao()

    fun save(obj: Library): Long {
        return mDataBase.save(obj)
    }

    fun update(obj: Library) {
        mDataBase.update(obj)
    }

    fun delete(obj: Library) {
        if (obj.id != null)
            mDataBase.delete(obj.id!!)
    }

    fun list(): List<Library> {
        return try {
            mDataBase.list()
        } catch (e: Exception) {
            mLOGGER.error("Error when list Library: " + e.message, e)
            listOf()
        }
    }

    fun listEnabled(): List<Library> {
        return try {
            mDataBase.listEnabled()
        } catch (e: Exception) {
            mLOGGER.error("Error when list Library: " + e.message, e)
            listOf()
        }
    }

    fun get(id: Long): Library? {
        return try {
            mDataBase.get(id)
        } catch (e: Exception) {
            mLOGGER.error("Error when get Library: " + e.message, e)
            null
        }
    }

    fun get(type: Libraries): Library? {
        return try {
            mDataBase.get(type)
        } catch (e: Exception) {
            mLOGGER.error("Error when get Library: " + e.message, e)
            null
        }
    }

    fun findDeleted(path: String): Library? {
        return try {
            mDataBase.findDeleted(path)
        } catch (e: Exception) {
            mLOGGER.error("Error when find Library: " + e.message, e)
            null
        }
    }

    fun removeDefault(path: String) {
        try {
            mDataBase.removeDefault(path)
        } catch (e: Exception) {
            mLOGGER.error("Error when delete Library: " + e.message, e)
        }
    }

}
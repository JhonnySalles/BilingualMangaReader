package br.com.fenix.mangareader.service

import android.app.Application
import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import br.com.fenix.mangareader.common.Util
import br.com.fenix.mangareader.constants.DataBaseConsts
import br.com.fenix.mangareader.constants.GeneralConsts
import br.com.fenix.mangareader.model.Cover

class CoverService(application: Application) : BaseService<Cover>(application) {

    private fun getContents(idBook: Long?, cover: Cover): ContentValues {
        val contentsValues = ContentValues()
        if (idBook != null && idBook > 0) contentsValues.put(DataBaseConsts.COVER.COLUMNS.FK_ID_BOOK, idBook)
        contentsValues.put(DataBaseConsts.COVER.COLUMNS.NAME, cover.name)
        contentsValues.put(DataBaseConsts.COVER.COLUMNS.SIZE, cover.size)
        contentsValues.put(DataBaseConsts.COVER.COLUMNS.TYPE, cover.type)
        contentsValues.put(
            DataBaseConsts.COVER.COLUMNS.IMAGE,
            Util.encodeImageBase64(cover.image!!)
        )
        return contentsValues
    }

    private fun getProjection(): Array<String> {
        return arrayOf(
            DataBaseConsts.COVER.COLUMNS.ID,
            DataBaseConsts.COVER.COLUMNS.FK_ID_BOOK,
            DataBaseConsts.COVER.COLUMNS.NAME,
            DataBaseConsts.COVER.COLUMNS.SIZE,
            DataBaseConsts.COVER.COLUMNS.TYPE,
            DataBaseConsts.COVER.COLUMNS.IMAGE
        )
    }

    private fun parseToCover(cursor: Cursor): Cover {
        return Cover(
            cursor.getLong(cursor.getColumnIndexOrThrow(DataBaseConsts.COVER.COLUMNS.ID)),
            cursor.getString(cursor.getColumnIndexOrThrow(DataBaseConsts.COVER.COLUMNS.NAME)),
            cursor.getInt(cursor.getColumnIndexOrThrow(DataBaseConsts.COVER.COLUMNS.SIZE)),
            cursor.getString(cursor.getColumnIndexOrThrow(DataBaseConsts.COVER.COLUMNS.TYPE)),
            Util.decodeImageBase64(cursor.getString(cursor.getColumnIndexOrThrow(DataBaseConsts.COVER.COLUMNS.IMAGE)))
        )
    }

    fun saveOrUpdate(idBook: Long, cover: Cover) {
        if (cover.id <= 0)
            save(idBook, cover)
        else
            update(idBook, cover)
    }

    @Deprecated(level = DeprecationLevel.ERROR,
        message = "you must use save with idBook for a insert",
        replaceWith = ReplaceWith(
            expression = "CoverService.save(idBook, cover)",
            imports = ["br.com.fenix.mangareader.service.CoverService"]
        )
    )
    override fun save(obj: Cover) {
        throw IllegalArgumentException("Necessary to inform id book for a cover")
    }

    fun save(idBook: Long, obj: Cover) {
        val contents: ContentValues = getContents(idBook, obj)
        mDBRepository.save(DataBaseConsts.COVER.TABLE_NAME, contents)
    }

    override fun update(obj: Cover) {
        val contents: ContentValues = getContents(null, obj)
        val selection = DataBaseConsts.COVER.COLUMNS.ID + " = ?"
        val args = arrayOf(obj.id.toString())
        mDBRepository.update(DataBaseConsts.COVER.TABLE_NAME, contents, selection, args)
    }

    fun update(idBook: Long, obj: Cover) {
        val contents: ContentValues = getContents(idBook, obj)
        val selection = DataBaseConsts.COVER.COLUMNS.ID + " = ?"
        val args = arrayOf(obj.id.toString())
        mDBRepository.update(DataBaseConsts.COVER.TABLE_NAME, contents, selection, args)
    }

    override fun delete(obj: Cover) {
        val selection = DataBaseConsts.COVER.COLUMNS.ID + " = ?"
        val args = arrayOf(obj.id.toString())
        mDBRepository.delete(DataBaseConsts.COVER.TABLE_NAME, selection, args)
    }

    fun deleteByIdBook(id: Long) {
        val selection = DataBaseConsts.COVER.COLUMNS.FK_ID_BOOK + " = ?"
        val args = arrayOf(id.toString())
        mDBRepository.delete(DataBaseConsts.COVER.TABLE_NAME, selection, args)
    }

    override fun list(): List<Cover>? {
        val cursor: Cursor? =
            mDBRepository.query(DataBaseConsts.COVER.TABLE_NAME, getProjection(), null, null)
        val covers: MutableList<Cover> = ArrayList()
        return try {
            if (cursor != null && cursor.count > 0) {
                if (cursor.moveToNext())
                    covers.add(parseToCover(cursor))
            }
            covers
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, e.message.toString())
            covers
        } finally {
            cursor?.close()
        }
    }

    override fun get(id: Long): Cover? {
        val selection = DataBaseConsts.COVER.COLUMNS.ID + " = ?"
        val args = arrayOf(id.toString())
        val cursor: Cursor? = mDBRepository.query(
            DataBaseConsts.COVER.TABLE_NAME, getProjection(),
            selection, args
        )
        return try {
            if (cursor != null && cursor.moveToFirst()) {
                parseToCover(cursor)
            } else
                null
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, e.message.toString())
            null
        } finally {
            cursor?.close()
        }
    }

    fun findFirstByIdBook(id: Long): Cover? {
        val selection = DataBaseConsts.COVER.COLUMNS.FK_ID_BOOK + " = ?"
        val args = arrayOf(id.toString())
        val cursor: Cursor? = mDBRepository.query(
            DataBaseConsts.COVER.TABLE_NAME, getProjection(),
            selection, args
        )
        return try {
            if (cursor != null && cursor.moveToFirst()) {
                parseToCover(cursor)
            } else
                null
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, e.message.toString())
            null
        } finally {
            cursor?.close()
        }
    }

    fun findByIdBook(id: Long): List<Cover>? {
        val selection = DataBaseConsts.COVER.COLUMNS.FK_ID_BOOK + " = ?"
        val args = arrayOf(id.toString())
        val covers: MutableList<Cover> = ArrayList()
        val cursor: Cursor? = mDBRepository.query(
            DataBaseConsts.COVER.TABLE_NAME, getProjection(),
            selection, args
        )
        return try {
            if (cursor != null && cursor.count > 0) {
                if (cursor.moveToNext())
                    covers.add(parseToCover(cursor))
            }
            covers
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, e.message.toString())
            covers
        } finally {
            cursor?.close()
        }
    }

}
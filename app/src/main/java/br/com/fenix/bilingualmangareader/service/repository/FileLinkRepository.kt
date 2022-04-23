package br.com.fenix.bilingualmangareader.service.repository

import android.content.Context
import br.com.fenix.bilingualmangareader.model.entity.FileLink
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.model.entity.PageLink

class FileLinkRepository(context: Context) {

    private var mDataBase = DataBase.getDataBase(context).getFileLinkDao()
    private var mDataBasePage = DataBase.getDataBase(context).getPageLinkDao()

    fun save(obj: FileLink): Long {
        val id = mDataBase.save(obj)
        if (obj.pagesLink != null) {
            mDataBasePage.deleteAll(id)
            for (page in obj.pagesLink!!)
                mDataBasePage.save(page)
        }

        return id
    }

    fun update(obj: FileLink) {
        mDataBase.save(obj)
        if (obj.pagesLink != null) {
            mDataBasePage.deleteAll(obj.id!!)
            for (page in obj.pagesLink!!)
                mDataBasePage.save(page)
        }
    }

    fun delete(obj: FileLink) {
        if (obj.id != null) {
            mDataBasePage.deleteAll(obj.id!!)
            mDataBase.delete(obj)
        }
    }

    fun delete(obj: Manga) {
        if (obj.id != null) {
            mDataBasePage.deleteAllByManga(obj.id!!)
            mDataBase.deleteAllByManga(obj.id!!)
        }
    }

    fun findByFileName(idManga: Long, name: String, pages: Int): FileLink? {
        val fileLink = mDataBase.get(idManga, name, pages)
        if (fileLink != null)
            fileLink.pagesLink = findPagesLink(fileLink.id!!)

        return fileLink
    }

    private fun findPagesLink(idFileLink: Long): List<PageLink>? {
        return mDataBasePage.get(idFileLink)
    }

}
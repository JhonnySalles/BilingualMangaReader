package br.com.fenix.bilingualmangareader.service.repository

import android.content.Context
import br.com.fenix.bilingualmangareader.model.entity.FileLink
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.model.entity.PageLink

class FileLinkRepository(context: Context) {

    private var mDataBase = DataBase.getDataBase(context).getFileLinkDao()
    private var mDataBasePage = DataBase.getDataBase(context).getPageLinkDao()

    fun save(obj: FileLink): Long {
        delete(obj.manga!!)
        val id = mDataBase.save(obj)
        if (obj.pagesLink != null) {
            mDataBasePage.deleteAll(id)
            save(id, obj.pagesLink!!)
            save(id, obj.pagesNotLink!!)
        }

        return id
    }

    private fun save(idFile : Long, pages : List<PageLink>) {
        for (page in pages) {
            page.idFile = idFile
            page.id = mDataBasePage.save(page)
        }
    }

    fun update(obj: FileLink) {
        mDataBase.update(obj)
        if (obj.pagesLink != null) {
            mDataBasePage.deleteAll(obj.id!!)
            save(obj.id!!, obj.pagesLink!!)
            save(obj.id!!, obj.pagesNotLink!!)
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

    fun get(obj: Manga) : FileLink? {
        val fileLink = if (obj.id != null && obj.id != 0L) mDataBase.get(obj.id!!) else null
        if (fileLink != null) {
            fileLink.pagesLink = findPagesLink(fileLink.id!!)
            fileLink.pagesNotLink = findPagesNotLink(fileLink.id!!)
        }
        return fileLink
    }

    fun findByFileName(idManga: Long, name: String, pages: Int): FileLink? {
        val fileLink = mDataBase.get(idManga, name, pages)
        if (fileLink != null) {
            fileLink.pagesLink = findPagesLink(fileLink.id!!)
            fileLink.pagesNotLink = findPagesNotLink(fileLink.id!!)
        }
        return fileLink
    }

    private fun findPagesLink(idFileLink: Long): List<PageLink> {
        return mDataBasePage.getPageLink(idFileLink)
    }

    private fun findPagesNotLink(idFileLink: Long): List<PageLink> {
        return mDataBasePage.getPageNotLink(idFileLink)
    }

}
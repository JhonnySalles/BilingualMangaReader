package br.com.fenix.bilingualmangareader.view.ui.pages_link

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Message
import android.os.Process
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.com.fenix.bilingualmangareader.model.entity.FileLink
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.model.entity.PageLink
import br.com.fenix.bilingualmangareader.model.enums.Languages
import br.com.fenix.bilingualmangareader.model.enums.LoadFile
import br.com.fenix.bilingualmangareader.model.enums.Pages
import br.com.fenix.bilingualmangareader.service.controller.SubTitleController
import br.com.fenix.bilingualmangareader.service.parses.Parse
import br.com.fenix.bilingualmangareader.service.parses.ParseFactory
import br.com.fenix.bilingualmangareader.service.parses.RarParse
import br.com.fenix.bilingualmangareader.service.repository.FileLinkRepository
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.util.constants.PageLinkConsts
import br.com.fenix.bilingualmangareader.util.constants.ReaderConsts
import br.com.fenix.bilingualmangareader.util.helpers.Util
import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStream
import java.io.InterruptedIOException
import java.time.LocalDateTime


class PagesLinkViewModel(application: Application) : AndroidViewModel(application) {

    private val mLOGGER = LoggerFactory.getLogger(PagesLinkViewModel::class.java)
    private val mContext = application.applicationContext
    private val mFileLinkRepository: FileLinkRepository = FileLinkRepository(application.applicationContext)

    private var mManga : Manga? = null
    private var mFileLink = MutableLiveData<FileLink>()
    val fileLink: LiveData<FileLink> = mFileLink
    private var mPagesLink = MutableLiveData<ArrayList<PageLink>>(ArrayList())
    val pagesLink: LiveData<ArrayList<PageLink>> = mPagesLink
    private var mPagesNotLinked = MutableLiveData<ArrayList<PageLink>>(ArrayList())
    val pagesLinkNotLinked: LiveData<ArrayList<PageLink>> = mPagesNotLinked
    private var mLanguage = MutableLiveData(Languages.PORTUGUESE)
    val language: LiveData<Languages> = mLanguage

    private var mGenerateImageHandler: MutableList<Handler>? = java.util.ArrayList()
    private var mGenerateImageThread: ArrayList<ImageLoadThread> = ArrayList()

    private fun getParse(path: String): Parse? = getParse(File(path))

    private fun getParse(file: File): Parse? {
        val parse = ParseFactory.create(file)
        if (parse is RarParse) {
            val folder = GeneralConsts.CACHEFOLDER.LINKED + '/' + Util.normalizeNameCache(file.nameWithoutExtension)
            val cacheDir = File(mContext.externalCacheDir, folder)
            (parse as RarParse?)!!.setCacheDirectory(cacheDir)
        }
        return parse
    }

    fun getFileLink(manga : Manga? = null, isBackup: Boolean = false) : FileLink? {
        return if (!isBackup && (mFileLink.value == null ||  mFileLink.value!!.path == ""))
            if (manga != null)
                mFileLinkRepository.get(manga)
            else
                null
        else this.get()
    }

    fun onDestroy() {
        if (mFileLink.value?.parseManga == null) {
            Util.destroyParse(mFileLink.value?.parseManga)
            mFileLink.value?.parseManga = null
        }

        if (mFileLink.value?.parseFileLink == null) {
            Util.destroyParse(mFileLink.value?.parseManga)
            mFileLink.value?.parseFileLink = null
        }
    }

    private fun verify(fileLink: FileLink?) {
        if (fileLink == null) return

        if (fileLink.parseManga == null)
            fileLink.parseManga = getParse(fileLink.manga!!.path)

        if (fileLink.parseFileLink == null && fileLink.path.isNotEmpty())
            fileLink.parseFileLink = getParse(fileLink.path)
    }

    private fun reload(refresh: (index: Int?, type: Pages) -> (Unit)) : Boolean {
        val fileLink = SubTitleController.getInstance(mContext).getFileLink()?: return false
        return if (mManga == fileLink.manga) {
            endThread(true)
            verify(fileLink)
            mFileLink.value = fileLink
            mPagesLink.value = fileLink.pagesLink?.let { ArrayList(it) }
            mPagesNotLinked.value = fileLink.pagesNotLink?.let { ArrayList(it) }
            setLanguage(fileLink.language)
            refresh(null, Pages.ALL)

            getImage(mFileLink.value!!.parseManga, mFileLink.value!!.parseFileLink, mPagesLink.value!!, Pages.ALL, true)

            if (mPagesNotLinked.value!!.isNotEmpty())
                getImage(null, mFileLink.value!!.parseFileLink, mPagesNotLinked.value!!, Pages.NOT_LINKED, true)

            true
        } else false
    }

    fun reload(fileLink: FileLink?, refresh: (index: Int?, type: Pages) -> (Unit)) : Boolean {
        return if (fileLink != null) {
            endThread(true)
            verify(fileLink)
            mFileLink.value = fileLink
            mPagesLink.value = fileLink.pagesLink?.let { ArrayList(it) }
            mPagesNotLinked.value = fileLink.pagesNotLink?.let { ArrayList(it) }
            refresh(null, Pages.ALL)

            getImage(mFileLink.value!!.parseManga, mFileLink.value!!.parseFileLink, mPagesLink.value!!, Pages.ALL, true)

            if (mPagesNotLinked.value!!.isNotEmpty())
                getImage(null, mFileLink.value!!.parseFileLink, mPagesNotLinked.value!!, Pages.NOT_LINKED, true)

            true
        } else false
    }

    private fun find(isLoadManga: Boolean, refresh: (index: Int?, type: Pages) -> (Unit)) : Boolean {
        if (mManga == null) return false
        val obj = mFileLinkRepository.get(mManga!!) ?: return false
        set(obj, refresh, isLoadManga)
        return (obj.pagesLink != null) && (obj.pagesLink!!.isNotEmpty())
    }

    fun find(name: String, pages: Int, refresh: (index: Int?, type: Pages) -> (Unit)) : Boolean {
        if (mManga == null || mManga!!.id == null) return false
        val obj = mFileLinkRepository.findByFileName(mManga!!.id!!, name, pages) ?: return false
        set(obj, refresh)
        return true
    }

    fun set(obj: FileLink, refresh: (index: Int?, type: Pages) -> (Unit), isLoadManga: Boolean = false) {
        endThread(true)

        Util.destroyParse(mFileLink.value?.parseManga)
        Util.destroyParse(mFileLink.value?.parseFileLink)

        if (mPagesLink.value != null && mPagesLink.value!!.isNotEmpty())
            obj.pagesLink?.forEachIndexed { index, pageLink -> pageLink.imageMangaPage = mPagesLink.value!![index].imageMangaPage   }

        mFileLink.value = obj
        mPagesLink.value?.forEach { it.clearFileLink()  }
        mPagesNotLinked.value?.clear()
        setLanguage(obj.language)

        val mParseManga = getParse(mManga!!.file) ?: return
        mFileLink.value?.parseManga = mParseManga
        val mParseLink = getParse(obj.file) ?: return
        mFileLink.value?.parseFileLink = mParseLink

        verify(obj)
        if (isLoadManga)
            mPagesLink.value = obj.pagesLink?.let { ArrayList(it) }
        else
            obj.pagesLink?.forEachIndexed { index, pageLink -> mPagesLink.value!![index].merge(pageLink) }
        mPagesNotLinked.value = obj.pagesNotLink?.let { ArrayList(it) }
        refresh(null, Pages.ALL)

        val type = if (isLoadManga) Pages.ALL else Pages.LINKED

        getImage(mParseManga, mParseLink, mPagesLink.value!!, type)
        if (mPagesNotLinked.value!!.isNotEmpty()) {
            refresh(null, Pages.NOT_LINKED)
            getImage(mParseManga, mParseLink, mPagesNotLinked.value!!, Pages.NOT_LINKED)
        }
    }

    fun save(obj: FileLink): FileLink {
        obj.lastAccess = LocalDateTime.now()
        if (obj.id == 0L)
            obj.id = mFileLinkRepository.save(obj)
        else
            mFileLinkRepository.update(obj)

        return obj
    }

    fun save() {
        val obj = this.get()
        obj.lastAccess = LocalDateTime.now()
        if (obj.id == null || obj.id == 0L)
            mFileLink.value!!.id = mFileLinkRepository.save(obj)
        else
            mFileLinkRepository.update(obj)
    }

    fun get(): FileLink {
        val obj = mFileLink.value!!
        obj.manga = mManga
        obj.idManga = mManga!!.id!!
        obj.pagesLink = mPagesLink.value
        obj.pagesNotLink = mPagesNotLinked.value
        return obj
    }

    fun delete(refresh: (index: Int?, type: Pages) -> (Unit)) {
        if (mFileLink.value != null)
            mFileLinkRepository.delete(mFileLink.value!!)

        clearFileLink(refresh)
    }

    fun delete(obj: FileLink) {
        mFileLinkRepository.delete(obj)
    }

    fun restoreBackup(refresh: (index: Int?, type: Pages) -> (Unit)) {
        if (mFileLink.value != null) {
            val fileLink = mFileLinkRepository.get(mManga!!)
            if (fileLink != null) {
                endThread()
                fileLink.parseFileLink = mFileLink.value!!.parseFileLink
                fileLink.parseManga = mFileLink.value!!.parseManga

                for ((index, page) in fileLink.pagesLink!!.withIndex()) {
                    page.imageMangaPage = mPagesLink.value!![index].imageMangaPage

                    if (page.fileLinkPage != PageLinkConsts.VALUES.PAGE_EMPTY) {
                        val item = mPagesLink.value?.find { it.fileLinkPage.compareTo(page.fileLinkPage) == 0 || it.fileRightLinkPage.compareTo(page.fileLinkPage) == 0 } ?:
                            mPagesNotLinked.value?.find { it.fileLinkPage.compareTo(page.fileLinkPage) == 0 }

                        if (item != null) {
                            if (item.fileLinkPage.compareTo(page.fileLinkPage) == 0)
                                page.imageLeftFileLinkPage = item.imageLeftFileLinkPage
                            else if (item.fileRightLinkPage.compareTo(page.fileLinkPage) == 0)
                                page.imageLeftFileLinkPage = item.imageRightFileLinkPage
                        }
                    }

                    if (page.fileRightLinkPage != PageLinkConsts.VALUES.PAGE_EMPTY) {
                        val item = mPagesLink.value?.find { it.fileLinkPage.compareTo(page.fileRightLinkPage) == 0 || it.fileRightLinkPage.compareTo(page.fileRightLinkPage) == 0 } ?:
                        mPagesNotLinked.value?.find { it.fileLinkPage.compareTo(page.fileRightLinkPage) == 0 }

                        if (item != null) {
                            if (item.fileLinkPage.compareTo(page.fileRightLinkPage) == 0)
                                page.imageRightFileLinkPage = item.imageLeftFileLinkPage
                            else if (item.fileRightLinkPage.compareTo(page.fileRightLinkPage) == 0)
                                page.imageRightFileLinkPage = item.imageRightFileLinkPage
                        }
                    }
                }

                for (page in fileLink.pagesNotLink!!) {
                    val item = mPagesNotLinked.value?.find { it.fileLinkPage.compareTo(page.fileLinkPage) == 0 } ?:
                    mPagesLink.value?.find { it.fileLinkPage.compareTo(page.fileLinkPage) == 0 || it.fileRightLinkPage.compareTo(page.fileLinkPage) == 0 }

                    if (item != null) {
                        if (item.fileLinkPage.compareTo(page.fileLinkPage) == 0)
                            page.imageLeftFileLinkPage = item.imageLeftFileLinkPage
                        else if (item.fileRightLinkPage.compareTo(page.fileLinkPage) == 0)
                            page.imageLeftFileLinkPage = item.imageRightFileLinkPage
                    }
                }

                verify(fileLink)
                mFileLink.value = fileLink
                mPagesLink.value = fileLink.pagesLink?.let { ArrayList(it) }
                mPagesNotLinked.value = fileLink.pagesNotLink?.let { ArrayList(it) }

                getImage(fileLink.parseManga, fileLink.parseFileLink, mPagesLink.value!!, Pages.ALL, true)

                if (mPagesNotLinked.value!!.isNotEmpty())
                    getImage(null, fileLink.parseFileLink, mPagesNotLinked.value!!, Pages.NOT_LINKED, true)

            } else if (mFileLink.value!!.path.isNotEmpty())
                readFileLink(mFileLink.value!!.path, true, refresh)
        }
    }

    fun setLanguage(language: Languages? = null, isClear : Boolean = false) {
        mLanguage.value = if (isClear || language == null) Languages.PORTUGUESE else language
    }

    fun loadManga(manga : Manga, refresh: (index: Int?, type: Pages) -> (Unit)) {
        mManga = manga
        mPagesLink.value?.clear()
        setLanguage(isClear = true)

        if (reload(refresh)) return
        if (find(true, refresh)) return

        Util.destroyParse(mFileLink.value?.parseManga)

        val parse = getParse(manga.file) ?: return
        mFileLink.value = FileLink(manga)
        mFileLink.value!!.parseManga = parse

        val list = ArrayList<PageLink>()
        for (i in 0 until parse.numPages()) {
            var name = parse.getPagePath(i) ?: ""
            if (Util.isImage(name)) {
                name = Util.getNameFromPath(name)
                list.add(PageLink(mFileLink.value!!.id, i, manga.pages, name))
            }
        }
        mPagesLink.value = list
        refresh(null, Pages.MANGA)
        getImage(parse, null, mPagesLink.value!!, Pages.MANGA)
    }

    fun readFileLink(path : String, isReload: Boolean = false, refresh: (index: Int?, type: Pages) -> (Unit)) : LoadFile {
        var loaded = LoadFile.ERROR_NOT_LOAD

        val file = File(path)
        if (file.name.endsWith(".rar") ||
            file.name.endsWith(".zip") ||
            file.name.endsWith(".cbr") ||
            file.name.endsWith(".cbz")) {

            Util.destroyParse(mFileLink.value?.parseFileLink)
            val parse = getParse(path)
            if (parse != null) {
                loaded = LoadFile.LOADED

                if (!isReload && find(file.name, parse.numPages(), refresh)) return loaded

                endThread(true)
                mFileLink.value = FileLink(mManga!!, mFileLink.value!!.parseManga, parse.numPages(), path,
                    file.name, file.extension, file.parent)
                mFileLink.value!!.parseFileLink = parse
                mPagesLink.value?.forEach { it.clearFileLink()  }
                val listNotLink = ArrayList<PageLink>()
                for (i in 0 until parse.numPages()) {
                    var name = parse.getPagePath(i)?: ""
                    if (Util.isImage(name)) {
                        name = Util.getNameFromPath(name)

                        if (i < mPagesLink.value!!.size) {
                            val page = mPagesLink.value!![i]
                            page.fileLinkPage = i
                            page.fileLinkPageName = name
                            page.fileLinkPages = parse.numPages()
                            refresh(i, Pages.LINKED)
                        } else
                            listNotLink.add(PageLink(mFileLink.value!!.id, PageLinkConsts.VALUES.PAGE_EMPTY, 0, "",
                                i, parse.numPages(), name, true))
                    }
                }

                mPagesNotLinked.value = listNotLink
                refresh(null, Pages.LINKED)
                getImage(null, parse, mPagesLink.value!!, Pages.LINKED)

                if (mPagesNotLinked.value!!.isNotEmpty()) {
                    refresh(null, Pages.NOT_LINKED)
                    getImage(null, parse, mPagesNotLinked.value!!, Pages.NOT_LINKED)
                }

            }
        } else
            loaded = LoadFile.ERROR_FILE_WRONG

        if (loaded != LoadFile.LOADED)
            clearFileLink(refresh)

        return loaded
    }

    fun clearFileLink(refresh: (index: Int?, type: Pages) -> (Unit)) {
        endThread(true)
        mFileLink.value?.clear()
        setLanguage(isClear = true)
        mPagesNotLinked.value?.clear()
        mPagesLink.value?.forEach { page ->  page.clearFileLink() }
        refresh(null, Pages.ALL)
    }

    fun getPageLink(page : PageLink) : String = mPagesLink.value!!.indexOf(page).toString()

    fun getPageLink(index : Int) : PageLink? {
        return if (index >= mPagesLink.value!!.size || index == -1)
            null
        else
            mPagesLink.value!![index]
    }

    fun getPageNotLink(page : PageLink) : String = mPagesNotLinked.value!!.indexOf(page).toString()

    fun getPageNotLink(index : Int) : PageLink? {
        return if (index >= mPagesNotLinked.value!!.size || index == -1)
            null
        else
            mPagesNotLinked.value!![index]
    }

    private fun getPageNotLinkLastIndex(): Int {
        return if (mPagesNotLinked.value!!.isEmpty())
            0
        else
            mPagesNotLinked.value!!.size - 1
    }

    fun getPageNotLinkIndex(page : PageLink?) : Int? {
        return if (page == null || mPagesNotLinked.value!!.isEmpty()) null
        else {
            val index = mPagesNotLinked.value!!.indexOf(page)
            if (index < 0)
                null
            else
                index
        }
    }

    private fun addNotLinked(page: PageLink) {
        if (page.fileLinkPage != PageLinkConsts.VALUES.PAGE_EMPTY) {
            mPagesNotLinked.value!!.add(
                PageLink(
                    page.idFile, PageLinkConsts.VALUES.PAGE_EMPTY, 0, "",
                    page.fileLinkPage, page.fileLinkPages, page.fileLinkPageName,
                    isFileLeftDualPage = page.isFileLeftDualPage, imageFileLinkPage = page.imageLeftFileLinkPage
                )
            )
            notifyMessages(Pages.NOT_LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_ADDED, getPageNotLinkLastIndex())
        }

        if (page.dualImage) {
            mPagesNotLinked.value!!.add(
                PageLink(
                    page.idFile, PageLinkConsts.VALUES.PAGE_EMPTY, 0, "",
                    page.fileRightLinkPage, page.fileLinkPages, page.fileRightLinkPageName,
                    isFileLeftDualPage = page.isFileRightDualPage, imageFileLinkPage = page.imageRightFileLinkPage
                )
            )
            page.clearRightFileLink()
            notifyMessages(Pages.NOT_LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_ADDED, getPageNotLinkLastIndex())
        }
    }

    fun onMoveDualPage(originType: Pages, origin: PageLink?, destinyType: Pages, destiny: PageLink?) {
        if (origin == null || destiny == null) {
            notifyMessages(Pages.ALL, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_UPDATED)
            return
        }

        if (origin == destiny && destinyType == Pages.DUAL_PAGE) {
            notifyMessages(Pages.LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_UPDATED, mPagesLink.value!!.indexOf(destiny))
            return
        }

        if (destinyType != Pages.LINKED && destiny.dualImage) {
            mPagesNotLinked.value!!.add(
                PageLink(
                    destiny.idFile, PageLinkConsts.VALUES.PAGE_EMPTY, 0, "", destiny.fileRightLinkPage,
                    destiny.fileLinkPages, destiny.fileRightLinkPageName, isFileLeftDualPage = destiny.isFileRightDualPage,
                    imageFileLinkPage = destiny.imageRightFileLinkPage
                )
            )
            notifyMessages(Pages.NOT_LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_ADDED, getPageNotLinkLastIndex())
        } else if (destinyType == Pages.LINKED && destiny.fileLinkPage != PageLinkConsts.VALUES.PAGE_EMPTY) {
            mPagesNotLinked.value!!.add(
                PageLink(
                    destiny.idFile, PageLinkConsts.VALUES.PAGE_EMPTY, 0, "", destiny.fileLinkPage,
                    destiny.fileLinkPages, destiny.fileLinkPageName, isFileLeftDualPage = destiny.isFileLeftDualPage,
                    imageFileLinkPage = destiny.imageLeftFileLinkPage
                )
            )
            notifyMessages(Pages.NOT_LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_ADDED, getPageNotLinkLastIndex())
        }

        when {
            (originType == Pages.DUAL_PAGE && destinyType == Pages.DUAL_PAGE) -> {
                val originIndex = mPagesLink.value!!.indexOf(origin)
                val destinyIndex = mPagesLink.value!!.indexOf(destiny)

                destiny.addRightFileLinkImage(origin.fileRightLinkPage, origin.fileRightLinkPageName, origin.isFileRightDualPage, origin.imageRightFileLinkPage)
                origin.clearRightFileLink()

                notifyMessages(originType, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_UPDATED, originIndex)
                notifyMessages(destinyType, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_UPDATED, destinyIndex)
            }
            (originType == Pages.NOT_LINKED || destinyType == Pages.NOT_LINKED) ->  {
                if (originType == Pages.NOT_LINKED && destinyType == Pages.NOT_LINKED)
                    return
                else if (originType == Pages.NOT_LINKED) {
                    val originIndex = mPagesNotLinked.value!!.indexOf(origin)
                    val destinyIndex = mPagesLink.value!!.indexOf(destiny)

                    destiny.addRightFileLinkImage(origin.fileLinkPage, origin.fileLinkPageName, origin.isFileLeftDualPage, origin.imageLeftFileLinkPage)
                    mPagesNotLinked.value!!.remove(origin)

                    notifyMessages(originType, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_REMOVED, originIndex)
                    notifyMessages(destinyType, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_UPDATED, destinyIndex)
                } else if (destinyType == Pages.NOT_LINKED) {
                    val originIndex = mPagesLink.value!!.indexOf(destiny)
                    origin.clearRightFileLink()
                    notifyMessages(originType, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_UPDATED, originIndex)
                }
            }
            else -> {
                var originIndex = mPagesLink.value!!.indexOf(origin)
                var destinyIndex = mPagesLink.value!!.indexOf(destiny)

                when {
                    originType != Pages.DUAL_PAGE && destinyType == Pages.LINKED -> destiny.addLeftFileLinkImage(origin.fileLinkPage, origin.fileLinkPages, origin.fileLinkPageName, origin.isFileLeftDualPage, origin.imageLeftFileLinkPage)
                    originType != Pages.DUAL_PAGE && destinyType != Pages.LINKED -> destiny.addRightFileLinkImage(origin.fileLinkPage, origin.fileLinkPageName, origin.isFileLeftDualPage, origin.imageLeftFileLinkPage)
                    originType == Pages.DUAL_PAGE && destinyType == Pages.LINKED -> destiny.addLeftFileLinkImage(origin.fileRightLinkPage, origin.fileLinkPages, origin.fileRightLinkPageName, origin.isFileRightDualPage, origin.imageRightFileLinkPage)
                    originType == Pages.DUAL_PAGE && destinyType != Pages.LINKED -> destiny.addRightFileLinkImage(origin.fileRightLinkPage, origin.fileRightLinkPageName, origin.isFileRightDualPage, origin.imageRightFileLinkPage)
                }

                notifyMessages(destinyType, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_UPDATED, destinyIndex)

                val moved = when (originType) {
                    Pages.LINKED -> origin.clearLeftFileLink(true)
                    Pages.DUAL_PAGE -> {
                        origin.clearRightFileLink()
                        false
                    }
                    else -> false
                }

                notifyMessages(originType, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_UPDATED, originIndex)

                if (originIndex > destinyIndex && originType != Pages.DUAL_PAGE && !moved) {
                    originIndex += 1
                    destinyIndex += 1

                    if (originIndex >= mPagesLink.value!!.size || destinyIndex >= mPagesLink.value!!.size)
                        return

                    val nextOrigin = mPagesLink.value!![originIndex]
                    val nextDestiny = mPagesLink.value!![destinyIndex]

                    if (nextOrigin.fileLinkPage != 1)
                        onMove(nextOrigin, nextDestiny)
                }
            }
        }
    }

    fun onMove(origin: PageLink?, destiny: PageLink?) {
        if (origin == null || destiny == null) {
            notifyMessages(Pages.ALL, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_UPDATED)
            return
        }
        if (origin == destiny) return
        val originIndex = mPagesLink.value!!.indexOf(origin)
        val destinyIndex = mPagesLink.value!!.indexOf(destiny)
        var differ = destinyIndex - originIndex

        if (originIndex > destinyIndex) {
            var limit = mPagesLink.value!!.size-1
            var index = mPagesLink.value!!.indexOf(mPagesLink.value!!.findLast { it.imageLeftFileLinkPage != null })
            if (index < 0)
                index = mPagesLink.value!!.size-1

            for(i in index downTo originIndex)
                if (mPagesLink.value!![i].fileLinkPage == PageLinkConsts.VALUES.PAGE_EMPTY)
                    limit = i

            for (i in destinyIndex until originIndex)
                addNotLinked(mPagesLink.value!![i])

            differ *=-1
            for (i in destinyIndex until limit) {
                when {
                    i == destinyIndex -> mPagesLink.value!![i].addLeftFileLinkImage(origin.fileLinkPage, origin.fileLinkPages, origin.fileLinkPageName,
                        origin.isFileLeftDualPage, origin.imageLeftFileLinkPage)
                    (i + differ) > (limit) -> continue
                    else -> {
                        mPagesLink.value!![i].addLeftFileLinkImage(mPagesLink.value!![i + differ].fileLinkPage,
                            mPagesLink.value!![i + differ].fileLinkPages,
                            mPagesLink.value!![i + differ].fileLinkPageName,
                            mPagesLink.value!![i + differ].isFileLeftDualPage,
                            mPagesLink.value!![i + differ].imageLeftFileLinkPage)
                        mPagesLink.value!![i + differ].clearLeftFileLink()
                    }
                }
                notifyMessages(Pages.LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_UPDATED, i)
            }

            for (i in destinyIndex until limit)
                if (mPagesLink.value!![i].dualImage && mPagesLink.value!![i].fileLinkPage == PageLinkConsts.VALUES.PAGE_EMPTY &&
                    mPagesLink.value!![i].fileRightLinkPage != PageLinkConsts.VALUES.PAGE_EMPTY) {
                    mPagesLink.value!![i].moveFileLinkRightToLeft()
                    notifyMessages(Pages.LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_UPDATED, i)
                }
        } else {
            var limit = mPagesLink.value!!.size-1
            var spacesFree = 0

            for(i in originIndex until limit)
                if (mPagesLink.value!![i].fileLinkPage == PageLinkConsts.VALUES.PAGE_EMPTY)
                    spacesFree++

            if (differ > spacesFree) {
                for (i in limit downTo limit - differ)
                    addNotLinked(mPagesLink.value!![i])

                for (i in limit downTo originIndex) {
                    when {
                        i < destinyIndex -> mPagesLink.value!![i].clearLeftFileLink()
                        else -> mPagesLink.value!![i].addLeftFileLinkImage(mPagesLink.value!![i - differ].fileLinkPage,
                            mPagesLink.value!![i - differ].fileLinkPages,
                            mPagesLink.value!![i - differ].fileLinkPageName,
                            mPagesLink.value!![i - differ].isFileLeftDualPage,
                            mPagesLink.value!![i - differ].imageLeftFileLinkPage)
                    }
                    notifyMessages(Pages.LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_UPDATED, i)
                }
            } else {
                var spaceUsed = 0
                for(i in originIndex until limit) {
                    if (mPagesLink.value!![i].fileLinkPage == PageLinkConsts.VALUES.PAGE_EMPTY) {
                        spaceUsed++
                        if (spaceUsed >= differ) {
                            limit = i
                            break
                        }
                    }
                }

                spaceUsed = 0
                var index: Int
                for (i in limit downTo originIndex) {
                    if (i < destinyIndex)
                        mPagesLink.value!![i].clearLeftFileLink(true)
                    else {
                        index = i - (1 + spaceUsed)
                        if (mPagesLink.value!![index].fileLinkPage == PageLinkConsts.VALUES.PAGE_EMPTY) {
                            do {
                                spaceUsed++
                                index = i - (1 + spaceUsed)
                            } while (mPagesLink.value!![index].fileLinkPage == PageLinkConsts.VALUES.PAGE_EMPTY)
                        }
                        mPagesLink.value!![i].addLeftFileLinkImage(mPagesLink.value!![index].fileLinkPage,
                            mPagesLink.value!![index].fileLinkPages,
                            mPagesLink.value!![index].fileLinkPageName,
                            mPagesLink.value!![index].isFileLeftDualPage,
                            mPagesLink.value!![index].imageLeftFileLinkPage)
                    }
                    notifyMessages(Pages.LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_UPDATED, i)
                }
            }
        }
    }

    fun onNotLinked(origin : PageLink?) {
        if (origin == null) {
            notifyMessages(Pages.LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_UPDATED)
            return
        }

        mPagesNotLinked.value!!.add(PageLink(origin.idFile, PageLinkConsts.VALUES.PAGE_EMPTY, 0, "", origin.fileLinkPage,
            origin.fileLinkPages, origin.fileLinkPageName, isFileLeftDualPage = origin.isFileLeftDualPage,
            imageFileLinkPage = origin.imageLeftFileLinkPage))

        notifyMessages(Pages.NOT_LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_ADDED, mPagesNotLinked.value!!.size-1)

        val originIndex = mPagesLink.value!!.indexOf(origin)
        if (origin.dualImage)
            origin.moveFileLinkRightToLeft()
        else
            origin.clearFileLink()

        notifyMessages(Pages.LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_UPDATED, originIndex)
    }

    fun fromNotLinked(origin : PageLink?, destiny :PageLink?) {
        if (origin == null || destiny == null) {
            notifyMessages(Pages.NOT_LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_UPDATED)
            return
        }

        val destinyIndex = mPagesLink.value!!.indexOf(destiny)
        val size = mPagesLink.value!!.size-1
        mPagesNotLinked.value!!.remove(origin)
        notifyMessages(Pages.NOT_LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_REMOVED, size)

        if (destiny.imageLeftFileLinkPage == null) {
            mPagesLink.value!![destinyIndex].addLeftFileLinkImage(
                origin.fileLinkPage,
                origin.fileLinkPages,
                origin.fileLinkPageName,
                origin.isFileLeftDualPage,
                origin.imageLeftFileLinkPage
            )
            notifyMessages(Pages.LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_UPDATED, destinyIndex)
        } else {
            if (mPagesLink.value!![size].imageLeftFileLinkPage != null)
                addNotLinked(mPagesLink.value!![size])

            for (i in size downTo destinyIndex) {
                when (i) {
                    destinyIndex -> mPagesLink.value!![i].addLeftFileLinkImage(origin.fileLinkPage, origin.fileLinkPages, origin.fileLinkPageName,
                        origin.isFileLeftDualPage, origin.imageLeftFileLinkPage)
                    else -> mPagesLink.value!![i].addLeftFileLinkImage(mPagesLink.value!![i - 1].fileLinkPage,
                        mPagesLink.value!![i - 1].fileLinkPages,
                        mPagesLink.value!![i - 1].fileLinkPageName,
                        mPagesLink.value!![i - 1].isFileLeftDualPage,
                        mPagesLink.value!![i - 1].imageLeftFileLinkPage)
                }
                notifyMessages(Pages.LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_UPDATED, i)
            }
        }
    }

    fun generateDualPages(isIgnoreCalculateDualPage: Boolean = true) {
        if (mFileLink.value == null || mFileLink.value!!.path.isEmpty())    return

        if (mFileLink.value != null) {
            notifyMessages(Pages.LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_GENERATE_DUAL_PAGES_START)

            val pagesLink = mPagesLink.value!!
            var padding = 1

            for ((index, page) in pagesLink.withIndex()) {
                if(page.dualImage || (!isIgnoreCalculateDualPage && page.isFileLeftDualPage && page.fileRightLinkPage == PageLinkConsts.VALUES.PAGE_EMPTY))
                    continue

                var next = pagesLink[index + padding]
                if (next.fileLinkPage == PageLinkConsts.VALUES.PAGE_EMPTY) {
                    do {
                        if ((index + padding) > pagesLink.size-1)
                            break
                        padding++
                        next = pagesLink[index + padding]
                    } while (next.fileLinkPage == PageLinkConsts.VALUES.PAGE_EMPTY)
                }

                if ((index + padding) > pagesLink.size-1)
                    break

                if (isIgnoreCalculateDualPage) {
                    if (next.dualImage) {
                        if (page.fileLinkPage != PageLinkConsts.VALUES.PAGE_EMPTY)
                            continue
                        else
                            page.merge(next)
                        next.clearFileLink()
                    } else {
                        if (page.fileLinkPage == PageLinkConsts.VALUES.PAGE_EMPTY) {
                            page.addLeftFileLinkImage(
                                next.fileLinkPage,
                                next.fileLinkPage,
                                next.fileLinkPageName,
                                next.isFileLeftDualPage,
                                next.imageLeftFileLinkPage
                            )
                            next.clearLeftFileLink()

                            if (next.fileRightLinkPage != PageLinkConsts.VALUES.PAGE_EMPTY) {
                                page.addRightFileLinkImage(
                                    next.fileRightLinkPage,
                                    next.fileRightLinkPageName,
                                    next.isFileRightDualPage,
                                    next.imageRightFileLinkPage
                                )
                                next.clearRightFileLink()
                            } else {
                                padding++
                                next = pagesLink[index + padding]
                                if (!next.dualImage && next.fileLinkPage != PageLinkConsts.VALUES.PAGE_EMPTY) {
                                    page.addRightFileLinkImage(
                                        next.fileLinkPage,
                                        next.fileLinkPageName,
                                        next.isFileLeftDualPage,
                                        next.imageLeftFileLinkPage
                                    )
                                    next.clearLeftFileLink()
                                    notifyMessages(Pages.LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_UPDATED, index + padding)
                                } else
                                    padding--
                            }
                        } else {
                            page.addRightFileLinkImage(
                                next.fileLinkPage,
                                next.fileLinkPageName,
                                next.isFileLeftDualPage,
                                next.imageLeftFileLinkPage
                            )
                            next.clearLeftFileLink()
                        }
                    }
                } else {
                    if (next.dualImage || next.isFileLeftDualPage) {
                        if (page.fileLinkPage != PageLinkConsts.VALUES.PAGE_EMPTY)
                            continue
                        else
                            page.merge(next)
                        next.clearFileLink()
                    }

                }

                notifyMessages(Pages.LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_UPDATED, index)
                notifyMessages(Pages.LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_UPDATED, index + padding)
            }

            notifyMessages(Pages.LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_GENERATE_DUAL_PAGES_FINISHED)
        }
    }

    fun clearDualPages(isNotify: Boolean = true) {
        if (mFileLink.value == null || mFileLink.value!!.path.isEmpty()) return

        val hasDualImage = mPagesLink.value?.any { it.dualImage } ?: false

        if (hasDualImage) {
            if (isNotify)
                notifyMessages(Pages.LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_CLEAR_DUAL_PAGES_START)

            val pagesLink = mPagesLink.value!!
            var amount = 0
            pagesLink.forEach { if (it.dualImage) amount+=1 }

            for (i in pagesLink.size -1 downTo (pagesLink.size -1) - amount)
                addNotLinked(pagesLink[i])

            if (isNotify)
                notifyMessages(Pages.NOT_LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_CLEAR_DUAL_PAGES_FINISHED)

            var process = amount
            for (i in pagesLink.size -1 downTo 0) {
                val page = pagesLink[i-process]
                if (page.dualImage) {
                    pagesLink[i].addLeftFileLinkImage(page.fileRightLinkPage, page.fileRightLinkPage,
                        page.fileRightLinkPageName, page.isFileRightDualPage, page.imageRightFileLinkPage)
                    page.clearRightFileLink()
                    notifyMessages(Pages.LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_UPDATED, i-process)
                    process -=1
                } else
                    pagesLink[i].addLeftFileLinkImage(page.fileLinkPage, page.fileLinkPages,
                        page.fileLinkPageName, page.isFileLeftDualPage, page.imageLeftFileLinkPage)

                notifyMessages(Pages.LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_UPDATED, i)
                if (process <= 0)
                    break
            }

            if (isNotify)
                notifyMessages(Pages.LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_CLEAR_DUAL_PAGES_FINISHED)
        }
    }

    fun autoGenerateDualPages(type: Pages, isClear: Boolean = false) {
        if (mFileLink.value == null || mFileLink.value!!.path.isEmpty()) return

        notifyMessages(type, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_AUTO_GENERATE_DUAL_PAGES_START)

        val hasDualImage = if(isClear) {
            clearDualPages(false)
            false
        } else
            mPagesLink.value?.any { it.dualImage } ?: false

        if (!hasDualImage && (mFileLink.value?.id == null || isClear)) {
            val pagesLink = mPagesLink.value!!
            val lastIndex = pagesLink.size -1
            var ignoreNext = false
            for ((index, page) in pagesLink.withIndex()) {
                if (ignoreNext || index >= lastIndex) {
                    ignoreNext = false
                    continue
                }

                if (page.isMangaDualPage && page.isFileLeftDualPage)
                    continue

                if (page.isMangaDualPage) {
                    val nextPage = pagesLink[index + 1]

                    if (nextPage.fileLinkPage == PageLinkConsts.VALUES.PAGE_EMPTY)
                        continue

                    page.addRightFileLinkImage(nextPage.fileLinkPage, nextPage.fileLinkPageName, nextPage.isFileLeftDualPage, nextPage.imageLeftFileLinkPage)
                    nextPage.clearFileLink()

                    for ((idxNext, next) in pagesLink.withIndex()) {
                        if (idxNext < (index + 1) || idxNext >= lastIndex)
                            continue

                        val aux = pagesLink[idxNext + 1]
                        next.addLeftFileLinkImage(aux.fileLinkPage, aux.fileLinkPages, aux.fileLinkPageName, aux.isFileLeftDualPage, aux.imageLeftFileLinkPage)
                        aux.clearLeftFileLink()
                    }
                } else if(page.isFileLeftDualPage) {
                    ignoreNext = true
                    if (pagesLink[index + 1].fileLinkPage == PageLinkConsts.VALUES.PAGE_EMPTY)
                        continue

                    var process = pagesLink[lastIndex]
                    if (process.imageLeftFileLinkPage != null )
                        addNotLinked(process)

                    for (i in lastIndex downTo (index +1)) {
                        process = pagesLink[i]
                        val aux = pagesLink[i - 1]

                        process.addLeftFileLinkImage(aux.fileLinkPage, aux.fileLinkPages, aux.fileLinkPageName, aux.isFileLeftDualPage, aux.imageLeftFileLinkPage)
                        aux.clearLeftFileLink()
                    }
                }
            }
        }

        notifyMessages(type, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_AUTO_GENERATE_DUAL_PAGES_FINISHED)
    }

    fun reorderPages() {
        if (mFileLink.value == null || mFileLink.value!!.path.isEmpty()) return

        notifyMessages(Pages.LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_REORDER_PAGES_START)

        val pagesNotLink = mPagesNotLinked.value!!
        val pagesLink = mPagesLink.value!!
        val pagesLinkTemp = pagesLink.toMutableList()
        val pagesNotLinkTemp = ArrayList<PageLink>()
        var maxNumPage = 0

        for (page in pagesLink) {
            if (page.fileLinkPage > maxNumPage)
                maxNumPage = page.fileLinkPage

            if (page.fileRightLinkPage > maxNumPage)
                maxNumPage = page.fileLinkPage
        }

        for (page in pagesNotLink) {
            if (page.fileLinkPage > maxNumPage)
                maxNumPage = page.fileLinkPage
        }

        pagesLinkTemp.removeAll { it.mangaPage == PageLinkConsts.VALUES.PAGE_EMPTY }

        for (page in pagesLinkTemp) {
            if (page.mangaPage == PageLinkConsts.VALUES.PAGE_EMPTY) {
                page.clearFileLink()
                continue
            }

            if (page.mangaPage > maxNumPage)
                page.clearFileLink()
            else {
                val pageLink = pagesLink.find { it.fileLinkPage == page.mangaPage || it.fileRightLinkPage == page.mangaPage  } ?: pagesNotLink.find { it.fileLinkPage == page.mangaPage }

                if (pageLink != null) {
                    if (pageLink.fileLinkPage == page.mangaPage)
                        page.addLeftFileLinkImage(pageLink.fileLinkPage, pageLink.fileLinkPages, pageLink.fileLinkPageName, pageLink.isFileLeftDualPage, pageLink.imageLeftFileLinkPage)
                    else
                        page.addLeftFileLinkImage(pageLink.fileRightLinkPage, pageLink.fileLinkPages, pageLink.fileRightLinkPageName, pageLink.isFileRightDualPage, pageLink.imageRightFileLinkPage)

                    page.clearRightFileLink()
                } else
                    page.clearFileLink()
            }
        }

        if (maxNumPage >= pagesLinkTemp.size) {
            for (numPage in pagesLinkTemp.size until maxNumPage) {
                val pageLink = pagesLink.find { it.fileLinkPage == numPage || it.fileRightLinkPage == numPage }
                    ?: pagesNotLink.find { it.fileLinkPage == numPage }

                if (pageLink != null) {
                    if (pageLink.fileLinkPage == numPage)
                        pagesNotLinkTemp.add(
                            PageLink(
                                pageLink.idFile, PageLinkConsts.VALUES.PAGE_EMPTY, 0, "",
                                pageLink.fileLinkPage, pageLink.fileLinkPages, pageLink.fileLinkPageName,
                                isFileLeftDualPage = pageLink.isFileLeftDualPage, imageFileLinkPage = pageLink.imageLeftFileLinkPage
                            )
                        )
                    else
                        pagesNotLinkTemp.add(
                            PageLink(
                                pageLink.idFile, PageLinkConsts.VALUES.PAGE_EMPTY, 0, "",
                                pageLink.fileRightLinkPage, pageLink.fileLinkPages, pageLink.fileRightLinkPageName,
                                isFileLeftDualPage = pageLink.isFileRightDualPage, imageFileLinkPage = pageLink.imageRightFileLinkPage
                            )
                        )
                }
            }
        }

        pagesLink.clear()
        pagesNotLink.clear()
        mPagesLink.value = ArrayList(pagesLinkTemp)
        mPagesNotLinked.value = pagesNotLinkTemp

        notifyMessages(Pages.LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_REORDER_PAGES_FINISHED)
    }

    private fun removeThread(type: Pages, isEnd: Boolean = false) {
        if (!isEnd)
            mGenerateImageThread.filter { it.type.compareTo(type) == 0 }.forEach {
                (it.runnable as ImageLoadRunnable).forceEnd = true
                it.thread.interrupt()
            }

        mGenerateImageThread.removeAll { it.type.compareTo(type) == 0 }
    }

    fun endThread(isIgnoreManga: Boolean = false) {
        if (isIgnoreManga) {
            val reloadManga = mGenerateImageThread.isNotEmpty() && mGenerateImageThread.any { it.type.compareTo(Pages.ALL) == 0 } &&
                    !mGenerateImageThread.any { it.type.compareTo(Pages.MANGA) == 0 }

            mGenerateImageThread.filter { it.type.compareTo(Pages.MANGA) != 0 }.forEach {
                (it.runnable as ImageLoadRunnable).forceEnd = true
                it.thread.interrupt()
            }
            mGenerateImageThread.removeAll { it.type.compareTo(Pages.MANGA) != 0 }

            if (reloadManga)
                reLoadImages(Pages.MANGA, isVerifyImages = true, isForced = true)
        } else {
            mGenerateImageThread.forEach {
                (it.runnable as ImageLoadRunnable).forceEnd = true
                it.thread.interrupt()
            }
            mGenerateImageThread.clear()
        }
    }

    fun imageThreadLoadingProgress(): Int = mGenerateImageThread.size

    fun addImageLoadHandler(handler: Handler) {
        mGenerateImageHandler!!.add(handler)
    }

    fun removeImageLoadHandler(handler: Handler) {
        mGenerateImageHandler!!.remove(handler)
    }

    private fun notifyMessages(type: Pages, message: Int, index: Int? = null) {
        val msg = Message()
        msg.obj = ImageLoad(index, type)
        msg.what = message
        for (h in mGenerateImageHandler!!)
            h.sendMessage(msg)
    }

    fun getProgress() : Pair<Int, Int> {
        if (mGenerateImageThread.isEmpty())
            return Pair(-1, 1)

        var size = 0
        var progress = 0
        mGenerateImageThread.forEach {
            size += (it.runnable as ImageLoadRunnable).size
            progress += (it.runnable as ImageLoadRunnable).progress
        }

        return Pair(progress, size)
    }

    private fun isAllImagesLoaded(type : Pages = Pages.ALL): Boolean {
        val isFileLink = mFileLink.value!!.path != ""
        if (type != Pages.NOT_LINKED)
            for (page in mPagesLink.value!!) {
                if ((type == Pages.ALL || type == Pages.MANGA) && (page.imageMangaPage == null))
                    return false
                if (isFileLink && (type == Pages.ALL || type == Pages.LINKED)) {
                    if ((page.fileLinkPage != PageLinkConsts.VALUES.PAGE_EMPTY && page.imageLeftFileLinkPage == null)
                        || (page.dualImage && page.imageRightFileLinkPage == null))
                        return false
                }
            }

        if (isFileLink && (type == Pages.NOT_LINKED || type == Pages.ALL))
            for (page in mPagesNotLinked.value!!) {
                if (page.fileLinkPage != PageLinkConsts.VALUES.PAGE_EMPTY && page.imageLeftFileLinkPage == null)
                    return false
            }

        notifyMessages(type, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_ALL_IMAGES_LOADED)
        return true
    }

    private var mLoadVerify : Int = 0
    private var mLoadError : Int = 0
    fun reLoadImages(type : Pages = Pages.ALL, isVerifyImages: Boolean = false, isForced: Boolean = false, isCloseThreads: Boolean = false) {
        mLoadVerify += 1
        if (!isForced && (mLoadError > 5 || (isVerifyImages && mLoadVerify > 3))) return
        if (!isForced && isVerifyImages && isAllImagesLoaded(type)) return

        if (isCloseThreads) endThread()

        Util.destroyParse(mFileLink.value?.parseManga)
        Util.destroyParse(mFileLink.value?.parseFileLink)

        val parseManga = getParse(mManga!!.file)
        val parseFileLink = getParse(mFileLink.value!!.file)

        mFileLink.value!!.parseManga = parseManga
        mFileLink.value!!.parseFileLink = parseFileLink

        if (type != Pages.NOT_LINKED)
            getImage(parseManga, parseFileLink, mPagesLink.value!!, type, true)

        if ((type == Pages.NOT_LINKED || type == Pages.ALL) && mPagesNotLinked.value!!.isNotEmpty())
            getImage(null, parseFileLink, mPagesNotLinked.value!!, Pages.NOT_LINKED, true)
    }

    private fun getImage(parseManga: Parse?, parsePageLink: Parse?, list : ArrayList<PageLink>, type : Pages, reload : Boolean = false) {
        if (!reload) mLoadVerify = 0

        removeThread(type)
        var imageLoadThread: ImageLoadThread? = null
        val runnable = ImageLoadRunnable(parseManga, parsePageLink, list, type, reload) {
            if (imageLoadThread != null && mGenerateImageThread.contains(imageLoadThread))
                mGenerateImageThread.remove(imageLoadThread)
        }

        val thread = Thread(runnable)
        thread.priority = Process.THREAD_PRIORITY_DEFAULT + Process.THREAD_PRIORITY_LESS_FAVORABLE
        thread.start()
        imageLoadThread = ImageLoadThread(type, thread, runnable)
        mGenerateImageThread.add(imageLoadThread)
    }

    private fun generateBitmap(parse: Parse, index: Int, setImage: (isDualPage: Boolean, image: Bitmap?) -> (Unit)) {
        val image = generateBitmap(parse, index)
        setImage(image.first, image.second)
    }

    private fun generateBitmap(parse: Parse, index: Int): Pair<Boolean, Bitmap?> {
        return if (index == -1) Pair(false, null) else
            try {
                val stream: InputStream = parse.getPage(index)
                val image  = BitmapFactory.decodeStream(stream)
                val isDualPage = (image.width / image.height) > 0.9
                val bitmap = Bitmap.createScaledBitmap(image, ReaderConsts.PAGESLINK.IMAGES_WIDTH, ReaderConsts.PAGESLINK.IMAGES_HEIGHT, false)
                Util.closeInputStream(stream)
                Pair(isDualPage, bitmap)
            } catch (i: InterruptedIOException) {
                mLOGGER.info("Interrupted error when generate bitmap: " + i.message)
                Pair(false, null)
            } catch (e: Exception) {
                mLOGGER.info("Error when generate bitmap: " + e.message)
                Pair(false, null)
            }
    }

    inner class ImageLoad(var index: Int?, var type: Pages)
    private inner class ImageLoadThread(var type: Pages, var thread: Thread, var runnable: Runnable)
    private inner class ImageLoadRunnable(private var parseManga: Parse?, private var parsePageLink: Parse?, private var list: ArrayList<PageLink>,
                                          private var type: Pages, private var reload : Boolean = false, private var callEnded: () -> (Unit)) : Runnable {
        var forceEnd: Boolean = false
        var progress: Int = 0
        var size: Int = 0

        override fun run() {
            var error = false
            try {
                progress = 0
                forceEnd = false
                size = list.size
                notifyMessages(type, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_START)
                for ((index, page) in list.withIndex()) {
                    progress = index
                    if (reload) {
                        when (type) {
                            Pages.ALL -> {
                                if ((page.dualImage) && (page.imageMangaPage != null && page.imageLeftFileLinkPage != null && page.imageRightFileLinkPage != null))
                                    continue
                                else if(page.imageMangaPage != null && page.imageLeftFileLinkPage != null)
                                    continue
                            }
                            Pages.MANGA -> if (page.imageMangaPage != null) continue
                            Pages.NOT_LINKED -> if (page.imageLeftFileLinkPage != null) continue
                            Pages.LINKED -> {
                                if (page.dualImage && (page.imageLeftFileLinkPage != null && page.imageRightFileLinkPage != null))
                                    continue
                                else if(page.imageLeftFileLinkPage != null)
                                    continue
                            }
                            else -> {}
                        }
                    }

                    when (type) {
                        Pages.ALL -> {
                            if (parseManga != null) {
                                val (isDualPage, image) = generateBitmap(parseManga!!, page.mangaPage)
                                page.isMangaDualPage = isDualPage
                                page.imageMangaPage = image
                            }

                            if (parsePageLink != null) {
                                val (isDualPage, image) = generateBitmap(parsePageLink!!, page.fileLinkPage)
                                page.imageLeftFileLinkPage = image
                                page.isFileLeftDualPage = isDualPage

                                if (page.dualImage)
                                    generateBitmap(parsePageLink!!, page.fileRightLinkPage) { IsDualPage, Image -> page.imageRightFileLinkPage = Image; page.isFileRightDualPage = IsDualPage }
                            }
                        }
                        Pages.MANGA -> generateBitmap(parseManga!!, page.mangaPage) { isDualPage, image -> page.imageMangaPage = image; page.isMangaDualPage = isDualPage }
                        Pages.NOT_LINKED -> generateBitmap(parsePageLink!!, page.fileLinkPage) { isDualPage, image -> page.imageLeftFileLinkPage = image; page.isFileLeftDualPage = isDualPage }
                        Pages.LINKED -> {
                            val (isDualPage, image) = generateBitmap(parsePageLink!!, page.fileLinkPage)
                            page.imageLeftFileLinkPage = image
                            page.isFileLeftDualPage = isDualPage

                            if (page.dualImage)
                                generateBitmap(parsePageLink!!, page.fileRightLinkPage) { IsDualPage, Image -> page.imageRightFileLinkPage = Image; page.isFileRightDualPage = IsDualPage }
                        }
                        else -> {}
                    }

                    if (forceEnd)
                        break
                    else
                        notifyMessages(type, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_UPDATED, index)
                }
            } catch(e: Exception) {
                error = true
                if (!forceEnd) {
                    mLoadError += 1
                    notifyMessages(type, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_LOAD_ERROR)
                }
            } finally {
                callEnded()

                progress = size
                if (!error)
                    mLoadError = 0

                if (!forceEnd)
                    notifyMessages(type, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_FINISHED)
            }
        }
    }
}
package br.com.fenix.bilingualmangareader.view.ui.pages_link

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.Process
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.com.fenix.bilingualmangareader.model.entity.FileLink
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.model.entity.Page
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

    private var mManga: Manga? = null
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

    private var mUsePagePath = GeneralConsts.getSharedPreferences(mContext)
        .getBoolean(GeneralConsts.KEYS.PAGE_LINK.USE_PAGE_PATH_FOR_LINKED, false)

    fun getMangaName(): String {
        return mManga?.fileName ?: ""
    }

    private fun getParse(path: String, type: Pages): Parse? =
        getParse(File(path), type)

    private fun getParse(file: File, type: Pages): Parse? {
        val parse = ParseFactory.create(file)
        if (parse is RarParse) {
            val prefix = (if (type == Pages.MANGA) GeneralConsts.FILE_LINK.FOLDER_MANGA else GeneralConsts.FILE_LINK.FOLDER_LINK) + "_"
            val folder = GeneralConsts.CACHE_FOLDER.LINKED + '/' + Util.normalizeNameCache(file.nameWithoutExtension, prefix, false)
            val cacheDir = File(GeneralConsts.getCacheDir(mContext), folder)
            (parse as RarParse?)!!.setCacheDirectory(cacheDir)
        }
        return parse
    }

    fun getFileLink(manga: Manga? = null, isBackup: Boolean = false): FileLink? {
        return if (!isBackup && (mFileLink.value == null || mFileLink.value!!.path == ""))
            if (manga != null)
                mFileLinkRepository.get(manga)
            else
                null
        else this.get()
    }

    // Keeps the data to quickly load the file. Cleaning takes place by the main activity
    fun onDestroy() {
        if (mFileLink.value?.parseManga == null) {
            Util.destroyParse(mFileLink.value?.parseManga, false)
            mFileLink.value?.parseManga = null
        }

        if (mFileLink.value?.parseFileLink == null) {
            Util.destroyParse(mFileLink.value?.parseFileLink, false)
            mFileLink.value?.parseFileLink = null
        }
    }

    private fun verify(fileLink: FileLink?) {
        if (fileLink == null) return

        if (fileLink.parseManga == null)
            fileLink.parseManga = getParse(fileLink.manga!!.path, Pages.MANGA)

        if (fileLink.parseFileLink == null && fileLink.path.isNotEmpty())
            fileLink.parseFileLink = getParse(fileLink.path, Pages.LINKED)
    }

    private fun reload(refresh: (index: Int?, type: Pages) -> (Unit)): Boolean {
        val fileLink = SubTitleController.getInstance(mContext).getFileLink() ?: return false
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

    fun reload(fileLink: FileLink?, refresh: (index: Int?, type: Pages) -> (Unit)): Boolean {
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

    private fun find(isLoadManga: Boolean, refresh: (index: Int?, type: Pages) -> (Unit)): Boolean {
        if (mManga == null) return false
        val obj = mFileLinkRepository.get(mManga!!) ?: return false
        set(obj, refresh, isLoadManga)
        return (obj.pagesLink != null) && (obj.pagesLink!!.isNotEmpty())
    }

    fun find(name: String, pages: Int, refresh: (index: Int?, type: Pages) -> (Unit)): Boolean {
        if (mManga == null || mManga!!.id == null) return false
        val obj = mFileLinkRepository.findByFileName(mManga!!.id!!, name, pages) ?: return false
        set(obj, refresh)
        return true
    }

    fun set(obj: FileLink, refresh: (index: Int?, type: Pages) -> (Unit), isLoadManga: Boolean = false) {
        endThread(true)

        Util.destroyParse(mFileLink.value?.parseManga, false)
        Util.destroyParse(mFileLink.value?.parseFileLink, false)

        if (mPagesLink.value != null && mPagesLink.value!!.isNotEmpty())
            obj.pagesLink?.forEachIndexed { index, pageLink -> pageLink.imageMangaPage = mPagesLink.value!![index].imageMangaPage }

        mFileLink.value = obj
        mPagesLink.value?.forEach { it.clearPageLink() }
        mPagesNotLinked.value?.clear()
        setLanguage(obj.language)

        val mParseManga = getParse(mManga!!.file, Pages.MANGA) ?: return
        mFileLink.value?.parseManga = mParseManga
        val mParseLink = getParse(obj.file, Pages.LINKED) ?: return
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

    fun getPagesIndex(isMangaIndexes: Boolean): Map<String, Int> {
        val paths = mutableMapOf<String, Int>()

        for ((index, page) in mPagesLink.value!!.withIndex()) {
            val path = if (isMangaIndexes)
                page.mangaPagePath
            else
                page.fileLinkLeftPagePath

            if (path.isNotEmpty() && !paths.containsKey(path))
                paths[path] = index
        }

        return paths
    }

    fun getFilesNames(): Pair<String, String> {
        val manga = mManga?.fileName ?: ""
        val fileLink = mFileLink.value?.name ?: ""
        return Pair(manga, fileLink)
    }

    fun restoreBackup(refresh: (index: Int?, type: Pages) -> (Unit)) {
        if (mFileLink.value != null) {
            val fileLink = mFileLinkRepository.get(mManga!!)
            if (fileLink != null) {
                endThread()
                fileLink.parseFileLink = mFileLink.value!!.parseFileLink
                fileLink.parseManga = mFileLink.value!!.parseManga

                for ((index, page) in fileLink.pagesLink!!.withIndex()) {
                    if (index >= mPagesLink.value!!.size)
                        break

                    page.imageMangaPage = mPagesLink.value!![index].imageMangaPage

                    if (page.fileLinkLeftPage != PageLinkConsts.VALUES.PAGE_EMPTY) {
                        val item = mPagesLink.value?.find {
                            it.fileLinkLeftPage.compareTo(page.fileLinkLeftPage) == 0 || it.fileLinkRightPage.compareTo(
                                page.fileLinkLeftPage
                            ) == 0
                        } ?: mPagesNotLinked.value?.find { it.fileLinkLeftPage.compareTo(page.fileLinkLeftPage) == 0 }

                        if (item != null) {
                            if (item.fileLinkLeftPage.compareTo(page.fileLinkLeftPage) == 0)
                                page.imageLeftFileLinkPage = item.imageLeftFileLinkPage
                            else if (item.fileLinkRightPage.compareTo(page.fileLinkLeftPage) == 0)
                                page.imageLeftFileLinkPage = item.imageRightFileLinkPage
                        }
                    }

                    if (page.fileLinkRightPage != PageLinkConsts.VALUES.PAGE_EMPTY) {
                        val item = mPagesLink.value?.find {
                            it.fileLinkLeftPage.compareTo(page.fileLinkRightPage) == 0 || it.fileLinkRightPage.compareTo(
                                page.fileLinkRightPage
                            ) == 0
                        } ?: mPagesNotLinked.value?.find { it.fileLinkLeftPage.compareTo(page.fileLinkRightPage) == 0 }

                        if (item != null) {
                            if (item.fileLinkLeftPage.compareTo(page.fileLinkRightPage) == 0)
                                page.imageRightFileLinkPage = item.imageLeftFileLinkPage
                            else if (item.fileLinkRightPage.compareTo(page.fileLinkRightPage) == 0)
                                page.imageRightFileLinkPage = item.imageRightFileLinkPage
                        }
                    }
                }

                for (page in fileLink.pagesNotLink!!) {
                    val item = mPagesNotLinked.value?.find { it.fileLinkLeftPage.compareTo(page.fileLinkLeftPage) == 0 }
                        ?: mPagesLink.value?.find { it.fileLinkLeftPage.compareTo(page.fileLinkLeftPage) == 0 || it.fileLinkRightPage.compareTo(page.fileLinkLeftPage) == 0 }

                    if (item != null) {
                        if (item.fileLinkLeftPage.compareTo(page.fileLinkLeftPage) == 0)
                            page.imageLeftFileLinkPage = item.imageLeftFileLinkPage
                        else if (item.fileLinkRightPage.compareTo(page.fileLinkLeftPage) == 0)
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

    fun setLanguage(language: Languages? = null, isClear: Boolean = false) {
        mLanguage.value = if (isClear || language == null) Languages.PORTUGUESE else language
    }

    fun loadManga(manga: Manga, refresh: (index: Int?, type: Pages) -> (Unit)) {
        mManga = manga
        mPagesLink.value?.clear()
        setLanguage(isClear = true)

        if (reload(refresh)) return
        if (find(true, refresh)) return

        Util.destroyParse(mFileLink.value?.parseManga, false)

        val parse = getParse(manga.file, Pages.MANGA) ?: return
        mFileLink.value = FileLink(manga)
        mFileLink.value!!.parseManga = parse

        val list = ArrayList<PageLink>()
        for (i in 0 until parse.numPages()) {
            val name = parse.getPagePath(i) ?: ""
            if (Util.isImage(name))
                list.add(PageLink(mFileLink.value!!.id, i, manga.pages, Util.getNameFromPath(name), Util.getFolderFromPath(name)))
        }
        mPagesLink.value = list
        refresh(null, Pages.MANGA)
        getImage(parse, null, mPagesLink.value!!, Pages.MANGA)
    }

    fun readFileLink(path: String, isReload: Boolean = false, refresh: (index: Int?, type: Pages) -> (Unit)): LoadFile {
        var loaded = LoadFile.ERROR_NOT_LOAD

        val file = File(path)
        if (file.name.endsWith(".rar") ||
            file.name.endsWith(".zip") ||
            file.name.endsWith(".cbr") ||
            file.name.endsWith(".cbz")
        ) {

            Util.destroyParse(mFileLink.value?.parseFileLink, false)
            val parse = getParse(path, Pages.LINKED)
            if (parse != null) {
                loaded = LoadFile.LOADED

                if (!isReload && find(file.name, parse.numPages(), refresh)) return loaded

                endThread(true)
                mFileLink.value = FileLink(
                    mManga!!, mFileLink.value!!.parseManga, parse.numPages(), path,
                    file.name, file.extension, file.parent
                )
                mFileLink.value!!.parseFileLink = parse
                mPagesLink.value?.forEach { it.clearPageLink() }

                var folder = ""
                var lastFolder = ""
                var padding = 0
                val mangaParse = mFileLink.value!!.parseManga!!
                val hasFolders = mangaParse.getPagePaths().isNotEmpty() && parse.getPagePaths().isNotEmpty()

                val pagesLink = mPagesLink.value!!
                val listNotLink = ArrayList<PageLink>()
                for (i in 0 until parse.numPages()) {
                    val pagePath = parse.getPagePath(i) ?: ""
                    if (Util.isImage(pagePath)) {
                        if (mUsePagePath && hasFolders) {
                            folder = Util.getFolderFromPath(pagePath)
                            if (!folder.equals(lastFolder, true)) {
                                lastFolder = folder
                                if (i > 0) {
                                    if (pagesLink[i + padding].mangaPagePath.equals(pagesLink[i + padding - 1].mangaPagePath, true)) {
                                        do {
                                            padding++
                                            if ((i + padding) > pagesLink.size)
                                                break
                                        } while (pagesLink[i + padding].mangaPagePath.equals(
                                                pagesLink[i + padding - 1].mangaPagePath,
                                                true
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        val index = i + padding
                        if (index > -1 && index < pagesLink.size) {
                            val page = pagesLink[index]
                            page.fileLinkLeftPage = i
                            page.fileLinkLeftPageName = Util.getNameFromPath(pagePath)
                            page.fileLinkLeftPagePath = Util.getFolderFromPath(pagePath)
                            page.fileLinkLeftPages = parse.numPages()
                            refresh(i, Pages.LINKED)
                        } else
                            listNotLink.add(
                                PageLink(
                                    mFileLink.value!!.id, true, i, parse.numPages(), Util.getNameFromPath(pagePath),
                                    Util.getFolderFromPath(pagePath)
                                )
                            )
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
        mFileLink.value = FileLink(mManga!!, mFileLink.value!!.parseManga)
        setLanguage(isClear = true)
        mPagesNotLinked.value?.clear()
        mPagesLink.value?.forEach { page -> page.clearPageLink() }
        refresh(null, Pages.ALL)
    }

    fun getPageLink(page: PageLink): String =
        mPagesLink.value!!.indexOf(page).toString()

    fun getPageLink(index: Int): PageLink? {
        return if (index >= mPagesLink.value!!.size || index == -1)
            null
        else
            mPagesLink.value!![index]
    }

    fun getPageNotLink(page: PageLink): String =
        mPagesNotLinked.value!!.indexOf(page).toString()

    fun getPageNotLink(index: Int): PageLink? {
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

    fun getPageNotLinkIndex(page: PageLink?): Int? {
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
        if (page.fileLinkLeftPage != PageLinkConsts.VALUES.PAGE_EMPTY) {
            mPagesNotLinked.value!!.add(
                PageLink(
                    page.idFile, true, page.fileLinkLeftPage, page.fileLinkLeftPages, page.fileLinkLeftPageName,
                    page.fileLinkLeftPagePath, page.isFileLeftDualPage, page.imageLeftFileLinkPage
                )
            )
            notifyMessages(Pages.NOT_LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_ADDED, getPageNotLinkLastIndex())
        }

        if (page.isDualImage) {
            mPagesNotLinked.value!!.add(
                PageLink(
                    page.idFile, true, page.fileLinkRightPage, page.fileLinkLeftPages, page.fileLinkRightPageName,
                    page.fileLinkRightPagePath, page.isFileRightDualPage, page.imageRightFileLinkPage
                )
            )
            page.clearRightPageLink()
            notifyMessages(Pages.NOT_LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_ADDED, getPageNotLinkLastIndex())
        }
    }

    fun onMoveDualPage(originType: Pages, origin: PageLink?, destinyType: Pages, destiny: PageLink?) {
        if (origin == null || destiny == null) {
            notifyMessages(Pages.ALL, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_ITEM_CHANGE)
            return
        }

        if (origin == destiny && destinyType == Pages.DUAL_PAGE) {
            notifyMessages(Pages.LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_ITEM_CHANGE, mPagesLink.value!!.indexOf(destiny))
            return
        }

        if (destinyType != Pages.LINKED && destiny.isDualImage) {
            mPagesNotLinked.value!!.add(
                PageLink(
                    destiny.idFile, true, destiny.fileLinkRightPage, destiny.fileLinkLeftPages, destiny.fileLinkRightPageName,
                    destiny.fileLinkRightPagePath, destiny.isFileRightDualPage, destiny.imageRightFileLinkPage
                )
            )
            notifyMessages(Pages.NOT_LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_ADDED, getPageNotLinkLastIndex())
        } else if (destinyType == Pages.LINKED && destiny.fileLinkLeftPage != PageLinkConsts.VALUES.PAGE_EMPTY) {
            mPagesNotLinked.value!!.add(
                PageLink(
                    destiny.idFile, true, destiny.fileLinkLeftPage, destiny.fileLinkLeftPages, destiny.fileLinkLeftPageName,
                    destiny.fileLinkLeftPagePath, destiny.isFileLeftDualPage, destiny.imageLeftFileLinkPage
                )
            )
            notifyMessages(Pages.NOT_LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_ADDED, getPageNotLinkLastIndex())
        }

        when {
            (originType == Pages.DUAL_PAGE && destinyType == Pages.DUAL_PAGE) -> {
                val originIndex = mPagesLink.value!!.indexOf(origin)
                val destinyIndex = mPagesLink.value!!.indexOf(destiny)

                destiny.addRightFromLeftPageLink(origin)
                origin.clearRightPageLink()

                notifyMessages(originType, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_ITEM_CHANGE, originIndex)
                notifyMessages(destinyType, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_ITEM_CHANGE, destinyIndex)
            }
            (originType == Pages.NOT_LINKED || destinyType == Pages.NOT_LINKED) -> {
                if (originType == Pages.NOT_LINKED && destinyType == Pages.NOT_LINKED)
                    return
                else if (originType == Pages.NOT_LINKED) {
                    val originIndex = mPagesNotLinked.value!!.indexOf(origin)
                    val destinyIndex = mPagesLink.value!!.indexOf(destiny)

                    destiny.addRightFromLeftPageLink(origin)
                    mPagesNotLinked.value!!.remove(origin)

                    notifyMessages(originType, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_REMOVED, originIndex)
                    notifyMessages(destinyType, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_ITEM_CHANGE, destinyIndex)
                } else if (destinyType == Pages.NOT_LINKED) {
                    val originIndex = mPagesLink.value!!.indexOf(destiny)
                    origin.clearRightPageLink()
                    notifyMessages(originType, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_ITEM_CHANGE, originIndex)
                }
            }
            else -> {
                var originIndex = mPagesLink.value!!.indexOf(origin)
                var destinyIndex = mPagesLink.value!!.indexOf(destiny)

                when {
                    originType != Pages.DUAL_PAGE && destinyType == Pages.LINKED -> destiny.addLeftPageLink(origin)
                    originType != Pages.DUAL_PAGE && destinyType != Pages.LINKED -> destiny.addRightFromLeftPageLink(origin)
                    originType == Pages.DUAL_PAGE && destinyType == Pages.LINKED -> destiny.addLeftPageLink(origin)
                    originType == Pages.DUAL_PAGE && destinyType != Pages.LINKED -> destiny.addRightFromLeftPageLink(origin)
                }

                notifyMessages(destinyType, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_ITEM_CHANGE, destinyIndex)

                val moved = when (originType) {
                    Pages.LINKED -> origin.clearLeftPageLink(true)
                    Pages.DUAL_PAGE -> {
                        origin.clearRightPageLink()
                        false
                    }
                    else -> false
                }

                notifyMessages(originType, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_ITEM_CHANGE, originIndex)

                if (originIndex > destinyIndex && originType != Pages.DUAL_PAGE && !moved) {
                    originIndex += 1
                    destinyIndex += 1

                    if (originIndex >= mPagesLink.value!!.size || destinyIndex >= mPagesLink.value!!.size)
                        return

                    val nextOrigin = mPagesLink.value!![originIndex]
                    val nextDestiny = mPagesLink.value!![destinyIndex]

                    if (nextOrigin.fileLinkLeftPage != 1)
                        onMove(nextOrigin, nextDestiny)
                }
            }
        }
    }

    fun onMove(origin: PageLink?, destiny: PageLink?) {
        if (origin == null || destiny == null) {
            notifyMessages(Pages.ALL, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_ITEM_CHANGE)
            return
        }
        if (origin == destiny) return
        val originIndex = mPagesLink.value!!.indexOf(origin)
        val destinyIndex = mPagesLink.value!!.indexOf(destiny)
        var differ = destinyIndex - originIndex

        if (originIndex > destinyIndex) {
            var limit = mPagesLink.value!!.size - 1
            var index = mPagesLink.value!!.indexOf(mPagesLink.value!!.findLast { it.imageLeftFileLinkPage != null })
            if (index < 0)
                index = mPagesLink.value!!.size - 1

            for (i in index downTo originIndex)
                if (mPagesLink.value!![i].fileLinkLeftPage == PageLinkConsts.VALUES.PAGE_EMPTY)
                    limit = i

            for (i in destinyIndex until originIndex)
                addNotLinked(mPagesLink.value!![i])

            differ *= -1
            for (i in destinyIndex until limit) {
                when {
                    i == destinyIndex -> mPagesLink.value!![i].addLeftPageLink(origin)
                    (i + differ) > (limit) -> continue
                    else -> {
                        mPagesLink.value!![i].addLeftPageLink(mPagesLink.value!![i + differ])
                        mPagesLink.value!![i + differ].clearLeftPageLink()
                    }
                }
                notifyMessages(Pages.LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_ITEM_CHANGE, i)
            }

            for (i in destinyIndex until limit)
                if (mPagesLink.value!![i].isDualImage && mPagesLink.value!![i].fileLinkLeftPage == PageLinkConsts.VALUES.PAGE_EMPTY &&
                    mPagesLink.value!![i].fileLinkRightPage != PageLinkConsts.VALUES.PAGE_EMPTY
                ) {
                    mPagesLink.value!![i].movePageLinkRightToLeft()
                    notifyMessages(Pages.LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_ITEM_CHANGE, i)
                }
        } else {
            var limit = mPagesLink.value!!.size - 1
            var spacesFree = 0

            for (i in originIndex until limit)
                if (mPagesLink.value!![i].fileLinkLeftPage == PageLinkConsts.VALUES.PAGE_EMPTY)
                    spacesFree++

            if (differ > spacesFree) {
                for (i in limit downTo limit - differ)
                    addNotLinked(mPagesLink.value!![i])

                for (i in limit downTo originIndex) {
                    when {
                        i < destinyIndex -> mPagesLink.value!![i].clearLeftPageLink()
                        else -> mPagesLink.value!![i].addLeftPageLink(mPagesLink.value!![i - differ])
                    }
                    notifyMessages(Pages.LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_ITEM_CHANGE, i)
                }
            } else {
                var spaceUsed = 0
                for (i in originIndex until limit) {
                    if (mPagesLink.value!![i].fileLinkLeftPage == PageLinkConsts.VALUES.PAGE_EMPTY) {
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
                        mPagesLink.value!![i].clearLeftPageLink(true)
                    else {
                        index = i - (1 + spaceUsed)
                        if (mPagesLink.value!![index].fileLinkLeftPage == PageLinkConsts.VALUES.PAGE_EMPTY) {
                            do {
                                spaceUsed++
                                index = i - (1 + spaceUsed)
                            } while (mPagesLink.value!![index].fileLinkLeftPage == PageLinkConsts.VALUES.PAGE_EMPTY)
                        }
                        mPagesLink.value!![i].addLeftPageLink(mPagesLink.value!![index])
                    }
                    notifyMessages(Pages.LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_ITEM_CHANGE, i)
                }
            }
        }
    }

    fun onNotLinked(origin: PageLink?) {
        if (origin == null) {
            notifyMessages(Pages.LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_ITEM_CHANGE)
            return
        }

        mPagesNotLinked.value!!.add(
            PageLink(
                origin.idFile, true, origin.fileLinkLeftPage, origin.fileLinkLeftPages, origin.fileLinkLeftPageName,
                origin.fileLinkLeftPagePath, origin.isFileLeftDualPage, origin.imageLeftFileLinkPage
            )
        )

        notifyMessages(Pages.NOT_LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_ADDED, mPagesNotLinked.value!!.size - 1)

        val originIndex = mPagesLink.value!!.indexOf(origin)
        if (origin.isDualImage)
            origin.movePageLinkRightToLeft()
        else
            origin.clearPageLink()

        notifyMessages(Pages.LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_ITEM_CHANGE, originIndex)
    }

    fun fromNotLinked(origin: PageLink?, destiny: PageLink?) {
        if (origin == null || destiny == null) {
            notifyMessages(Pages.NOT_LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_ITEM_CHANGE)
            return
        }

        val destinyIndex = mPagesLink.value!!.indexOf(destiny)
        val size = mPagesLink.value!!.size - 1
        mPagesNotLinked.value!!.remove(origin)
        notifyMessages(Pages.NOT_LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_REMOVED, size)

        if (destiny.imageLeftFileLinkPage == null) {
            mPagesLink.value!![destinyIndex].addLeftPageLink(origin)
            notifyMessages(Pages.LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_ITEM_CHANGE, destinyIndex)
        } else {
            addNotLinked(mPagesLink.value!![size])

            for (i in size downTo destinyIndex) {
                when (i) {
                    destinyIndex -> mPagesLink.value!![i].addLeftPageLink(origin)
                    else -> mPagesLink.value!![i].addLeftPageLink(mPagesLink.value!![i - 1])
                }
                notifyMessages(Pages.LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_ITEM_CHANGE, i)
            }
        }
    }

    fun reorderDoublePages(isUseDualPageCalculate: Boolean = false) {
        if (mFileLink.value == null || mFileLink.value!!.path.isEmpty()) return

        if (mFileLink.value != null) {
            notifyMessages(Pages.LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_REORDER_DOUBLE_PAGES_START)

            val indexChanges = mutableSetOf<Int>()
            val pagesLink = mPagesLink.value!!
            var padding = 1

            for ((index, page) in pagesLink.withIndex()) {
                if (page.isDualImage || (isUseDualPageCalculate && page.isFileLeftDualPage))
                    continue

                if ((index + padding) >= pagesLink.size)
                    break

                var next = pagesLink[index + padding]
                if (next.fileLinkLeftPage == PageLinkConsts.VALUES.PAGE_EMPTY) {
                    do {
                        padding++
                        if ((index + padding) >= pagesLink.size)
                            break
                        next = pagesLink[index + padding]
                    } while (next.fileLinkLeftPage == PageLinkConsts.VALUES.PAGE_EMPTY)
                }

                if ((index + padding) >= pagesLink.size)
                    break

                if (next.isDualImage || (isUseDualPageCalculate && page.isFileLeftDualPage)) {
                    if (page.fileLinkLeftPage != PageLinkConsts.VALUES.PAGE_EMPTY)
                        continue
                    else
                        page.merge(next)
                    next.clearPageLink()
                    indexChanges.addAll(arrayOf(index, index + padding))
                } else {
                    if (isUseDualPageCalculate) {
                        if (page.fileLinkLeftPage == PageLinkConsts.VALUES.PAGE_EMPTY) {
                            page.addLeftPageLink(next)
                            next.clearLeftPageLink()
                            indexChanges.addAll(arrayOf(index, index + padding))

                            if (!page.isFileLeftDualPage) {
                                if (next.fileLinkRightPage != PageLinkConsts.VALUES.PAGE_EMPTY) {
                                    page.addRightPageLink(next)
                                    next.clearRightPageLink()
                                } else {
                                    padding++
                                    if ((index + padding) >= pagesLink.size) {
                                        padding--
                                        continue
                                    }

                                    next = pagesLink[index + padding]
                                    if (next.fileLinkLeftPage != PageLinkConsts.VALUES.PAGE_EMPTY && !next.isDualImage && !next.isFileLeftDualPage) {
                                        page.addRightFromLeftPageLink(next)
                                        next.clearLeftPageLink()
                                        indexChanges.add(index + padding)
                                    } else
                                        padding--
                                }
                            }
                        } else {
                            if (next.isFileLeftDualPage)
                                continue
                            else {
                                page.addRightFromLeftPageLink(next)
                                next.clearLeftPageLink()
                                indexChanges.addAll(arrayOf(index, index + padding))
                            }
                        }

                    } else {
                        if (page.fileLinkLeftPage == PageLinkConsts.VALUES.PAGE_EMPTY) {
                            page.addLeftPageLink(next)
                            next.clearLeftPageLink()
                            indexChanges.addAll(arrayOf(index, index + padding))

                            if (next.fileLinkRightPage != PageLinkConsts.VALUES.PAGE_EMPTY) {
                                page.addRightPageLink(next)
                                next.clearRightPageLink()
                            } else {
                                padding++
                                if ((index + padding) >= pagesLink.size) {
                                    padding--
                                    continue
                                }

                                next = pagesLink[index + padding]
                                if (!next.isDualImage && next.fileLinkLeftPage != PageLinkConsts.VALUES.PAGE_EMPTY) {
                                    page.addRightFromLeftPageLink(next)
                                    next.clearLeftPageLink()
                                    indexChanges.add(index + padding)
                                } else
                                    padding--
                            }
                        } else {
                            page.addRightFromLeftPageLink(next)
                            next.clearLeftPageLink()
                            indexChanges.addAll(arrayOf(index, index + padding))
                        }
                    }
                }
            }

            if (mPagesNotLinked.value!!.isNotEmpty()) {
                val pagesNotLinked = mPagesNotLinked.value!!.sortedBy { it.fileLinkLeftPage }.toMutableList()

                for (page in pagesLink) {
                    if (pagesNotLinked.isEmpty())
                        break

                    if (page.isDualImage || (isUseDualPageCalculate && page.isFileRightDualPage))
                        continue

                    if (page.fileLinkLeftPage != PageLinkConsts.VALUES.PAGE_EMPTY)
                        page.addRightFromLeftPageLink(pagesNotLinked.removeAt(0))
                    else if (isUseDualPageCalculate) {
                        val notLinked = pagesNotLinked.removeAt(0)
                        page.addLeftPageLink(notLinked)

                        if (notLinked.isFileLeftDualPage)
                            continue

                        if (pagesNotLinked.isEmpty())
                            break
                        page.addRightFromLeftPageLink(pagesNotLinked.removeAt(0))
                    } else {
                        page.addLeftPageLink(pagesNotLinked.removeAt(0))
                        if (pagesNotLinked.isEmpty())
                            break

                        page.addRightFromLeftPageLink(pagesNotLinked.removeAt(0))
                    }
                }

                mPagesNotLinked.value!!.clear()
                if (pagesNotLinked.isNotEmpty())
                    mPagesNotLinked.value!!.addAll(pagesNotLinked)

                notifyMessages(Pages.NOT_LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_ITEM_CHANGE)
            }

            for (index in indexChanges)
                notifyMessages(Pages.LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_ITEM_CHANGE, index)

            notifyMessages(Pages.LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_REORDER_DOUBLE_PAGES_FINISHED)
        }
    }

    fun reorderSimplePages(isNotify: Boolean = true) {
        if (mFileLink.value == null || mFileLink.value!!.path.isEmpty()) return

        val hasDualImage = mPagesLink.value?.any { it.isDualImage } ?: false

        if (hasDualImage) {
            if (isNotify)
                notifyMessages(Pages.LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_REORDER_SIMPLE_PAGES_START)

            val indexChanges = mutableSetOf<Int>()
            val pagesLink = mPagesLink.value!!
            var amount = 0
            pagesLink.forEach { if (it.isDualImage) amount += 1 }
            var amountNotLink = (amount * 2) - pagesLink.size

            if (amountNotLink > 0) {
                for (i in (pagesLink.size - 1) downTo 0) {
                    val item = pagesLink[i]
                    if (item.fileLinkLeftPage == PageLinkConsts.VALUES.PAGE_EMPTY)
                        continue

                    val page = pagesLink[i]
                    if (item.isDualImage) {
                        addNotLinked(page)
                        amountNotLink -= 2
                        page.clearPageLink()
                    } else if (item.fileLinkLeftPage != PageLinkConsts.VALUES.PAGE_EMPTY) {
                        addNotLinked(page)
                        amountNotLink--
                        page.clearLeftPageLink()
                    }

                    if (amountNotLink < 1)
                        break
                }

                mPagesNotLinked.value!!.sortBy { it.fileLinkLeftPage }

                if (isNotify)
                    notifyMessages(Pages.NOT_LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_ITEM_CHANGE)
            }

            var padding = 0
            for (i in (pagesLink.size - 1) downTo 0) {
                if (pagesLink[i].fileLinkLeftPage != PageLinkConsts.VALUES.PAGE_EMPTY) {
                    padding = i + 1
                    break
                }
            }

            val pagesLinkTemp = mutableListOf<PageLink>()

            for (i in pagesLink.size - 1 downTo 0) {
                val newPage = PageLink(pagesLink[i])
                pagesLinkTemp.add(newPage)

                if (i - padding >= 0) {
                    val page = pagesLink[i - padding]

                    if (page.isDualImage) {
                        newPage.addLeftFromRightPageLink(page)
                        page.clearRightPageLink()
                        padding--
                    } else
                        newPage.addLeftPageLink(page)
                }

                indexChanges.addAll(arrayOf(i, i - padding))
            }

            pagesLinkTemp.sortBy { it.mangaPage }
            mPagesLink.value = ArrayList(pagesLinkTemp)

            if (isNotify) {
                for (index in indexChanges)
                    notifyMessages(Pages.LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_ITEM_CHANGE, index)

                notifyMessages(Pages.LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_REORDER_SIMPLE_PAGES_FINISHED)
            }
        }
    }

    fun autoReorderDoublePages(type: Pages, isClear: Boolean = false, isNotify: Boolean = true) {
        if (mFileLink.value == null || mFileLink.value!!.path.isEmpty()) return

        if (isNotify)
            notifyMessages(type, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_REORDER_AUTO_PAGES_START)

        val hasDualImage = if (isClear) {
            reorderSimplePages(false)
            false
        } else
            mPagesLink.value?.any { it.isDualImage } ?: false

        if (!hasDualImage && (mFileLink.value?.id == null || isClear)) {
            val indexChanges = mutableSetOf<Int>()
            val pagesLink = mPagesLink.value!!
            val lastIndex = pagesLink.size - 1
            for ((index, page) in pagesLink.withIndex()) {
                if (page.fileLinkLeftPage == PageLinkConsts.VALUES.PAGE_EMPTY || index >= lastIndex)
                    continue

                if (page.isMangaDualPage && page.isFileLeftDualPage)
                    continue

                if (page.isMangaDualPage) {
                    val nextPage = pagesLink[index + 1]

                    if (nextPage.fileLinkLeftPage == PageLinkConsts.VALUES.PAGE_EMPTY || nextPage.isFileLeftDualPage)
                        continue

                    indexChanges.addAll(arrayOf(index, index + 1))
                    page.addRightFromLeftPageLink(nextPage)
                    nextPage.clearPageLink()

                    for ((idxNext, next) in pagesLink.withIndex()) {
                        if (idxNext < (index + 1) || idxNext >= lastIndex)
                            continue

                        val aux = pagesLink[idxNext + 1]
                        next.addLeftPageLink(aux)
                        aux.clearLeftPageLink()
                        indexChanges.addAll(arrayOf(idxNext, idxNext + 1))
                    }
                } else if (page.isFileLeftDualPage) {
                    if (pagesLink[index + 1].fileLinkLeftPage == PageLinkConsts.VALUES.PAGE_EMPTY)
                        continue

                    indexChanges.addAll(arrayOf(index, index + 1))
                    var indexEmpty = lastIndex
                    for (i in (index + 1) until lastIndex) {
                        if (pagesLink[i].fileLinkLeftPage == PageLinkConsts.VALUES.PAGE_EMPTY) {
                            indexEmpty = i
                            break
                        }
                    }

                    addNotLinked(pagesLink[indexEmpty])

                    for (i in indexEmpty downTo (index + 2)) {
                        val aux = pagesLink[i - 1]
                        pagesLink[i].addLeftPageLink(aux)
                        aux.clearLeftPageLink()
                        indexChanges.addAll(arrayOf(i, i - 1))
                    }
                }
            }

            if (isNotify)
                for (index in indexChanges)
                    notifyMessages(type, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_ITEM_CHANGE, index)
        }

        if (isNotify)
            notifyMessages(type, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_REORDER_AUTO_PAGES_FINISHED)
    }

    fun reorderBySortPages() {
        if (mFileLink.value == null || mFileLink.value!!.path.isEmpty()) return

        notifyMessages(Pages.LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_REORDER_SORTED_PAGES_START)

        val pagesNotLink = mPagesNotLinked.value!!
        val pagesLink = mPagesLink.value!!
        val pagesLinkTemp = mutableListOf<PageLink>()
        val pagesNotLinkTemp = arrayListOf<PageLink>()
        var maxNumPage = 0

        for (page in pagesLink) {
            if (page.fileLinkLeftPage > maxNumPage)
                maxNumPage = page.fileLinkLeftPage

            if (page.fileLinkRightPage > maxNumPage)
                maxNumPage = page.fileLinkRightPage
        }

        for (page in pagesNotLink) {
            if (page.fileLinkLeftPage > maxNumPage)
                maxNumPage = page.fileLinkLeftPage
        }

        for (page in pagesLink) {
            if (page.mangaPage == PageLinkConsts.VALUES.PAGE_EMPTY)
                continue

            if (page.mangaPage > maxNumPage)
                break
            else {
                val pageLink = PageLink(page)
                pagesLinkTemp.add(pageLink)

                val findPageLink = pagesLink.find { it.fileLinkLeftPage == page.mangaPage || it.fileLinkRightPage == page.mangaPage }
                    ?: pagesNotLink.find { it.fileLinkLeftPage == page.mangaPage }

                if (findPageLink != null) {
                    if (pageLink.fileLinkRightPage == page.mangaPage)
                        pageLink.addLeftFromRightPageLink(findPageLink)
                    else
                        pageLink.addLeftPageLink(findPageLink)
                }
            }
        }

        if (maxNumPage >= pagesLinkTemp.size) {
            for (numPage in pagesLinkTemp.size until maxNumPage) {
                val pageLink = pagesLink.find { it.fileLinkLeftPage == numPage || it.fileLinkRightPage == numPage }
                    ?: pagesNotLink.find { it.fileLinkLeftPage == numPage }

                if (pageLink != null) {
                    if (pageLink.fileLinkLeftPage == numPage)
                        pagesNotLinkTemp.add(
                            PageLink(
                                pageLink.idFile, true, pageLink.fileLinkLeftPage, pageLink.fileLinkLeftPages, pageLink.fileLinkLeftPageName,
                                pageLink.fileLinkLeftPagePath, pageLink.isFileLeftDualPage, pageLink.imageLeftFileLinkPage
                            )
                        )
                    else
                        pagesNotLinkTemp.add(
                            PageLink(
                                pageLink.idFile, true, pageLink.fileLinkRightPage, pageLink.fileLinkLeftPages, pageLink.fileLinkRightPageName,
                                pageLink.fileLinkRightPagePath, pageLink.isFileRightDualPage, pageLink.imageRightFileLinkPage
                            )
                        )
                }
            }
        }

        pagesLink.clear()
        pagesNotLink.clear()
        pagesLinkTemp.sortBy { it.mangaPage }
        pagesNotLinkTemp.sortBy { it.fileLinkLeftPage }

        mPagesLink.value = ArrayList(pagesLinkTemp)
        mPagesNotLinked.value = pagesNotLinkTemp

        notifyMessages(Pages.LINKED, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_REORDER_SORTED_PAGES_FINISHED)
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

    fun imageThreadLoadingProgress(): Int =
        mGenerateImageThread.size

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

    fun getProgress(): Pair<Int, Int> {
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

    private fun isAllImagesLoaded(type: Pages = Pages.ALL): Boolean {
        val isFileLink = mFileLink.value!!.path != ""
        if (type != Pages.NOT_LINKED)
            for (page in mPagesLink.value!!) {
                if ((type == Pages.ALL || type == Pages.MANGA) && (page.imageMangaPage == null))
                    return false
                if (isFileLink && (type == Pages.ALL || type == Pages.LINKED)) {
                    if ((page.fileLinkLeftPage != PageLinkConsts.VALUES.PAGE_EMPTY && page.imageLeftFileLinkPage == null)
                        || (page.isDualImage && page.imageRightFileLinkPage == null)
                    )
                        return false
                }
            }

        if (isFileLink && (type == Pages.NOT_LINKED || type == Pages.ALL))
            for (page in mPagesNotLinked.value!!) {
                if (page.fileLinkLeftPage != PageLinkConsts.VALUES.PAGE_EMPTY && page.imageLeftFileLinkPage == null)
                    return false
            }

        notifyMessages(type, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_ALL_IMAGES_LOADED)
        return true
    }

    private var mLoadVerify: Int = 0
    private var mLoadError: Int = 0
    fun reLoadImages(type: Pages = Pages.ALL, isVerifyImages: Boolean = false, isForced: Boolean = false, isCloseThreads: Boolean = false) {
        mLoadVerify += 1
        if (!isForced && (mLoadError > 3 || (isVerifyImages && mLoadVerify > 3))) {
            if (mLoadError > 3)
                notifyMessages(type, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_LOAD_ERROR_ENABLE_MANUAL)
            return
        }

        if (!isForced && isVerifyImages && isAllImagesLoaded(type)) return

        if (isCloseThreads) endThread()

        Util.destroyParse(mFileLink.value?.parseManga, false)
        Util.destroyParse(mFileLink.value?.parseFileLink, false)

        val parseManga = getParse(mManga!!.file, Pages.MANGA)
        val parseFileLink = getParse(mFileLink.value!!.file, Pages.LINKED)

        mFileLink.value!!.parseManga = parseManga
        mFileLink.value!!.parseFileLink = parseFileLink

        if (type != Pages.NOT_LINKED)
            getImage(parseManga, parseFileLink, mPagesLink.value!!, type, true)

        if ((type == Pages.NOT_LINKED || type == Pages.ALL) && mPagesNotLinked.value!!.isNotEmpty())
            getImage(null, parseFileLink, mPagesNotLinked.value!!, Pages.NOT_LINKED, true)
    }

    private fun getImage(parseManga: Parse?, parsePageLink: Parse?, list: ArrayList<PageLink>, type: Pages, reload: Boolean = false) {
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
                val image = BitmapFactory.decodeStream(stream)
                val isDualPage = (image.width / image.height) > 0.9
                val bitmap = Bitmap.createScaledBitmap(
                    image,
                    ReaderConsts.PAGESLINK.IMAGES_WIDTH,
                    ReaderConsts.PAGESLINK.IMAGES_HEIGHT,
                    false
                )
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
    private inner class ImageLoadRunnable(
        private var parseManga: Parse?, private var parsePageLink: Parse?, private var list: ArrayList<PageLink>,
        private var type: Pages, private var reload: Boolean = false, private var callEnded: () -> (Unit)
    ) : Runnable {
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
                                if ((page.isDualImage) && (page.imageMangaPage != null && page.imageLeftFileLinkPage != null && page.imageRightFileLinkPage != null))
                                    continue
                                else if (page.imageMangaPage != null && page.imageLeftFileLinkPage != null)
                                    continue
                            }
                            Pages.MANGA -> if (page.imageMangaPage != null) continue
                            Pages.NOT_LINKED -> if (page.imageLeftFileLinkPage != null) continue
                            Pages.LINKED -> {
                                if (page.isDualImage && (page.imageLeftFileLinkPage != null && page.imageRightFileLinkPage != null))
                                    continue
                                else if (page.imageLeftFileLinkPage != null)
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
                                val (isDualPage, image) = generateBitmap(parsePageLink!!, page.fileLinkLeftPage)
                                page.imageLeftFileLinkPage = image
                                page.isFileLeftDualPage = isDualPage

                                if (page.isDualImage)
                                    generateBitmap(
                                        parsePageLink!!,
                                        page.fileLinkRightPage
                                    ) { IsDualPage, Image -> page.imageRightFileLinkPage = Image; page.isFileRightDualPage = IsDualPage }
                            }
                        }
                        Pages.MANGA -> generateBitmap(
                            parseManga!!,
                            page.mangaPage
                        ) { isDualPage, image -> page.imageMangaPage = image; page.isMangaDualPage = isDualPage }
                        Pages.NOT_LINKED -> generateBitmap(
                            parsePageLink!!,
                            page.fileLinkLeftPage
                        ) { isDualPage, image -> page.imageLeftFileLinkPage = image; page.isFileLeftDualPage = isDualPage }
                        Pages.LINKED -> {
                            val (isDualPage, image) = generateBitmap(parsePageLink!!, page.fileLinkLeftPage)
                            page.imageLeftFileLinkPage = image
                            page.isFileLeftDualPage = isDualPage

                            if (page.isDualImage)
                                generateBitmap(
                                    parsePageLink!!,
                                    page.fileLinkRightPage
                                ) { IsDualPage, Image -> page.imageRightFileLinkPage = Image; page.isFileRightDualPage = IsDualPage }
                        }
                        else -> {}
                    }

                    if (forceEnd)
                        break
                    else
                        notifyMessages(type, PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_UPDATED, index)
                }
            } catch (e: Exception) {
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
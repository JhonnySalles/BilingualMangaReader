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
import mu.KotlinLogging
import java.io.File
import java.io.InputStream
import java.io.InterruptedIOException
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList


class PagesLinkViewModel(application: Application) : AndroidViewModel(application) {

    private val mLOGGER = KotlinLogging.logger {}
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
        obj.lastAccess = Date()
        if (obj.id == 0L)
            obj.id = mFileLinkRepository.save(obj)
        else
            mFileLinkRepository.update(obj)

        return obj
    }

    fun save() {
        val obj = this.get()
        obj.lastAccess = Date()
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

                if (!isReload && find(file.nameWithoutExtension, parse.numPages(), refresh)) return loaded

                endThread(true)
                mFileLink.value = FileLink(mManga!!, mFileLink.value!!.parseManga, parse.numPages(), path,
                    file.nameWithoutExtension, file.extension, file.parent)
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
                            listNotLink.add(PageLink( mFileLink.value!!.id, PageLinkConsts.VALUES.PAGE_EMPTY, 0, "",
                                i, parse.numPages(), name, true ))
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
        mFileLink.value?.path = ""
        setLanguage(isClear = true)
        mPagesNotLinked.value?.clear()
        mPagesLink.value?.forEach { page ->  page.clearFileLink() }
        refresh(null, Pages.ALL)
    }

    private fun generateBitmap(parse: Parse, index: Int): Bitmap? {
        return if (index == -1) null else
            try {
                val stream: InputStream = parse.getPage(index)
                val image  = BitmapFactory.decodeStream(stream)
                val bitmap = Bitmap.createScaledBitmap(image, ReaderConsts.PAGESLINK.IMAGES_WIDTH, ReaderConsts.PAGESLINK.IMAGES_HEIGHT, false)
                Util.closeInputStream(stream)
                bitmap
            } catch (i: InterruptedIOException) {
                mLOGGER.error { "Interrupted error when generate bitmap: " + i.message }
                null
            } catch (e: Exception) {
                mLOGGER.error { "Error when generate bitmap: " + e.message }
                null
            }
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

    fun getPageNotLinkLastIndex(): Int {
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
                    true, null, page.imageLeftFileLinkPage
                )
            )
            notifyImageLoadAdded(getPageNotLinkLastIndex(), Pages.NOT_LINKED)
        }

        if (page.dualImage) {
            mPagesNotLinked.value!!.add(
                PageLink(
                    page.idFile, PageLinkConsts.VALUES.PAGE_EMPTY, 0, "",
                    page.fileRightLinkPage, page.fileLinkPages, page.fileRightLinkPageName,
                    true, null, page.imageRightFileLinkPage
                )
            )
            page.clearRightFileLink()
            notifyImageLoadAdded(getPageNotLinkLastIndex(), Pages.NOT_LINKED)
        }
    }

    fun onMoveDualPage(originType: Pages, origin: PageLink?, destinyType: Pages, destiny: PageLink?) {
        if (origin == null || destiny == null) {
            notifyImageLoad(null, Pages.ALL)
            return
        }

        if (origin == destiny && destinyType == Pages.DUAL_PAGE) {
            notifyImageLoad(mPagesLink.value!!.indexOf(destiny), originType)
            return
        }

        if (destinyType != Pages.LINKED && destiny.dualImage) {
            mPagesNotLinked.value!!.add(
                PageLink(
                    destiny.idFile, PageLinkConsts.VALUES.PAGE_EMPTY, 0, "", destiny.fileRightLinkPage,
                    destiny.fileLinkPages, destiny.fileRightLinkPageName, true, null, destiny.imageRightFileLinkPage
                )
            )
            notifyImageLoadAdded(getPageNotLinkLastIndex(), Pages.NOT_LINKED)
        } else if (destinyType == Pages.LINKED && destiny.fileLinkPage != PageLinkConsts.VALUES.PAGE_EMPTY) {
            mPagesNotLinked.value!!.add(
                PageLink(
                    destiny.idFile, PageLinkConsts.VALUES.PAGE_EMPTY, 0, "", destiny.fileLinkPage,
                    destiny.fileLinkPages, destiny.fileLinkPageName, true, null, destiny.imageLeftFileLinkPage
                )
            )
            notifyImageLoadAdded(getPageNotLinkLastIndex(), Pages.NOT_LINKED)
        }

        when {
            (originType == Pages.DUAL_PAGE && destinyType == Pages.DUAL_PAGE) -> {
                val originIndex = mPagesLink.value!!.indexOf(origin)
                val destinyIndex = mPagesLink.value!!.indexOf(destiny)

                destiny.addRightFileLinkImage(origin.fileRightLinkPage, origin.fileRightLinkPageName, origin.imageRightFileLinkPage)
                origin.clearRightFileLink()

                notifyImageLoad(originIndex, originType)
                notifyImageLoad(destinyIndex, destinyType)
            }
            (originType == Pages.NOT_LINKED || destinyType == Pages.NOT_LINKED) ->  {
                if (originType == Pages.NOT_LINKED && destinyType == Pages.NOT_LINKED)
                    return
                else if (originType == Pages.NOT_LINKED) {
                    val originIndex = mPagesNotLinked.value!!.indexOf(origin)
                    val destinyIndex = mPagesLink.value!!.indexOf(destiny)

                    destiny.addRightFileLinkImage(origin.fileLinkPage, origin.fileLinkPageName, origin.imageLeftFileLinkPage)
                    mPagesNotLinked.value!!.remove(origin)

                    notifyImageLoadRemoved(originIndex, originType)
                    notifyImageLoad(destinyIndex, destinyType)
                } else if (destinyType == Pages.NOT_LINKED) {
                    val originIndex = mPagesLink.value!!.indexOf(destiny)
                    origin.clearRightFileLink()
                    notifyImageLoad(originIndex, originType)
                }
            }
            else -> {
                var originIndex = mPagesLink.value!!.indexOf(origin)
                var destinyIndex = mPagesLink.value!!.indexOf(destiny)

                when {
                    originType != Pages.DUAL_PAGE && destinyType == Pages.LINKED -> destiny.addLeftFileLinkImage(origin.fileLinkPage, origin.fileLinkPages, origin.fileLinkPageName, origin.imageLeftFileLinkPage)
                    originType != Pages.DUAL_PAGE && destinyType != Pages.LINKED -> destiny.addRightFileLinkImage(origin.fileLinkPage, origin.fileLinkPageName, origin.imageLeftFileLinkPage)
                    originType == Pages.DUAL_PAGE && destinyType == Pages.LINKED -> destiny.addLeftFileLinkImage(origin.fileRightLinkPage, origin.fileLinkPages, origin.fileRightLinkPageName, origin.imageRightFileLinkPage)
                    originType == Pages.DUAL_PAGE && destinyType != Pages.LINKED -> destiny.addRightFileLinkImage(origin.fileRightLinkPage, origin.fileRightLinkPageName, origin.imageRightFileLinkPage)
                }

                notifyImageLoad(destinyIndex, destinyType)

                val moved = when (originType) {
                    Pages.LINKED -> origin.clearLeftFileLink(true)
                    Pages.DUAL_PAGE -> {
                        origin.clearRightFileLink()
                        false
                    }
                    else -> false
                }

                notifyImageLoad(originIndex, originType)

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
            notifyImageLoad(null, Pages.ALL)
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
                    i == destinyIndex -> mPagesLink.value!![i].addLeftFileLinkImage(origin.fileLinkPage, origin.fileLinkPages, origin.fileLinkPageName, origin.imageLeftFileLinkPage)
                    (i + differ) > (limit) -> { continue }
                    else -> {
                        mPagesLink.value!![i].addLeftFileLinkImage(mPagesLink.value!![i + differ].fileLinkPage,
                            mPagesLink.value!![i + differ].fileLinkPages,
                            mPagesLink.value!![i + differ].fileLinkPageName,
                            mPagesLink.value!![i + differ].imageLeftFileLinkPage)
                        mPagesLink.value!![i + differ].clearLeftFileLink()
                    }
                }
                notifyImageLoad(i, Pages.LINKED)
            }

            for (i in destinyIndex until limit)
                if (mPagesLink.value!![i].dualImage && mPagesLink.value!![i].fileLinkPage == PageLinkConsts.VALUES.PAGE_EMPTY &&
                    mPagesLink.value!![i].fileRightLinkPage != PageLinkConsts.VALUES.PAGE_EMPTY) {
                    mPagesLink.value!![i].moveFileLinkRightToLeft()
                    notifyImageLoad(i, Pages.LINKED)
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
                            mPagesLink.value!![i - differ].imageLeftFileLinkPage)
                    }
                    notifyImageLoad(i, Pages.LINKED)
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
                            mPagesLink.value!![index].imageLeftFileLinkPage)
                    }
                    notifyImageLoad(i, Pages.LINKED)
                }
            }
        }
    }

    fun onNotLinked(origin : PageLink?) {
        if (origin == null) {
            notifyImageLoad(null, Pages.LINKED)
            return
        }

        mPagesNotLinked.value!!.add(PageLink(origin.idFile, PageLinkConsts.VALUES.PAGE_EMPTY, 0, "", origin.fileLinkPage,
            origin.fileLinkPages, origin.fileLinkPageName, true, null, origin.imageLeftFileLinkPage))

        notifyImageLoadAdded(getPageNotLinkLastIndex(), Pages.NOT_LINKED)
        val originIndex = mPagesLink.value!!.indexOf(origin)

        if (origin.dualImage)
            origin.moveFileLinkRightToLeft()
        else
            origin.clearFileLink()

        notifyImageLoad(originIndex, Pages.LINKED)
    }

    fun fromNotLinked(origin : PageLink?, destiny :PageLink?) {
        if (origin == null || destiny == null) {
            notifyImageLoad(null, Pages.NOT_LINKED)
            return
        }

        val destinyIndex = mPagesLink.value!!.indexOf(destiny)
        val size = mPagesLink.value!!.size-1
        mPagesNotLinked.value!!.remove(origin)
        notifyImageLoadRemoved(size, Pages.NOT_LINKED)

        if (destiny.imageLeftFileLinkPage == null) {
            mPagesLink.value!![destinyIndex].addLeftFileLinkImage(
                origin.fileLinkPage,
                origin.fileLinkPages,
                origin.fileLinkPageName,
                origin.imageLeftFileLinkPage
            )
            notifyImageLoad(destinyIndex, Pages.LINKED)
        } else {
            if (mPagesLink.value!![size].imageLeftFileLinkPage != null)
                addNotLinked(mPagesLink.value!![size])

            for (i in size downTo destinyIndex) {
                when (i) {
                    destinyIndex -> mPagesLink.value!![i].addLeftFileLinkImage(origin.fileLinkPage, origin.fileLinkPages, origin.fileLinkPageName, origin.imageLeftFileLinkPage)
                    else -> mPagesLink.value!![i].addLeftFileLinkImage(mPagesLink.value!![i - 1].fileLinkPage,
                        mPagesLink.value!![i - 1].fileLinkPages,
                        mPagesLink.value!![i - 1].fileLinkPageName,
                        mPagesLink.value!![i - 1].imageLeftFileLinkPage)
                }
                notifyImageLoad(i, Pages.LINKED)
            }
        }
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
        val runnable = ImageLoadRunnable(parseManga, parsePageLink, list, type, reload)
        val thread = Thread(runnable)
        thread.priority = Process.THREAD_PRIORITY_DEFAULT + Process.THREAD_PRIORITY_LESS_FAVORABLE
        thread.start()
        mGenerateImageThread.add(ImageLoadThread(type, thread, runnable))
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

    fun imageThreadLoadingProgress(): Boolean = mGenerateImageThread.isNotEmpty()

    fun addImageLoadHandler(handler: Handler) {
        mGenerateImageHandler!!.add(handler)
    }

    fun removeImageLoadHandler(handler: Handler) {
        mGenerateImageHandler!!.remove(handler)
    }

    private fun notifyImageLoadStart(type: Pages) {
        val message = Message()
        message.obj = ImageLoad(null, type)
        message.what = PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_START
        for (h in mGenerateImageHandler!!)
            h.sendMessage(message)
    }

    private fun notifyImageLoadAdded(index: Int?, type: Pages) {
        val message = Message()
        message.obj = ImageLoad(index, type)
        message.what = PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_ADDED
        for (h in mGenerateImageHandler!!)
            h.sendMessage(message)
    }

    private fun notifyImageLoadRemoved(index: Int?, type: Pages) {
        val message = Message()
        message.obj = ImageLoad(index, type)
        message.what = PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_REMOVED
        for (h in mGenerateImageHandler!!)
            h.sendMessage(message)
    }

    private fun notifyImageLoad(index: Int?, type: Pages) {
        val message = Message()
        message.obj = ImageLoad(index, type)
        message.what = PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_UPDATED
        for (h in mGenerateImageHandler!!)
            h.sendMessage(message)
    }

    private fun notifyImageLoadFinished(type: Pages) {
        removeThread(type, true)

        val message = Message()
        message.obj = ImageLoad(null, type)
        message.what = PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_FINISHED
        for (h in mGenerateImageHandler!!)
            h.sendMessage(message)
    }

    private fun notifyErrorLoad(type: Pages) {
        removeThread(type)

        val message = Message()
        message.obj = ImageLoad(null, type)
        message.what = PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_LOAD_ERROR
        for (h in mGenerateImageHandler!!)
            h.sendMessage(message)
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

    inner class ImageLoad(var index: Int?, var type: Pages)
    private inner class ImageLoadThread(var type: Pages, var thread: Thread, var runnable: Runnable)
    private inner class ImageLoadRunnable(private var parseManga: Parse?, private var parsePageLink: Parse?, private var list: ArrayList<PageLink>, private var type: Pages, private var reload : Boolean = false) : Runnable {
        var forceEnd: Boolean = false
        var progress: Int = 0
        var size: Int = 0

        override fun run() {
            var error = false
            try {
                progress = 0
                forceEnd = false
                size = list.size
                notifyImageLoadStart(type)
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
                            if (parseManga != null)
                                page.imageMangaPage = generateBitmap(parseManga!!, page.mangaPage)

                            if (parsePageLink != null) {
                                page.imageLeftFileLinkPage = generateBitmap(parsePageLink!!, page.fileLinkPage)

                                if (page.dualImage)
                                    page.imageRightFileLinkPage = generateBitmap(parsePageLink!!, page.fileRightLinkPage)
                            }
                        }
                        Pages.MANGA -> page.imageMangaPage = generateBitmap(parseManga!!, page.mangaPage)
                        Pages.NOT_LINKED -> page.imageLeftFileLinkPage = generateBitmap(parsePageLink!!, page.fileLinkPage)
                        Pages.LINKED -> {
                            page.imageLeftFileLinkPage = generateBitmap(parsePageLink!!, page.fileLinkPage)
                            if (page.dualImage)
                                page.imageRightFileLinkPage = generateBitmap(parsePageLink!!, page.fileRightLinkPage)
                        }
                        else -> {}
                    }

                    if (forceEnd)
                        break
                    else
                        notifyImageLoad(index, type)
                }
            } catch(e: Exception) {
                error = true
                if (!forceEnd) {
                    mLoadError += 1
                    notifyErrorLoad(type)
                }
            } finally {
                progress = size
                if (!error)
                    mLoadError = 0

                if (!forceEnd)
                    notifyImageLoadFinished(type)
            }
        }
    }
}
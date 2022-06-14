package br.com.fenix.bilingualmangareader.view.ui.pages_link

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Message
import android.os.Process
import android.util.Log
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
import br.com.fenix.bilingualmangareader.service.repository.FileLinkRepository
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.util.constants.PageLinkConsts
import br.com.fenix.bilingualmangareader.util.constants.ReaderConsts
import br.com.fenix.bilingualmangareader.util.helpers.Util
import java.io.File
import java.io.InputStream
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList


class PagesLinkViewModel(application: Application) : AndroidViewModel(application) {

    private val mContext = application.applicationContext
    private val mFileLinkRepository: FileLinkRepository = FileLinkRepository(mContext)

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

    fun getFileLink(manga : Manga? = null, isBackup: Boolean = false) : FileLink? {
        return if (!isBackup && (mFileLink.value == null ||  mFileLink.value!!.path == ""))
            if (manga != null)
                mFileLinkRepository.get(manga)
            else
                null
        else this.get()
    }

    private fun reload(refresh: (index: Int?, type: Pages) -> (Unit)) : Boolean {
        val fileLink = SubTitleController.getInstance(mContext).getFileLink()?: return false
        return if (mManga == fileLink.manga) {
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

    fun reload(obj: FileLink?, refresh: (index: Int?, type: Pages) -> (Unit)) : Boolean {
        return if (obj != null) {
            mFileLink.value = obj
            mPagesLink.value = obj.pagesLink?.let { ArrayList(it) }
            mPagesNotLinked.value = obj.pagesNotLink?.let { ArrayList(it) }
            refresh(null, Pages.ALL)
            getImage(mFileLink.value!!.parseManga, mFileLink.value!!.parseFileLink, mPagesLink.value!!, Pages.ALL, true)

            if (mPagesNotLinked.value!!.isNotEmpty())
                getImage(null, mFileLink.value!!.parseFileLink, mPagesNotLinked.value!!, Pages.NOT_LINKED, true)

            true
        } else false
    }

    private fun find(refresh: (index: Int?, type: Pages) -> (Unit)) : Boolean {
        if (mManga == null) return false
        val obj = mFileLinkRepository.get(mManga!!) ?: return false
        set(obj, refresh)
        return (obj.pagesLink != null) && (obj.pagesLink!!.isNotEmpty())
    }

    fun find(name: String, pages: Int, refresh: (index: Int?, type: Pages) -> (Unit)) : Boolean {
        if (mManga == null || mManga!!.id == null) return false
        val obj = mFileLinkRepository.findByFileName(mManga!!.id!!, name, pages) ?: return false
        set(obj, refresh)
        return true
    }

    fun set(obj: FileLink, refresh: (index: Int?, type: Pages) -> (Unit)) {
        mFileLink.value = obj
        mPagesLink.value!!.clear()
        mPagesNotLinked.value!!.clear()
        setLanguage(obj.language)

        Util.destroyParse(mFileLink.value!!.parseManga)
        Util.destroyParse(mFileLink.value!!.parseFileLink)

        val mParseManga = ParseFactory.create(mManga!!.file) ?: return
        mFileLink.value!!.parseManga = mParseManga
        val mParseLink = ParseFactory.create(obj.file) ?: return
        mFileLink.value!!.parseFileLink = mParseLink

        if(obj.path.isNotEmpty())
            obj.pagesLink!!.forEach { it.isFileLinkLoading = true }

        mPagesLink.value = obj.pagesLink?.let { ArrayList(it) }
        mPagesNotLinked.value = obj.pagesNotLink?.let { ArrayList(it) }
        refresh(null, Pages.ALL)
        getImage(mParseManga, mParseLink, mPagesLink.value!!, Pages.ALL)
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
                fileLink.parseFileLink = mFileLink.value!!.parseFileLink
                fileLink.parseManga = mFileLink.value!!.parseManga

                for ((index, page) in fileLink.pagesLink!!.withIndex()) {
                    page.imageMangaPage = mPagesLink.value!![index].imageMangaPage

                    val item = mPagesLink.value!!.find { it.fileLinkPage.compareTo(page.fileLinkPage) == 0 } ?:
                        mPagesNotLinked.value!!.find { it.fileLinkPage.compareTo(page.fileLinkPage) == 0 }

                    if (item != null)
                        page.imageLeftFileLinkPage = item.imageLeftFileLinkPage
                }

                for (page in fileLink.pagesNotLink!!) {
                    val item = mPagesNotLinked.value!!.find { it.fileLinkPage.compareTo(page.fileLinkPage) == 0 } ?:
                    mPagesLink.value!!.find { it.fileLinkPage.compareTo(page.fileLinkPage) == 0 }

                    if (item != null)
                        page.imageLeftFileLinkPage = item.imageLeftFileLinkPage
                }

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
        mPagesLink.value!!.clear()
        setLanguage(isClear = true)

        if (reload(refresh)) return
        if (find(refresh)) return

        Util.destroyParse(mFileLink.value!!.parseManga)

        val parse = ParseFactory.create(manga.file) ?: return
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

            val parse = ParseFactory.create(path)
            if (parse != null) {
                loaded = LoadFile.LOADED

                if (!isReload && find(file.nameWithoutExtension, parse.numPages(), refresh)) return loaded

                Util.destroyParse(mFileLink.value!!.parseFileLink)
                mFileLink.value = FileLink(mManga!!, mFileLink.value!!.parseManga, parse.numPages(), path,
                    file.nameWithoutExtension, file.extension, file.parent)
                mFileLink.value!!.parseFileLink = parse

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
                            page.isFileLinkLoading = true
                            refresh(i, Pages.LINKED)
                        } else
                            listNotLink.add(PageLink(
                                mFileLink.value!!.id, -1, mManga!!.pages, i, mFileLink.value!!.pages,
                                mManga!!.name, mFileLink.value!!.name
                            ))
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
        mFileLink.value!!.path = ""
        setLanguage(isClear = true)
        mPagesNotLinked.value!!.clear()
        mPagesLink.value!!.forEach { page ->  page.clearFileLink() }
        refresh(null, Pages.ALL)
    }

    private fun generateBitmap(parse: Parse, index: Int): Bitmap? {
        return try {
            val stream: InputStream = parse.getPage(index)
            val image  = BitmapFactory.decodeStream(stream)
            val bitmap = Bitmap.createScaledBitmap(image, ReaderConsts.PAGESLINK.IMAGES_WIDTH, ReaderConsts.PAGESLINK.IMAGES_HEIGHT, false)
            Util.closeInputStream(stream)
            bitmap
        } catch (e: Exception) {
            Log.i(
                GeneralConsts.TAG.LOG,
                "Error, not loading image - " + e.message
            )
            null
        }
    }

    fun getPageLink(page : PageLink) : String = mPagesLink.value!!.indexOf(page).toString()

    fun getPageLink(index : Int) : PageLink = mPagesLink.value!![index]

    fun getPageNotLink(page : PageLink) : String = mPagesNotLinked.value!!.indexOf(page).toString()

    fun getPageNotLink(index : Int) : PageLink = mPagesNotLinked.value!![index]

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
        if (page.fileLinkPage != -1) {
            mPagesNotLinked.value!!.add(
                PageLink(
                    page.idFile, -1, 0, "",
                    page.fileLinkPage, page.fileLinkPages, page.fileLinkPageName,
                    true, null, page.imageLeftFileLinkPage
                )
            )
            notifyImageLoadAdded(getPageNotLinkLastIndex(), Pages.NOT_LINKED)
        }

        if (page.dualImage) {
            mPagesNotLinked.value!!.add(
                PageLink(
                    page.idFile, -1, 0, "",
                    page.fileRightLinkPage, page.fileLinkPages, page.fileRightLinkPageName,
                    true, null, page.imageRightFileLinkPage
                )
            )
            page.clearRightFileLink()
            notifyImageLoadAdded(getPageNotLinkLastIndex(), Pages.NOT_LINKED)
        }
    }

    fun onMoveDualPage(originType: Pages, origin: PageLink, destinyType: Pages, destiny: PageLink) {
        if (origin == destiny && destinyType == Pages.DUAL_PAGE) {
            notifyImageLoad(mPagesLink.value!!.indexOf(destiny), originType)
            return
        }

        if (destinyType != Pages.LINKED && destiny.dualImage) {
            mPagesNotLinked.value!!.add(
                PageLink(
                    destiny.idFile, -1, 0, "", destiny.fileRightLinkPage,
                    destiny.fileLinkPages, destiny.fileRightLinkPageName, true, null, destiny.imageRightFileLinkPage
                )
            )
            notifyImageLoadAdded(getPageNotLinkLastIndex(), Pages.NOT_LINKED)
        } else if (destinyType == Pages.LINKED && destiny.fileLinkPage != -1) {
            mPagesNotLinked.value!!.add(
                PageLink(
                    destiny.idFile, -1, 0, "", destiny.fileLinkPage,
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

    fun onMove(origin: PageLink, destiny: PageLink) {
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
                if (mPagesLink.value!![i].fileLinkPage == -1)
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
                if (mPagesLink.value!![i].dualImage && mPagesLink.value!![i].fileLinkPage == -1 && mPagesLink.value!![i].fileRightLinkPage != -1) {
                    mPagesLink.value!![i].moveFileLinkRightToLeft()
                    notifyImageLoad(i, Pages.LINKED)
                }
        } else {
            var limit = mPagesLink.value!!.size-1
            var spacesFree = 0

            for(i in originIndex until limit)
                if (mPagesLink.value!![i].fileLinkPage == -1)
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
                    if (mPagesLink.value!![i].fileLinkPage == -1) {
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
                        if (mPagesLink.value!![index].fileLinkPage == -1) {
                            do {
                                spaceUsed++
                                index = i - (1 + spaceUsed)
                            } while (mPagesLink.value!![index].fileLinkPage == -1)
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

    fun onNotLinked(origin : PageLink) {
        mPagesNotLinked.value!!.add(PageLink(origin.idFile, -1, 0, "", origin.fileLinkPage,
            origin.fileLinkPages, origin.fileLinkPageName, true, null, origin.imageLeftFileLinkPage))

        notifyImageLoadAdded(getPageNotLinkLastIndex(), Pages.NOT_LINKED)
        val originIndex = mPagesLink.value!!.indexOf(origin)

        if (origin.dualImage)
            origin.moveFileLinkRightToLeft()
        else
            origin.clearFileLink()

        notifyImageLoad(originIndex, Pages.LINKED)
    }

    fun fromNotLinked(origin : PageLink, destiny :PageLink) {
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
                if (page.imageMangaPage == null)
                    return false
                if (isFileLink && page.fileLinkPage > -1 && ((page.imageLeftFileLinkPage == null) || (page.dualImage && page.imageRightFileLinkPage == null)))
                    return false
            }

        if (isFileLink && (type == Pages.NOT_LINKED || type == Pages.ALL))
            for (page in mPagesNotLinked.value!!) {
                if (page.fileLinkPage > -1 && page.imageLeftFileLinkPage == null)
                    return false
            }

        return true
    }

    private var mLoadVerify : Int = 0
    private var mLoadError : Int = 0
    fun reLoadImages(type : Pages, isVerifyImages: Boolean = false, isForced: Boolean = false) {
        mLoadVerify += 1
        if ((!isForced) && (mLoadError > 5 || (isVerifyImages && mLoadVerify > 3))) return
        if (!isForced && isVerifyImages && isAllImagesLoaded(type)) return

        val parseManga = ParseFactory.create(mManga!!.file)
        val parseFileLink = ParseFactory.create(mFileLink.value!!.file)

        Util.destroyParse(mFileLink.value!!.parseManga)
        Util.destroyParse(mFileLink.value!!.parseFileLink)

        mFileLink.value!!.parseManga = parseManga
        mFileLink.value!!.parseFileLink = parseFileLink

        if (type != Pages.NOT_LINKED)
            getImage(parseManga, parseFileLink, mPagesLink.value!!, Pages.ALL, true)

        if ((type == Pages.NOT_LINKED || type == Pages.ALL) && mPagesNotLinked.value!!.isNotEmpty())
            getImage(null, parseFileLink, mPagesNotLinked.value!!, Pages.NOT_LINKED, true)
    }

    private fun getImage(parseManga: Parse?, parsePageLink: Parse?, list : ArrayList<PageLink>, type : Pages, reload : Boolean = false) {
        if (!reload) mLoadVerify = 0

        mGenerateImageThread.forEach { if (it.type == type) it.thread.interrupt() }
        mGenerateImageThread.removeAll { it.type == type }

        val runnable = ImageLoadRunnable(parseManga, parsePageLink, list, type, reload)
        val thread = Thread(runnable)
        thread.priority = Process.THREAD_PRIORITY_DEFAULT + Process.THREAD_PRIORITY_LESS_FAVORABLE
        thread.start()
        mGenerateImageThread.add(ImageLoadThread(type, thread))
    }

    fun endThread() {
        mForceEnd = true
        mGenerateImageThread.forEach { it.thread.interrupt() }
        mGenerateImageThread.clear()
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
        mGenerateImageThread.removeAll { it.type == type }

        val message = Message()
        message.obj = ImageLoad(null, type)
        message.what = PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_FINISHED
        for (h in mGenerateImageHandler!!)
            h.sendMessage(message)
    }

    private fun notifyErrorLoad(type: Pages) {
        mGenerateImageThread.removeAll { it.type == type }

        val message = Message()
        message.obj = ImageLoad(null, type)
        message.what = PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_LOAD_ERROR
        for (h in mGenerateImageHandler!!)
            h.sendMessage(message)
    }

    private var mForceEnd : Boolean = false
    inner class ImageLoad(var index: Int?, var type: Pages)
    private inner class ImageLoadThread(var type: Pages, var thread : Thread)
    private inner class ImageLoadRunnable(private var parseManga: Parse?, private var parsePageLink: Parse?, private var list: ArrayList<PageLink>, private var type: Pages, private var reload : Boolean = false) : Runnable {
        override fun run() {
            var error = false
            try {
                mForceEnd = false
                notifyImageLoadStart(type)
                for ((index, page) in list.withIndex()) {
                    if (reload) {
                        when (type) {
                            Pages.ALL -> {
                                if ((page.dualImage) && (page.imageMangaPage != null && page.imageLeftFileLinkPage != null && page.imageRightFileLinkPage != null))
                                    continue
                                else if(page.imageMangaPage != null && page.imageLeftFileLinkPage != null)
                                    continue
                            }
                            Pages.NOT_LINKED -> {
                                if (page.imageLeftFileLinkPage != null)
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

                            page.isFileLinkLoading = false
                        }
                        Pages.MANGA -> {
                            page.imageMangaPage = generateBitmap(parseManga!!, page.mangaPage)
                            if (page.dualImage)
                                page.imageRightFileLinkPage = generateBitmap(parsePageLink!!, page.fileRightLinkPage)
                        }
                        Pages.NOT_LINKED -> page.imageLeftFileLinkPage = generateBitmap(parsePageLink!!, page.fileLinkPage)
                        Pages.LINKED -> {
                            page.imageLeftFileLinkPage = generateBitmap(parsePageLink!!, page.fileLinkPage)
                            page.isFileLinkLoading = false
                        }
                        else -> {}
                    }

                    if (mForceEnd)
                        break
                    else
                        notifyImageLoad(index, type)
                }
            } catch(e: Exception) {
                mLoadError += 1
                error = true
                notifyErrorLoad(type)
            } finally {
                if (!error)
                    mLoadError = 0

                if (!mForceEnd)
                    notifyImageLoadFinished(type)
            }
        }
    }
}
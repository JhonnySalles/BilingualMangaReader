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
import br.com.fenix.bilingualmangareader.model.enums.LoadFile
import br.com.fenix.bilingualmangareader.model.enums.Pages
import br.com.fenix.bilingualmangareader.service.controller.SubTitleController
import br.com.fenix.bilingualmangareader.service.parses.Parse
import br.com.fenix.bilingualmangareader.service.parses.ParseFactory
import br.com.fenix.bilingualmangareader.service.repository.FileLinkRepository
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.util.constants.ReaderConsts
import br.com.fenix.bilingualmangareader.util.helpers.Util
import java.io.File
import java.io.InputStream

class PagesLinkViewModel(application: Application) : AndroidViewModel(application) {

    private val mContext = application.applicationContext
    private val mFileLinkRepository: FileLinkRepository = FileLinkRepository(mContext)

    private var mFileLink = MutableLiveData<FileLink>()
    val fileLink: LiveData<FileLink> = mFileLink
    private var mPagesLink = MutableLiveData<ArrayList<PageLink>>(ArrayList())
    val pagesLink: LiveData<ArrayList<PageLink>> = mPagesLink
    private var mPagesNotLinked = MutableLiveData<ArrayList<PageLink>>(ArrayList())
    val pagesLinkNotLinked: LiveData<ArrayList<PageLink>> = mPagesNotLinked

    private var mGenerateImageHandler: MutableList<Handler>? = java.util.ArrayList()
    private var mGenerateImageThread: ArrayList<ImageLoadThread> = ArrayList()

    fun getFileLink(manga : Manga?) : FileLink? {
        return if (mFileLink.value == null ||  mFileLink.value!!.path == "")
            if (manga == null)
                null
            else
                mFileLinkRepository.get(manga)
        else {
            mFileLink.value!!.pagesLink = mPagesLink.value
            mFileLink.value
        }
    }

    private fun reload(manga : Manga, refresh: (index: Int?, type: Pages) -> (Unit)) : Boolean {
        var fileLink = SubTitleController.getInstance(mContext).getFileLink()?: return false
        return if (manga == fileLink.manga) {
            mFileLink.value = fileLink
            mPagesLink.value = fileLink.pagesLink?.let { ArrayList(it) }
            mPagesNotLinked.value = fileLink.pagesNotLink?.let { ArrayList(it) }
            refresh(null, Pages.ALL)

            val mParseManga = ParseFactory.create(fileLink.manga!!.file)
            if (mParseManga != null)
                getImage(mParseManga, fileLink.parse, mPagesLink.value!!, Pages.ALL, true)

            if (fileLink.pagesNotLink!!.isNotEmpty())
                getImage(null, fileLink.parse, mPagesNotLinked.value!!, Pages.NOT_LINKED, true)

            true
        } else false
    }

    private fun find(manga : Manga, refresh: (index: Int?, type: Pages) -> (Unit)) : Boolean {
        var obj = mFileLinkRepository.get(manga) ?: return false
        obj.manga = manga
        set(obj, refresh)
        return (obj.pagesLink != null) && (obj.pagesLink!!.isNotEmpty())
    }

    fun find(idManga: Long, name: String, pages: Int, refresh: (index: Int?, type: Pages) -> (Unit)) : Boolean {
        var obj = mFileLinkRepository.findByFileName(idManga, name, pages)

        if (obj != null)
            set(obj, refresh)

        return obj != null
    }

    fun set(obj: FileLink, refresh: (index: Int?, type: Pages) -> (Unit)) {
        mFileLink.value = obj
        mPagesLink.value!!.clear()
        mPagesNotLinked.value!!.clear()

        val mParseManga = ParseFactory.create(obj.manga!!.file) ?: return
        val mParseLink = ParseFactory.create(obj.file) ?: return

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
        if (obj.id == 0L)
            obj.id = mFileLinkRepository.save(obj)
        else
            mFileLinkRepository.update(obj)

        return obj
    }

    fun save() {
        val obj = mFileLink.value!!
        obj.pagesLink = mPagesLink.value
        obj.pagesNotLink = mPagesNotLinked.value
        if (obj.id == null || obj.id == 0L)
            mFileLink.value!!.id = mFileLinkRepository.save(obj)
        else
            mFileLinkRepository.update(obj)
    }

    fun delete(obj: FileLink) {
        mFileLinkRepository.delete(obj)
    }

    fun refresh(refresh: (index: Int?, type: Pages) -> (Unit)) {
        if (mFileLink.value != null && mFileLink.value!!.path.isNotEmpty())
            readFileLink(mFileLink.value!!.path, refresh)
    }

    fun loadManga(manga : Manga, refresh: (index: Int?, type: Pages) -> (Unit)) {
        mPagesLink.value!!.clear()

        if (reload(manga, refresh)) return
        if (find(manga, refresh)) return

        val parse = ParseFactory.create(manga.file) ?: return
        mFileLink.value = FileLink(manga)

        val list = ArrayList<PageLink>()
        for (i in 0 until parse.numPages()) {
            var name = parse.getPagePath(i) ?: ""
            if (Util.isImage(name)) {
                name = if (name.contains('/'))
                    name.substringAfterLast("/")
                else
                    name.substringAfterLast('\\')

                list.add(PageLink(mFileLink.value!!.id, i, manga.pages, name))
            }
        }
        mPagesLink.value = list
        refresh(null, Pages.MANGA)
        getImage(parse, null, mPagesLink.value!!, Pages.MANGA)
    }

    fun readFileLink(path : String, refresh: (index: Int?, type: Pages) -> (Unit)) : LoadFile {
        var loaded = LoadFile.ERROR_NOT_LOAD
        mFileLink.value!!.path = ""
        mPagesLink.value!!.forEach { page ->  page.clearFileLInk() }
        mPagesNotLinked.value!!.clear()

      val file = File(path)
        if (file.name.endsWith(".rar") ||
            file.name.endsWith(".zip") ||
            file.name.endsWith(".cbr") ||
            file.name.endsWith(".cbz")) {
            val parse = ParseFactory.create(path)?: return loaded
            loaded = LoadFile.LOADED
            mFileLink.value = FileLink(mFileLink.value!!.manga!!, parse.numPages(), path, file.nameWithoutExtension, file.extension, file.parent)
            mFileLink.value!!.parse = parse

            val listNotLink = ArrayList<PageLink>()
            for (i in 0 until parse.numPages()) {
                var name = parse.getPagePath(i)?: ""
                if (Util.isImage(name)) {
                    name = if (name.contains('/'))
                        name.substringAfterLast("/")
                    else
                        name.substringAfterLast('\\')

                    if (i < mPagesLink.value!!.size) {
                        val page = mPagesLink.value!![i]
                        page.fileLinkPage = i
                        page.fileLinkPageName = name
                        page.fileLinkPages = parse.numPages()
                        page.isFileLinkLoading = true
                        refresh(i, Pages.LINKED)
                    } else
                        listNotLink.add(PageLink(
                            mFileLink.value!!.id, -1, mFileLink.value!!.manga!!.pages, i, mFileLink.value!!.pages,
                            mFileLink.value!!.manga!!.name, mFileLink.value!!.name
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

        } else
            loaded = LoadFile.ERROR_FILE_WRONG

        return loaded
    }

    private fun generateBitmap(parse: Parse, index: Int): Bitmap? {
        return try {
            var stream: InputStream? = parse.getPage(index)
            var image  = BitmapFactory.decodeStream(stream)
            Bitmap.createScaledBitmap(image, ReaderConsts.PAGESLINK.IMAGES_WIDTH, ReaderConsts.PAGESLINK.IMAGES_HEIGHT, false)
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

    fun onMove(origin : PageLink, destiny :PageLink) {
        if (origin == destiny) return
        val originIndex = mPagesLink.value!!.indexOf(origin)
        val destinyIndex = mPagesLink.value!!.indexOf(destiny)
        var differ = destinyIndex - originIndex

        if (originIndex > destinyIndex) {
            for (i in destinyIndex until originIndex)
                if (mPagesLink.value!![i].imageFileLinkPage != null)
                    mPagesNotLinked.value!!.add(PageLink(mPagesLink.value!![i].idFile, -1, 0, mPagesLink.value!![i].fileLinkPage,
                        mPagesLink.value!![i].fileLinkPages, "", mPagesLink.value!![i].fileLinkPageName, true, null,
                        mPagesLink.value!![i].imageFileLinkPage))

            differ *=-1
            for (i in destinyIndex until mPagesLink.value!!.size) {
                when {
                    i == destinyIndex -> {
                        mPagesLink.value!![i].imageFileLinkPage = origin.imageFileLinkPage
                        mPagesLink.value!![i].fileLinkPage = origin.fileLinkPage
                        mPagesLink.value!![i].fileLinkPages = origin.fileLinkPages
                        mPagesLink.value!![i].fileLinkPageName = origin.fileLinkPageName
                    }
                    (i + differ) > (mPagesLink.value!!.size-1) -> mPagesLink.value!![i].clearFileLInk()
                    else -> {
                        mPagesLink.value!![i].imageFileLinkPage = mPagesLink.value!![i + differ].imageFileLinkPage
                        mPagesLink.value!![i].fileLinkPage = mPagesLink.value!![i + differ].fileLinkPage
                        mPagesLink.value!![i].fileLinkPages = mPagesLink.value!![i + differ].fileLinkPages
                        mPagesLink.value!![i].fileLinkPageName = mPagesLink.value!![i + differ].fileLinkPageName
                    }
                }
            }
        } else {
            val size = mPagesLink.value!!.size-1
            for (i in size downTo size - differ)
                if (mPagesLink.value!![i].imageFileLinkPage != null)
                    mPagesNotLinked.value!!.add(PageLink(mPagesLink.value!![i].idFile, -1, 0, mPagesLink.value!![i].fileLinkPage,
                        mPagesLink.value!![i].fileLinkPages, "", mPagesLink.value!![i].fileLinkPageName, true, null,
                        mPagesLink.value!![i].imageFileLinkPage))

            for (i in size downTo originIndex) {
                when {
                    i < destinyIndex -> mPagesLink.value!![i].clearFileLInk()
                    else -> {
                        mPagesLink.value!![i].imageFileLinkPage = mPagesLink.value!![i - differ].imageFileLinkPage
                        mPagesLink.value!![i].fileLinkPage = mPagesLink.value!![i - differ].fileLinkPage
                        mPagesLink.value!![i].fileLinkPages = mPagesLink.value!![i - differ].fileLinkPages
                        mPagesLink.value!![i].fileLinkPageName = mPagesLink.value!![i - differ].fileLinkPageName
                    }
                }
            }
        }
    }

    fun onNotLinked(origin : PageLink) {
        mPagesNotLinked.value!!.add(PageLink(origin.idFile, -1, 0, origin.fileLinkPage,
            origin.fileLinkPages, "", origin.fileLinkPageName, true, null,
            origin.imageFileLinkPage))

        origin.clearFileLInk()
    }

    fun fromNotLinked(origin : PageLink, destiny :PageLink) {
        val destinyIndex = mPagesLink.value!!.indexOf(destiny)
        val size = mPagesLink.value!!.size-1
        mPagesNotLinked.value!!.remove(origin)

        if (destiny.imageFileLinkPage == null) {
            mPagesLink.value!![destinyIndex].imageFileLinkPage = origin.imageFileLinkPage
            mPagesLink.value!![destinyIndex].fileLinkPage = origin.fileLinkPage
            mPagesLink.value!![destinyIndex].fileLinkPages = origin.fileLinkPages
            mPagesLink.value!![destinyIndex].fileLinkPageName = origin.fileLinkPageName
        } else {
            if (mPagesLink.value!![size].imageFileLinkPage != null)
                mPagesNotLinked.value!!.add(PageLink(mPagesLink.value!![size].idFile, -1, 0, mPagesLink.value!![size].fileLinkPage,
                    mPagesLink.value!![size].fileLinkPages, "", mPagesLink.value!![size].fileLinkPageName, true, null,
                    mPagesLink.value!![size].imageFileLinkPage))

            for (i in size downTo destinyIndex) {
                when (i) {
                    destinyIndex -> {
                        mPagesLink.value!![destinyIndex].imageFileLinkPage = origin.imageFileLinkPage
                        mPagesLink.value!![destinyIndex].fileLinkPage = origin.fileLinkPage
                        mPagesLink.value!![destinyIndex].fileLinkPages = origin.fileLinkPages
                        mPagesLink.value!![destinyIndex].fileLinkPageName = origin.fileLinkPageName
                    }
                    else -> {
                        mPagesLink.value!![i].imageFileLinkPage = mPagesLink.value!![i - 1].imageFileLinkPage
                        mPagesLink.value!![i].fileLinkPage = mPagesLink.value!![i - 1].fileLinkPage
                        mPagesLink.value!![i].fileLinkPages = mPagesLink.value!![i - 1].fileLinkPages
                        mPagesLink.value!![i].fileLinkPageName = mPagesLink.value!![i - 1].fileLinkPageName
                    }
                }
            }
        }
    }

    private fun getImage(parseManga: Parse?, parsePageLink: Parse?, list : ArrayList<PageLink>, type : Pages, reload : Boolean = false) {
        mGenerateImageThread.forEach { if (it.type == type) it.thread.interrupt() }

        val runnable = ImageLoadRunnable(parseManga, parsePageLink, list, type, reload)
        val thread = Thread(runnable)
        thread.priority = Process.THREAD_PRIORITY_DEFAULT + Process.THREAD_PRIORITY_LESS_FAVORABLE
        thread.start()
        mGenerateImageThread.add(ImageLoadThread(type, thread))
    }

    fun endThread() {
        mForceEnd = true
        mGenerateImageThread.clear()
    }

    fun addImageLoadHandler(handler: Handler) {
        mGenerateImageHandler!!.add(handler)
    }

    fun removeImageLoadHandler(handler: Handler) {
        mGenerateImageHandler!!.remove(handler)
    }

    private fun notifyImageLoad(index: Int?, type: Pages) {
        val message = Message()
        message.obj = ImageLoad(index, type)
        message.what = GeneralConsts.PAGESLINK.MESSAGE_PAGES_LINK_UPDATED
        for (h in mGenerateImageHandler!!)
            h.sendMessage(message)
    }

    private fun notifyImageLoadFinished(type: Pages) {
        val message = Message()
        message.obj = ImageLoad(null, type)
        message.what = GeneralConsts.PAGESLINK.MESSAGE_PAGES_LINK_FINISHED
        for (h in mGenerateImageHandler!!)
            h.sendMessage(message)

        mGenerateImageThread.removeIf { it.type == type }
    }

    private var mForceEnd : Boolean = false
    inner class ImageLoad(var index: Int?, var type: Pages)
    private inner class ImageLoadThread(var type: Pages, var thread : Thread)
    private inner class ImageLoadRunnable(private var parseManga: Parse?, private var parsePageLink: Parse?, private var list: ArrayList<PageLink>, private var type: Pages, private var reload : Boolean = false) : Runnable {
        override fun run() {
            try {
                mForceEnd = false
                for ((index, page) in list.withIndex()) {

                    if (reload) {
                        when {
                            (type == Pages.ALL && page.imageMangaPage != null && page.imageFileLinkPage != null) -> continue
                            (type == Pages.NOT_LINKED && page.imageFileLinkPage != null) -> continue
                        }
                    }

                    when {
                        type == Pages.ALL -> {
                            page.imageMangaPage = generateBitmap(parseManga!!, page.mangaPage)
                            page.imageFileLinkPage = generateBitmap(parsePageLink!!, page.fileLinkPage)
                            page.isFileLinkLoading = false
                        }
                        type == Pages.MANGA -> page.imageMangaPage = generateBitmap(parseManga!!, page.mangaPage)
                        type == Pages.NOT_LINKED -> page.imageFileLinkPage = generateBitmap(parsePageLink!!, page.fileLinkPage)
                        page.fileLinkPage > -1 -> {
                            page.imageFileLinkPage = generateBitmap(parsePageLink!!, page.fileLinkPage)
                            page.isFileLinkLoading = false
                        }
                    }

                    if (mForceEnd)
                        break
                    else
                        notifyImageLoad(index, type)
                }
            } finally {
                if (!mForceEnd)
                    notifyImageLoadFinished(type)
            }
        }
    }
}
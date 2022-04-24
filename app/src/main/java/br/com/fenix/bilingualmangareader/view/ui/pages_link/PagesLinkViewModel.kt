package br.com.fenix.bilingualmangareader.view.ui.pages_link

import android.app.Application
import android.graphics.BitmapFactory
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Cover
import br.com.fenix.bilingualmangareader.model.entity.FileLink
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.model.entity.PageLink
import br.com.fenix.bilingualmangareader.model.enums.LoadFile
import br.com.fenix.bilingualmangareader.service.controller.ImageCoverController
import br.com.fenix.bilingualmangareader.service.parses.Parse
import br.com.fenix.bilingualmangareader.service.parses.ParseFactory
import br.com.fenix.bilingualmangareader.service.repository.CoverRepository
import br.com.fenix.bilingualmangareader.service.repository.FileLinkRepository
import br.com.fenix.bilingualmangareader.service.repository.MangaRepository
import br.com.fenix.bilingualmangareader.util.constants.PageLinkConsts
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
    private var mPagesLinkNotBond = MutableLiveData<ArrayList<PageLink>>(ArrayList())
    val pagesLinkNotBond: LiveData<ArrayList<PageLink>> = mPagesLinkNotBond

    fun find(idManga: Long, name: String, pages: Int) : Boolean {
        var obj = mFileLinkRepository.findByFileName(idManga, name, pages)

        if (obj != null)
            set(obj)

        return obj != null
    }

    fun set(obj: Manga) {
        val file = FileLink(obj.id?:0L, obj)
        mFileLink.value = file
        mPagesLink.value!!.clear()

        if (file.idManga == 0L) return

        val parse: Parse? = ParseFactory.create(obj.file)
        if (parse != null)
            if (parse.numPages() > 0) {
                for (index in 0 until parse.numPages()) {
                    var stream: InputStream? = parse.getPage(index)

                    val options = BitmapFactory.Options()
                    options.inJustDecodeBounds = true
                    BitmapFactory.decodeStream(stream, null, options)
                    options.inSampleSize = Util.calculateInSampleSize(
                        options,
                        PageLinkConsts.PAGE.WIDTH,
                        PageLinkConsts.PAGE.HEIGHT
                    )

                    options.inJustDecodeBounds = false
                    stream?.close()
                    stream = parse.getPage(index)
                    val image = BitmapFactory.decodeStream(stream, null, options)
                    var pageName = parse.getPagePath(index)?:""
                    pageName = if (pageName.contains('/'))
                        pageName.substringAfterLast("/")
                    else
                        pageName.substringAfterLast('\\')

                    mPagesLink.value!!.add(PageLink(null, file.id, index, parse.numPages(), 0, 0, pageName,"", image, null))
                }
            }
    }

    fun set(obj: FileLink) {
        mFileLink.value = obj
        mPagesLink.value = obj.pagesLink?.let { ArrayList(it) }
    }

    fun save(obj: FileLink): FileLink {
        if (obj.id == 0L)
            obj.id = mFileLinkRepository.save(obj)
        else
            mFileLinkRepository.update(obj)

        return obj
    }

    fun delete(obj: FileLink) {
        mFileLinkRepository.delete(obj)
    }

    fun load(path : String) : LoadFile {
        var loaded = LoadFile.ERROR_NOT_LOAD
        val file = File(path)
        if (file.name.endsWith(".rar") ||
            file.name.endsWith(".zip") ||
            file.name.endsWith(".cbr") ||
            file.name.endsWith(".cbz")
        ) {
            val parse: Parse? = ParseFactory.create(file)
            if (parse != null)
                if (parse.numPages() > 0) {
                    loaded = LoadFile.LOADED
                    val list = ArrayList<PageLink>()
                    for (index in 0 until parse.numPages()) {
                        var stream: InputStream? = parse.getPage(index)

                        val options = BitmapFactory.Options()
                        options.inJustDecodeBounds = true
                        BitmapFactory.decodeStream(stream, null, options)
                        options.inSampleSize = Util.calculateInSampleSize(
                            options,
                            PageLinkConsts.PAGE.WIDTH,
                            PageLinkConsts.PAGE.HEIGHT
                        )

                        options.inJustDecodeBounds = false
                        stream?.close()
                        stream = parse.getPage(index)
                        val image = BitmapFactory.decodeStream(stream, null, options)
                        var pageName = parse.getPagePath(index)?:""
                        pageName = if (pageName.contains('/'))
                            pageName.substringAfterLast("/")
                        else
                            pageName.substringAfterLast('\\')

                        if (mPagesLink.value!!.size < index) {
                            val page = mPagesLink.value!![index]
                            page.fileLinkPageName = pageName
                            page.fileLinkPages = parse.numPages()
                            page.imageFileLinkPage = image
                        } else
                            mPagesLinkNotBond.value!!.add(PageLink(null, mFileLink.value!!.id, -1, mFileLink.value!!.pages, index, parse.numPages(), "", pageName, null, image))
                    }
                    mPagesLink.value = list
                }
        } else
            loaded = LoadFile.ERROR_FILE_WRONG

        return loaded
    }

}
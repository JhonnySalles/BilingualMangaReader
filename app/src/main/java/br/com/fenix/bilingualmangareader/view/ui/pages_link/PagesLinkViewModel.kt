package br.com.fenix.bilingualmangareader.view.ui.pages_link

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.com.fenix.bilingualmangareader.model.entity.FileLink
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.model.entity.PageLink
import br.com.fenix.bilingualmangareader.model.enums.LoadFile
import br.com.fenix.bilingualmangareader.service.parses.Parse
import br.com.fenix.bilingualmangareader.service.parses.ParseFactory
import br.com.fenix.bilingualmangareader.service.repository.FileLinkRepository
import br.com.fenix.bilingualmangareader.util.constants.ReaderConsts
import br.com.fenix.bilingualmangareader.util.helpers.Util
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
    private var mParse: Parse? = null

    fun find(idManga: Long, name: String, pages: Int) : Boolean {
        var obj = mFileLinkRepository.findByFileName(idManga, name, pages)

        if (obj != null)
            set(obj)

        return obj != null
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

    fun loadManga(manga : Manga) {
        mPagesLink.value!!.clear()
        mParse = ParseFactory.create(manga.file)?: return
        mFileLink.value = FileLink(manga)

        for (i in 0 until mParse!!.numPages()) {
            var name = mParse!!.getPagePath(i)?: ""

            name = if (name.contains('/'))
                name.substringAfterLast("/")
            else
                name.substringAfterLast('\\')

            val page = PageLink(mFileLink.value!!.id, i, manga.pages, name)
            //getImage(mParse!!, i) { image -> page.imageFileLinkPage = image }
            mPagesLink.value!!.add(page)
        }
    }

    fun readFileLink(path : String, function: (Int) -> (Unit)) : LoadFile {
        var loaded = LoadFile.ERROR_NOT_LOAD
        mPagesLink.value!!.forEach { page ->
            page.clearFileLInk()
            page.isFileLinkLoading = false
        }
        mPagesNotLinked.value!!.clear()

        val file = File(path)
        if (file.name.endsWith(".rar") ||
            file.name.endsWith(".zip") ||
            file.name.endsWith(".cbr") ||
            file.name.endsWith(".cbz")) {
            loaded = LoadFile.LOADED
            val parse = ParseFactory.create(path)?: return loaded
            mFileLink.value = FileLink(mFileLink.value!!.manga!!, parse.numPages(), file.path, file.nameWithoutExtension, file.extension, file.parent)

            for (i in 0 until parse.numPages()) {
                var name = parse.getPagePath(i)?: ""

                name = if (name.contains('/'))
                    name.substringAfterLast("/")
                else
                    name.substringAfterLast('\\')

                if (i < mPagesLink.value!!.size){
                    val page = mPagesLink.value!![i]
                    page.fileLinkPage = i
                    page.fileLinkPageName = name
                    page.fileLinkPages = parse.numPages()
                    page.isFileLinkLoading = true
                    /*getImage(parse, i) { image ->
                        page.imageFileLinkPage = image
                        page.isFileLinkLoading = false
                    }*/
                    function(i)
                } else {
                    val page = PageLink(mFileLink.value!!.id, -1, mFileLink.value!!.manga!!.pages, i, mFileLink.value!!.pages,
                        mFileLink.value!!.manga!!.name, mFileLink.value!!.name)
                    //getImage(parse, i) { image -> page.imageFileLinkPage = image }
                    mPagesLink.value!!.add(page)
                }
            }
        } else
            loaded = LoadFile.ERROR_FILE_WRONG

        return loaded
    }

    private fun generateBitmap(parse: Parse, index : Int) : Bitmap? {
        var stream: InputStream? = parse.getPage(index)
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeStream(stream, null, options)
        options.inSampleSize = Util.calculateInSampleSize(
            options,
            ReaderConsts.PAGESLINK.IMAGES_WIDTH,
            ReaderConsts.PAGESLINK.IMAGES_HEIGHT
        )

        options.inJustDecodeBounds = false
        stream?.close()
        stream = parse.getPage(index)
        return BitmapFactory.decodeStream(stream, null, options)
    }

    private fun getImage(parse: Parse, index : Int, function: (Bitmap?) -> (Unit)) =
        runBlocking { // this: CoroutineScope
            launch { // launch a new coroutine and continue
                function(generateBitmap(parse, index))
            }
        }

}
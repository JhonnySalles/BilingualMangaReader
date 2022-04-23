package br.com.fenix.bilingualmangareader.view.ui.pages_link

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.com.fenix.bilingualmangareader.model.entity.Cover
import br.com.fenix.bilingualmangareader.model.entity.FileLink
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.model.entity.PageLink
import br.com.fenix.bilingualmangareader.service.controller.ImageCoverController
import br.com.fenix.bilingualmangareader.service.repository.CoverRepository
import br.com.fenix.bilingualmangareader.service.repository.FileLinkRepository
import br.com.fenix.bilingualmangareader.service.repository.MangaRepository

class PagesLinkViewModel(application: Application) : AndroidViewModel(application) {

    private val mContext = application.applicationContext
    private val mFileLinkRepository: FileLinkRepository = FileLinkRepository(mContext)

    private var mFileLink = MutableLiveData<FileLink>()
    val fileLink: LiveData<FileLink> = mFileLink
    private var mPagesLink = MutableLiveData<ArrayList<PageLink>>(ArrayList())
    val pagesLink: LiveData<ArrayList<PageLink>> = mPagesLink

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

}
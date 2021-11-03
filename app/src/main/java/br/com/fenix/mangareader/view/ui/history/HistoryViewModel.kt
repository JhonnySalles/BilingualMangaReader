package br.com.fenix.mangareader.view.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.com.fenix.mangareader.model.entity.Cover
import br.com.fenix.mangareader.model.entity.Manga
import br.com.fenix.mangareader.service.controller.ImageCoverController
import br.com.fenix.mangareader.service.repository.CoverRepository
import br.com.fenix.mangareader.service.repository.MangaRepository

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val mContext = application.applicationContext
    private val mMangaRepository: MangaRepository = MangaRepository(mContext)

    private var mListMangas = MutableLiveData<ArrayList<Manga>>(ArrayList())
    val save: LiveData<ArrayList<Manga>> = mListMangas

    fun list() {
        val list = mMangaRepository.listHistory()
        if (list != null)
            mListMangas.value = ArrayList(list)
        else
            mListMangas.value = ArrayList()
    }

    fun list(refreshComplete: () -> (Unit)) {
        val list = mMangaRepository.listHistory()
        if (list != null) {
            if (mListMangas.value == null || mListMangas.value!!.isEmpty())
                mListMangas.value = ArrayList(list)
            else
                update(list)
        } else
            mListMangas.value = ArrayList()
        refreshComplete()
    }

    fun update(list: List<Manga>) {
        if (list.isNotEmpty()) {
            for (manga in list) {
                if (!mListMangas.value!!.contains(manga))
                    mListMangas.value!!.add(manga)
            }
        }
    }

    fun updateLastAccess(manga: Manga) {
        mMangaRepository.update(manga)
    }

}
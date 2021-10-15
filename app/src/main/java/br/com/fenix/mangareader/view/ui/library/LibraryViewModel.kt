package br.com.fenix.mangareader.view.ui.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.com.fenix.mangareader.model.entity.Manga
import br.com.fenix.mangareader.model.entity.Cover
import br.com.fenix.mangareader.service.controller.ImageCoverController
import br.com.fenix.mangareader.service.repository.MangaRepository
import br.com.fenix.mangareader.service.repository.CoverRepository

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val mContext = application.applicationContext
    private val mMangaRepository: MangaRepository = MangaRepository(mContext)
    private val mCoverRepository: CoverRepository = CoverRepository(mContext)

    private var mListMangas = MutableLiveData<ArrayList<Manga>>(ArrayList())
    val save: LiveData<ArrayList<Manga>> = mListMangas

    fun save(obj: Manga): Manga {
        if (obj.id == 0L)
            obj.id = mMangaRepository.save(obj)
        else
            mMangaRepository.update(obj)

        return obj
    }

    fun save(obj: Cover): Cover {
        if (obj.id == 0L)
            obj.id = mCoverRepository.save(obj)
        else
            mCoverRepository.update(obj)

        return obj
    }

    fun remove(manga: Manga) {
        if (mListMangas.value != null)
            mListMangas.value!!.remove(manga)
    }

    fun update() {
        if (mListMangas.value != null) {
            for (manga in mListMangas.value!!) {
                val item = mMangaRepository.get(manga.id!!)
                manga.bookMark = item?.bookMark!!
                manga.lastAccess = item.lastAccess
            }
        }
    }

    fun update(list: List<Manga>) {
        if (list.isNotEmpty()) {
            for (manga in mListMangas.value!!) {
                if (!mListMangas.value!!.contains(manga))
                    mListMangas.value!!.add(manga)
            }
        }
    }

    fun updateList() {
        val list = mMangaRepository.list() ?: return
        if (list.isNotEmpty()) {
            for (manga in list) {
                if (!mListMangas.value!!.contains(manga))
                    mListMangas.value!!.add(manga)
            }
        }
    }

    fun list() {
        val list = mMangaRepository.list()
        if (list != null)
            mListMangas.value = ArrayList(list)
        else
            mListMangas.value = ArrayList()
    }

    fun list(refreshComplete: () -> (Unit)) {
        val list = mMangaRepository.list()
        if (list != null) {
            if (mListMangas.value == null || mListMangas.value!!.isEmpty())
                mListMangas.value = ArrayList(list)
            else
                update(list)
            /*ImageCoverController.instance.setImageCoverAsync(mContext, mListMangas.value!!) {
                refreshComplete()
            }*/
        } else
            mListMangas.value = ArrayList()
        refreshComplete()
    }

    fun updateLastAcess(manga: Manga) {
        mMangaRepository.update(manga)
    }
}
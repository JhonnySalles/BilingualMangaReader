package br.com.fenix.bilingualmangareader.view.ui.history

import android.app.Application
import android.widget.Filter
import android.widget.Filterable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.com.fenix.bilingualmangareader.model.entity.Library
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.service.repository.LibraryRepository
import br.com.fenix.bilingualmangareader.service.repository.MangaRepository
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.util.helpers.LibraryUtil
import java.util.*

class HistoryViewModel(application: Application) : AndroidViewModel(application), Filterable {

    private val mMangaRepository: MangaRepository = MangaRepository(application.applicationContext)
    private val mLibraryRepository: LibraryRepository = LibraryRepository(application.applicationContext)
    private val mDefaultLibrary = LibraryUtil.getDefault(application.applicationContext)

    private var mLibrary: Library? = null
    private var mFilter: String = ""

    private var mListMangasFull = MutableLiveData<ArrayList<Manga>>(arrayListOf())
    private var mListMangas = MutableLiveData<ArrayList<Manga>>(arrayListOf())
    val listMangas: LiveData<ArrayList<Manga>> = mListMangas

    fun list() {
        val list = mMangaRepository.listHistory()
        if (list != null) {
            getLibraries(list)
            mListMangasFull.value = ArrayList(list)
            mListMangas.value = ArrayList(list)
        } else {
            mListMangasFull.value = ArrayList(list)
            mListMangas.value = ArrayList()
        }
    }

    fun list(refreshComplete: (Int) -> (Unit)) {
        val list = mMangaRepository.listHistory()
        if (list != null) {
            getLibraries(list)
            if (mListMangas.value == null || mListMangas.value!!.isEmpty()) {
                mListMangas.value = ArrayList(list)
                mListMangasFull.value = ArrayList(list)
            } else
                update(list)
        } else {
            mListMangas.value = ArrayList()
            mListMangasFull.value = ArrayList()
        }

        refreshComplete(mListMangas.value!!.size - 1)
    }

    private fun getLibraries(list: List<Manga>) {
        val libraries = mLibraryRepository.list()
        list.forEach {
            if (it.fkLibrary != GeneralConsts.KEYS.LIBRARY.DEFAULT)
                it.library = libraries.find { lb -> lb.id == it.fkLibrary } ?: it.library
        }
    }

    fun update(list: List<Manga>) {
        if (list.isNotEmpty()) {
            for (manga in list) {
                if (!mListMangas.value!!.contains(manga))
                    mListMangas.value!!.add(manga)

                if (!mListMangasFull.value!!.contains(manga))
                    mListMangasFull.value!!.add(manga)
            }
        }
    }

    fun updateDelete(manga: Manga) {
        mMangaRepository.delete(manga)
    }

    fun updateLastAccess(manga: Manga) {
        mMangaRepository.update(manga)
    }

    fun clear(manga: Manga?) {
        if (manga != null) {
            save(manga)
            if (mListMangas.value!!.contains(manga))
                mListMangas.value!!.remove(manga)

            if (mListMangasFull.value!!.contains(manga))
                mListMangasFull.value!!.remove(manga)
        }
    }

    fun deletePermanent(manga: Manga?) {
        if (manga != null)
            mMangaRepository.deletePermanent(manga)
    }

    fun save(manga: Manga?) {
        manga ?: return

        if (manga.id == 0L)
            manga.id = mMangaRepository.save(manga)
        else
            mMangaRepository.update(manga)
    }

    fun remove(manga: Manga) {
        if (mListMangas.value != null && mListMangas.value!!.contains(manga))
            mListMangas.value!!.remove(manga)

        if (mListMangasFull.value != null && mListMangasFull.value!!.contains(manga))
            mListMangasFull.value!!.remove(manga)
    }

    fun add(manga: Manga, index: Int) {
        if (mListMangas.value != null)
            mListMangas.value!!.add(index, manga)

        if (mListMangasFull.value != null)
            mListMangasFull.value!!.add(index, manga)
    }

    fun getAndRemove(position: Int): Manga? {
        val manga = if (mListMangas.value != null) mListMangas.value!!.removeAt(position) else null

        if (mListMangas.value != null && mListMangas.value!!.contains(manga))
            mListMangas.value!!.remove(manga)

        return manga
    }

    private fun filterList(): ArrayList<Manga> {
        val list = arrayListOf<Manga>()

        val isTitleId = null
        var title: Manga? = null
        if (mListMangasFull.value != null && mListMangasFull.value!!.isNotEmpty())
            for (manga in mListMangasFull.value!!) {
                if (manga == null)
                    continue

                if (manga.id == isTitleId) {
                    title = manga
                    continue
                }

                if (mLibrary != null && manga.fkLibrary != mLibrary!!.id)
                    continue

                if (mFilter.isNotEmpty()) {
                    if (manga.name.lowercase(Locale.getDefault()).contains(mFilter) ||
                        manga.type.lowercase(Locale.getDefault()).contains(mFilter)
                    ) {
                        if (title != null) {
                            list.add(title)
                            title = null
                        }
                        list.add(manga)
                    }
                } else {
                    if (title != null) {
                        list.add(title)
                        title = null
                    }
                    list.add(manga)
                }
            }

        return list
    }

    fun filterLibrary(library: Library?) {
        if (library == mLibrary)
            return

        mLibrary = library
        mListMangas.value = filterList()
    }

    override fun getFilter(): Filter {
        return mMangaFilter
    }

    private val mMangaFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            mFilter = constraint.toString().lowercase(Locale.getDefault()).trim()
            val results = FilterResults()
            results.values = filterList()
            return results
        }

        override fun publishResults(constraint: CharSequence?, filterResults: FilterResults?) {
            val list = arrayListOf<Manga>()
            filterResults?.let {
                list.addAll(it.values as Collection<Manga>)
            }
            mListMangas.value = list
        }
    }

    fun getLibraryList(): List<Library> {
        val list = mutableListOf<Library>()
        list.add(mDefaultLibrary)
        list.addAll(mLibraryRepository.list())
        return list
    }

}
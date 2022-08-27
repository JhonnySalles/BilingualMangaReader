package br.com.fenix.bilingualmangareader.view.ui.menu

import android.app.Application
import android.widget.Filter
import android.widget.Filterable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.com.fenix.bilingualmangareader.model.entity.Library
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.model.enums.Libraries
import br.com.fenix.bilingualmangareader.service.repository.LibraryRepository
import br.com.fenix.bilingualmangareader.service.repository.MangaRepository
import br.com.fenix.bilingualmangareader.util.helpers.LibraryUtil
import br.com.fenix.bilingualmangareader.util.helpers.Util
import org.slf4j.LoggerFactory
import java.util.*


class SelectMangaViewModel(application: Application) : AndroidViewModel(application), Filterable {

    private val mLOGGER = LoggerFactory.getLogger(SelectMangaViewModel::class.java)

    private val mLibraryRepository: LibraryRepository = LibraryRepository(application.applicationContext)
    private val mMangaRepository: MangaRepository = MangaRepository(application.applicationContext)

    private var mDefaultLibrary = LibraryUtil.getDefault(application.applicationContext)
    private var mLibrary: Library = mDefaultLibrary

    private var mListMangasFull = MutableLiveData<MutableList<Manga>>(mutableListOf())
    private var mListMangas = MutableLiveData<MutableList<Manga>>(mutableListOf())
    val listMangas: LiveData<MutableList<Manga>> = mListMangas

    var manga: String = ""

    fun setDefaultLibrary(library: Library) {
        if (mLibrary.id == library.id)
            mLibrary = library
    }

    fun setDefaultLibrary(type: Libraries) {
        mLibrary = mLibraryRepository.get(type) ?: mDefaultLibrary
    }

    fun setLibrary(library: Library) {
        if (mLibrary.id != library.id) {
            mListMangasFull.value = mutableListOf()
            mListMangas.value = mutableListOf()
        }
        mLibrary = library
    }

    fun changeLibrary(library: Library) {
        mListMangasFull.value = mutableListOf()
        mListMangas.value = mutableListOf()
        mLibrary = library

        val list = mMangaRepository.list(mLibrary)
        if (list == null || list.isEmpty()) {
            mListMangasFull.value = mutableListOf()
            mListMangas.value = mutableListOf()
        } else {
            mListMangas.value = list.toMutableList()
            mListMangasFull.value = list.toMutableList()
        }
    }

    fun getLibrary() =
        mLibrary

    fun list(manga: String, refreshComplete: (Boolean) -> (Unit)) {
        val list = mMangaRepository.list(mLibrary)
        if (list != null) {
            if (mListMangasFull.value == null || mListMangasFull.value!!.isEmpty()) {
                mListMangas.value = list.toMutableList()
                mListMangasFull.value = list.toMutableList()
            } else {
                for (manga in list) {
                    if (!mListMangasFull.value!!.contains(manga)) {
                        mListMangas.value!!.add(manga)
                        mListMangasFull.value!!.add(manga)
                    }
                }
            }
        } else {
            mListMangasFull.value = mutableListOf()
            mListMangas.value = mutableListOf()
        }

        sorted(manga)
        refreshComplete(mListMangas.value!!.isNotEmpty())
    }

    fun getLibraryList(): List<Library> {
        val list = mutableListOf<Library>()
        list.add(mDefaultLibrary)
        list.addAll(mLibraryRepository.list())
        return list
    }

    fun sorted(manga: String) {
        val name = Util.getNameWithoutVolumeAndChapter(manga)
        mListMangasFull.value!!.sortWith(compareByDescending<Manga> { it.name.contains(name) }.thenBy { it.name })
        mListMangas.value!!.sortWith(compareByDescending<Manga> { it.name.contains(name) }.thenBy { it.name })
    }

    override fun getFilter(): Filter {
        return mMangaFilter
    }

    private val mMangaFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList: MutableList<Manga> = mutableListOf()

            if (constraint == null || constraint.isEmpty()) {
                filteredList.addAll(mListMangasFull.value!!)
            } else {
                val filterPattern = constraint.toString().lowercase(Locale.getDefault()).trim()

                filteredList.addAll(mListMangasFull.value!!.filter {
                    it.name.lowercase(Locale.getDefault()).contains(filterPattern) ||
                            it.type.lowercase(Locale.getDefault()).contains(filterPattern)
                })
            }

            val results = FilterResults()
            results.values = filteredList

            return results
        }

        override fun publishResults(constraint: CharSequence?, filterResults: FilterResults?) {
            val list = mutableListOf<Manga>()
            filterResults?.let {
                list.addAll(it.values as Collection<Manga>)
            }
            mListMangas.value = list
        }
    }

}
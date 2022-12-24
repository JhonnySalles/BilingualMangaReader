package br.com.fenix.bilingualmangareader.view.ui.library

import android.app.Application
import android.widget.Filter
import android.widget.Filterable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.com.fenix.bilingualmangareader.model.entity.Library
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.model.enums.ListMod
import br.com.fenix.bilingualmangareader.model.enums.Order
import br.com.fenix.bilingualmangareader.service.repository.MangaRepository
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import java.util.*

class LibraryViewModel(application: Application) : AndroidViewModel(application), Filterable {

    private var mStackLibrary = mutableMapOf<String, Triple<Int, Library, MutableList<Manga>>>()
    private var mLibrary: Library = Library(GeneralConsts.KEYS.LIBRARY.DEFAULT)
    private val mMangaRepository: MangaRepository = MangaRepository(application.applicationContext)
    private val mPreferences = GeneralConsts.getSharedPreferences(application.applicationContext)

    private var mListMangasFull = MutableLiveData<MutableList<Manga>>(mutableListOf())
    private var mListMangas = MutableLiveData<MutableList<Manga>>(mutableListOf())
    val listMangas: LiveData<MutableList<Manga>> = mListMangas

    fun setDefaultLibrary(library: Library) {
        if (mLibrary.id == library.id)
            mLibrary = library
    }

    fun setLibrary(library: Library) {
        if (mLibrary.id != library.id) {
            mListMangasFull.value = mutableListOf()
            mListMangas.value = mutableListOf()
        }
        mLibrary = library
    }

    fun saveLastLibrary() {
        mPreferences.edit().putLong(GeneralConsts.KEYS.LIBRARY.LAST_LIBRARY, mLibrary.id ?: GeneralConsts.KEYS.LIBRARY.DEFAULT).apply()
    }

    fun getLibrary() =
        mLibrary

    fun existStack(id: String): Boolean =
        mStackLibrary.contains(id)

    fun restoreLastStackLibrary(id: String) {
        if (mStackLibrary.contains(id)) {
            mStackLibrary.remove(id)
            for (item in mStackLibrary) {
                if (item.value.first == mStackLibrary.size) {
                    mLibrary = item.value.second
                    mListMangasFull.value = item.value.third
                    mListMangas.value = item.value.third.toMutableList()
                    break
                }
            }
        }
    }

    fun addStackLibrary(id: String, library: Library) =
        mStackLibrary.put(id, Triple(mStackLibrary.size + 1, library, mListMangasFull.value!!))

    fun removeStackLibrary(id: String) =
        mStackLibrary.remove(id)

    fun save(obj: Manga): Manga {
        if (obj.id == 0L)
            obj.id = mMangaRepository.save(obj)
        else
            mMangaRepository.update(obj)

        return obj
    }

    fun add(manga: Manga, position: Int = -1) {
        if (position > -1) {
            mListMangas.value!!.add(position, manga)
            mListMangasFull.value!!.add(position, manga)
        } else {
            mListMangas.value!!.add(manga)
            mListMangasFull.value!!.add(manga)
        }
    }

    fun delete(obj: Manga) {
        mMangaRepository.delete(obj)
        remove(obj)
    }

    fun getAndRemove(position: Int): Manga? {
        val manga = if (mListMangas.value != null) mListMangas.value!!.removeAt(position) else null
        if (manga != null) mListMangasFull.value!!.remove(manga)
        return manga
    }

    fun remove(manga: Manga) {
        if (mListMangasFull.value != null) {
            mListMangas.value!!.remove(manga)
            mListMangasFull.value!!.remove(manga)
        }
    }

    fun remove(position: Int) {
        if (mListMangasFull.value != null) {
            val manga = mListMangas.value!!.removeAt(position)
            mListMangasFull.value!!.remove(manga)
        }
    }

    fun update(list: List<Manga>) {
        if (list.isNotEmpty()) {
            for (manga in list) {
                if (!mListMangasFull.value!!.contains(manga)) {
                    mListMangas.value!!.add(manga)
                    mListMangasFull.value!!.add(manga)
                }
            }
        }
    }

    fun setList(list: ArrayList<Manga>) {
        mListMangas.value = list
        mListMangasFull.value = list.toMutableList()
    }

    fun addList(manga: Manga): Int {
        var index = -1
        if (!mListMangasFull.value!!.contains(manga)) {
            index = mListMangas.value!!.size
            mListMangas.value!!.add(manga)
            mListMangasFull.value!!.add(manga)
        }

        return index
    }

    fun remList(manga: Manga): Int {
        var index = -1

        if (mListMangasFull.value!!.contains(manga)) {
            index = mListMangas.value!!.indexOf(manga)
            mListMangas.value!!.remove(manga)
            mListMangasFull.value!!.remove(manga)
        }

        return index
    }

    fun updateList(refreshComplete: (Boolean, indexes: MutableList<Pair<ListMod, Int>>) -> (Unit)) {
        var change = false
        val indexes = mutableListOf<Pair<ListMod, Int>>()
        if (mListMangasFull.value != null && mListMangasFull.value!!.isNotEmpty()) {
            val list = mMangaRepository.listRecentChange(mLibrary)
            if (list != null && list.isNotEmpty()) {
                change = true
                for (manga in list) {
                    if (mListMangasFull.value!!.contains(manga)) {
                        mListMangasFull.value!![mListMangasFull.value!!.indexOf(manga)].update(manga)
                        val index = mListMangas.value!!.indexOf(manga)
                        if (index > -1)
                            indexes.add(Pair(ListMod.MOD, index))
                    } else {
                        mListMangas.value!!.add(manga)
                        mListMangasFull.value!!.add(manga)
                        indexes.add(Pair(ListMod.ADD, mListMangas.value!!.size))
                    }
                }
            }
            val listDel = mMangaRepository.listRecentDeleted(mLibrary)
            if (listDel != null && listDel.isNotEmpty()) {
                change = true
                for (manga in listDel) {
                    if (mListMangasFull.value!!.contains(manga)) {
                        val index = mListMangas.value!!.indexOf(manga)
                        mListMangas.value!!.remove(manga)
                        mListMangasFull.value!!.remove(manga)
                        indexes.add(Pair(ListMod.REM, index))
                    }
                }
            }
        } else {
            val list = mMangaRepository.list(mLibrary)
            if (list != null) {
                indexes.add(Pair(ListMod.FULL, list.size))
                mListMangas.value = list.toMutableList()
                mListMangasFull.value = list.toMutableList()
            } else {
                mListMangas.value = mutableListOf()
                mListMangasFull.value = mutableListOf()
                indexes.add(Pair(ListMod.FULL, 0))
            }
            //Receive value force refresh, not necessary notify
            change = false
        }

        refreshComplete(change, indexes)
    }

    fun list(refreshComplete: (Boolean) -> (Unit)) {
        val list = mMangaRepository.list(mLibrary)
        if (list != null) {
            if (mListMangasFull.value == null || mListMangasFull.value!!.isEmpty()) {
                mListMangas.value = list.toMutableList()
                mListMangasFull.value = list.toMutableList()
            } else
                update(list)
        } else {
            mListMangasFull.value = mutableListOf()
            mListMangas.value = mutableListOf()
        }

        refreshComplete(mListMangas.value!!.isNotEmpty())
    }

    fun isEmpty(): Boolean =
        mListMangas.value == null || mListMangas.value!!.isEmpty()

    fun sorted(order: Order) {
        when (order) {
            Order.Date -> {
                mListMangasFull.value!!.sortBy { it.dateCreate }
                mListMangas.value!!.sortBy { it.dateCreate }
            }
            Order.LastAccess -> {
                mListMangasFull.value!!.sortWith(compareByDescending<Manga> { it.lastAccess }.thenBy { it.name })
                mListMangas.value!!.sortWith(compareByDescending<Manga> { it.lastAccess }.thenBy { it.name })
            }
            Order.Favorite -> {
                mListMangasFull.value!!.sortWith(compareByDescending<Manga> { it.favorite }.thenBy { it.name })
                mListMangas.value!!.sortWith(compareByDescending<Manga> { it.favorite }.thenBy { it.name })
            }
            else -> {
                mListMangasFull.value!!.sortBy { it.name }
                mListMangas.value!!.sortBy { it.name }
            }
        }
    }

    fun clearFilter() {
        val newList: MutableList<Manga> = mutableListOf()
        newList.addAll(mListMangasFull.value!!.filter(Objects::nonNull))
        mListMangas.value = newList
    }

    override fun getFilter(): Filter {
        return mMangaFilter
    }

    private val mMangaFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList: MutableList<Manga> = mutableListOf()

            if (constraint == null || constraint.isEmpty()) {
                filteredList.addAll(mListMangasFull.value!!.filter(Objects::nonNull))
            } else {
                val filterPattern = constraint.toString().lowercase(Locale.getDefault()).trim()

                filteredList.addAll(mListMangasFull.value!!.filter(Objects::nonNull).filter {
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
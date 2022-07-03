package br.com.fenix.bilingualmangareader.view.ui.library

import android.app.Application
import android.widget.Filter
import android.widget.Filterable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.model.enums.Order
import br.com.fenix.bilingualmangareader.service.repository.MangaRepository
import java.util.*

class LibraryViewModel(application: Application) : AndroidViewModel(application), Filterable {

    private val mMangaRepository: MangaRepository = MangaRepository(application.applicationContext)

    private var mListMangasFull = MutableLiveData<MutableList<Manga>>(mutableListOf())
    private var mListMangas = MutableLiveData<MutableList<Manga>>(mutableListOf())
    val listMangas: LiveData<MutableList<Manga>> = mListMangas

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
        if (mListMangas.value != null) {
            mListMangas.value!!.remove(manga)
            mListMangasFull.value!!.remove(manga)
        }
    }

    fun remove(position: Int) {
        if (mListMangas.value != null) {
            mListMangas.value!!.removeAt(position)
            mListMangasFull.value!!.removeAt(position)
        }
    }

    fun update(list: List<Manga>) {
        if (list.isNotEmpty()) {
            for (manga in list) {
                if (!mListMangas.value!!.contains(manga)) {
                    mListMangas.value!!.add(manga)
                    mListMangasFull.value!!.add(manga)
                }
            }
        }
    }

    fun updateListAdd(): ArrayList<Int> {
        val indexes = arrayListOf<Int>()
        val list = mMangaRepository.list() ?: return indexes
        if (list.isNotEmpty()) {
            for (manga in list) {
                if (!mListMangas.value!!.contains(manga)) {
                    mListMangas.value!!.add(manga)
                    indexes.add(mListMangas.value!!.size - 1)
                }
            }
        }

        return indexes
    }

    fun updateListRem(): ArrayList<Int> {
        val indexes = arrayListOf<Int>()
        val list = mMangaRepository.listDeleted() ?: return indexes
        if (list.isNotEmpty()) {
            for (manga in list) {
                if (mListMangas.value!!.contains(manga)) {
                    indexes.add(mListMangas.value!!.indexOf(manga))
                    mListMangas.value!!.remove(manga)
                    mListMangasFull.value!!.remove(manga)
                }
            }
        }

        return indexes
    }

    fun setList(list: ArrayList<Manga>) {
        mListMangas.value = list
        mListMangasFull.value = list.toMutableList()
    }

    fun addList(manga: Manga): Int {
        var index = -1
        if (!mListMangas.value!!.contains(manga)) {
            index = mListMangas.value!!.size
            mListMangas.value!!.add(manga)
            mListMangasFull.value!!.add(manga)
        }

        return index
    }

    fun remList(manga: Manga): Int {
        var index = -1

        if (mListMangas.value!!.contains(manga)) {
            index = mListMangas.value!!.indexOf(manga)
            mListMangas.value!!.remove(manga)
            mListMangasFull.value!!.remove(manga)
        }

        return index
    }

    fun updateList(refreshComplete: (Boolean) -> (Unit)) {
        var change = false
        if (mListMangas.value != null && mListMangas.value!!.isNotEmpty()) {
            val list = mMangaRepository.listRecentChange()
            if (list != null && list.isNotEmpty()) {
                change = true
                for (manga in list) {
                    if (mListMangas.value!!.contains(manga)) {
                        val index = mListMangas.value!!.indexOf(manga)
                        mListMangas.value!![index].bookMark = manga.bookMark
                        mListMangas.value!![index].favorite = manga.favorite
                        mListMangas.value!![index].lastAccess = manga.lastAccess
                    } else {
                        mListMangas.value!!.add(manga)
                        mListMangasFull.value!!.add(manga)
                    }
                }
            }
        } else {
            val list = mMangaRepository.list()
            if (list != null) {
                mListMangas.value = list.toMutableList()
                mListMangasFull.value = list.toMutableList()
            } else {
                mListMangas.value = mutableListOf()
                mListMangasFull.value = mutableListOf()
            }

            change = true
        }

        refreshComplete(change)
    }

    fun list(refreshComplete: (Boolean) -> (Unit)) {
        val list = mMangaRepository.list()
        if (list != null) {
            if (mListMangas.value == null || mListMangas.value!!.isEmpty()) {
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

    fun getLastIndex(): Int =
        if (mListMangas.value == null) 0 else mListMangas.value!!.size - 1

    fun sorted(order: Order) {
        when (order) {
            Order.Date -> {
                mListMangas.value!!.sortBy { it.dateCreate }
                mListMangasFull.value!!.sortBy { it.dateCreate }
            }
            Order.LastAccess -> {
                mListMangas.value!!.sortWith(compareByDescending<Manga> { it.lastAccess }.thenBy { it.name })
                mListMangasFull.value!!.sortWith(compareByDescending<Manga> { it.lastAccess }.thenBy { it.name })
            }
            Order.Favorite -> {
                mListMangas.value!!.sortWith(compareByDescending<Manga> { it.favorite }.thenBy { it.name })
                mListMangasFull.value!!.sortWith(compareByDescending<Manga> { it.favorite }.thenBy { it.name })
            }
            else -> {
                mListMangas.value!!.sortBy { it.name }
                mListMangasFull.value!!.sortBy { it.name }
            }
        }
    }

    override fun getFilter(): Filter {
        return mMangaFilter
    }

    private val mMangaFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList: MutableList<Manga> = mutableListOf()

            if (constraint == null || constraint.length === 0) {
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
            list.addAll(filterResults!!.values as Collection<Manga>)
            mListMangas.value = list
        }
    }
}
package br.com.fenix.bilingualmangareader.view.ui.menu

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.com.fenix.bilingualmangareader.model.entity.Library
import br.com.fenix.bilingualmangareader.service.repository.LibraryRepository

class ConfigLibrariesViewModel(application: Application) : AndroidViewModel(application) {

    private val mRepository: LibraryRepository = LibraryRepository(application.applicationContext)

    private var mListLibraries = MutableLiveData<MutableList<Library>>(mutableListOf())
    val listLibraries: LiveData<MutableList<Library>> = mListLibraries


    fun new(library: Library) {
        if (library.title.isEmpty() || library.path.isEmpty())
            return

        if (mListLibraries.value!!.contains(library)) {
            mListLibraries.value!![mListLibraries.value!!.indexOf(library)].merge(library)
            mListLibraries.value = mListLibraries.value
        } else {
            val deleted = mRepository.findDeleted(library.path)
            if (deleted != null)
                library.id = deleted.id
            add(library)
        }

        save(library)
    }

    fun add(library: Library, position: Int = -1) {
        if (mListLibraries.value!!.contains(library))
            mListLibraries.value!![mListLibraries.value!!.indexOf(library)].merge(library)
        else if (position > -1)
            mListLibraries.value!!.add(position, library)
        else
            mListLibraries.value!!.add(library)

        mListLibraries.value = mListLibraries.value
    }

    fun delete(library: Library) {
        if (library.id != null)
            mRepository.delete(library)
    }

    fun save(library: Library) {
        if (library.id == null)
            library.id = mRepository.save(library)
        else
            mRepository.update(library)
    }

    fun findDeleted(path: String): Library? {
        return mRepository.findDeleted(path)
    }

    fun load() {
        val libraries = mRepository.list()
        mListLibraries.value = libraries.toMutableList()
    }

    fun getAndRemove(position: Int): Library? {
        return if (mListLibraries.value != null) mListLibraries.value!!.removeAt(position) else null
    }

    fun removeDefault(path: String) {
        mRepository.removeDefault(path)
        mListLibraries.value!!.removeIf { it.path.equals(path, true) }
    }

    fun getList(): List<Library> {
        return mListLibraries.value?.filter { it.enabled } ?: mutableListOf()
    }

}
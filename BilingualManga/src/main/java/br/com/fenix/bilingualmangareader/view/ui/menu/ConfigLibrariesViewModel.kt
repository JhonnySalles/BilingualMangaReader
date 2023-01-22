package br.com.fenix.bilingualmangareader.view.ui.menu

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.com.fenix.bilingualmangareader.model.entity.Library
import br.com.fenix.bilingualmangareader.model.enums.Themes
import br.com.fenix.bilingualmangareader.service.repository.LibraryRepository

class ConfigLibrariesViewModel(application: Application) : AndroidViewModel(application) {

    private val mRepository: LibraryRepository = LibraryRepository(application.applicationContext)

    private var mListLibraries = MutableLiveData<MutableList<Library>>(mutableListOf())
    val libraries: LiveData<MutableList<Library>> = mListLibraries

    private var mListThemes: MutableLiveData<MutableList<Pair<Themes, Boolean>>> = MutableLiveData(arrayListOf())
    val themes: LiveData<MutableList<Pair<Themes, Boolean>>> = mListThemes


    fun newLibrary(library: Library) {
        if (library.title.isEmpty() || library.path.isEmpty())
            return

        if (mListLibraries.value!!.contains(library)) {
            mListLibraries.value!![mListLibraries.value!!.indexOf(library)].merge(library)
            mListLibraries.value = mListLibraries.value
        } else {
            val deleted = mRepository.findDeleted(library.path)
            if (deleted != null)
                library.id = deleted.id
            addLibrary(library)
        }

        saveLibrary(library)
    }

    fun addLibrary(library: Library, position: Int = -1) {
        if (mListLibraries.value!!.contains(library))
            mListLibraries.value!![mListLibraries.value!!.indexOf(library)].merge(library)
        else if (position > -1)
            mListLibraries.value!!.add(position, library)
        else
            mListLibraries.value!!.add(library)

        mListLibraries.value = mListLibraries.value
    }

    fun deleteLibrary(library: Library) {
        if (library.id != null)
            mRepository.delete(library)
    }

    fun saveLibrary(library: Library) {
        if (library.id == null)
            library.id = mRepository.save(library)
        else
            mRepository.update(library)
    }

    fun findLibraryDeleted(path: String): Library? {
        return mRepository.findDeleted(path)
    }

    fun loadLibrary() {
        val libraries = mRepository.list()
        mListLibraries.value = libraries.toMutableList()
    }

    fun loadThemes(initial: Themes = Themes.ORIGINAL) {
        mListThemes.value = Themes.values().map { Pair(it, it == initial) }.toMutableList()
    }

    fun getSelectedThemeIndex(): Int  {
        val index = mListThemes.value?.indexOfFirst { it.second } ?: 0
        return if (index == -1) 0 else index
    }

    fun getLibraryAndRemove(position: Int): Library? {
        return if (mListLibraries.value != null) mListLibraries.value!!.removeAt(position) else null
    }

    fun removeLibraryDefault(path: String) {
        mRepository.removeDefault(path)
        mListLibraries.value!!.removeIf { it.path.equals(path, true) }
    }

    fun getListLibrary(): List<Library> {
        return mListLibraries.value?.filter { it.enabled } ?: mutableListOf()
    }

    fun setEnableTheme(theme: Themes) {
        if (mListThemes.value != null)
            mListThemes.value = mListThemes.value!!.map { Pair(it.first, it.first == theme) }.toMutableList()
    }

}
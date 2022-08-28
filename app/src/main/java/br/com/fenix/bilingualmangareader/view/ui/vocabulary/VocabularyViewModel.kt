package br.com.fenix.bilingualmangareader.view.ui.vocabulary

import android.app.Application
import androidx.lifecycle.*
import androidx.paging.*
import br.com.fenix.bilingualmangareader.service.repository.VocabularyRepository


class VocabularyViewModel(application: Application) : AndroidViewModel(application) {

    private val mDataBase: VocabularyRepository = VocabularyRepository(application.applicationContext)

    private var mIsQuery = MutableLiveData(false)
    val isQuery: LiveData<Boolean> = mIsQuery

    private val currentQuery = MutableLiveData(DEFAULT_QUERY)

    private fun flowPager(query: Triple<String, String, Boolean>) =
        Pager(PagingConfig(pageSize = 40)) {
            val list = mDataBase.list(query.first, query.second, query.third)
            list
        }.liveData.map { live ->
            live.map { voc ->
                mDataBase.findByVocabulary(voc)
            }
        }

    val vocabularyPager = currentQuery.switchMap { query ->
        mIsQuery.value = true
        val result = flowPager(query).cachedIn(viewModelScope)
        mIsQuery.value = false
        result
    }

    fun setQueryVocabulary(vocabulary: String) {
        currentQuery.value = Triple(currentQuery.value?.first ?: "", vocabulary, currentQuery.value?.third ?: false)
    }

    fun setQueryManga(manga: String) {
        currentQuery.value = Triple(manga, currentQuery.value?.second ?: "", currentQuery.value?.third ?: false)
    }

    fun setQuery(favorite: Boolean) {
        currentQuery.value = Triple(currentQuery.value?.first ?: "", currentQuery.value?.second ?: "", favorite)
    }

    fun setQuery(manga: String, vocabulary: String, favorite: Boolean) {
        currentQuery.value = Triple(manga, vocabulary, favorite)
    }

    fun clearQuery() {
        currentQuery.value = DEFAULT_QUERY
    }

    fun getFavorite() : Boolean = currentQuery.value?.third ?: false

    companion object {
        private val DEFAULT_QUERY = Triple("", "", false)
    }

}
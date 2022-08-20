package br.com.fenix.bilingualmangareader.view.ui.vocabulary

import android.app.Application
import androidx.lifecycle.*
import androidx.paging.*
import br.com.fenix.bilingualmangareader.service.repository.VocabularyRepository


class VocabularyViewModel(application: Application) : AndroidViewModel(application) {

    private val mDataBase: VocabularyRepository = VocabularyRepository(application.applicationContext)

    private val currentQuery = MutableLiveData(DEFAULT_QUERY)

    private fun flowPager(query: Pair<Long?, String>) =
        Pager(PagingConfig(pageSize = 50)) {
            val list = mDataBase.list(query.first, query.second)
            list
        }.liveData.map { live ->
            live.map { voc ->
                mDataBase.findVocabularyManga(voc)
            }
        }

    val vocabularyPager = currentQuery.switchMap { query -> flowPager(query).cachedIn(viewModelScope) }

    fun setQuery(vocabulary: String) {
        currentQuery.value = Pair(currentQuery.value?.first, vocabulary)
    }

    fun setQuery(idManga: Long?) {
        currentQuery.value = Pair(idManga, currentQuery.value?.second ?: "")
    }

    fun clearQuery() {
        currentQuery.value = DEFAULT_QUERY
    }

    companion object {
        private val DEFAULT_QUERY = Pair<Long?, String>(null, "")
    }

}
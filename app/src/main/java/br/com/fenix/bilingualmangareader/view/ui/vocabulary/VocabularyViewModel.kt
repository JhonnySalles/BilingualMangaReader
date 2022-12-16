package br.com.fenix.bilingualmangareader.view.ui.vocabulary

import android.app.Application
import androidx.lifecycle.*
import androidx.paging.*
import br.com.fenix.bilingualmangareader.model.entity.Vocabulary
import br.com.fenix.bilingualmangareader.service.repository.VocabularyRepository


class VocabularyViewModel(application: Application) : AndroidViewModel(application) {

    private val mDataBase: VocabularyRepository = VocabularyRepository(application.applicationContext)

    private var mIsQuery = MutableLiveData(false)
    val isQuery: LiveData<Boolean> = mIsQuery

    inner class Query(var manga: String = "", var vocabulary: String = "", var favorite: Boolean = false, var orderInverse: Boolean = false)

    private val currentQuery = MutableLiveData(Query())

    private fun flowPager(query: Query) =
        Pager(PagingConfig(pageSize = 40)) {
            mDataBase.list(query)
        }.liveData.map { live ->
            live.map { voc ->
                mDataBase.findByVocabulary(query.manga, voc)
            }
        }

    val vocabularyPager = currentQuery.switchMap { query ->
        mIsQuery.value = true
        val result = flowPager(query).cachedIn(viewModelScope)
        mIsQuery.value = false
        result
    }

    fun setQuery(manga: String, vocabulary: String) {
        currentQuery.value = Query(manga, vocabulary, currentQuery.value?.favorite ?: false, currentQuery.value?.orderInverse ?: false)
    }

    fun setQueryVocabulary(vocabulary: String) {
        currentQuery.value = Query(currentQuery.value?.manga ?: "", vocabulary, currentQuery.value?.favorite ?: false, currentQuery.value?.orderInverse ?: false)
    }

    fun setQueryManga(manga: String) {
        currentQuery.value = Query(manga, currentQuery.value?.vocabulary ?: "", currentQuery.value?.favorite ?: false, currentQuery.value?.orderInverse ?: false)
    }

    fun setQueryFavorite(favorite: Boolean) {
        currentQuery.value = Query(currentQuery.value?.manga ?: "", currentQuery.value?.vocabulary ?: "", favorite, currentQuery.value?.orderInverse ?: false)
    }

    fun setQueryOrder(orderInverse: Boolean) {
        currentQuery.value = Query(currentQuery.value?.manga ?: "", currentQuery.value?.vocabulary ?: "", currentQuery.value?.favorite ?: false, orderInverse)
    }

    fun setQuery(manga: String, vocabulary: String, favorite: Boolean) {
        currentQuery.value = Query(manga, vocabulary, favorite)
    }

    fun clearQuery() {
        currentQuery.value = Query()
    }

    fun getFavorite(): Boolean =
        currentQuery.value?.favorite ?: false

    fun getOrder(): Boolean =
        currentQuery.value?.orderInverse ?: false

    fun update(vocabulary: Vocabulary) {
        mDataBase.update(vocabulary)
    }

}
package br.com.fenix.bilingualmangareader.view.ui.vocabulary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.cachedIn
import br.com.fenix.bilingualmangareader.model.entity.Vocabulary
import br.com.fenix.bilingualmangareader.service.repository.VocabularyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.transformLatest
import org.slf4j.LoggerFactory


class VocabularyViewModel(application: Application) : AndroidViewModel(application) {

    private val mLOGGER = LoggerFactory.getLogger(VocabularyViewModel::class.java)

    private val mDataBase: VocabularyRepository = VocabularyRepository(application.applicationContext)

    private var mIsQuery = MutableLiveData(false)
    val isQuery: LiveData<Boolean> = mIsQuery

    inner class Query(var manga: String = "", var vocabulary: String = "", var favorite: Boolean = false, var orderInverse: Boolean = false)

    private val currentQuery = MutableStateFlow(Query())

    fun vocabularyPager() =
        currentQuery.transformLatest { query ->
            Pager(PagingConfig(pageSize = 40)) {
                PagingSource(mDataBase, query)
            }.flow.collectLatest {
                emit(it)
            }
        }.cachedIn(viewModelScope)

    private fun setQuery(query: Query) {
        currentQuery.value = query
    }

    fun setQuery(manga: String, vocabulary: String) {
        setQuery(Query(manga, vocabulary, currentQuery.value.favorite, currentQuery.value.orderInverse))
    }

    fun setQueryVocabulary(vocabulary: String) {
        setQuery(
            Query(
                currentQuery.value.manga,
                vocabulary,
                currentQuery.value.favorite,
                currentQuery.value.orderInverse
            )
        )
    }

    fun setQueryManga(manga: String) {
        setQuery(
            Query(
                manga,
                currentQuery.value.vocabulary,
                currentQuery.value.favorite,
                currentQuery.value.orderInverse
            )
        )
    }

    fun setQueryFavorite(favorite: Boolean) {
        setQuery(
            Query(
                currentQuery.value.manga,
                currentQuery.value.vocabulary,
                favorite,
                currentQuery.value.orderInverse
            )
        )
    }

    fun setQueryOrder(orderInverse: Boolean) {
        setQuery(
            Query(
                currentQuery.value.manga,
                currentQuery.value.vocabulary,
                currentQuery.value.favorite,
                orderInverse
            )
        )
    }

    fun setQuery(manga: String, vocabulary: String, favorite: Boolean) {
        setQuery(Query(manga, vocabulary, favorite))
    }

    fun clearQuery() {
        setQuery(Query())
    }

    fun getFavorite(): Boolean =
        currentQuery.value.favorite

    fun getOrder(): Boolean =
        currentQuery.value.orderInverse

    fun update(vocabulary: Vocabulary) {
        mDataBase.update(vocabulary)
    }

    inner class PagingSource(private val dao: VocabularyRepository, private val query: Query) :
        androidx.paging.PagingSource<Int, Vocabulary>() {
        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Vocabulary> {
            val page = params.key ?: 0
            return try {
                val list = dao.list(query, page * params.loadSize, params.loadSize)

                for (vocabulary in list)
                    dao.findByVocabulary(query.manga, vocabulary)

                //Simulation delay
                //if (page != 0) delay(10000)

                LoadResult.Page(
                    data = list,
                    prevKey = if (page == 0) null else page - 1,
                    nextKey = if (list.isEmpty()) null else page + 1
                )
            } catch (e: Exception) {
                mLOGGER.error("Error paging list vocabulary.", e)
                LoadResult.Error(e)
            }
        }

        override fun getRefreshKey(state: PagingState<Int, Vocabulary>): Int? {
            return state.anchorPosition?.let { anchorPosition ->
                val anchorPage = state.closestPageToPosition(anchorPosition)
                anchorPage?.prevKey ?: anchorPage?.nextKey
            }
        }
    }

}
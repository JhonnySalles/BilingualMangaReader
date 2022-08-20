package br.com.fenix.bilingualmangareader.service.repository

import android.content.Context
import androidx.paging.PagingSource
import br.com.fenix.bilingualmangareader.model.entity.Chapter
import br.com.fenix.bilingualmangareader.model.entity.Vocabulary
import br.com.fenix.bilingualmangareader.model.entity.VocabularyManga
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

class VocabularyRepository(context: Context) {

    private val mLOGGER = LoggerFactory.getLogger(VocabularyRepository::class.java)
    private val mBase = DataBase.getDataBase(context)
    private var mDataBaseDAO = mBase.getVocabularyDao()
    private val mMangaDAO = mBase.getMangaDao()

    fun save(obj: Vocabulary): Long {
        val exist = mDataBaseDAO.exists(obj.word, obj.basicForm ?: "")
        return if (exist != null)
            exist.id!!
        else
            mDataBaseDAO.save(obj)
    }

    fun update(obj: Vocabulary) {
        mDataBaseDAO.update(obj)
    }

    fun delete(obj: Vocabulary) {
        mDataBaseDAO.delete(obj)
    }

    fun list(manga: String = "", vocabulary: String = "", favorite: Boolean = false): PagingSource<Int, Vocabulary> {
        return if (manga.isNotEmpty() && vocabulary.isNotEmpty())
            mDataBaseDAO.list(manga, vocabulary, vocabulary, favorite)
        else if (manga.isNotEmpty())
            mDataBaseDAO.list(manga, favorite)
        else if (vocabulary.isNotEmpty())
            mDataBaseDAO.list(vocabulary, vocabulary, favorite)
        else
            mDataBaseDAO.list(favorite)
    }

    fun findByVocabulary(vocabulary: Vocabulary): Vocabulary {
        vocabulary.vocabularyMangas = findByVocabulary(vocabulary.id!!)
        vocabulary.appears = 0
        vocabulary.vocabularyMangas.forEach { vocabulary.appears += it.appears }
        return vocabulary
    }

    private fun findByVocabulary(idVocabulary: Long): List<VocabularyManga> {
        val list = mDataBaseDAO.findByVocabulary(idVocabulary)
        list.forEach {
            it.manga = mMangaDAO.get(it.idManga)
        }
        return list
    }

    fun get(id: Long): Vocabulary? {
        return try {
            mDataBaseDAO.get(id)
        } catch (e: Exception) {
            mLOGGER.error("Error when get Vocabulary: " + e.message, e)
            null
        }
    }

    fun find(vocabulary: String): Vocabulary? {
        return try {
            mDataBaseDAO.find(vocabulary)
        } catch (e: Exception) {
            mLOGGER.error("Error when get Library: " + e.message, e)
            null
        }
    }

    fun findAll(vocabulary: String): List<Vocabulary> {
        return try {
            mDataBaseDAO.findAll(vocabulary)
        } catch (e: Exception) {
            mLOGGER.error("Error when find Library: " + e.message, e)
            listOf()
        }
    }

    fun find(idManga: Long): List<Vocabulary> {
        return try {
            mDataBaseDAO.find(idManga)
        } catch (e: Exception) {
            mLOGGER.error("Error when find Library: " + e.message, e)
            listOf()
        }
    }

    fun insert(idManga: Long, idVocabulary: Long, appears: Int) {
        mDataBaseDAO.insert(mBase.openHelper, idManga, idVocabulary, appears)
    }

    fun processVocabulary(idManga: Long?, list: List<Chapter>) {
        if (idManga == null || list.isEmpty())
            return

        CoroutineScope(Dispatchers.IO).launch {
            async {
                try {
                    for (chapter in list)
                        if (chapter.vocabulary.isNotEmpty())
                            for (vocabulary in chapter.vocabulary) {
                                var appears = 0
                                chapter.pages.forEach { p ->
                                    p.vocabulary.forEach { v -> if (v == vocabulary) appears++ }
                                }
                                vocabulary.id = save(vocabulary)
                                vocabulary.id?.let { insert(idManga, it, appears) }
                            }
                } catch (e: Exception) {
                    mLOGGER.error("Error process vocabulary.", e)
                }
            }
        }
    }

}
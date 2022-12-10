package br.com.fenix.bilingualmangareader.service.repository

import android.content.Context
import android.widget.Toast
import androidx.paging.PagingSource
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Chapter
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.model.entity.Vocabulary
import br.com.fenix.bilingualmangareader.model.entity.VocabularyManga
import br.com.fenix.bilingualmangareader.model.enums.Languages
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

class VocabularyRepository(context: Context) {

    private val mLOGGER = LoggerFactory.getLogger(VocabularyRepository::class.java)
    private val mBase = DataBase.getDataBase(context)
    private var mDataBaseDAO = mBase.getVocabularyDao()
    private val mVocabImported = Toast.makeText(context, context.getString(R.string.vocabulary_imported), Toast.LENGTH_SHORT)

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


    private val manga: Set<Manga> = setOf()
    private fun findByVocabulary(idVocabulary: Long): List<VocabularyManga> {
        val list = mDataBaseDAO.findByVocabulary(idVocabulary)
        list.forEach {
            it.manga = manga.firstOrNull { m -> m.id == it.idManga }

            if (it.manga == null)
                it.manga = mDataBaseDAO.getManga(it.idManga)
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

    fun processVocabulary(idManga: Long?, chapters: List<Chapter>) {
        if (idManga == null || chapters.isEmpty())
            return

        CoroutineScope(Dispatchers.IO).launch {
            async {
                try {
                    val list = mutableListOf<Vocabulary>()

                    chapters.parallelStream().filter { it.language == Languages.JAPANESE && it.vocabulary.isNotEmpty() }
                        .forEach {
                            for (vocabulary in it.vocabulary)
                                if (!list.contains(vocabulary))
                                    list.add(vocabulary)
                        }

                    for (vocabulary in list) {
                        var appears = 0

                        chapters.parallelStream().filter { it.language == Languages.JAPANESE && it.vocabulary.isNotEmpty() }
                            .forEach { c ->
                                c.pages.parallelStream()
                                    .forEach { p -> p.vocabulary.parallelStream().forEach { v -> if (v == vocabulary) appears++ } }
                            }

                        vocabulary.id = save(vocabulary)
                        vocabulary.id?.let { insert(idManga, it, appears) }
                    }

                    mVocabImported.show()
                } catch (e: Exception) {
                    mLOGGER.error("Error process vocabulary. ", e)
                }
            }
        }
    }

}
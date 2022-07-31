package br.com.fenix.bilingualmangareader.service.repository

import android.content.Context
import br.com.fenix.bilingualmangareader.model.entity.Chapter
import br.com.fenix.bilingualmangareader.model.entity.Vocabulary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

class VocabularyRepository(context: Context) {

    private val mLOGGER = LoggerFactory.getLogger(VocabularyRepository::class.java)
    private val mBase = DataBase.getDataBase(context)
    private var mDataBase = mBase.getVocabularyDao()

    fun save(obj: Vocabulary): Long {
        val exist = mDataBase.exists(obj.word, obj.basicForm ?: "")
        return if (exist != null)
            exist.id!!
        else
            mDataBase.save(obj)
    }

    fun update(obj: Vocabulary) {
        mDataBase.update(obj)
    }

    fun delete(obj: Vocabulary) {
        mDataBase.delete(obj)
    }

    fun list(): List<Vocabulary> {
        return try {
            mDataBase.list()
        } catch (e: Exception) {
            mLOGGER.error("Error when list Vocabulary: " + e.message, e)
            listOf()
        }
    }

    fun get(id: Long): Vocabulary? {
        return try {
            mDataBase.get(id)
        } catch (e: Exception) {
            mLOGGER.error("Error when get Vocabulary: " + e.message, e)
            null
        }
    }

    fun find(vocabulary: String): Vocabulary? {
        return try {
            mDataBase.find(vocabulary)
        } catch (e: Exception) {
            mLOGGER.error("Error when get Library: " + e.message, e)
            null
        }
    }

    fun findAll(vocabulary: String): List<Vocabulary> {
        return try {
            mDataBase.findAll(vocabulary)
        } catch (e: Exception) {
            mLOGGER.error("Error when find Library: " + e.message, e)
            listOf()
        }
    }

    fun find(idManga: Long): List<Vocabulary> {
        return try {
            mDataBase.find(idManga)
        } catch (e: Exception) {
            mLOGGER.error("Error when find Library: " + e.message, e)
            listOf()
        }
    }

    fun insert(idManga: Long, idVocabulary: Long) {
        mDataBase.insert(mBase.openHelper, idManga, idVocabulary)
    }

    fun processVocabulary(idManga: Long?, list: List<Chapter>) {
        if (idManga == null || list.isEmpty())
            return

        CoroutineScope(Dispatchers.IO).launch {
            async {
                try {
                    for (chapter in list)
                        for (vocabulary in chapter.vocabulary) {
                            vocabulary.id = save(vocabulary)
                            insert(idManga, vocabulary.id!!)
                        }
                } catch (e: Exception) {
                    mLOGGER.error("Error process vocabulary.", e)
                }
            }
        }
    }

}
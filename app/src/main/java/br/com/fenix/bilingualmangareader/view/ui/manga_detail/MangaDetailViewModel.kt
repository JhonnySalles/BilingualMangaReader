package br.com.fenix.bilingualmangareader.view.ui.manga_detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.com.fenix.bilingualmangareader.model.entity.FileLink
import br.com.fenix.bilingualmangareader.model.entity.Information
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.service.parses.ParseFactory
import br.com.fenix.bilingualmangareader.service.parses.RarParse
import br.com.fenix.bilingualmangareader.service.repository.FileLinkRepository
import br.com.fenix.bilingualmangareader.service.repository.MangaRepository
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.util.helpers.Util
import com.kttdevelopment.mal4j.manga.MangaPreview
import java.io.File
import java.time.LocalDateTime

class MangaDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val mContext = application.applicationContext
    private val mMangaRepository: MangaRepository = MangaRepository(mContext)
    private val mFileLinkRepository: FileLinkRepository = FileLinkRepository(mContext)

    private var mManga = MutableLiveData<Manga>(null)
    val manga: LiveData<Manga> = mManga

    private var mPaths: Map<String, Int> = mapOf()

    private var mListChapters = MutableLiveData<MutableList<String>>(mutableListOf())
    val listChapters: LiveData<MutableList<String>> = mListChapters

    private var mListFileLinks = MutableLiveData<MutableList<FileLink>>(mutableListOf())
    val listFileLinks: LiveData<MutableList<FileLink>> = mListFileLinks

    private var mListSubtitles = MutableLiveData<MutableList<String>>(mutableListOf())
    val listSubtitles: LiveData<MutableList<String>> = mListSubtitles

    private var mInformation = MutableLiveData<Information>(null)
    val information: LiveData<Information> = mInformation

    private var mInformationRelations = MutableLiveData<MutableList<Information>>(mutableListOf())
    val informationRelations: LiveData<MutableList<Information>> = mInformationRelations

    fun setManga(manga: Manga) {
        mManga.value = manga
        mListFileLinks.value = mFileLinkRepository.findAllByManga(manga.id!!)?.toMutableList()
        mInformation.value = null
        mInformationRelations.value = mutableListOf()

        val parse = ParseFactory.create(manga.file) ?: return
        try {
            if (parse is RarParse) {
                val folder = GeneralConsts.CACHE_FOLDER.RAR + '/' + Util.normalizeNameCache(manga.file.nameWithoutExtension)
                val cacheDir = File(GeneralConsts.getCacheDir(mContext), folder)
                (parse as RarParse?)!!.setCacheDirectory(cacheDir)
            }

            mPaths = parse.getPagePaths()
            mListChapters.value = mPaths.keys.toMutableList()
            mListSubtitles.value = parse.getSubtitlesNames().keys.toMutableList()
        } finally {
            Util.destroyParse(parse)
        }
    }


    private val PATTERN = Regex("[^\\w\\s]")
    fun setInformation(mangas: List<MangaPreview>) {
        var information: Information? = null
        val name = Util.getNameFromMangaTitle(mManga.value?.title ?: "").replace(PATTERN, "")
        val manga = mangas.find {
            it.title.replace(PATTERN, "").trim().equals(name, true) ||
                    it.alternativeTitles.japanese.trim().equals(name, true) ||
                    it.alternativeTitles.english.trim().equals(name, true) ||
                    it.alternativeTitles.synonyms.any { sy -> sy.trim().equals(name, true) }
        }

        if (manga != null)
            information = Information(manga)

        mInformation.value = information

        val relations: MutableList<Information> = mutableListOf()
        for (aux in mangas) {
            if (aux != manga)
                relations.add(Information(aux))
        }

        mInformationRelations.value = relations
    }

    fun getPage(folder: String): Int {
        return mPaths[folder] ?: mManga.value?.bookMark ?: 0
    }

    fun clear() {
        mManga.value = null
        mListFileLinks.value = mutableListOf()
        mListChapters.value = mutableListOf()
        mListSubtitles.value = mutableListOf()
    }

    fun delete() {
        if (mManga.value != null)
            mMangaRepository.delete(mManga.value!!)
    }

    fun save(manga: Manga?) {
        manga ?: return
        manga.lastAccess = LocalDateTime.now()

        if (manga.id == 0L)
            manga.id = mMangaRepository.save(manga)
        else
            mMangaRepository.update(manga)

        mManga.value = manga
    }

}
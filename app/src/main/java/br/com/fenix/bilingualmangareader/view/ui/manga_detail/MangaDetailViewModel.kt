package br.com.fenix.bilingualmangareader.view.ui.manga_detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.com.fenix.bilingualmangareader.model.entity.FileLink
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.service.parses.ParseFactory
import br.com.fenix.bilingualmangareader.service.parses.RarParse
import br.com.fenix.bilingualmangareader.service.repository.FileLinkRepository
import br.com.fenix.bilingualmangareader.service.repository.MangaRepository
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.util.helpers.Util
import java.io.File

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

    fun setManga(manga: Manga) {
        mManga.value = manga
        mListFileLinks.value = mFileLinkRepository.findAllByManga(manga.id!!)?.toMutableList()

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

        if (manga.id == 0L)
            manga.id = mMangaRepository.save(manga)
        else
            mMangaRepository.update(manga)

        mManga.value = manga
    }

}
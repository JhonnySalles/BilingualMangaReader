package br.com.fenix.mangareader.service.controller

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.collection.arraySetOf
import br.com.fenix.mangareader.R
import br.com.fenix.mangareader.model.entity.Chapter
import br.com.fenix.mangareader.model.entity.Manga
import br.com.fenix.mangareader.model.entity.SubTitle
import br.com.fenix.mangareader.model.entity.Volume
import br.com.fenix.mangareader.model.enums.Languages
import br.com.fenix.mangareader.service.parses.Parse
import br.com.fenix.mangareader.service.repository.SubTitleRepository
import br.com.fenix.mangareader.util.constants.GeneralConsts
import br.com.fenix.mangareader.view.ui.reader.PopupSubtitleConfiguration
import br.com.fenix.mangareader.view.ui.reader.PopupSubtitleReader
import br.com.fenix.mangareader.view.ui.reader.ReaderFragment
import com.google.gson.Gson
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.commons.codec.digest.DigestUtils
import java.io.InputStream
import java.util.*

class SubTitleController {

    companion object {
        lateinit var mParse: Parse
        var mManga: Manga? = null
        var mLanguages: MutableSet<Languages> = arraySetOf()
        var mComboList: HashMap<String, Chapter> = hashMapOf()
        var mSubtitleLang: Languages = Languages.JAPANESE
        var mTranslateLang: Languages = Languages.PORTUGUESE
        private lateinit var mSubtitleRepository: SubTitleRepository
        private lateinit var labelChapter: String

        private fun initialize(context: Context) {
            labelChapter = context.resources.getString(R.string.popup_reading_subtitle_chapter)
            mSubtitleRepository = SubTitleRepository(context)
            val sharedPreferences = GeneralConsts.getSharedPreferences(context)
            if (sharedPreferences != null) {
                try {
                    mSubtitleLang = Languages.valueOf(
                        sharedPreferences.getString(
                            GeneralConsts.KEYS.SUBTITLE.LANGUAGE,
                            Languages.JAPANESE.toString()
                        )!!
                    )
                    mTranslateLang = Languages.valueOf(
                        sharedPreferences.getString(
                            GeneralConsts.KEYS.SUBTITLE.TRANSLATE,
                            Languages.PORTUGUESE.toString()
                        )!!
                    )
                } catch (e: Exception) {
                    Log.i(
                        GeneralConsts.TAG.LOG,
                        "Erro ao carregar as preferencias de linguagem - " + e.message
                    )
                }
            }
        }

        fun getListChapter(context: Context, parse: Parse) = runBlocking { // this: CoroutineScope
            launch { // launch a new coroutine and continue
                mParse = parse
                initialize(context)
                val listJson: List<String> = mParse.getSubtitles()
                getChapterFromJson(listJson)
                PopupSubtitleConfiguration.setSubtitles(context, mComboList)
            }
        }

        fun getChapterFromJson(listJson: List<String>) {
            mLanguages.clear()
            mComboList.clear()
            if (listJson.isNotEmpty()) {
                val gson = Gson()
                val listChapter: MutableList<Chapter> = arrayListOf()

                listJson.forEach {
                    try {
                        val volume: Volume = gson.fromJson(it, Volume::class.java)
                        for (chapter in volume.chapters) {
                            chapter.manga = volume.manga
                            chapter.volume = volume.volume
                            chapter.language = volume.language
                        }
                        listChapter.addAll(volume.chapters)
                    } catch (volExcept: Exception) {
                        try {
                            val chapter: Chapter = gson.fromJson(it, Chapter::class.java)
                            listChapter.add(chapter)
                        } catch (chapExcept: Exception) {
                        }
                    }
                }
                setListChapter(listChapter)
            }
        }

        private fun setListChapter(chapters: MutableList<Chapter>) {
            if (chapters.isEmpty())
                return

            var lastLanguage: Languages = chapters[0].language
            mLanguages.add(chapters[0].language)
            for (chapter in chapters) {
                if (lastLanguage != chapter.language) {
                    mLanguages.add(chapters[0].language)
                    lastLanguage = chapter.language
                }

                mComboList[lastLanguage.name + " - " + labelChapter + " " + chapter.chapter] =
                    chapter
            }

        }

        fun findSubtitle(context: Context) {
            val currentPage = ReaderFragment.mCurrentPage

            if (currentPage < 0 || mParse.numPages() < currentPage) {
                Toast.makeText(
                    context,
                    context.resources.getString(R.string.popup_reading_subtitle_not_find_subtitle),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            val image: InputStream? = mParse.getPage(currentPage)
            val hash: String? = DigestUtils.md5Hex(image)
            var pageName: String? = mParse.getPageName(currentPage)

            if (pageName == null || pageName.isEmpty()) {
                Toast.makeText(
                    context,
                    context.resources.getString(R.string.popup_reading_subtitle_not_find_subtitle),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            pageName = if (pageName!!.contains('/'))
                pageName.substringAfterLast("/")
            else
                pageName.substringAfterLast('\\')

            var chapterKey = ""
            var pageNumber = 0
            val subtitles = PopupSubtitleConfiguration.getSubtitle()
            val keys = if (PopupSubtitleConfiguration.getSelectedChapter() != null) {
                val selectedLanguage = PopupSubtitleConfiguration.getSelectedChapter()!!.language
                subtitles.keys.filter { it.contentEquals(selectedLanguage.name) }
            } else
                subtitles.keys

            for (k in keys) {
                var find = false
                for (p in subtitles[k]?.pages!!) {
                    if (p.name.equals(pageName, true) || p.hash == hash) {
                        chapterKey = k
                        pageNumber = p.number
                        find = true
                        break
                    }
                }
                if (find)
                    break
            }

            if (chapterKey.isNotEmpty()) {
                mSelectedSubTitle?.chapterKey = chapterKey
                mSelectedSubTitle?.pageKey =
                    if (PopupSubtitleReader.mListPages.keys.contains(pageNumber)) pageNumber else 0
                updatePageSelect()
                PopupSubtitleConfiguration.initialize(context, chapterKey, pageNumber)

                var text: String =
                    context.resources.getString(R.string.popup_reading_subtitle_find_subtitle)
                Toast.makeText(
                    context,
                    text.format(subtitles[chapterKey]?.chapter.toString(), pageNumber.toString()),
                    Toast.LENGTH_SHORT
                ).show()
            } else
                Toast.makeText(
                    context,
                    context.resources.getString(R.string.popup_reading_subtitle_not_find_subtitle),
                    Toast.LENGTH_SHORT
                ).show()
        }

        private fun findSubtitle(
            manga: Manga,
            pageNumber: Int
        ): SubTitle {
            val image: InputStream? = mParse.getPage(pageNumber)
            val hash: String? = DigestUtils.md5Hex(image)
            var pageName: String? = mParse.getPageName(pageNumber)

            pageName = if (pageName!!.contains('/'))
                pageName.substringAfterLast("/")
            else
                pageName.substringAfterLast('\\')

            var chapterKey = ""
            var pageNumber = 0
            val subtitles = PopupSubtitleConfiguration.getSubtitle()
            val keys = subtitles.keys

            for (k in keys) {
                var find = false
                for (p in subtitles[k]?.pages!!) {
                    if (p.name.equals(pageName, true) || p.hash == hash) {
                        chapterKey = k
                        pageNumber = p.number
                        find = true
                        break
                    }
                }
                if (find)
                    break
            }

            return if (chapterKey.isNotEmpty()) {
                val chapter = PopupSubtitleConfiguration.getSelectedChapter()
                SubTitle(
                    manga.id!!,
                    mSubtitleLang,
                    chapterKey,
                    0,
                    pageNumber,
                    PopupSubtitleConfiguration.getPathSubtitle(),
                    chapter
                )
            } else
                SubTitle(
                    manga.id!!,
                    mSubtitleLang,
                    "",
                    0,
                    pageNumber,
                    PopupSubtitleConfiguration.getPathSubtitle(),
                    null
                )
        }


        private var mSelectedSubTitle: SubTitle? = null
        fun changeSubtitleInReader(context: Context, manga: Manga, pageNumber: Int) =
            runBlocking { // this: CoroutineScope
                launch {
                    mManga = manga
                    if (mSelectedSubTitle == null) {
                        mSelectedSubTitle = mSubtitleRepository.findByIdManga(manga.id!!)

                        if (mSelectedSubTitle == null)
                            try {
                                mSelectedSubTitle = findSubtitle(manga, pageNumber)
                            } catch (e: java.lang.Exception) {
                                Log.e(
                                    GeneralConsts.TAG.LOG,
                                    "Erro ao tentar encontrar o subtitle do arquivo. " + e.message
                                )
                                return@launch
                            }

                    }

                    if (mSelectedSubTitle!!.pageCount != pageNumber) {
                        val run = if (mSelectedSubTitle!!.pageCount < pageNumber)
                            PopupSubtitleReader.getNextSelectPage()
                        else
                            PopupSubtitleReader.getBeforeSelectPage()

                        if (!run) {
                            if (mSelectedSubTitle!!.pageCount < pageNumber)
                                PopupSubtitleConfiguration.getNextSelectSubtitle(context)
                            else
                                PopupSubtitleConfiguration.getBeforeSelectSubtitle(context)
                        }
                    }

                    mSelectedSubTitle!!.chapter = PopupSubtitleReader.chapterSelected
                    mSelectedSubTitle!!.pageCount = pageNumber
                    updatePageSelect()
                }
            }

        fun updatePageSelect() {
            val chapter = PopupSubtitleConfiguration.getChapterKey()
            if (mSelectedSubTitle != null && chapter.isNotEmpty()) {
                mSelectedSubTitle!!.chapterKey = chapter
                mSelectedSubTitle!!.pageKey = PopupSubtitleReader.getPageKey()
                mSubtitleRepository.save(mSelectedSubTitle!!)
            }
        }


    }
}
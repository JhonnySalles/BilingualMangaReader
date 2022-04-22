package br.com.fenix.bilingualmangareader.service.controller

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.collection.arraySetOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.*
import br.com.fenix.bilingualmangareader.model.enums.Languages
import br.com.fenix.bilingualmangareader.service.parses.Parse
import br.com.fenix.bilingualmangareader.service.repository.SubTitleRepository
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.util.constants.ReaderConsts
import br.com.fenix.bilingualmangareader.view.ui.reader.PageImageView
import br.com.fenix.bilingualmangareader.view.ui.reader.ReaderFragment
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import java.io.BufferedInputStream
import java.io.InputStream
import java.lang.ref.WeakReference


class SubTitleController private constructor(private val context: Context) {

    var mReaderFragment: ReaderFragment? = null
    private val mSubtitleRepository: SubTitleRepository = SubTitleRepository(context)
    private lateinit var mParse: Parse
    var mManga: Manga? = null
    private var mLanguages: MutableSet<Languages> = arraySetOf()
    private var mComboListInternal: HashMap<String, Chapter> = hashMapOf()
    private var mComboListSelected: HashMap<String, Chapter> = hashMapOf()
    private var mListPages: HashMap<String, Page> = hashMapOf()
    var pathSubtitle: String = ""

    private var mChaptersKeys: MutableLiveData<List<String>> = MutableLiveData()
    var chaptersKeys: LiveData<List<String>> = mChaptersKeys
    private var mPagesKeys: MutableLiveData<List<String>> = MutableLiveData()
    var pagesKeys: LiveData<List<String>> = mPagesKeys

    private var mChapterSelected: MutableLiveData<Chapter> = MutableLiveData()
    var chapterSelected: LiveData<Chapter> = mChapterSelected
    private var mPageSelected: MutableLiveData<Page> = MutableLiveData()
    var pageSelected: LiveData<Page> = mPageSelected
    private var mTextSelected: MutableLiveData<Text> = MutableLiveData()
    var textSelected: LiveData<Text> = mTextSelected

    private var mForceExpandFloatingPopup: MutableLiveData<Boolean> = MutableLiveData(true)
    var forceExpandFloatingPopup: LiveData<Boolean> = mForceExpandFloatingPopup

    private var mSelectedSubTitle: MutableLiveData<SubTitle> = MutableLiveData()
    var selectedSubtitle: LiveData<SubTitle> = mSelectedSubTitle

    private lateinit var mSubtitleLang: Languages
    private lateinit var mTranslateLang: Languages
    private var labelChapter: String =
        context.resources.getString(R.string.popup_reading_subtitle_chapter)
    private var labelExtra: String =
        context.resources.getString(R.string.popup_reading_subtitle_extra)

    var isSelected = false
    var isNotEmpty = false
    private fun getSubtitle(): HashMap<String, Chapter> =
        if (isSelected) mComboListSelected else mComboListInternal

    init {
        val sharedPreferences = GeneralConsts.getSharedPreferences(context)
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
                "Error, preferences languages not loaded - " + e.message
            )
        }
    }

    companion object {
        private lateinit var INSTANCE: SubTitleController

        fun getInstance(context: Context): SubTitleController {
            if (!::INSTANCE.isInitialized)
                INSTANCE = SubTitleController(context)
            return INSTANCE
        }
    }

    fun initialize(chapterKey: String, pageKey: String) {
        if (chapterKey.isEmpty())
            return

        selectedSubtitle(chapterKey)
        selectedPage(pageKey)
    }

    fun getPageKey(page: Page): String =
        page.number.toString().padStart(3, '0') + " " + page.name

    fun getChapterKey(chapter: Chapter): String {
        val number = if ((chapter.chapter % 1).compareTo(0) == 0)
            "%.0f".format(chapter.chapter)
        else
            "%.1f".format(chapter.chapter)
        val label = if (chapter.extra) labelExtra else labelChapter
        return chapter.language.name + " - " + label + " " + number.padStart(2, '0')
    }


    fun getListChapter(parse: Parse) =
        runBlocking { // this: CoroutineScope
            launch { // launch a new coroutine and continue
                mParse = parse
                val listJson: List<String> = mParse.getSubtitles()
                isSelected = false
                getChapterFromJson(listJson)
            }
        }

    fun getChapterFromJson(listJson: List<String>, isSelected: Boolean = false) {
        this.isSelected = isSelected
        mLanguages.clear()
        getSubtitle().clear()
        isNotEmpty = listJson.isNotEmpty()
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
            getSubtitle()[getChapterKey(chapter)] = chapter
        }

        mChaptersKeys.value = getSubtitle().keys.toTypedArray().sorted()
    }

    fun findSubtitle() {
        val currentPage = ReaderFragment.mCurrentPage

        if (currentPage < 0 || mParse.numPages() < currentPage) {
            Toast.makeText(
                context,
                context.resources.getString(R.string.popup_reading_subtitle_not_find_subtitle),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val image: InputStream = mParse.getPage(currentPage)
        val hash = String(Hex.encodeHex(DigestUtils.md5(image)))

        var pageName: String? = mParse.getPagePath(currentPage)

        if (chapterSelected.value == null || pageName == null || pageName.isEmpty()) {
            Toast.makeText(
                context,
                context.resources.getString(R.string.popup_reading_subtitle_not_find_subtitle),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        pageName = if (pageName.contains('/'))
            pageName.substringAfterLast("/")
        else
            pageName.substringAfterLast('\\')

        var chapterKey = ""
        var pageKey = ""
        var pageNumber = 0
        val subtitles = getSubtitle()
        val keys = run {
            val selectedLanguage = chapterSelected.value!!.language
            subtitles.keys.filter { it.contains(selectedLanguage.name) }
        }

        for (k in keys) {
            var find = false
            for (p in subtitles[k]?.pages!!) {
                if (p.name.equals(pageName, true) || p.hash == hash) {
                    chapterKey = k
                    pageKey = getPageKey(p)
                    pageNumber = p.number
                    find = true
                    break
                }
            }
            if (find)
                break
        }

        if (chapterKey.isNotEmpty()) {
            mSelectedSubTitle.value?.chapterKey = chapterKey
            mSelectedSubTitle.value?.pageKey =
                if (mListPages.keys.contains(pageKey)) pageKey else ""
            updatePageSelect()
            initialize(chapterKey, pageKey)

            val text: String =
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
        var pageName: String? = mParse.getPagePath(pageNumber)

        pageName = if (pageName!!.contains('/'))
            pageName.substringAfterLast("/")
        else
            pageName.substringAfterLast('\\')

        var chapterKey = ""
        var number = 0
        val subtitles = getSubtitle()
        val keys = subtitles.keys

        for (k in keys) {
            var find = false
            for (p in subtitles[k]?.pages!!) {
                if (p.name.equals(pageName, true) || p.hash == hash) {
                    chapterKey = k
                    number = p.number
                    find = true
                    break
                }
            }
            if (find)
                break
        }

        return if (chapterKey.isNotEmpty()) {
            SubTitle(
                manga.id!!,
                mSubtitleLang,
                chapterKey,
                "",
                number,
                pathSubtitle,
                chapterSelected.value
            )
        } else
            SubTitle(
                manga.id!!,
                mSubtitleLang,
                "",
                "",
                number,
                pathSubtitle,
                null
            )
    }

    fun changeLanguage() =
        runBlocking { // this: CoroutineScope
            launch {
                if (chaptersKeys.value == null || chaptersKeys.value!!.isEmpty()) {
                    Toast.makeText(
                        context,
                        context.resources.getString(R.string.popup_reading_subtitle_list_empty),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                var language = mSubtitleLang
                var chapterSelect = ""
                var pageSelect = ""
                if (selectedSubtitle.value != null && selectedSubtitle.value!!.chapterKey.isNotEmpty()) {
                    language = if (selectedSubtitle.value!!.language.compareTo(mTranslateLang) == 0)
                        mSubtitleLang
                    else
                        mTranslateLang

                    chapterSelect = selectedSubtitle.value!!.chapterKey
                    if (chapterSelect.isNotEmpty())
                        chapterSelect = chapterSelect.substringAfterLast("-").trim()

                    pageSelect = selectedSubtitle.value!!.pageKey

                    if (pageSelect.isNotEmpty())
                        pageSelect = pageSelect.substringBefore(" ").trim()
                }

                var key = ""
                var first = ""
                for (chapter in mChaptersKeys.value!!) {
                    if (chapter.contains(language.name)) {
                        if (chapterSelect.isNotEmpty()) {
                            if (chapter.contains(chapterSelect)) {
                                key = chapter
                                break
                            } else if (first.isEmpty())
                                first = chapter
                        } else {
                            key = chapter
                            break
                        }
                    }
                }

                if (key.isEmpty() && first.isEmpty()) {
                    Toast.makeText(
                        context,
                        context.resources.getString(R.string.popup_reading_subtitle_chapter_not_found) + " (" +
                                language.name + ")",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                if (key.isEmpty())
                    selectedSubtitle(first)
                else
                    selectedSubtitle(key)

                if (mSelectedSubTitle.value != null)
                    mSelectedSubTitle.value?.language = language

                if (mPagesKeys.value == null || pageSelect.isEmpty())
                    return@launch

                key = ""
                first = ""
                for (page in mPagesKeys.value!!) {
                    if (first.isEmpty())
                        first = page

                    if (page.substringBefore(" ").contains(pageSelect)) {
                        key = page
                        break
                    }
                }

                if (key.isEmpty())
                    selectedPage(first)
                else
                    selectedPage(key)
            }
        }

    fun changeSubtitleInReader(manga: Manga, pageNumber: Int) =
        runBlocking { // this: CoroutineScope
            launch {
                mManga = manga
                if (mSelectedSubTitle.value == null || mSelectedSubTitle.value?.id == null) {
                    mSelectedSubTitle.value = mSubtitleRepository.findByIdManga(manga.id!!)

                    if (mSelectedSubTitle.value == null)
                        try {
                            mSelectedSubTitle.value = findSubtitle(manga, pageNumber)
                        } catch (e: java.lang.Exception) {
                            Log.e(
                                GeneralConsts.TAG.LOG,
                                "Error, subtitle not founded in file. " + e.message
                            )
                            return@launch
                        }
                }

                if (mSelectedSubTitle.value?.pageCount != pageNumber) {
                    var differ = pageNumber - mSelectedSubTitle.value?.pageCount!!
                    if (differ == 0) differ = 1
                    val run = if (mSelectedSubTitle.value?.pageCount!! < pageNumber)
                        getNextSelectPage(differ)
                    else
                        getBeforeSelectPage(false, differ * -1)

                    if (!run) {
                        if (mSelectedSubTitle.value?.pageCount!! < pageNumber)
                            getNextSelectSubtitle()
                        else
                            getBeforeSelectSubtitle()
                    }
                }

                mSelectedSubTitle.value?.pageCount = pageNumber
                updatePageSelect()
            }
        }

    private fun updatePageSelect() {
        if (mSelectedSubTitle.value != null)
            mSubtitleRepository.save(mSelectedSubTitle.value!!)
    }

    ///////////////////// DRAWING //////////////
    private var imageBackup: Bitmap? = null
    private var isDrawing = false
    private var target: MyTarget? = null
    fun drawSelectedText() {
        if (mReaderFragment == null || pageSelected.value == null || pageSelected.value?.texts!!.isEmpty())
            return

        val view: PageImageView = mReaderFragment!!.getCurrencyImageView() ?: return
        if (isDrawing) {
            view.setImageBitmap(imageBackup)
            isDrawing = false
        } else {
            target = MyTarget(view)
            mReaderFragment!!.loadImage(target!!, ReaderFragment.mCurrentPage, false)
        }
    }

    inner class MyTarget(layout: View) : Target {
        private val mLayout: WeakReference<View> = WeakReference(layout)

        override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
            val layout = mLayout.get() ?: return
            val iv = layout.findViewById<View>(R.id.page_image_view) as ImageView
            imageBackup = bitmap
            if (imageBackup == null)
                return

            val image: Bitmap = imageBackup!!.copy(imageBackup!!.config, true)
            val canvas = Canvas(image)
            val paint = Paint()
            paint.color = Color.RED
            paint.strokeWidth = 3f
            paint.textSize = 50f
            for (text in pageSelected.value!!.texts) {
                paint.style = Paint.Style.FILL
                canvas.drawText(
                    text.sequence.toString(),
                    text.x1.toFloat(),
                    text.y1.toFloat(),
                    paint
                )
                paint.style = Paint.Style.STROKE
                canvas.drawRect(
                    text.x1.toFloat(),
                    text.y1.toFloat(),
                    text.x2.toFloat(),
                    text.y2.toFloat(),
                    paint
                )
            }
            iv.setImageBitmap(image)
            isDrawing = true
        }

        override fun onBitmapFailed(e: Exception, errorDrawable: Drawable?) {
            Log.e(GeneralConsts.TAG.LOG, "${e.message}")
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
        }

    }

    ///////////////////////// LANGUAGE ///////////////
    fun clearLanguage() {
        val sharedPreferences = GeneralConsts.getSharedPreferences(context)
        mTranslateLang = Languages.valueOf(
            sharedPreferences.getString(
                GeneralConsts.KEYS.SUBTITLE.TRANSLATE,
                Languages.PORTUGUESE.toString()
            )!!
        )
    }

    fun selectedLanguage(language: Languages) {
        mTranslateLang = language
    }

    ///////////////////////// VOLUME ///////////////
    fun clearExternalSubtitlesSelected() {
        isSelected = false
        mChaptersKeys.value = mComboListInternal.keys.toTypedArray().sorted()
        clearSubtitlesSelected()
    }

    fun clearSubtitlesSelected() {
        mChapterSelected.value = null
        mPageSelected.value = null
        mTextSelected.value = null
        mSelectedSubTitle.value?.language = Languages.JAPANESE
    }

    fun selectedSubtitle(key: String) {
        if (key.isNotEmpty() && getSubtitle().containsKey(key))
            setChapter(getSubtitle()[key])
    }

    private fun getNextSelectSubtitle(): Boolean {
        val index: Int = if (mChaptersKeys.value!!.isNotEmpty()
        ) mChaptersKeys.value!!.indexOf(mSelectedSubTitle.value?.chapterKey!!)
            .plus(1) else 0

        return if (getSubtitle().keys.size >= index && getSubtitle().containsKey(mChaptersKeys.value!![index])) {
            setChapter(getSubtitle()[mChaptersKeys.value!![index]])
            true
        } else
            false
    }

    private fun getBeforeSelectSubtitle(): Boolean {
        val index: Int = if (mChaptersKeys.value!!.isNotEmpty()
        ) mChaptersKeys.value!!.indexOf(mSelectedSubTitle.value?.chapterKey!!)
            .minus(1) else 0

        return if (index >= 0 && getSubtitle().containsKey(mChaptersKeys.value!![index])) {
            setChapter(getSubtitle()[mChaptersKeys.value!![index]])
            true
        } else
            false
    }

    ///////////////////// CHAPTER //////////////
    private fun setChapter(chapter: Chapter?) {
        mChapterSelected.value = chapter
        mListPages.clear()
        mSelectedSubTitle.value?.chapter = chapter
        mSelectedSubTitle.value?.language = Languages.JAPANESE
        if (chapterSelected.value != null) {
            chapterSelected.value!!.pages.forEach { mListPages[getPageKey(it)] = it }
            mPagesKeys.value = mListPages.keys.toTypedArray().sorted()
            mSelectedSubTitle.value?.chapterKey = getChapterKey(mChapterSelected.value!!)
            mSelectedSubTitle.value!!.language = chapterSelected.value!!.language
            setPage(false, chapterSelected.value!!.pages[0])
        } else
            mSelectedSubTitle.value?.chapterKey = ""
    }

    private fun setPage(lastText: Boolean, page: Page?) {
        mOriginalSize = null
        isDrawing = false
        mPageSelected.value = page
        mSelectedSubTitle.value?.pageKey =
            if (mPageSelected.value == null) "" else getPageKey(mPageSelected.value!!)
        if (pageSelected.value!!.texts.isNotEmpty()) {
            val text = if (lastText) pageSelected.value!!.texts.last()
            else pageSelected.value!!.texts.first()
            setText(text)
        } else
            setText(null)

        updatePageSelect()
    }

    private fun setText(text: Text?) {
        mTextSelected.value = text
    }

    fun selectedPage(index: String) {
        if (chapterSelected.value != null) {
            if (mListPages.containsKey(index))
                setPage(false, mListPages[index])
        }
    }

    fun getNextSelectPage(differ: Int = 1): Boolean {
        if (chapterSelected.value == null)
            return true

        val index: Int =
            if (mSelectedSubTitle.value?.pageKey!!.isNotEmpty())
                mPagesKeys.value!!.indexOf(mSelectedSubTitle.value?.pageKey!!)
                    .plus(differ) else 0

        return if (mListPages.size > index && mListPages.containsKey(mPagesKeys.value!![index])) {
            setPage(false, mListPages[mPagesKeys.value!![index]])
            true
        } else
            false
    }

    private fun getBeforeSelectPage(lastText: Boolean, differ: Int = 1): Boolean {
        if (chapterSelected.value == null)
            return true

        val index: Int =
            if (mSelectedSubTitle.value?.pageKey!!.isNotEmpty()) mPagesKeys.value!!.indexOf(
                mSelectedSubTitle.value?.pageKey!!
            ).minus(differ) else 0

        return if (index >= 0 && mListPages.containsKey(mPagesKeys.value!![index])) {
            setPage(lastText, mListPages[mPagesKeys.value!![index]])
            true
        } else
            false
    }

    fun getNextText(): Boolean {
        if (pageSelected.value == null)
            return true

        val index: Int =
            if (textSelected.value != null) pageSelected.value!!.texts.indexOf(
                textSelected.value
            )
                .plus(1) else 0

        return if (pageSelected.value!!.texts.size > index) {
            setText(pageSelected.value!!.texts[index])
            true
        } else {
            getNextSelectPage()
            false
        }
    }

    fun getBeforeText(): Boolean {
        if (pageSelected.value == null)
            return true

        val index: Int =
            if (textSelected.value != null) pageSelected.value!!.texts.indexOf(
                textSelected.value
            ).minus(1) else 0

        return if (index >= 0 && pageSelected.value!!.texts.isNotEmpty()) {
            setText(pageSelected.value!!.texts[index])
            true
        } else {
            getBeforeSelectPage(true)
            false
        }
    }

    private var mOriginalSize: FloatArray? = null
    fun selectTextByCoordinate(coord: FloatArray) {
        if (pageSelected.value == null)
            return

        val point = if (!isDrawing && imageBackup == null) {
            if (mOriginalSize == null) {
                val input = BufferedInputStream(mParse.getPage(ReaderFragment.mCurrentPage))
                val image = BitmapFactory.decodeStream(input)
                //Position 2 has a new image size
                mOriginalSize = if (image != null && image.width > ReaderConsts.READER.MAX_PAGE_WIDTH)
                    floatArrayOf(image.width.toFloat(), image.height.toFloat())
                else
                    floatArrayOf(coord[2], coord[3])

                //Log.e(GeneralConsts.TAG.LOG, "${coord[0]} ${coord[1]} / ${coord[2]} ${coord[3]} - ${mOriginalSize!![0]} - ${mOriginalSize!![1]} - ${image.width}  ${image.height} - ${ReaderConsts.READER.MAX_PAGE_WIDTH}")
            }

            Point((coord[0] / coord[2] * mOriginalSize!![0]).toInt(), (coord[1] / coord[3] * mOriginalSize!![1]).toInt())
        } else
            Point(coord[0].toInt(), coord[1].toInt())

        pageSelected.value!!.texts.forEach {
            //Log.e(GeneralConsts.TAG.LOG, "${point.x}  ${point.y} - ${it.x1}  ${it.x2} / ${it.y1}  ${it.y2}")
            if (it.x1 <= point.x && it.x2 >= point.x && it.y1 <= point.y && it.y2 >= point.y) {
                setText(it)
                mForceExpandFloatingPopup.value = !mForceExpandFloatingPopup.value!!
                return@forEach
            }
        }
    }
}
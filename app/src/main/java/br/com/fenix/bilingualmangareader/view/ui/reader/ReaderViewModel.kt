package br.com.fenix.bilingualmangareader.view.ui.reader

import android.app.Application
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.model.entity.Pages
import br.com.fenix.bilingualmangareader.model.enums.Languages
import br.com.fenix.bilingualmangareader.service.parses.Parse
import br.com.fenix.bilingualmangareader.service.parses.ParseFactory
import br.com.fenix.bilingualmangareader.service.parses.RarParse
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.util.constants.ReaderConsts
import br.com.fenix.bilingualmangareader.util.helpers.Util
import com.squareup.picasso.Transformation
import jp.wasabeef.picasso.transformations.ColorFilterTransformation
import jp.wasabeef.picasso.transformations.GrayscaleTransformation
import jp.wasabeef.picasso.transformations.gpu.InvertFilterTransformation
import jp.wasabeef.picasso.transformations.gpu.SepiaFilterTransformation
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.io.File

class ReaderViewModel(application: Application) : AndroidViewModel(application) {

    private val mContext = application.applicationContext
    private var mPreferences: SharedPreferences? = GeneralConsts.getSharedPreferences(mContext)

    private val mLOGGER = LoggerFactory.getLogger(ReaderViewModel::class.java)

    private var mFilters: MutableLiveData<MutableList<Transformation>> = MutableLiveData(arrayListOf())
    val filters: LiveData<MutableList<Transformation>> = mFilters

    private var mChapters: MutableLiveData<MutableList<Pages>> = MutableLiveData(arrayListOf())
    val chapters: LiveData<MutableList<Pages>> = mChapters

    private var mCustomFilter: MutableLiveData<Boolean> = MutableLiveData(false)
    val customFilter: LiveData<Boolean> = mCustomFilter

    private var mColorRed: MutableLiveData<Int> = MutableLiveData(0)
    val colorRed: LiveData<Int> = mColorRed
    private var mColorGreen: MutableLiveData<Int> = MutableLiveData(0)
    val colorGreen: LiveData<Int> = mColorGreen
    private var mColorBlue: MutableLiveData<Int> = MutableLiveData(0)
    val colorBlue: LiveData<Int> = mColorBlue
    private var mColorAlpha: MutableLiveData<Int> = MutableLiveData(0)
    val colorAlpha: LiveData<Int> = mColorAlpha

    private var mGrayScale: MutableLiveData<Boolean> = MutableLiveData(false)
    val grayScale: LiveData<Boolean> = mGrayScale
    private var mInvertColor: MutableLiveData<Boolean> = MutableLiveData(false)
    val invertColor: LiveData<Boolean> = mInvertColor
    private var mBlueLight: MutableLiveData<Boolean> = MutableLiveData(false)
    val blueLight: LiveData<Boolean> = mBlueLight
    private var mBlueLightAlpha: MutableLiveData<Int> = MutableLiveData(100)
    val blueLightAlpha: LiveData<Int> = mBlueLightAlpha
    private var mSepia: MutableLiveData<Boolean> = MutableLiveData(false)
    val sepia: LiveData<Boolean> = mSepia

    private var mOcrItem: MutableLiveData<ArrayList<String>> = MutableLiveData(arrayListOf())
    var ocrItem: LiveData<ArrayList<String>> = mOcrItem

    var mLanguageOcr: Languages? = null
    var mIsAlertSubtitle = false

    private var mBlueLightColor = Color.argb(mBlueLightAlpha.value!!, 255, 50, 0)

    init {
        loadPreferences()
    }

    fun changeCustomFilter(value: Boolean) {
        mCustomFilter.value = value
        generateFilters()
    }

    fun changeGrayScale(value: Boolean) {
        mGrayScale.value = value
        generateFilters()
    }

    fun changeInvertColor(value: Boolean) {
        mInvertColor.value = value
        generateFilters()
    }

    fun changeBlueLight(value: Boolean) {
        mBlueLight.value = value
        generateFilters()
    }

    fun changeSepia(value: Boolean) {
        mSepia.value = value
        generateFilters()
    }

    fun changeColorsFilter(red: Int, green: Int, blue: Int, alpha: Int) {
        mColorRed.value = red
        mColorGreen.value = green
        mColorBlue.value = blue
        mColorAlpha.value = alpha
        generateFilters()
    }

    fun changeBlueLightAlpha(value: Int) {
        mBlueLightAlpha.value = value
        mBlueLightColor = Color.argb(mBlueLightAlpha.value!!, 255, 50, 0)
        generateFilters()
    }

    fun clearOcrItem() {
        mOcrItem.value = arrayListOf()
    }

    fun addOcrItem(text: ArrayList<String>) {
        mOcrItem.value?.addAll(text)
        mOcrItem.value = mOcrItem.value // Force live data in add item
    }

    fun addOcrItem(text: String?) {
        if (text == null || mOcrItem.value == null) return

        if (!mOcrItem.value!!.contains(text)) {
            mOcrItem.value!!.add(text)
            mOcrItem.value = mOcrItem.value // Force live data in add item
        }
    }

    private fun loadPreferences() {
        mCustomFilter.value = mPreferences!!.getBoolean(
            GeneralConsts.KEYS.COLOR_FILTER.CUSTOM_FILTER,
            false
        )

        mColorRed.value = mPreferences!!.getInt(
            GeneralConsts.KEYS.COLOR_FILTER.COLOR_RED,
            0
        )

        mColorGreen.value = mPreferences!!.getInt(
            GeneralConsts.KEYS.COLOR_FILTER.COLOR_BLUE,
            0
        )

        mColorBlue.value = mPreferences!!.getInt(
            GeneralConsts.KEYS.COLOR_FILTER.COLOR_GREEN,
            0
        )

        mColorAlpha.value = mPreferences!!.getInt(
            GeneralConsts.KEYS.COLOR_FILTER.COLOR_ALPHA,
            0
        )

        mGrayScale.value = mPreferences!!.getBoolean(
            GeneralConsts.KEYS.COLOR_FILTER.GRAY_SCALE,
            false
        )

        mInvertColor.value = mPreferences!!.getBoolean(
            GeneralConsts.KEYS.COLOR_FILTER.INVERT_COLOR,
            false
        )

        mSepia.value = mPreferences!!.getBoolean(
            GeneralConsts.KEYS.COLOR_FILTER.SEPIA,
            false
        )

        mBlueLight.value = mPreferences!!.getBoolean(
            GeneralConsts.KEYS.COLOR_FILTER.BLUE_LIGHT,
            false
        )

        mBlueLightAlpha.value = mPreferences!!.getInt(
            GeneralConsts.KEYS.COLOR_FILTER.BLUE_LIGHT_ALPHA,
            100
        )

        generateFilters()
    }

    private fun savePreferences() {
        with(mPreferences?.edit()) {
            this!!.putBoolean(
                GeneralConsts.KEYS.COLOR_FILTER.CUSTOM_FILTER,
                mCustomFilter.value!!
            )
            this.putInt(
                GeneralConsts.KEYS.COLOR_FILTER.COLOR_RED,
                mColorRed.value!!
            )
            this.putInt(
                GeneralConsts.KEYS.COLOR_FILTER.COLOR_BLUE,
                mColorGreen.value!!
            )
            this.putInt(
                GeneralConsts.KEYS.COLOR_FILTER.COLOR_GREEN,
                mColorBlue.value!!
            )
            this.putInt(
                GeneralConsts.KEYS.COLOR_FILTER.COLOR_ALPHA,
                mColorAlpha.value!!
            )
            this.putBoolean(
                GeneralConsts.KEYS.COLOR_FILTER.GRAY_SCALE,
                mGrayScale.value!!
            )
            this.putBoolean(
                GeneralConsts.KEYS.COLOR_FILTER.INVERT_COLOR,
                mInvertColor.value!!
            )
            this.putBoolean(
                GeneralConsts.KEYS.COLOR_FILTER.SEPIA,
                mSepia.value!!
            )
            this.putBoolean(
                GeneralConsts.KEYS.COLOR_FILTER.BLUE_LIGHT,
                mBlueLight.value!!
            )
            this.putInt(
                GeneralConsts.KEYS.COLOR_FILTER.BLUE_LIGHT_ALPHA,
                mBlueLightAlpha.value!!
            )
            this.commit()
        }
    }

    private fun generateFilters() {
        savePreferences()
        val filters: MutableList<Transformation> = arrayListOf()

        if (mCustomFilter.value!!) {
            val color = Color.argb(mColorAlpha.value!!, mColorRed.value!!, mColorGreen.value!!, mColorBlue.value!!)
            filters.add(ColorFilterTransformation(color))
        }

        if (mBlueLight.value!!)
            filters.add(ColorFilterTransformation(mBlueLightColor))

        if (mGrayScale.value!!)
            filters.add(GrayscaleTransformation())

        if (mInvertColor.value!!)
            filters.add(InvertFilterTransformation(mContext))

        if (mSepia.value!!)
            filters.add(SepiaFilterTransformation(mContext))

        mFilters.value = filters
    }

    fun clearChapter() {
        mChapters.value = arrayListOf()
    }

    fun selectPage(number: Int) {
        if (mChapters.value == null || mChapters.value!!.isEmpty())
            return

        mChapters.value?.forEach { it.isSelected = it.number == number }
        mChapters.value = mChapters.value
    }

    private fun loadImage(parse: Parse, page: Int) : Bitmap? {
        try {
            var stream = parse.getPage(page)
            val option = BitmapFactory.Options()
            option.inJustDecodeBounds = true
            BitmapFactory.decodeStream(stream, null, option)
            option.inSampleSize = Util.calculateInSampleSize(
                option,
                ReaderConsts.PAGE.PAGE_CHAPTER_LIST_WIDTH,
                ReaderConsts.PAGE.PAGE_CHAPTER_LIST_HEIGHT
            )
            option.inJustDecodeBounds = false
            Util.closeInputStream(stream)

            stream = parse.getPage(page)
            val image = BitmapFactory.decodeStream(stream, null, option)
            Util.closeInputStream(stream)
            return image
        } catch (m: OutOfMemoryError) {
            System.gc()
            mLOGGER.error("Memory full, cleaning", m)
        } catch (e: Exception) {
            mLOGGER.error("Error to load image page", e)
        }
        return null
    }

    fun loadChapter(manga: Manga?, number: Int, refresh: (Int) -> (Unit)): Boolean {
        if (manga == null) {
            clearChapter()
            return false
        }

        var loaded = false
        if (mChapters.value == null || mChapters.value!!.isEmpty()) {
            val parse = ParseFactory.create(manga.file) ?: return loaded
            loaded = true
            val list = arrayListOf<Pages>()
            if (parse is RarParse) {
                val cacheDir = File(GeneralConsts.getCacheDir(mContext), GeneralConsts.CACHE_FOLDER.IMAGE)
                (parse as RarParse?)!!.setCacheDirectory(cacheDir)
            }

            for (i in 0 until parse.numPages())
                list.add(Pages(Util.getNameFromPath(parse.getPagePath(i) ?: ""), i, i+1))

            mChapters.value = list

            CoroutineScope(Dispatchers.IO).launch {
                val deferred = async {
                    if (number > 5) {
                        for (i in number - 5 until list.size){
                            val page = list[i]
                            page.image = loadImage(parse, page.number)
                            withContext(Dispatchers.Main) { refresh(page.number) }
                        }

                        for (i in number - 5 downTo 0){
                            val page = list[i]
                            page.image = loadImage(parse, page.number)
                            withContext(Dispatchers.Main) { refresh(page.number) }
                        }
                    } else
                        for (page in list) {
                            page.image = loadImage(parse, page.number)
                            withContext(Dispatchers.Main) { refresh(page.number) }
                        }

                    Util.destroyParse(parse)
                }

                deferred.await()
                withContext(Dispatchers.Main) {
                    mChapters.value = list
                }
            }
        }

        return loaded
    }

}
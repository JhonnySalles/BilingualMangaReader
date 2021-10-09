package br.com.fenix.mangareader.view.ui.reader

import android.app.Application
import android.content.SharedPreferences
import android.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.com.fenix.mangareader.util.constants.GeneralConsts
import com.squareup.picasso.Transformation
import jp.wasabeef.picasso.transformations.ColorFilterTransformation
import jp.wasabeef.picasso.transformations.GrayscaleTransformation
import jp.wasabeef.picasso.transformations.gpu.InvertFilterTransformation

class ReaderViewModel(application: Application) : AndroidViewModel(application) {

    private val mContext = application.applicationContext
    private var mPreferences: SharedPreferences? = GeneralConsts.getSharedPreferences(mContext)

    private var mFilters: MutableLiveData<MutableList<Transformation>> = MutableLiveData(arrayListOf())
    val filters: LiveData<MutableList<Transformation>> = mFilters

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

    init {
        loadPreferences()
    }

    fun changeCustomFilter(value : Boolean) {
        mCustomFilter.value = value
        generateFilters()
    }

    fun changeGrayScale(value : Boolean) {
        mGrayScale.value = value
        generateFilters()
    }

    fun changeInvertColor(value : Boolean) {
        mInvertColor.value = value
        generateFilters()
    }

    fun changeColorsFilter(red : Int, green : Int, blue : Int, alpha : Int) {
        mColorRed.value = red
        mColorGreen.value = green
        mColorBlue.value = blue
        mColorAlpha.value = alpha

        if (mCustomFilter.value!!)
            generateFilters()
    }

    fun loadPreferences() {
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

        generateFilters()
    }

    fun savePreferences() {
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
            this.commit()
        }
    }

    private fun generateFilters() {
        savePreferences()
        val filters : MutableList<Transformation>  = arrayListOf()

        if (mCustomFilter.value!!) {
            val color = Color.argb(mColorAlpha.value!!, mColorRed.value!!, mColorGreen.value!!, mColorBlue.value!!)
            filters.add(ColorFilterTransformation(color))
        }

        if (mGrayScale.value!!)
            filters.add(GrayscaleTransformation())

        if (mInvertColor.value!!)
            filters.add(InvertFilterTransformation(mContext))

        mFilters.value = filters
    }


}
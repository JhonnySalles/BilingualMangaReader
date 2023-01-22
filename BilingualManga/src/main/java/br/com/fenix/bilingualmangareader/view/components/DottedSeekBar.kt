package br.com.fenix.bilingualmangareader.view.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.core.graphics.drawable.toBitmap
import br.com.fenix.bilingualmangareader.R


/**
 * Seek bar with dots on it on specific time / percent
 */
class DottedSeekBar : androidx.appcompat.widget.AppCompatSeekBar {
    /** Int values which corresponds to dots  */
    private var mDotsPositions: IntArray = intArrayOf()

    /** Drawable for dot  */
    private var mDotMark: Drawable? = null

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs)
    }

    /**
     * Initializes Seek bar extended attributes from xml
     *
     * @param attributeSet [AttributeSet]
     */
    private fun init(attributeSet: AttributeSet?) {
        val attrsArray =
            context.obtainStyledAttributes(attributeSet, R.styleable.DottedSeekBar, 0, 0)
        val dotsArrayResource =
            attrsArray.getResourceId(R.styleable.DottedSeekBar_dots_positions, 0)
        if (0 != dotsArrayResource)
            mDotsPositions = resources.getIntArray(dotsArrayResource)

        val dotDrawableId = attrsArray.getResourceId(R.styleable.DottedSeekBar_dots_drawable, 0)
        if (0 != dotDrawableId)
            mDotMark = resources.getDrawable(dotDrawableId, context.theme)
    }

    /**
     * @param dots to be displayed on this SeekBar
     */
    fun setDots(dots: IntArray) {
        mDotsPositions = dots
        invalidate()
    }

    /**
     * @param dotsResource resource id to be used for dots drawing
     */
    fun setDotsDrawable(dotsResource: Int) {
        mDotMark = resources.getDrawable(dotsResource, context.theme)
        invalidate()
    }

    @Synchronized
    override fun onDraw(canvas: Canvas) {
        if (mDotsPositions.isNotEmpty() && mDotMark != null) {
            val w: Int = mDotMark!!.intrinsicWidth
            val h: Int = mDotMark!!.intrinsicHeight
            val halfW = if (w >= 0) w / 2 else 1
            val halfH = if (h >= 0) h / 2 else 1
            mDotMark!!.setBounds(-halfW, -halfH, halfW, halfH)

            val padding = paddingLeft - (thumb.intrinsicHeight / 6)
            val range = (max - min).toFloat()
            val available = (measuredWidth - paddingLeft - paddingRight)
            val image = mDotMark!!.toBitmap()
            for (position in mDotsPositions) {
                val scale : Float = if (range > 0) (position - min) / range else 0f
                val step = (available * scale + 0.5f)
                canvas.drawBitmap(image, padding + step, 5f, null)
            }
        }

        super.onDraw(canvas)
    }
}
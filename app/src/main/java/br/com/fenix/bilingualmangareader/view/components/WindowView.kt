package br.com.fenix.bilingualmangareader.view.components

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.RelativeLayout
import androidx.core.view.GestureDetectorCompat

class WindowView @JvmOverloads constructor(context: Context, attrs:AttributeSet? = null, defStyleAttr: Int =0): RelativeLayout(context, attrs, defStyleAttr) {

    private var mWindowListener: WindowListener? = null
    private var mDetector: GestureDetectorCompat? = null

    fun setWindowListener(windowListener: WindowListener) {
        mWindowListener = windowListener
    }

    fun setDetector(detector: GestureDetectorCompat?) {
        mDetector = detector
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        mDetector?.onTouchEvent(e)
        return mWindowListener?.onTouch(e)?: false
    }
}
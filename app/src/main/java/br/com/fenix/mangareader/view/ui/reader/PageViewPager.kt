package br.com.fenix.mangareader.view.ui.reader

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class PageViewPager(context: Context, attributeSet: AttributeSet) : ViewPager(context, attributeSet) {
    var mStartX = 0f
    var mSwipeOutListener: OnSwipeOutListener? = null

    interface OnSwipeOutListener {
        fun onSwipeOutAtStart()
        fun onSwipeOutAtEnd()
    }

    fun setOnSwipeOutListener(listener: OnSwipeOutListener?) {
        mSwipeOutListener = listener
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        performClick()
        if (ev!!.action == MotionEvent.ACTION_UP) {
            val diff = ev.x - mStartX
            if (diff > 0 && currentItem == 0) {
                mSwipeOutListener?.onSwipeOutAtStart()
            } else if (diff < 0 && currentItem == adapter!!.count - 1) {
                mSwipeOutListener?.onSwipeOutAtEnd()
            }
        }
        return super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN)
            mStartX = ev.x
        return super.onInterceptTouchEvent(ev)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
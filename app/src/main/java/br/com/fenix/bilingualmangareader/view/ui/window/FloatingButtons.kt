package br.com.fenix.bilingualmangareader.view.ui.window

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.*
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageButton
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.service.controller.SubTitleController
import br.com.fenix.bilingualmangareader.view.components.ComponentsUtil
import br.com.fenix.bilingualmangareader.view.ui.reader.ReaderActivity
import kotlin.math.abs


class FloatingButtons constructor(private val context: Context, private val activity: AppCompatActivity) {

    private var windowManager: WindowManager? = null
        get() {
            if (field == null) field =
                (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
            return field
        }

    private var mFloatingView: View =
        LayoutInflater.from(context).inflate(R.layout.floating_buttons, null)

    private lateinit var layoutParams: WindowManager.LayoutParams

    private var lastX: Int = 0
    private var lastY: Int = 0
    private var firstX: Int = 0
    private var firstY: Int = 0

    var isShowing = false

    private val mOnFlingListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            if (e1 != null && e2 != null)
                if (abs(e1.x - e2.x) > 150) {
                    if (e2.x > e1.x)
                        moveWindow(false)
                    else if (e2.x < e1.x)
                        moveWindow(true)

                    return false
                }

            return super.onFling(e1, e2, velocityX, velocityY)
        }
    }

    private fun moveWindow(toLeft: Boolean) {
        if (toLeft) {
            mMoveWindow.setImageDrawable(mIconToRight)
            inLeft = true
            layoutParams.x = 10
        } else {
            mMoveWindow.setImageDrawable(mIconToLeft)
            inLeft = false
            layoutParams.x = mRealDisplaySize.x - (mContent.width + 10)
        }

        windowManager?.apply {
            updateViewLayout(mFloatingView, layoutParams)
        }
    }

    private val mOnFlingDetector = GestureDetector(context, mOnFlingListener)

    private val onTouchListener = View.OnTouchListener { view, event ->
        mOnFlingDetector.onTouchEvent(event)
        val totalDeltaX = lastX - firstX
        val totalDeltaY = lastY - firstY

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.rawX.toInt()
                lastY = event.rawY.toInt()
                firstX = lastX
                firstY = lastY
            }
            MotionEvent.ACTION_UP -> {
                //view.performClick()
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.rawX.toInt() - lastX
                val deltaY = event.rawY.toInt() - lastY
                lastX = event.rawX.toInt()
                lastY = event.rawY.toInt()
                if (abs(totalDeltaX) >= 5 || abs(totalDeltaY) >= 5) {
                    if (event.pointerCount == 1) {
                        layoutParams.x += deltaX
                        layoutParams.y += deltaY
                        windowManager?.apply {
                            updateViewLayout(mFloatingView, layoutParams)
                        }
                    }

                    if (layoutParams.x > mMiddle && inLeft) {
                        inLeft = false
                        mMoveWindow.setImageDrawable(mIconToLeft)
                    } else if (layoutParams.x < mMiddle && !inLeft) {
                        inLeft = true
                        mMoveWindow.setImageDrawable(mIconToRight)
                    }
                }
            }
            else -> {  }
        }
        true
    }

    private var mSubTitleController = SubTitleController.getInstance(context)
    private var mContent: LinearLayout
    private var mMoveWindow: AppCompatImageButton
    private var mIconToRight: Drawable?
    private var mIconToLeft: Drawable?

    private var mRealDisplaySize: Point
    private val mMiddle: Int
    private var inLeft = true

    init {
        with(mFloatingView) {
            mContent = this.findViewById(R.id.floating_buttons_content)

            this.findViewById<AppCompatImageButton>(R.id.floating_buttons_close)
                .setOnClickListener { dismiss() }
            this.findViewById<AppCompatImageButton>(R.id.floating_buttons_page_linked)
                .setOnClickListener { mSubTitleController.drawPageLinked()  }
            this.findViewById<AppCompatImageButton>(R.id.floating_buttons_draw_text)
                .setOnClickListener { mSubTitleController.drawSelectedText() }
            this.findViewById<AppCompatImageButton>(R.id.floating_buttons_floating_window)
                .setOnClickListener { (activity as ReaderActivity).openFloatingSubtitle() }
            this.findViewById<AppCompatImageButton>(R.id.floating_buttons_file_link)
                .setOnClickListener { (activity as ReaderActivity).openFileLink() }
            mMoveWindow = this.findViewById(R.id.floating_buttons_move_window)
            mMoveWindow.setOnClickListener { onMove() }

            mIconToRight = AppCompatResources.getDrawable(context, R.drawable.ic_floating_button_change_right)
            mIconToLeft = AppCompatResources.getDrawable(context, R.drawable.ic_floating_button_change_left)

        }

        mFloatingView.setOnTouchListener(onTouchListener)

        val metrics = Resources.getSystem().displayMetrics
        val displaySize = Point(metrics.widthPixels, metrics.heightPixels)
        mRealDisplaySize = displaySize
        mMiddle = mRealDisplaySize.x/2
        inLeft = true

        val layoutType = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_PHONE
        else
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

        layoutParams = WindowManager.LayoutParams().apply {
            format = PixelFormat.TRANSLUCENT
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            type = layoutType
            gravity = Gravity.TOP or Gravity.LEFT
            width = ViewGroup.LayoutParams.WRAP_CONTENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            x = 10
            y = mRealDisplaySize.y/2
        }
    }

    private fun onMove() {
        if (inLeft) {
            mMoveWindow.setImageDrawable(mIconToLeft)
            inLeft = false
            layoutParams.x = mRealDisplaySize.x - (mContent.width + 10)
        } else {
            mMoveWindow.setImageDrawable(mIconToRight)
            inLeft = true
            layoutParams.x = 10
        }

        windowManager?.apply {
            updateViewLayout(mFloatingView, layoutParams)
        }
    }

    fun show() {
        if (ComponentsUtil.canDrawOverlays(context)) {
            dismiss()
            isShowing = true
            windowManager?.addView(mFloatingView, layoutParams)
        }
    }

    fun dismiss() {
        if (isShowing) {
            windowManager?.removeView(mFloatingView)
            isShowing = false
        }
    }

    fun destroy() {
        dismiss()
    }

}
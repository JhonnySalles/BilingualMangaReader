package br.com.fenix.bilingualmangareader.view.ui.window

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.provider.Settings
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.view.GestureDetectorCompat
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.enums.Languages
import br.com.fenix.bilingualmangareader.service.ocr.Tesseract
import br.com.fenix.bilingualmangareader.util.helpers.Util
import br.com.fenix.bilingualmangareader.view.components.OcrProcess
import br.com.fenix.bilingualmangareader.view.components.ResizeView
import br.com.fenix.bilingualmangareader.view.components.WindowListener
import br.com.fenix.bilingualmangareader.view.components.WindowView
import org.slf4j.LoggerFactory


class FloatingWindowOcr constructor(private val context: Context, private val activity: AppCompatActivity) : WindowListener, GestureDetector.OnDoubleTapListener {

    private val mLOGGER = LoggerFactory.getLogger(FloatingWindowOcr::class.java)
    private var windowManager: WindowManager? = null
        get() {
            if (field == null) field =
                (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
            return field
        }

    private var mFloatingView: View =
        LayoutInflater.from(context).inflate(R.layout.floating_window_ocr, null)

    private var layoutParams: WindowManager.LayoutParams

    var isShowing = false
    private var mRealDisplaySize: Point
    private var mDX = 0
    private var mDY = 0
    private val mViewHeight = 0
    private val mViewWidth = 0
    private var minSize = 0

    private var mParamUpdateTimer = System.currentTimeMillis()

    init {
        with(mFloatingView) {
            this@FloatingWindowOcr.mRealDisplaySize = getRealDisplaySizeFromContext()
            this@FloatingWindowOcr.minSize = Util.dpToPx(context, 40)
            this@FloatingWindowOcr.layoutParams = getDefaultParams()

            this.findViewById<AppCompatImageButton>(R.id.window_ocr_close).setOnClickListener { dismiss() }

            val windowView: WindowView = this.findViewById(R.id.window_ocr_clickable)
            val resizeView: ResizeView = this.findViewById(R.id.window_ocr_resize)

            windowView.setWindowListener(this@FloatingWindowOcr as WindowListener)
            resizeView.setWindowListener(this@FloatingWindowOcr as WindowListener)

            val detectorCompat = GestureDetectorCompat(context, this@FloatingWindowOcr as WindowListener)
            windowView.setDetector(detectorCompat)
        }
    }

    private fun getDefaultParams(): WindowManager.LayoutParams {
        val params = WindowManager.LayoutParams()
        params.width = Util.dpToPx(context, 100)
        params.height = Util.dpToPx(context, 100)
        params.type = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_PHONE
        else
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        params.format = PixelFormat.TRANSLUCENT
        params.gravity = Gravity.TOP or Gravity.LEFT
        params.x = mRealDisplaySize.x / 2
        params.y = mRealDisplaySize.y / 2
        return params
    }


    private fun getRealDisplaySizeFromContext(): Point {
        val displaySize = Point()
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getRealSize(displaySize)
        return displaySize
    }

    override fun onTouch(e: MotionEvent): Boolean {
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                mDX = layoutParams.x - e.rawX.toInt()
                mDY = layoutParams.y - e.rawY.toInt()
                return true
            }
            MotionEvent.ACTION_UP -> {
                fixBoxBounds()
                windowManager?.updateViewLayout(mFloatingView, mFloatingView.layoutParams)
                onUp(e)
                return true
            }
        }
        return false
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        if (e1 == null || e2 == null) {
            return false
        }

        layoutParams.x = mDX + e2.rawX.toInt()
        layoutParams.y = mDY + e2.rawY.toInt()
        fixBoxBounds()
        windowManager?.updateViewLayout(mFloatingView, mFloatingView.layoutParams)
        fixBoxBounds()
        return true
    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        return false
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        return false
    }

    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
        if (e?.action == MotionEvent.ACTION_DOWN)
            processTesseract((activity as OcrProcess).getLanguage())

        return false
    }

    fun onUp(e: MotionEvent?): Boolean {
        return false
    }

    override fun onDown(e: MotionEvent?): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent?) {}

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent?) {}

    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
        return false
    }

    override fun onResize(e: MotionEvent): Boolean {
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                layoutParams.width - e.rawX.toInt()
                layoutParams.height - e.rawY.toInt()
                return true
            }
            MotionEvent.ACTION_UP -> {
                fixBoxBounds()
                windowManager?.updateViewLayout(mFloatingView, mFloatingView.layoutParams)
                onUp(e)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                layoutParams.width = mDX + e.rawX.toInt()
                layoutParams.height = mDY + e.rawY.toInt()

                fixBoxBounds()
                val currTime = System.currentTimeMillis()
                if (currTime - mParamUpdateTimer > 50) {
                    mParamUpdateTimer = currTime
                    windowManager?.updateViewLayout(mFloatingView, mFloatingView.layoutParams)
                }
                return true
            }
        }
        return false
    }

    private fun getRealDisplaySize(): Point {
        return Point(mRealDisplaySize)
    }


    private fun getStatusBarHeight(): Int {
        if (mRealDisplaySize.y == mViewHeight) {
            return 0
        }
        var result = 0
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    private fun getViewHeight(): Int {
        return mViewHeight
    }

    private fun getViewWidth(): Int {
        return mViewWidth
    }

    private fun fixBoxBounds() {
        if (layoutParams.x < 0) {
            layoutParams.x = 0
        } else if (layoutParams.x + layoutParams.width > mRealDisplaySize.x) {
            layoutParams.x = mRealDisplaySize.x - layoutParams.width
        }
        if (layoutParams.y < 0) {
            layoutParams.y = 0
        } else if (layoutParams.y + layoutParams.height > mRealDisplaySize.y) {
            layoutParams.y = mRealDisplaySize.y - layoutParams.height - getStatusBarHeight()
        }
        if (layoutParams.width > mRealDisplaySize.x) {
            layoutParams.width = mRealDisplaySize.x
        }
        if (layoutParams.height > mRealDisplaySize.y) {
            layoutParams.height = mRealDisplaySize.y
        }
        if (layoutParams.width < minSize) {
            layoutParams.width = minSize
        }
        if (layoutParams.height < minSize) {
            layoutParams.height = minSize
        }
    }

    fun show() {
        synchronized(this) {
            if (canDrawOverlays(context)) {
                dismiss()
                isShowing = true
                windowManager?.addView(mFloatingView, layoutParams)
            }
        }
    }

    fun dismiss() {
        synchronized(this) {
            if (isShowing) {
                windowManager?.removeView(mFloatingView)
                isShowing = false
            }
        }
    }

    fun destroy() {
        dismiss()
    }

    fun setVisibility(visibility: Int) {
        mFloatingView.visibility = visibility
    }

    fun processTesseract(languages: Languages): String? {
        return try {
            val location = IntArray(2)
            mFloatingView.getLocationInWindow(location)
            val image = (activity as OcrProcess).getImage(location[0], location[1], mFloatingView.width, mFloatingView.height) ?: return null

            val tess = Tesseract.getInstance(context)
            tess.process(languages, image)
        } catch (e: Exception) {
            mLOGGER.error("Error when process tesseract: " + e.message, e)
            e.printStackTrace()
            null
        }
    }

    companion object {
        fun canDrawOverlays(context: Context): Boolean =
            Settings.canDrawOverlays(context)
    }
}
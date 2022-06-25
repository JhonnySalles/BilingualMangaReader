package br.com.fenix.bilingualmangareader.view.ui.window

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.view.GestureDetectorCompat
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.enums.Languages
import br.com.fenix.bilingualmangareader.service.ocr.OcrProcess
import br.com.fenix.bilingualmangareader.service.ocr.Tesseract
import br.com.fenix.bilingualmangareader.view.components.ResizeView
import br.com.fenix.bilingualmangareader.view.components.WindowListener
import br.com.fenix.bilingualmangareader.view.components.WindowView
import org.slf4j.LoggerFactory
import kotlin.math.abs


class FloatingWindowOcr constructor(private val context: Context, private val activity: AppCompatActivity) : WindowListener,
    GestureDetector.OnDoubleTapListener {

    private val mLOGGER = LoggerFactory.getLogger(FloatingWindowOcr::class.java)
    private var windowManager: WindowManager? = null
        get() {
            if (field == null) field =
                (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
            return field
        }

    private var mFloatingView: View =
        LayoutInflater.from(context).inflate(R.layout.floating_window_ocr, null)

    private lateinit var layoutParams: WindowManager.LayoutParams

    var isShowing = false
    private var mRealDisplaySize: Point
    private var mDX = 0
    private var mDY = 0
    private val mViewHeight = 0
    private val mViewWidth = 0
    private var minSize = 0
    private lateinit var mCloseButton: AppCompatImageButton
    private val mDismissCloseButton = Runnable {
        if (mCloseButton.visibility == View.VISIBLE) {
            mCloseButton.animate().alpha(0.0f).setDuration(300L)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        mCloseButton.visibility = View.GONE
                        layoutParams.width -= context.resources.getDimension(R.dimen.floating_ocr_button_close_size)
                            .toInt() + context.resources.getDimension(R.dimen.floating_ocr_button_close_margin).toInt()
                        windowManager?.updateViewLayout(mFloatingView, layoutParams)
                    }
                })
        }
    }
    private var mHandler = Handler(Looper.getMainLooper())

    private var mTouchParamUpdateTimer = System.currentTimeMillis()
    private var mSizeParamUpdateTimer = System.currentTimeMillis()

    init {
        with(mFloatingView) {
            this@FloatingWindowOcr.mRealDisplaySize = getRealDisplaySizeFromContext()
            this@FloatingWindowOcr.minSize = context.resources.getDimension(R.dimen.floating_ocr_min_size).toInt()
            this@FloatingWindowOcr.layoutParams = getDefaultParams()

            mCloseButton = this.findViewById(R.id.window_ocr_close)
            mCloseButton.setOnClickListener { dismiss() }

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
        params.width = context.resources.getDimension(R.dimen.floating_ocr_height).toInt()
        params.height = context.resources.getDimension(R.dimen.floating_ocr_width).toInt()
        params.type = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_PHONE
        else
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        params.format = PixelFormat.TRANSLUCENT
        params.gravity = Gravity.TOP or Gravity.LEFT
        params.x = (mRealDisplaySize.x / 2) - (params.width / 2)
        params.y = (mRealDisplaySize.y / 2) - (params.height / 2)
        return params
    }


    private fun getRealDisplaySizeFromContext(): Point {
        val displaySize = Point()
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getRealSize(displaySize)
        return displaySize
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        if (e1 == null || e2 == null) {
            return false
        }

        layoutParams.x = mDX + e2.rawX.toInt()
        layoutParams.y = mDY + e2.rawY.toInt()
        fixBoxBounds()
        windowManager?.updateViewLayout(mFloatingView, layoutParams)
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
            processTesseractAsync()

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

    override fun onLongPress(e: MotionEvent?) {
        if (mHandler.hasCallbacks(mDismissCloseButton))
            mHandler.removeCallbacks(mDismissCloseButton)

        mHandler.postDelayed(mDismissCloseButton, 3000)

        if (mCloseButton.visibility != View.VISIBLE) {
            mCloseButton.visibility = View.VISIBLE
            mCloseButton.alpha = 0.0f
            mCloseButton.animate().alpha(1.0f).setDuration(300L).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    mCloseButton.visibility = View.VISIBLE
                }
            })

            layoutParams.width += context.resources.getDimension(R.dimen.floating_ocr_button_close_size)
                .toInt() + context.resources.getDimension(R.dimen.floating_ocr_button_close_margin).toInt()
            windowManager?.updateViewLayout(mFloatingView, layoutParams)
        }
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
        if (e1 != null && e2 != null)
            if (abs(velocityX) > 200 && (e1.x - e2.x > 100 || e2.x - e1.x > 100))
                dismiss()

        return false
    }

    override fun onTouch(e: MotionEvent): Boolean {
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                mDX = layoutParams.x - e.rawX.toInt()
                mDY = layoutParams.y - e.rawY.toInt()
                return true
            }
            MotionEvent.ACTION_UP -> {
                val currTime = System.currentTimeMillis()
                if (currTime - mTouchParamUpdateTimer > 50) {
                    mTouchParamUpdateTimer = currTime
                    windowManager?.updateViewLayout(mFloatingView, layoutParams)
                }
                onUp(e)
                return true
            }
        }

        return false
    }

    override fun onResize(e: MotionEvent): Boolean {
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                mDX = layoutParams.width - e.rawX.toInt()
                mDY = layoutParams.height - e.rawY.toInt()

                return true
            }
            MotionEvent.ACTION_UP -> {
                fixBoxBounds()
                windowManager?.updateViewLayout(mFloatingView, layoutParams)
                onUp(e)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                layoutParams.width = mDX + e.rawX.toInt()
                layoutParams.height = mDY + e.rawY.toInt()

                fixBoxBounds()

                val currTime = System.currentTimeMillis()
                if (currTime - mSizeParamUpdateTimer > 50) {
                    mSizeParamUpdateTimer = currTime
                    windowManager?.updateViewLayout(mFloatingView, layoutParams)
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
        if (mHandler.hasCallbacks(mDismissCloseButton))
            mHandler.removeCallbacks(mDismissCloseButton)

        dismiss()
    }

    fun processTesseractAsync() {
        Toast.makeText(
            context,
            context.resources.getString(R.string.ocr_tesseract_get_request),
            Toast.LENGTH_SHORT
        ).show()

        try {
            val language = (activity as OcrProcess).getLanguage()
            val location = IntArray(2)
            mFloatingView.getLocationOnScreen(location)
            val image = (activity as OcrProcess).getImage(location[0], location[1], mFloatingView.width, mFloatingView.height) ?: return
            Tesseract.getInstance(context).processAsync(language, image) {
                (activity as OcrProcess).setText(it)
            }
        } catch (e: Exception) {
            mLOGGER.error("Error when start async process tesseract: " + e.message, e)
        }
    }

    fun processTesseract(languages: Languages): String? {
        return try {
            val location = IntArray(2)
            mFloatingView.getLocationOnScreen(location)
            val image = (activity as OcrProcess).getImage(location[0], location[1], mFloatingView.width, mFloatingView.height)
                ?: return null

            val tess = Tesseract.getInstance(context)
            tess.process(languages, image)
        } catch (e: Exception) {
            mLOGGER.error("Error when process tesseract: " + e.message, e)
            null
        }
    }

    companion object {
        fun canDrawOverlays(context: Context): Boolean =
            Settings.canDrawOverlays(context)
    }
}
package br.com.fenix.bilingualmangareader.view.components

import android.R.color
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.ViewConfiguration
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.OverScroller
import androidx.core.view.ViewCompat
import androidx.core.view.drawToBitmap
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.enums.ReaderMode
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


open class PageImageView(context: Context, attributeSet: AttributeSet?) :
    androidx.appcompat.widget.AppCompatImageView(context, attributeSet) {

    constructor(context: Context) : this(context, null)

    private var mViewMode: ReaderMode
    private var mHaveFrame = false
    private var mSkipScaling = false
    private var mTranslateRightEdge = false
    private var mOuterTouchListener: OnTouchListener? = null
    private var mScaleGestureDetector: ScaleGestureDetector
    private var mDragGestureDetector: GestureDetector
    private var mScroller: OverScroller
    private var mMinScale = 0F
    private var mMaxScale = 0F
    private var mZoomScale = 0F
    private var mOriginalScale = 0F
    private val m = FloatArray(9)
    private var mMatrix: Matrix = Matrix()

    var useMagnifierType = false
    private var mPinch = false
    private var mMagnifierMatrix: Matrix = Matrix()
    private var mZoomPos: PointF
    private var mZooming = false
    private var mPaint: Paint
    private var mBorder: Paint
    private lateinit var mBitmap: Bitmap
    private lateinit var mShader: BitmapShader
    private val mMagnifierScale = 2.5F
    private val mMagnifierCenter: Float
    private val mMagnifierSize: Float
    private val mMagnifierRadius: Float

    fun setViewMode(viewMode: ReaderMode) {
        mViewMode = viewMode
        mSkipScaling = false
        requestLayout()
        invalidate()
    }

    override fun setFrame(l: Int, t: Int, r: Int, b: Int): Boolean {
        val changed: Boolean = super.setFrame(l, t, r, b)
        mHaveFrame = true
        scale()
        return changed
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        mSkipScaling = false
        scale()
    }

    init {
        scaleType = ScaleType.MATRIX
        imageMatrix = mMatrix
        mScaleGestureDetector = ScaleGestureDetector(getContext(), PrivateScaleDetector())
        mDragGestureDetector = GestureDetector(getContext(), PrivateDragListener())
        super.setOnTouchListener { v, event ->
            v.performClick()

            mPinch = event.pointerCount > 1
            if (event.pointerCount > 1) {
                mScaleGestureDetector.onTouchEvent(event)
                parent.requestDisallowInterceptTouchEvent(true)
            } else
                mDragGestureDetector.onTouchEvent(event)

            if (mZooming)
                parent.requestDisallowInterceptTouchEvent(true)

            mOuterTouchListener?.onTouch(v, event)
            onTouchEvent(event)
            true
        }

        mScroller = OverScroller(context)
        mScroller.setFriction(ViewConfiguration.getScrollFriction() * 2)
        mViewMode = ReaderMode.FIT_WIDTH

        val isTablet = resources.getBoolean(R.bool.isTablet)
        mMagnifierSize = if (isTablet) resources.getDimension(R.dimen.reader_zoom_tablet_size) else resources.getDimension(R.dimen.reader_zoom_size)
        mMagnifierRadius = if (isTablet) resources.getDimension(R.dimen.reader_zoom_magnifier_tablet_size) else resources.getDimension(R.dimen.reader_zoom_magnifier_size)
        mMagnifierCenter = mMagnifierSize/2
        mZoomPos = PointF(0F, 0F)
        mPaint = Paint()

        mBorder = Paint()
        mBorder.color = resources.getColor(R.color.black)
        mBorder.style = Paint.Style.STROKE
        mBorder.strokeWidth = resources.getDimension(R.dimen.reader_zoom_border)
    }

    fun autoScroll(isBack: Boolean = false): Boolean {
        val displayMetrics = Resources.getSystem().displayMetrics

        val distance = if (isBack)
            m[Matrix.MTRANS_Y] + (displayMetrics.heightPixels).toFloat()
        else
            m[Matrix.MTRANS_Y] - (displayMetrics.heightPixels).toFloat()

        val imageSize = computeCurrentImageSize()
        val imageHeight = imageSize.y
        mMatrix.getValues(m)

        val isScroll = if (imageHeight < (displayMetrics.heightPixels).toFloat())
            true
        else if (isBack)
            m[Matrix.MTRANS_Y] >= 0F
        else if (imageHeight > height)
            (m[Matrix.MTRANS_Y] * -1) >= (imageHeight - height).toFloat()
        else
            (m[Matrix.MTRANS_Y] * -1) >= (height / 2 - imageHeight / 2).toFloat()

        post(ScrollAnimation(0F, m[Matrix.MTRANS_Y], 0F, distance))

        return !isScroll
    }

    override fun setOnTouchListener(l: OnTouchListener?) {
        mOuterTouchListener = l
    }

    fun setTranslateToRightEdge(translate: Boolean) {
        mTranslateRightEdge = translate
    }

    open fun scale() {
        if (drawable == null || !mHaveFrame || mSkipScaling) return
        val dWidth = drawable.intrinsicWidth
        val dHeight = drawable.intrinsicHeight
        val vWidth: Int = width
        val vHeight: Int = height
        when {
            mViewMode === ReaderMode.ASPECT_FILL -> {
                val scale: Float
                var dx = 0f
                if (dWidth * vHeight > vWidth * dHeight) {
                    scale = vHeight.toFloat() / dHeight.toFloat()
                    if (mTranslateRightEdge) dx = vWidth - dWidth * scale
                } else {
                    scale = vWidth.toFloat() / dWidth.toFloat()
                }
                mMatrix.setScale(scale, scale)
                mMatrix.postTranslate((dx + 0.5f), 0f)
            }
            mViewMode === ReaderMode.ASPECT_FIT -> {
                val mTempSrc = RectF(0F, 0F, dWidth.toFloat(), dHeight.toFloat())
                val mTempDst = RectF(0F, 0F, vWidth.toFloat(), vHeight.toFloat())
                mMatrix.setRectToRect(mTempSrc, mTempDst, Matrix.ScaleToFit.CENTER)
            }
            mViewMode === ReaderMode.FIT_WIDTH -> {
                val widthScale = width.toFloat() / drawable.intrinsicWidth
                mMatrix.setScale(widthScale, widthScale)
                mMatrix.postTranslate(0f, 0f)
            }
        }

        // calculate min/max scale
        val heightRatio = vHeight.toFloat() / dHeight
        val w = dWidth * heightRatio
        if (w < vWidth) {
            mMinScale = vHeight * 0.75f / dHeight
            mMaxScale = max(dWidth, vWidth) * 4f / dWidth
            mZoomScale = max(dWidth, vWidth) * 2f / dWidth
        } else {
            mMinScale = vWidth * 0.75f / dWidth
            mMaxScale = max(dHeight, vHeight) * 4f / dHeight
            mZoomScale = max(dHeight, vHeight) * 2f / dHeight
        }
        imageMatrix = mMatrix
        mOriginalScale = getCurrentScale()
        mSkipScaling = true
    }

    inner class PrivateScaleDetector : SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            mMatrix.getValues(m)
            val scale = m[Matrix.MSCALE_X]
            var scaleFactor = detector.scaleFactor
            val scaleNew = scale * scaleFactor
            var scalable = true
            if (scaleFactor > 1 && mMaxScale - scaleNew < 0) {
                scaleFactor = mMaxScale / scale
                scalable = false
            } else if (scaleFactor < 1 && mMinScale - scaleNew > 0) {
                scaleFactor = mMinScale / scale
                scalable = false
            }
            mMatrix.postScale(
                scaleFactor, scaleFactor,
                detector.focusX, detector.focusY
            )
            imageMatrix = mMatrix
            return scalable
        }
    }

    inner class PrivateDragListener : SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            mScroller.forceFinished(true)
            return true
        }

        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            mMatrix.postTranslate(-distanceX, -distanceY)
            imageMatrix = mMatrix
            return true
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val imageSize: Point = computeCurrentImageSize()
            val offset: Point = computeCurrentOffset()
            var minX: Int = -imageSize.x - this@PageImageView.width
            var minY: Int = -imageSize.y - this@PageImageView.height
            var maxX = 0
            var maxY = 0
            if (offset.x > 0) {
                minX = offset.x
                maxX = offset.x
            }
            if (offset.y > 0) {
                minY = offset.y
                maxY = offset.y
            }
            mScroller.fling(
                offset.x, offset.y,
                velocityX.toInt(), velocityY.toInt(),
                minX, maxX, minY, maxY
            )
            ViewCompat.postInvalidateOnAnimation(this@PageImageView)
            return true
        }

        override fun onDoubleTapEvent(e: MotionEvent): Boolean {
            if (e.action == MotionEvent.ACTION_UP) {
                val scale = if (mOriginalScale == getCurrentScale()) mZoomScale else mOriginalScale
                zoomAnimated(e, scale)
            }
            return true
        }

        override fun onLongPress(e: MotionEvent?) {
            super.onLongPress(e)
            mBitmap = this@PageImageView.drawToBitmap()
            mShader = BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            mPaint = Paint()
            mZooming = true
            this@PageImageView.invalidate()
        }
    }

    open fun zoomAnimated(e: MotionEvent, scale: Float) {
        post(ZoomAnimation(e.x, e.y, scale))
    }

    override fun computeScroll() {
        if (!mScroller.isFinished && mScroller.computeScrollOffset()) {
            val curX = mScroller.currX
            val curY = mScroller.currY
            mMatrix.getValues(m)
            m[Matrix.MTRANS_X] = curX.toFloat()
            m[Matrix.MTRANS_Y] = curY.toFloat()
            mMatrix.setValues(m)
            imageMatrix = mMatrix
            ViewCompat.postInvalidateOnAnimation(this)
        }
        super.computeScroll()
    }

    fun getPointerCoordinate(e: MotionEvent): FloatArray {
        val index = e.actionIndex
        val coordinates = floatArrayOf(e.getX(index), e.getY(index))
        val matrix = Matrix()
        imageMatrix.invert(matrix)
        m[Matrix.MTRANS_X] = mScroller.currX.toFloat()
        m[Matrix.MTRANS_Y] = mScroller.currY.toFloat()
        matrix.mapPoints(coordinates)
        val imageSize = computeCurrentImageSize()
        return floatArrayOf(coordinates[0], coordinates[1], imageSize.x.toFloat(), imageSize.y.toFloat())
    }

    open fun getCurrentScale(): Float {
        mMatrix.getValues(m)
        return m[Matrix.MSCALE_X]
    }

    open fun computeCurrentImageSize(): Point {
        val size = Point()
        val d: Drawable = drawable
        mMatrix.getValues(m)
        val scale = m[Matrix.MSCALE_X]
        val width = d.intrinsicWidth * scale
        val height = d.intrinsicHeight * scale
        size[width.toInt()] = height.toInt()
        return size
    }

    open fun computeCurrentOffset(): Point {
        val offset = Point()
        mMatrix.getValues(m)
        val transX = m[Matrix.MTRANS_X]
        val transY = m[Matrix.MTRANS_Y]
        offset[transX.toInt()] = transY.toInt()
        return offset
    }

    override fun setImageMatrix(matrix: Matrix) {
        super.setImageMatrix(fixMatrix(matrix))
        postInvalidate()
    }

    open fun fixMatrix(matrix: Matrix): Matrix {
        if (drawable == null) return matrix
        matrix.getValues(m)
        val imageSize = computeCurrentImageSize()
        val imageWidth = imageSize.x
        val imageHeight = imageSize.y
        val maxTransX: Float = (imageWidth - width).toFloat()
        val maxTransY: Float = (imageHeight - height).toFloat()

        if (imageWidth > width)
            m[Matrix.MTRANS_X] = min(0F, max(m[Matrix.MTRANS_X], -maxTransX))
        else
            m[Matrix.MTRANS_X] = (width / 2 - imageWidth / 2).toFloat()

        if (imageHeight > height)
            m[Matrix.MTRANS_Y] = min(0F, max(m[Matrix.MTRANS_Y], -maxTransY))
        else
            m[Matrix.MTRANS_Y] = (height / 2 - imageHeight / 2).toFloat()

        matrix.setValues(m)
        return matrix
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        if (drawable == null) return false
        val imageWidth = computeCurrentImageSize().x.toFloat()
        val offsetX = computeCurrentOffset().x.toFloat()
        if (offsetX >= 0 && direction < 0) {
            return false
        } else if (abs(offsetX) + width >= imageWidth && direction > 0) {
            return false
        }
        return true
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        performClick()
        val action = event?.action ?: return true

        mZoomPos.x = event.x
        mZoomPos.y = event.y

        when (action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                if (mZooming)
                    this.invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mZooming = false
                this.invalidate()
            }
            else -> {}
        }

        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (mZooming && !mPinch) {
            mPaint.shader = mShader
            mMagnifierMatrix.set(mMatrix)

            if (useMagnifierType) {
                mMagnifierMatrix.reset()
                mMagnifierMatrix.postScale(mMagnifierScale, mMagnifierScale, mZoomPos.x, mZoomPos.y)
                mPaint.shader.setLocalMatrix(mMagnifierMatrix)
                canvas?.drawCircle(mZoomPos.x, mZoomPos.y, mMagnifierRadius, mPaint)
            } else {
                val x = if (mZoomPos.x < (width/2)) width.minus(mMagnifierSize) else 0F
                val y = if (mZoomPos.y < (height/2)) height.minus(mMagnifierSize) else 0F

                mMagnifierMatrix.reset()
                mMagnifierMatrix.postScale(mMagnifierScale, mMagnifierScale, mZoomPos.x, mZoomPos.y)
                mMagnifierMatrix.postTranslate(-mZoomPos.x, -mZoomPos.y)
                mMagnifierMatrix.postTranslate(mMagnifierCenter, mMagnifierCenter)
                mMagnifierMatrix.postTranslate(x, y)
                mPaint.shader.setLocalMatrix(mMagnifierMatrix)

                canvas?.drawRect(x-1, y-2, x + mMagnifierSize+1, y + mMagnifierSize+1, mBorder)
                canvas?.drawRect(x, y, x + mMagnifierSize, y + mMagnifierSize, mPaint)
            }
        }
    }

    companion object {
        const val ZOOM_DURATION = 200
        const val SCROLL_DURATION = 300
    }

    inner class ZoomAnimation(x: Float, y: Float, scale: Float) :
        Runnable {
        private var mX: Float
        private var mY: Float
        private var mScale: Float
        private var mInterpolator: Interpolator
        private var mStartScale: Float
        private var mStartTime: Long
        override fun run() {
            var t = (System.currentTimeMillis() - mStartTime).toFloat() / ZOOM_DURATION
            val interpolateRatio = mInterpolator.getInterpolation(t)
            t = if (t > 1f) 1f else t
            mMatrix.getValues(m)
            val newScale = mStartScale + interpolateRatio * (mScale - mStartScale)
            val newScaleFactor = newScale / m[Matrix.MSCALE_X]
            mMatrix.postScale(newScaleFactor, newScaleFactor, mX, mY)
            imageMatrix = mMatrix
            if (t < 1f) {
                post(this)
            } else {
                // set exact scale
                mMatrix.getValues(m)
                mMatrix.setScale(mScale, mScale)
                mMatrix.postTranslate(
                    m[Matrix.MTRANS_X],
                    m[Matrix.MTRANS_Y]
                )
                setImageMatrix(mMatrix)
            }
        }

        init {
            mMatrix.getValues(m)
            mX = x
            mY = y
            mScale = scale
            mInterpolator = AccelerateDecelerateInterpolator()
            mStartScale = getCurrentScale()
            mStartTime = System.currentTimeMillis()
        }
    }

    inner class ScrollAnimation(xInitial: Float, yInitial: Float, xFinal: Float, yFinal: Float) :
        Runnable {
        private var mYInitial: Float = yInitial
        private var mXInitial: Float = xInitial
        private var mYFinal: Float = yFinal
        private var mXFinal: Float = xFinal
        private var mInterpolator: Interpolator = AccelerateDecelerateInterpolator()
        private var mStartTime: Long = System.currentTimeMillis()
        private var mInitialMatrix = Matrix(mMatrix)

        override fun run() {
            var t = (System.currentTimeMillis() - mStartTime).toFloat() / SCROLL_DURATION
            val interpolate = mInterpolator.getInterpolation(t)
            t = if (t > 1f) 1f else t

            val yTranslate = ((mYFinal - mYInitial) * interpolate)
            val xTranslate = ((mXFinal - mXInitial) * interpolate)

            mMatrix = Matrix(mInitialMatrix)
            mMatrix.postTranslate(xTranslate, yTranslate)
            imageMatrix = mMatrix
            if (t < 1f) {
                post(this)
            } else {
                // set exact scale
                mMatrix = Matrix(mInitialMatrix)
                mMatrix.postTranslate(mXFinal - mXInitial, mYFinal - mYInitial)
                setImageMatrix(mMatrix)
            }
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
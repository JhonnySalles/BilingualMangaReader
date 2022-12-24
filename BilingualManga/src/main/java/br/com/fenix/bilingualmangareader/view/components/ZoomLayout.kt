package br.com.fenix.bilingualmangareader.view.components

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout

class ZoomLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr),
    ZoomGestureDetector.Listener {

    private val gestureDetector = ZoomGestureDetector(this)

    var isZoomEnabled
        get() = gestureDetector.isZoomEnabled
        set(value) {
            gestureDetector.isZoomEnabled = value
        }

    var isScaleEnabled
        get() = gestureDetector.isScaleEnabled
        set(value) {
            gestureDetector.isScaleEnabled = value
        }

    var isRotationEnabled
        get() = gestureDetector.isRotationEnabled
        set(value) {
            gestureDetector.isRotationEnabled = value
        }

    var isTranslationEnabled
        get() = gestureDetector.isTranslationEnabled
        set(value) {
            gestureDetector.isTranslationEnabled = value
        }

    var isFlingEnabled
        get() = gestureDetector.isFlingEnabled
        set(value) {
            gestureDetector.isFlingEnabled = value
        }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (isZoomEnabled) return true
        gestureDetector.updateTouchLocation(event)
        return super.onInterceptTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
        gestureDetector.updateCanvasMatrix(canvas)
        return super.drawChild(canvas, child, drawingTime)
    }

    override fun onZoom(scaling: Float, rotation: Float, translation: Position, pivot: Position) {
        invalidate()
    }

}
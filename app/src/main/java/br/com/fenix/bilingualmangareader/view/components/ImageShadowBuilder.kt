package br.com.fenix.bilingualmangareader.view.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Point
import android.view.View


class ImageShadowBuilder(private val image: Bitmap) : View.DragShadowBuilder() {
    override fun onDrawShadow(canvas: Canvas) {
        canvas.drawBitmap(image, 0f, 0f, null)
    }

    override fun onProvideShadowMetrics(shadowSize: Point, shadowTouchPoint: Point) {
        shadowSize.x = image.width
        shadowSize.y = image.height
        shadowTouchPoint.x = shadowSize.x / 2
        shadowTouchPoint.y = shadowSize.y / 2
    }
}
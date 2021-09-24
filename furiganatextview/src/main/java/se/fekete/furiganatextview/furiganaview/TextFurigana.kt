package se.fekete.furiganatextview.furiganaview

import android.graphics.Canvas
import android.graphics.Paint

class TextFurigana(private val text: String, private val paintF: Paint) {

    // Coordinates
    private var offset = 0.0f
    private var width = 0.0f

    init {
        width = paintF.measureText(text)
    }

    fun getOffset(): Float {
        return offset
    }

    fun setOffset(value: Float) {
        offset = value
    }

    fun width(): Float {
        return width
    }

    fun draw(canvas: Canvas, x: Float, y: Float) {
        var mutableX = x
        mutableX -= width / 2.0f
        canvas.drawText(text, 0, text.length, mutableX, y, paintF)
    }
}
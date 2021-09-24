package se.fekete.furiganatextview.furiganaview

import android.graphics.Canvas
import android.graphics.Paint

class TextNormal(private val text: String, private val paint: Paint) {

    private var totalWidth: Float = 0.toFloat()
    private val charsWidth: FloatArray = FloatArray(text.length)

    init {
        paint.getTextWidths(text, charsWidth)

        // Total width
        totalWidth = 0.0f
        for (v in charsWidth)
            totalWidth += v
    }

    // Info
    fun length(): Int {
        return text.length
    }

    // Widths
    fun charsWidth(): FloatArray {
        return charsWidth
    }

    // Split
    fun split(offset: Int): Array<TextNormal> {
        return arrayOf(TextNormal(text.substring(0, offset), paint), TextNormal(text.substring(offset), paint))
    }

    // Draw
    fun draw(canvas: Canvas, x: Float, y: Float): Float {
        canvas.drawText(text, 0, text.length, x, y, paint)
        return totalWidth
    }
}

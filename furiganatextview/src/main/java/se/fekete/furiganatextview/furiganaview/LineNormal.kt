package se.fekete.furiganatextview.furiganaview

import android.graphics.Canvas
import android.graphics.Paint
import java.util.*

class LineNormal(val paint: Paint) {
    // Text
    private val text = Vector<TextNormal>()

    // Elements
    fun size(): Int {
        return text.size
    }

    fun add(text: Vector<TextNormal>) {
        this.text.addAll(text)
    }

    // Draw
    fun draw(canvas: Canvas, y: Float) {
        var mutableY = y
        mutableY -= paint.descent()

        var x = 0.0f

        for (text in text) {
            x += text.draw(canvas, x, mutableY)
        }
    }
}

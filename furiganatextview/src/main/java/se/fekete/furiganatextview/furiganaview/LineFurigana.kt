package se.fekete.furiganatextview.furiganaview

import android.graphics.Canvas
import android.graphics.Paint
import java.util.*

class LineFurigana(private val lineMax: Float, private val paint: Paint) {
    // Text
    private val texts = Vector<TextFurigana>()
    private val offsets = Vector<Float>()

    // Add
    fun add(text: TextFurigana?) {
        if (text != null) {
            this.texts.add(text)
        }
    }

    // Calculate
    fun calculate() {
        // Check size
        if (texts.size == 0) {
            return
        }

        val r = FloatArray(texts.size)

        for (i in texts.indices) {
            r[i] = texts[i].getOffset()
        }

        // a[] - constraint matrix
        val a = Array(texts.size + 1) { FloatArray(texts.size) }

        for (i in a.indices) {
            for (j in 0 until a[0].size) {
                a[i][j] = 0.0f
            }
        }

        a[0][0] = 1.0f

        for (i in 1 until a.size - 2) {
            a[i][i - 1] = -1.0f
            a[i][i] = 1.0f
        }

        a[a.size - 1][a[0].size - 1] = -1.0f

        // b[] - constraint vector
        val b = FloatArray(texts.size + 1)
        b[0] = -r[0] + 0.5f * texts[0].width()

        for (i in 1 until b.size - 2) {
            b[i] = 0.5f * (texts[i].width() + texts[i - 1].width()) + (r[i - 1] - r[i])
        }

        b[b.size - 1] = -lineMax + r[r.size - 1] + 0.5f * texts[texts.size - 1].width()

        // Calculate constraint optimization
        val x = FloatArray(texts.size)
        for (i in x.indices) {
            x[i] = 0.0f
        }

        val co = QuadraticOptimizer(a, b)
        co.calculate(x)

        for (i in x.indices) {
            offsets.add(x[i] + r[i])
        }
    }

    // Draw
    fun draw(canvas: Canvas, y: Float) {
        var mutableY = y
        mutableY -= paint.descent()

        if (offsets.size == texts.size) {
            // Render with fixed offsets
            for (i in offsets.indices) {
                texts[i].draw(canvas, offsets[i], mutableY)
            }
        } else {
            // Render with original offsets
            for (text in texts) {
                text.draw(canvas, text.getOffset(), mutableY)
            }
        }
    }
}

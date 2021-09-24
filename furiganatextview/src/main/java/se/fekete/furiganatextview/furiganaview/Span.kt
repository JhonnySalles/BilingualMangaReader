package se.fekete.furiganatextview.furiganaview

import android.graphics.Paint
import java.util.*

internal class Span {
    // Text
    private var furigana: TextFurigana? = null
    private var normal = Vector<TextNormal>()

    // Widths
    private val widthChars = Vector<Float>()
    private var widthTotal = 0.0f

    // Constructors
    constructor(textF: String, textK: String, markS: Int, markE: Int, paint: Paint, paintF: Paint) {

        var mutableMarkS = markS
        var mutableMarkE = markE

        // Furigana text
        if (textF.isNotEmpty()) {
            furigana = TextFurigana(textF, paintF)
        }

        // Normal text
        if (mutableMarkS < textK.length && mutableMarkE > 0 && mutableMarkS < mutableMarkE) {

            // Fix marked bounds
            mutableMarkS = Math.max(0, mutableMarkS)
            mutableMarkE = Math.min(textK.length, mutableMarkE)

            // Prefix
            if (mutableMarkS > 0) {
                normal.add(TextNormal(textK.substring(0, mutableMarkS), paint))
            }

            // Marked
            if (mutableMarkE > mutableMarkS) {
                normal.add(TextNormal(textK.substring(mutableMarkS, mutableMarkE), paint))
            }

            // Postfix
            if (mutableMarkE < textK.length) {
                normal.add(TextNormal(textK.substring(mutableMarkE), paint))
            }

        } else {
            // Non marked
            normal.add(TextNormal(textK, paint))
        }

        // Widths
        calculateWidths()
    }

    constructor(normal: Vector<TextNormal>) {
        // Only normal text
        this.normal = normal

        // Widths
        calculateWidths()
    }

    // Text
    fun furigana(x: Float): TextFurigana? {
        if (furigana == null) {
            return null
        }

        furigana?.setOffset(x + widthTotal / 2.0f)

        return furigana
    }

    fun normal(): Vector<TextNormal> {
        return normal
    }

    // Widths
    fun widths(): Vector<Float> {
        return widthChars
    }

    private fun calculateWidths() {
        // Chars
        if (furigana == null) {
            for (normal in normal) {
                for (v in normal.charsWidth()) {
                    widthChars.add(v)
                }
            }
        } else {
            var sum = 0.0f

            for (normal in normal) {
                for (v in normal.charsWidth()) {
                    sum += v
                }
            }
            widthChars.add(sum)
        }

        // Total
        widthTotal = 0.0f

        for (v in widthChars) {
            widthTotal += v
        }
    }

    // Split
    fun split(offset: Int, normalA: Vector<TextNormal>, normalB: Vector<TextNormal>) {
        var mutableOffset = offset

        // Check if no furigana
        if (furigana == null) {
            return
        }

        // Split normal list
        for (cur in normal) {
            when {
                mutableOffset <= 0 -> normalB.add(cur)
                mutableOffset >= cur.length() -> normalA.add(cur)
                else -> {
                    val split = cur.split(mutableOffset)
                    normalA.add(split[0])
                    normalB.add(split[1])
                }
            }
            mutableOffset -= cur.length()
        }
    }
}

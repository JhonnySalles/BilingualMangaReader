/*
 * FuriganaView widget
 * Copyright (C) 2013 sh0 <sh0@yutani.ee>
 * Licensed under Creative Commons BY-SA 3.0
 */

package se.fekete.furiganatextview.furiganaview

import android.content.Context
import android.graphics.Canvas
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import se.fekete.furiganatextview.R
import java.util.*

class FuriganaTextView : TextView {

    // Paints
    private var textPaintFurigana = TextPaint()
    private var textPaintNormal = TextPaint()

    // Sizes
    private var lineSize = 0.0f
    private var normalHeight = 0.0f
    private var furiganaHeight = 0.0f
    private var lineMax = 0.0f

    // Spans and lines
    private val spans = Vector<Span>()
    private val normalLines = Vector<LineNormal>()
    private val furiganaLines = Vector<LineFurigana>()

    //attributes
    private var hasRuby: Boolean = false
    private var furiganaTextColor: Int = 0

    // Constructors
    constructor(context: Context) : super(context) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.FuriganaTextView, 0, 0)
        try {
            hasRuby = typedArray.getBoolean(R.styleable.FuriganaTextView_contains_ruby_tags, false)
            furiganaTextColor = typedArray.getColor(R.styleable.FuriganaTextView_furigana_text_color, 0)
        } finally {
            typedArray.recycle()
        }

        initialize()
    }

    private fun initialize() {
        val viewText = text
        if (viewText.isNotEmpty()) {
            setFuriganaText(viewText as String, hasRuby)
        }
    }

    /**
     * The method parseRuby converts kanji enclosed in ruby tags to the
     * format which is supported by the textview {Kanji:furigana}

     * @param textWithRuby
     * The text string with Kanji enclosed in ruby tags.
     */
    private fun replaceRuby(textWithRuby: String): String {
        var parsed = textWithRuby.replace("<ruby>", "{")
        parsed = parsed.replace("<rt>", ";")
        parsed = parsed.replace("</rt>", "")

        return parsed.replace("</ruby>", "}")
    }

    override fun setTextColor(color: Int) {
        super.setTextColor(color)
        invalidate()
    }

    fun setFuriganaText(text: String) {
        setFuriganaText(text, hasRuby = false)
    }

    fun setFuriganaText(text: String, hasRuby: Boolean) {
        super.setText(text)

        var textToDisplay = text
        if (this.hasRuby || hasRuby) {
            textToDisplay = replaceRuby(text)
        }

        setText(paint, textToDisplay, 0, 0)
    }

    private fun setText(tp: TextPaint, text: String, markS: Int, markE: Int) {
        var mutableText = text
        var mutableMarkS = markS
        var mutableMarkE = markE

        // Text
        textPaintNormal = TextPaint(tp)
        textPaintFurigana = TextPaint(tp)
        textPaintFurigana.textSize = textPaintFurigana.textSize / 2.0f

        // Line size
        normalHeight = textPaintNormal.descent() - textPaintNormal.ascent()
        furiganaHeight = textPaintFurigana.descent() - textPaintFurigana.ascent()
        lineSize = normalHeight + furiganaHeight

        // Clear spans
        spans.clear()

        // Sizes
        lineSize = textPaintFurigana.fontSpacing + Math.max(textPaintNormal.fontSpacing, 0f)

        // Spannify text
        while (mutableText.isNotEmpty()) {
            var idx = mutableText.indexOf('{')
            if (idx >= 0) {
                // Prefix string
                if (idx > 0) {
                    // Spans
                    spans.add(Span("", mutableText.substring(0, idx), mutableMarkS, mutableMarkE, textPaintNormal, textPaintFurigana))

                    // Remove text
                    mutableText = mutableText.substring(idx)
                    mutableMarkS -= idx
                    mutableMarkE -= idx
                }

                // End bracket
                idx = mutableText.indexOf('}')
                if (idx < 1) {
                    // Error
                    break
                } else if (idx == 1) {
                    // Empty bracket
                    mutableText = mutableText.substring(2)
                    continue
                }

                // Spans
                val split = mutableText.substring(1, idx).split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                spans.add(Span(if (split.size > 1) split[1] else "", split[0], mutableMarkS, mutableMarkE, textPaintNormal, textPaintFurigana))

                // Remove text
                mutableText = mutableText.substring(idx + 1)
                mutableMarkS -= split[0].length
                mutableMarkE -= split[0].length

            } else {
                // Single span
                spans.add(Span("", mutableText, mutableMarkS, mutableMarkE, textPaintNormal, textPaintFurigana))
                mutableText = ""
            }
        }

        // Invalidate view
        this.invalidate()
        this.requestLayout()
    }

    // Size calculation
    override fun onMeasure(width_ms: Int, height_ms: Int) {
        // Modes
        val wmode = View.MeasureSpec.getMode(width_ms)
        val hmode = View.MeasureSpec.getMode(height_ms)

        // Dimensions
        val wold = View.MeasureSpec.getSize(width_ms)
        val hold = View.MeasureSpec.getSize(height_ms)

        if (text.isNotEmpty()) {
            // Draw mode
            if (wmode == View.MeasureSpec.EXACTLY || wmode == View.MeasureSpec.AT_MOST && wold > 0) {
                // Width limited
                calculateText(wold.toFloat())
            } else {
                // Width unlimited
                calculateText(-1.0f)
            }
        }

        // New height
        var hnew = Math.round(Math.ceil((lineSize * normalLines.size.toFloat()).toDouble())).toInt()
        var wnew = wold
        if (wmode != View.MeasureSpec.EXACTLY && normalLines.size <= 1)
            wnew = Math.round(Math.ceil(lineMax.toDouble())).toInt()
        if (hmode != View.MeasureSpec.UNSPECIFIED && hnew > hold)
            hnew = hnew or View.MEASURED_STATE_TOO_SMALL

        // Set result
        setMeasuredDimension(wnew, hnew)
    }

    private fun calculateText(lineMax: Float) {
        // Clear lines
        normalLines.clear()
        furiganaLines.clear()

        // Sizes
        this.lineMax = 0.0f

        // Check if no limits on width
        if (lineMax < 0.0) {

            // Create single normal and furigana line
            val lineN = LineNormal(textPaintNormal)
            val lineF = LineFurigana(this.lineMax, textPaintFurigana)

            // Loop spans
            for (span in spans) {
                // Text
                lineN.add(span.normal())
                lineF.add(span.furigana(this.lineMax))

                // Widths update
                for (width in span.widths())
                    this.lineMax += width
            }

            // Commit both lines
            normalLines.add(lineN)
            furiganaLines.add(lineF)

        } else {

            // Lines
            var lineX = 0.0f
            var lineN = LineNormal(textPaintNormal)
            var lineF = LineFurigana(this.lineMax, textPaintFurigana)

            // Initial span
            var spanI = 0
            var span: Span? = if (spans.isNotEmpty()) spans[spanI] else null

            // Iterate
            while (span != null) {
                // Start offset
                val lineS = lineX

                // Calculate possible line size
                val widths = span.widths()
                var i = 0
                while (i < widths.size) {
                    if (lineX + widths[i] <= lineMax) {
                        lineX += widths[i]
                    } else {
                        break
                    }
                    i++
                }

                // Add span to line
                if (i >= 0 && i < widths.size) {

                    // Span does not fit entirely
                    if (i > 0) {
                        // Split half that fits
                        val normalA = Vector<TextNormal>()
                        val normalB = Vector<TextNormal>()
                        span.split(i, normalA, normalB)
                        lineN.add(normalA)
                        span = Span(normalB)
                    }

                    // Add new line with current spans
                    if (lineN.size() != 0) {
                        // Add
                        this.lineMax = if (this.lineMax > lineX) this.lineMax else lineX
                        normalLines.add(lineN)
                        furiganaLines.add(lineF)

                        // Reset
                        lineN = LineNormal(textPaintNormal)
                        lineF = LineFurigana(this.lineMax, textPaintFurigana)
                        lineX = 0.0f

                        // Next span
                        continue
                    }

                } else {

                    // Span fits entirely
                    lineN.add(span.normal())
                    lineF.add(span.furigana(lineS))

                }

                // Next span
                span = null
                spanI++

                if (spanI < this.spans.size) {
                    span = this.spans[spanI]
                }
            }

            // Last span
            if (lineN.size() != 0) {
                // Add
                this.lineMax = if (this.lineMax > lineX) this.lineMax else lineX
                normalLines.add(lineN)
                furiganaLines.add(lineF)
            }
        }

        // Calculate furigana
        for (line in furiganaLines) {
            line.calculate()
        }
    }

    // Drawing
    public override fun onDraw(canvas: Canvas) {

        textPaintNormal.color = currentTextColor

        if (furiganaTextColor != 0) {
            textPaintFurigana.color = furiganaTextColor
        } else {
            textPaintFurigana.color = currentTextColor
        }

        // Coordinates
        var y = lineSize

        // Loop lines
        for (i in normalLines.indices) {
            normalLines[i].draw(canvas, y)
            furiganaLines[i].draw(canvas, y - normalHeight)
            y += lineSize
        }
    }
}

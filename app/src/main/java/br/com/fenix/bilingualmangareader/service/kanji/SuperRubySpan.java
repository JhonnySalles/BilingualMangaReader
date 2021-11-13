/*
 * Copyright (C) 2020 Nicolas Centa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.fenix.bilingualmangareader.service.kanji;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SuperRubySpan extends SuperReplacementSpan {
    private final @NonNull CharSequence mFurigana;
    private final @Alignment int mFuriganaAlignment;

    public SuperRubySpan(final @NonNull CharSequence aFurigana) {
        this(aFurigana, Alignment.CENTER, Alignment.CENTER);
    }

    // Priority on top for aTextAlignment: if aFurigana contains
    // a SuperRubySpan, its aTextAlignment will be applied,
    // not aFuriganaAlignment here.

    public SuperRubySpan(final @NonNull CharSequence aFurigana,
                         final @Alignment int aTextAlignment,
                         final @Alignment int aFuriganaAlignment) {
        super(aTextAlignment);

        mFurigana = aFurigana;
        mFuriganaAlignment = aFuriganaAlignment;
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fm) {
        final TextSizeInformation textSizeInformation = getTextSize(paint, text, start, end);
        final TextPaint inheritPaint = new TextPaint(paint);

        // For RelativeSizeSpan
        if (textSizeInformation.charSequenceSizedElements.size() > 0) {
            inheritPaint.setTextSize(textSizeInformation.charSequenceSizedElements.get(0).textPaint.getTextSize());
        }

        final TextSizeInformation furiganaSizeInformation = getTextSize(inheritPaint,
                mFurigana, 0, mFurigana.length());

        if (fm != null) {
            fm.bottom = textSizeInformation.fontMetricsInt.bottom;
            fm.ascent = textSizeInformation.fontMetricsInt.ascent +
                    (furiganaSizeInformation.fontMetricsInt.ascent - furiganaSizeInformation.fontMetricsInt.descent);
            fm.top = textSizeInformation.fontMetricsInt.ascent +
                    (furiganaSizeInformation.fontMetricsInt.top - furiganaSizeInformation.fontMetricsInt.descent);
            fm.descent = textSizeInformation.fontMetricsInt.descent;
            fm.leading = textSizeInformation.fontMetricsInt.leading;
        }

        return Math.round(Math.max(textSizeInformation.size,
                furiganaSizeInformation.size));
    }

    @Override
    void drawExpanded(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint,
                              final float expandedSpanSize) {
        final TextPaint inheritPaint = new TextPaint(paint);

        final TextSizeInformation textSizeInformation = getTextSize(paint, text, start, end);

        // For RelativeSizeSpan
        if (textSizeInformation.charSequenceSizedElements.size() > 0) {
            inheritPaint.setTextSize(textSizeInformation.charSequenceSizedElements.get(0).textPaint.getTextSize());
        }

        final TextSizeInformation furiganaSizeInformation = getTextSize(inheritPaint, mFurigana, 0, mFurigana.length());

        final float spanSize = Math.round(Math.max(Math.max(textSizeInformation.size,
                furiganaSizeInformation.size), expandedSpanSize));

        drawText(text, textSizeInformation, mAlignment, canvas,
                spanSize, x, y,
                top - textSizeInformation.fontMetricsInt.ascent +
                        furiganaSizeInformation.fontMetricsInt.descent,
                bottom);

        drawText(mFurigana, furiganaSizeInformation, mFuriganaAlignment, canvas,
                spanSize, x,
                y + textSizeInformation.fontMetricsInt.ascent -
                        furiganaSizeInformation.fontMetricsInt.descent,
                top,
                bottom + textSizeInformation.fontMetricsInt.ascent -
                        furiganaSizeInformation.fontMetricsInt.bottom);
    }
}

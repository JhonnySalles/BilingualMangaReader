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
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.MetricAffectingSpan;
import android.text.style.ReplacementSpan;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class SuperReplacementSpan extends ReplacementSpan {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Alignment.BEGIN, Alignment.END,
            Alignment.CENTER, Alignment.JUSTIFIED, Alignment.JIS})
    public @interface Alignment {
        int BEGIN = 0;
        int END = 1;
        int CENTER = 2;
        int JUSTIFIED = 3;
        int JIS = 4;
    }

    static class TextSizeInformation {
        final Paint.FontMetricsInt fontMetricsInt;
        final float size;
        final List<CharSequenceSizedElement> charSequenceSizedElements;

        TextSizeInformation(final Paint.FontMetricsInt aFontMetricsInt,
                            final float aSize,
                            final List<CharSequenceSizedElement> aCharSequenceSizedElements) {
            fontMetricsInt = aFontMetricsInt;
            size = aSize;
            charSequenceSizedElements = aCharSequenceSizedElements;
        }
    }

    static class CharSequenceElement {
        final int start;
        final int end;
        final List<ReplacementSpan> replacementSpans;
        final List<MetricAffectingSpan> metricAffectingSpans;
        final List<CharacterStyle> characterStyles;

        CharSequenceElement(final int aStart,
                                    final int aEnd,
                                    final List<ReplacementSpan> aReplacementSpans,
                                    final List<MetricAffectingSpan> aMetricAffectingSpans,
                                    final List<CharacterStyle> aCharacterStyles) {
            start = aStart;
            end = aEnd;
            replacementSpans = aReplacementSpans;
            metricAffectingSpans = aMetricAffectingSpans;
            characterStyles = aCharacterStyles;
        }
    }

    static class CharSequenceSizedElement {
        final CharSequenceElement charSequenceElement;
        final float size;
        final TextPaint textPaint;
        final Paint.FontMetricsInt fontMetricsInt;

        float spaceBefore;
        float spaceAfter;

        CharSequenceSizedElement(final CharSequenceElement aCharSequenceElement,
                                         final float aSize,
                                         final TextPaint aTextPaint,
                                         final Paint.FontMetricsInt aFontMetricsInt) {
            charSequenceElement = aCharSequenceElement;
            size = aSize;
            textPaint = aTextPaint;
            fontMetricsInt = aFontMetricsInt;

            spaceBefore = 0;
            spaceAfter = 0;
        }
    }

    private List<CharSequenceElement> getCharSequenceElements(final @NonNull CharSequence text, int start, int end) {
        final Spanned textSpanned = text instanceof Spanned ? (Spanned) text : null;
        final List<ReplacementSpan> replacementSpans = textSpanned != null ? getSpans(textSpanned, start, end, ReplacementSpan.class) : null;
        final List<MetricAffectingSpan> metricAffectingSpans = textSpanned != null ? getSpans(textSpanned, start, end, MetricAffectingSpan.class) : null;
        final List<CharacterStyle> characterStyles = textSpanned != null ? getSpans(textSpanned, start, end, CharacterStyle.class) : null;
        final String textString = text.toString();
        final LinkedList<CharSequenceElement> charSequenceElements = new LinkedList<>();

        int cursor = start;

        while (cursor < end) {
            int nextCursor = textString.offsetByCodePoints(cursor, 1);
            LinkedList<MetricAffectingSpan> metricAffectingSpansSub = null;
            LinkedList<CharacterStyle> characterStylesSub = null;

            if (textSpanned != null) {
                LinkedList<ReplacementSpan> replacementSpansSub = null;

                for (ReplacementSpan replacementSpan : replacementSpans) {
                    final int spanStart = textSpanned.getSpanStart(replacementSpan);
                    final int spanEnd = textSpanned.getSpanEnd(replacementSpan);

                    if (spanStart >= cursor) {
                        if (replacementSpansSub == null) {
                            replacementSpansSub = new LinkedList<>();
                        }

                        replacementSpansSub.add(replacementSpan);

                        if (spanEnd > nextCursor) {
                            nextCursor = spanEnd;
                        }
                    }
                }

                if (replacementSpansSub != null) {
                    charSequenceElements.add(new CharSequenceElement(cursor, nextCursor,
                            replacementSpansSub, null, null));

                    cursor = nextCursor;

                    continue;
                }

                for (MetricAffectingSpan metricAffectingSpan : metricAffectingSpans) {
                    final int spanStart = textSpanned.getSpanStart(metricAffectingSpan);
                    final int spanEnd = textSpanned.getSpanEnd(metricAffectingSpan);

                    if (spanStart <= cursor && spanEnd >= nextCursor) {
                        if (metricAffectingSpansSub == null) {
                            metricAffectingSpansSub = new LinkedList<>();
                        }

                        metricAffectingSpansSub.add(metricAffectingSpan);
                    }
                }

                for (CharacterStyle characterStyle : characterStyles) {
                    final int spanStart = textSpanned.getSpanStart(characterStyle);
                    final int spanEnd = textSpanned.getSpanEnd(characterStyle);

                    if (spanStart <= cursor && spanEnd >= nextCursor) {
                        if (characterStylesSub == null) {
                            characterStylesSub = new LinkedList<>();
                        }

                        characterStylesSub.add(characterStyle);
                    }
                }
            }

            charSequenceElements.add(new CharSequenceElement(cursor, nextCursor,
                    null, metricAffectingSpansSub, characterStylesSub));

            cursor = nextCursor;
        }

        return charSequenceElements;
    }

    private static void mergeFontMetricsInt(final @NonNull Paint.FontMetricsInt baseFontMetricsInt,
                                            final @NonNull Paint.FontMetricsInt newFontMetricsInt) {
        baseFontMetricsInt.leading = Math.max(baseFontMetricsInt.leading, newFontMetricsInt.leading);

        baseFontMetricsInt.descent = Math.max(baseFontMetricsInt.descent, newFontMetricsInt.descent);
        baseFontMetricsInt.bottom = Math.max(baseFontMetricsInt.bottom, newFontMetricsInt.bottom);

        baseFontMetricsInt.ascent = Math.min(baseFontMetricsInt.ascent, newFontMetricsInt.ascent);
        baseFontMetricsInt.top = Math.min(baseFontMetricsInt.top, newFontMetricsInt.top);
    }

    private static void centerText(final @NonNull TextSizeInformation textSizeInformation,
                                   final float size) {
        final Iterator<CharSequenceSizedElement> charSequenceElementIterator = textSizeInformation.charSequenceSizedElements.iterator();
        int count = 0;
        final float extraSpace = size - textSizeInformation.size;

        while (charSequenceElementIterator.hasNext()) {
            final CharSequenceSizedElement charSequenceSizedElement = charSequenceElementIterator.next();

            if (count == 0) {
                charSequenceSizedElement.spaceBefore = extraSpace / 2;
            }

            if (!charSequenceElementIterator.hasNext()) {
                charSequenceSizedElement.spaceAfter = extraSpace / 2;
            }

            count++;
        }
    }

    private static void alignTextLeft(final @NonNull TextSizeInformation textSizeInformation,
                                      final float size) {
        final Iterator<CharSequenceSizedElement> charSequenceElementIterator = textSizeInformation.charSequenceSizedElements.iterator();
        int count = 0;
        final float extraSpace = size - textSizeInformation.size;

        while (charSequenceElementIterator.hasNext()) {
            final CharSequenceSizedElement charSequenceSizedElement = charSequenceElementIterator.next();

            if (!charSequenceElementIterator.hasNext()) {
                charSequenceSizedElement.spaceAfter = extraSpace;
            }

            count++;
        }
    }

    private static void alignTextRight(final @NonNull TextSizeInformation textSizeInformation,
                                       final float size) {
        final Iterator<CharSequenceSizedElement> charSequenceElementIterator = textSizeInformation.charSequenceSizedElements.iterator();
        int count = 0;
        final float extraSpace = size - textSizeInformation.size;

        while (charSequenceElementIterator.hasNext()) {
            final CharSequenceSizedElement charSequenceSizedElement = charSequenceElementIterator.next();

            if (count == 0) {
                charSequenceSizedElement.spaceBefore = extraSpace;
            }

            count++;
        }
    }

    private static void justifyText(final @NonNull TextSizeInformation textSizeInformation,
                                    final float size, final boolean jis) {
        float divider = 0;
        int count = 0;

        if (textSizeInformation.charSequenceSizedElements.size() == 1) {
            centerText(textSizeInformation, size);

            return;
        }

        Iterator<CharSequenceSizedElement> charSequenceElementIterator = textSizeInformation.charSequenceSizedElements.iterator();

        while (charSequenceElementIterator.hasNext()) {
            final CharSequenceSizedElement charSequenceSizedElement = charSequenceElementIterator.next();

            if (charSequenceElementIterator.hasNext()) {
                divider += charSequenceSizedElement.size / 2;
            }

            if (count != 0) {
                divider += charSequenceSizedElement.size / 2;
            }

            if (jis) {
                if (!charSequenceElementIterator.hasNext()) {
                    divider += charSequenceSizedElement.size / 2;
                }

                if (count == 0) {
                    divider += charSequenceSizedElement.size / 2;
                }
            }

            count++;
        }

        final float extraSpaceUnit = (size - textSizeInformation.size) / divider;

        count = 0;

        charSequenceElementIterator = textSizeInformation.charSequenceSizedElements.iterator();

        while (charSequenceElementIterator.hasNext()) {
            final CharSequenceSizedElement charSequenceSizedElement = charSequenceElementIterator.next();

            if (charSequenceElementIterator.hasNext()) {
                charSequenceSizedElement.spaceAfter = charSequenceSizedElement.size * extraSpaceUnit / 2;
            }

            if (count != 0) {
                charSequenceSizedElement.spaceBefore = charSequenceSizedElement.size * extraSpaceUnit / 2;
            }

            if (jis) {
                if (!charSequenceElementIterator.hasNext()) {
                    charSequenceSizedElement.spaceAfter += charSequenceSizedElement.size * extraSpaceUnit / 2;
                }

                if (count == 0) {
                    charSequenceSizedElement.spaceBefore += charSequenceSizedElement.size * extraSpaceUnit / 2;
                }
            }

            count++;
        }
    }

    TextSizeInformation getTextSize(final @NonNull Paint paint, final @NonNull CharSequence text, int start, int end) {
        final List<CharSequenceElement> charSequenceElements = getCharSequenceElements(text, start, end);
        final Paint.FontMetricsInt fm = new Paint.FontMetricsInt();
        final LinkedList<CharSequenceSizedElement> charSequenceSizedElements = new LinkedList<>();

        int size = 0;

        for (CharSequenceElement charSequenceElement : charSequenceElements) {
            if (charSequenceElement.replacementSpans != null) {
                final ReplacementSpan replacementSpan = charSequenceElement.replacementSpans.get(charSequenceElement.replacementSpans.size() - 1);
                final Paint.FontMetricsInt fontMetricsInt = new Paint.FontMetricsInt();
                final float elementSize = replacementSpan.getSize(paint, text, charSequenceElement.start, charSequenceElement.end, fontMetricsInt);

                charSequenceSizedElements.add(new CharSequenceSizedElement(charSequenceElement,
                        elementSize, new TextPaint(paint), fontMetricsInt));

                size += elementSize;
                mergeFontMetricsInt(fm, fontMetricsInt);
            } else {
                final TextPaint textPaint = new TextPaint(paint);

                if (charSequenceElement.metricAffectingSpans != null) {
                    for (MetricAffectingSpan metricAffectingSpan : charSequenceElement.metricAffectingSpans) {
                        metricAffectingSpan.updateMeasureState(textPaint);
                    }
                }

                if (charSequenceElement.characterStyles != null) {
                    for (CharacterStyle characterStyle : charSequenceElement.characterStyles) {
                        characterStyle.updateDrawState(textPaint);
                    }
                }

                final Paint.FontMetricsInt fontMetricsInt = textPaint.getFontMetricsInt();
                final float elementSize = textPaint.measureText(text, charSequenceElement.start, charSequenceElement.end);

                charSequenceSizedElements.add(new CharSequenceSizedElement(charSequenceElement,
                        elementSize, textPaint, fontMetricsInt));

                size += elementSize;
                mergeFontMetricsInt(fm, fontMetricsInt);
            }
        }

        return new TextSizeInformation(fm, size, charSequenceSizedElements);
    }

    private static void drawBackground(final @NonNull CharSequenceSizedElement aCharSequenceSizedElement,
                                       final @NonNull Canvas aCanvas,
                                       final float aX,
                                       final int aY,
                                       final boolean aFirstChar,
                                       final boolean aLastChar) {
        if(aCharSequenceSizedElement.textPaint.bgColor != 0) {
            final float left = aFirstChar ? aX + aCharSequenceSizedElement.spaceBefore : aX;
            final float right = aLastChar ?
                    aX + aCharSequenceSizedElement.spaceBefore + aCharSequenceSizedElement.size :
                    aX + aCharSequenceSizedElement.spaceBefore + aCharSequenceSizedElement.size +
                            aCharSequenceSizedElement.spaceAfter;

            int previousColor = aCharSequenceSizedElement.textPaint.getColor();
            Paint.Style previousStyle = aCharSequenceSizedElement.textPaint.getStyle();
            aCharSequenceSizedElement.textPaint.setColor(aCharSequenceSizedElement.textPaint.bgColor);
            aCharSequenceSizedElement.textPaint.setStyle(Paint.Style.FILL);
            aCanvas.drawRect(left,
                    aY + aCharSequenceSizedElement.fontMetricsInt.top,
                    right,
                    aY + aCharSequenceSizedElement.fontMetricsInt.bottom, aCharSequenceSizedElement.textPaint);
            aCharSequenceSizedElement.textPaint.setStyle(previousStyle);
            aCharSequenceSizedElement.textPaint.setColor(previousColor);
        }
    }

    static void drawText(final @NonNull CharSequence aText,
                                 final @NonNull TextSizeInformation aTextSizeInformation,
                                 final @Alignment int aAlignment,
                                 final @NonNull Canvas aCanvas,
                                 final float aSpanSize,
                                 final float aStartX,
                                 final int aY,
                                 final int aTop,
                                 final int aBottom) {
        switch (aAlignment) {
            case Alignment.BEGIN:
                alignTextLeft(aTextSizeInformation, aSpanSize);
                break;
            case Alignment.CENTER:
                centerText(aTextSizeInformation, aSpanSize);
                break;
            case Alignment.END:
                alignTextRight(aTextSizeInformation, aSpanSize);
                break;
            case Alignment.JUSTIFIED:
                justifyText(aTextSizeInformation, aSpanSize, false);
                break;
            case Alignment.JIS:
                justifyText(aTextSizeInformation, aSpanSize, true);
                break;
        }

        float cursor = aStartX;
        int count = 0;

        for (CharSequenceSizedElement charSequenceSizedElement : aTextSizeInformation.charSequenceSizedElements) {
            drawBackground(charSequenceSizedElement, aCanvas, cursor, aY,
                    cursor == 0,
                    count == aTextSizeInformation.charSequenceSizedElements.size() - 1);

            if (charSequenceSizedElement.charSequenceElement.replacementSpans != null &&
                    charSequenceSizedElement.charSequenceElement.replacementSpans.size() > 0) {
                final ReplacementSpan replacementSpan = charSequenceSizedElement.charSequenceElement.replacementSpans.get(
                        charSequenceSizedElement.charSequenceElement.replacementSpans.size() - 1);

                if (replacementSpan instanceof SuperReplacementSpan) {
                    ((SuperReplacementSpan) replacementSpan).drawExpanded(aCanvas, aText,
                            charSequenceSizedElement.charSequenceElement.start,
                            charSequenceSizedElement.charSequenceElement.end,
                            cursor,
                            aTop, aY, aBottom, charSequenceSizedElement.textPaint,
                            charSequenceSizedElement.spaceBefore + charSequenceSizedElement.size + charSequenceSizedElement.spaceAfter);
                } else {
                    replacementSpan.draw(aCanvas, aText, charSequenceSizedElement.charSequenceElement.start,
                            charSequenceSizedElement.charSequenceElement.end,
                            cursor + charSequenceSizedElement.spaceBefore,
                            aTop, aY, aBottom, charSequenceSizedElement.textPaint);
                }
            } else {
                if (charSequenceSizedElement.spaceBefore != 0 && count != 0) {
                    final float spaceSize = charSequenceSizedElement.textPaint.measureText(" ");
                    final float scaleX = charSequenceSizedElement.textPaint.getTextScaleX();

                    charSequenceSizedElement.textPaint.setTextScaleX(charSequenceSizedElement.spaceBefore / spaceSize);

                    aCanvas.drawText(" ", 0, 1, cursor, aY,
                            charSequenceSizedElement.textPaint);

                    charSequenceSizedElement.textPaint.setTextScaleX(scaleX);
                }

                aCanvas.drawText(aText, charSequenceSizedElement.charSequenceElement.start,
                        charSequenceSizedElement.charSequenceElement.end,
                        cursor + charSequenceSizedElement.spaceBefore, aY, charSequenceSizedElement.textPaint);

                if (charSequenceSizedElement.spaceAfter != 0 && count != aTextSizeInformation.charSequenceSizedElements.size() - 1) {
                    final float spaceSize = charSequenceSizedElement.textPaint.measureText(" ");
                    final float scaleX = charSequenceSizedElement.textPaint.getTextScaleX();

                    charSequenceSizedElement.textPaint.setTextScaleX(charSequenceSizedElement.spaceAfter / spaceSize);

                    aCanvas.drawText(" ", 0, 1, cursor + charSequenceSizedElement.spaceBefore + charSequenceSizedElement.size,
                            aY, charSequenceSizedElement.textPaint);

                    charSequenceSizedElement.textPaint.setTextScaleX(scaleX);
                }
            }

            cursor += charSequenceSizedElement.spaceBefore + charSequenceSizedElement.size + charSequenceSizedElement.spaceAfter;
            count++;
        }
    }

    private <T> List<T> getSpans(final @NonNull Spanned text, int start, int end, Class<T> type) {
        final LinkedList<T> list = new LinkedList<>();

        for (T span : text.getSpans(start, end, type)) {
            if (span != this) {
                list.add(span);
            } else if (type == SuperReplacementSpan.class) {
                break;
            }
        }

        return list;
    }

    public SuperReplacementSpan(final int aAlignment) {
        mAlignment = aAlignment;
    }

    public SuperReplacementSpan() {
        this(Alignment.CENTER);
    }


    final @Alignment int mAlignment;

    void drawExpanded(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint,
                              final float expandedSpanSize) {
        final TextSizeInformation textSizeInformation = getTextSize(paint, text, start, end);

        drawText(text, textSizeInformation, mAlignment, canvas,
                textSizeInformation.size, x, y,
                top,
                bottom);
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fm) {
        final TextSizeInformation textSizeInformation = getTextSize(paint, text, start, end);

        if (fm != null) {
            fm.bottom = textSizeInformation.fontMetricsInt.bottom;
            fm.ascent = textSizeInformation.fontMetricsInt.ascent;
            fm.top = textSizeInformation.fontMetricsInt.ascent;
            fm.descent = textSizeInformation.fontMetricsInt.descent;
            fm.leading = textSizeInformation.fontMetricsInt.leading;
        }

        return Math.round(textSizeInformation.size);
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
        drawExpanded(canvas, text, start, end, x, top, y, bottom, paint, 0);
    }
}

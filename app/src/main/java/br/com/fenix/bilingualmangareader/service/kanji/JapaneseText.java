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

import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class JapaneseText {
    private final static Pattern m_examplePattern;

    static {
        m_examplePattern = Pattern.compile("\\{([^\\};]+);([^\\};]+)\\}");
    }

    public static void spannify(final SpannableStringBuilder aSpannableStringBuilder,
                                 final String aString) {
        int offset = 0;
        int insertPos = aSpannableStringBuilder.length();

        while (offset < aString.length()) {
            final int nextOffset = aString.offsetByCodePoints(offset, 1);
            final String substring = aString.substring(offset, nextOffset);
            final int substringLength = substring.length();

            aSpannableStringBuilder.append(substring);
            aSpannableStringBuilder.setSpan(new SuperReplacementSpan(),
                    insertPos, insertPos + substringLength,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            insertPos += substringLength;
            offset = nextOffset;
        }
    }

    public static void spannifyWithFurigana(final SpannableStringBuilder aSpannableStringBuilder,
                                             final String aString,
                                             float aRelativeSize) {
        final Matcher matcher = m_examplePattern.matcher(aString);
        int previousMatchEnd = 0;

        while (matcher.find()) {
            final int matchStart = matcher.start();

            if (matchStart > previousMatchEnd) {
                spannify(aSpannableStringBuilder, aString.substring(previousMatchEnd, matchStart));
            }

            final String text = matcher.group(1);
            final String furigana = matcher.group(2);
            final int spanStart = aSpannableStringBuilder.length();

            if (text != null) {
                aSpannableStringBuilder.append(text);

                if (furigana != null) {
                    final SpannableString furiganaSpannable = new SpannableString(furigana);
                    furiganaSpannable.setSpan(new RelativeSizeSpan(aRelativeSize), 0, furiganaSpannable.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    aSpannableStringBuilder.setSpan(new SuperRubySpan(furiganaSpannable, SuperRubySpan.Alignment.JIS, SuperRubySpan.Alignment.JIS),
                            spanStart, spanStart + text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

            previousMatchEnd = matcher.end();
        }

        if (previousMatchEnd < aString.length() - 1) {
            spannify(aSpannableStringBuilder, aString.substring(previousMatchEnd, aString.length() - 1));
        }
    }
}

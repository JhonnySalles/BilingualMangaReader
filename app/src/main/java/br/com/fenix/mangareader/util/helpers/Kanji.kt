package br.com.fenix.mangareader.util.helpers

import android.text.Spanned

import android.text.style.BackgroundColorSpan

import android.text.style.ForegroundColorSpan

import android.text.SpannableStringBuilder

import android.text.SpannableString

import android.R
import android.graphics.Color
import android.widget.TextView


class Kanji {
    companion object Storage {
        private val fcsRed = ForegroundColorSpan(Color.RED);
        private val fcsGreen = ForegroundColorSpan(Color.GREEN)
        private val bcsYellow = BackgroundColorSpan(Color.YELLOW)
        //Used for a generate link and color in text
        fun generatedKanjiLevel(textView: TextView, text:String) {

            val ss = SpannableString(text)
            val ssb = SpannableStringBuilder(text)
            ssb.setSpan(fcsRed, 7, 11, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            ssb.setSpan(fcsGreen, 16, 20, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            ssb.setSpan(bcsYellow, 27, 34, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            ssb.append(" and this to be appended")

            textView.text = ssb
        }
    }
}
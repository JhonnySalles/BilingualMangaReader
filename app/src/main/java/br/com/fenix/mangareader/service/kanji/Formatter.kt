package br.com.fenix.mangareader.service.kanji

import android.content.Context
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.View
import br.com.fenix.mangareader.R
import br.com.fenix.mangareader.service.repository.KanjaxRepository
import br.com.fenix.mangareader.service.repository.KanjiRepository
import br.com.fenix.mangareader.service.tokenizers.SudachiTokenizer
import br.com.fenix.mangareader.util.constants.GeneralConsts
import br.com.fenix.mangareader.util.constants.ReaderConsts
import br.com.fenix.mangareader.util.helpers.JapaneseCharacter
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import com.github.javiersantos.materialstyleddialogs.enums.Style
import com.worksap.nlp.sudachi.Tokenizer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking


class Formatter {
    companion object KANJI {
        private var mRepository: KanjaxRepository? = null
        private val mPattern = Regex(".*[\u4E00-\u9FFF].*")
        var mTokenizer: Tokenizer? = null
        private var JLPT: Map<String, Int>? = null
        private var ANOTHER: Int = 0
        private var N1: Int = 0
        private var N2: Int = 0
        private var N3: Int = 0
        private var N4: Int = 0
        private var N5: Int = 0
        private var VOCABULARY: Int = 0

        fun initializeAsync(context: Context) =
            runBlocking { // this: CoroutineScope
                GlobalScope.async { // launch a new coroutine and continue
                    try {
                        mRepository = KanjaxRepository(context)
                        mTokenizer = SudachiTokenizer(context).tokenizer
                        val repository = KanjiRepository(context)
                        JLPT = repository.getHashMap()

                        ANOTHER = context.getColor(R.color.JLPT0)
                        N1 = context.getColor(R.color.JLPT1)
                        N2 = context.getColor(R.color.JLPT2)
                        N3 = context.getColor(R.color.JLPT3)
                        N4 = context.getColor(R.color.JLPT4)
                        N5 = context.getColor(R.color.JLPT5)
                        VOCABULARY = context.getColor(R.color.VOCABULARY)
                    } catch (e: Exception) {
                        Log.e(GeneralConsts.TAG.LOG, "Erro ao abrir arquivo de tokenizer." + e.message)
                    }
                }
            }

        private fun getPopupKanji(context: Context, kanji: String) {
            val kanjax = mRepository?.get(kanji)
            val title = SpannableString(kanji)
            title.setSpan(RelativeSizeSpan(3f), 0, kanji.length, 0)

            var middle = ""
            var bottom = ""
            var description = SpannableString(middle + bottom)
            if (kanjax != null) {
                middle = kanjax.keyword + "  -  " + kanjax.keywordPt + "\n\n" +
                        kanjax.meaning + "\n" + kanjax.meaningPt + "\n\n"

                middle += "onYomi: " + kanjax.onYomi + "\nKunYomi: " + kanjax.kunYomi + "\n\n"

                bottom =
                    "jlpt: " + kanjax.jlpt + " grade: " + kanjax.grade + " frequence: " + kanjax.frequence + "\n"

                description = SpannableString(middle + bottom)
                description.setSpan(RelativeSizeSpan(1.2f), 0, middle.length, 0)
                description.setSpan(
                    RelativeSizeSpan(0.8f),
                    middle.length,
                    middle.length + bottom.length,
                    0
                )
            }

            MaterialStyledDialog.Builder(context)
                .setTitle(title)
                .setDescription(description)
                .setStyle(Style.HEADER_WITH_TITLE)
                .setHeaderColor(R.color.onSecondary)
                .show()
        }

        fun generateFurigana(text: String, furigana: (CharSequence) -> (Unit), vocabularyClick: (String) -> (Unit)) {
            if (text.isEmpty()) {
                furigana(text)
                return
            }

            val textBuilder = SpannableStringBuilder()
            textBuilder.append(text);
            for (t in mTokenizer!!.tokenize(ReaderConsts.TOKENIZER.SUDACHI.SPLIT_MODE, text)) {
                if (t.readingForm().isNotEmpty() && t.surface().matches(mPattern)
                ) {
                    var furigana = ""
                    for (c in t.readingForm())
                        furigana += JapaneseCharacter.toHiragana(c)

                    val furiganaBuilder = SpannableStringBuilder(furigana)
                    furiganaBuilder.setSpan(
                        RelativeSizeSpan(0.75f),
                        0, furiganaBuilder.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    textBuilder.setSpan(
                        SuperRubySpan(
                            furiganaBuilder,
                            SuperReplacementSpan.Alignment.CENTER,
                            SuperReplacementSpan.Alignment.CENTER
                        ),
                        t.begin(), t.end(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    val cs = object : ClickableSpan() {
                        override fun onClick(p0: View) {
                            vocabularyClick(t.dictionaryForm())
                        }

                        override fun updateDrawState(ds: TextPaint) {
                            super.updateDrawState(ds)
                            ds.isUnderlineText = false
                            ds.color = VOCABULARY
                        }
                    }

                    textBuilder.setSpan(
                        cs,
                        t.begin(), t.end(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }

            furigana(textBuilder)
        }

        fun generateKanjiColor(
            context: Context,
            text: String,
            function: (SpannableString) -> (Unit)
        ) {
            if (text.isEmpty()) {
                function(SpannableString(text))
                return
            }

            val ss = SpannableString(text)
            ss.forEachIndexed { index, element ->
                val kanji = element.toString()
                if (kanji.matches(mPattern)) {
                    val color = when (JLPT?.get(kanji)) {
                        1 -> N1
                        2 -> N2
                        3 -> N3
                        4 -> N4
                        5 -> N5
                        else -> ANOTHER
                    }

                    val cs = object : ClickableSpan() {
                        override fun onClick(p0: View) {
                            getPopupKanji(context, kanji)
                        }

                        override fun updateDrawState(ds: TextPaint) {
                            super.updateDrawState(ds)
                            ds.isUnderlineText = false
                            ds.color = color
                        }
                    }
                    ss.setSpan(cs, index, index + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }

            function(ss)
        }

        fun generateFuriganaAndKanjiCollor(
            context: Context,
            text: String,
            function: (CharSequence) -> (Unit)
        ) {
            //generateFurigana(text, function, null)
        }
    }
}
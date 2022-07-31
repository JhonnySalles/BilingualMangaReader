package br.com.fenix.bilingualmangareader.service.kanji

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.RelativeSizeSpan
import android.view.View
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.service.repository.KanjaxRepository
import br.com.fenix.bilingualmangareader.service.repository.KanjiRepository
import br.com.fenix.bilingualmangareader.util.helpers.JapaneseCharacter
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import com.github.javiersantos.materialstyleddialogs.enums.Style
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory


class Formatter {
    companion object KANJI {
        private val mLOGGER = LoggerFactory.getLogger(Formatter::class.java)
        private var mRepository: KanjaxRepository? = null
        private val mPattern = Regex(".*[\u4E00-\u9FFF].*")

        @TargetApi(26)
        var mSudachiTokenizer: com.worksap.nlp.sudachi.Tokenizer? = null
        var mKuromojiTokenizer: com.atilika.kuromoji.ipadic.Tokenizer? = null
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
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            mSudachiTokenizer = br.com.fenix.bilingualmangareader.service.tokenizers.SudachiTokenizer(context).tokenizer
                        else
                            mKuromojiTokenizer = com.atilika.kuromoji.ipadic.Tokenizer()

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
                        mLOGGER.warn("Error in open tokenizer file." + e.message, e)
                    }
                }
            }

        private fun getPopupKanjiAlert(kanji: String, setContentAlert: (SpannableString, SpannableString) -> (Unit)) {
            val kanjax = mRepository?.get(kanji)
            val title = SpannableString(kanji)
            title.setSpan(RelativeSizeSpan(3f), 0, kanji.length, 0)

            var middle = ""
            var bottom = ""
            var description = SpannableString(middle + bottom)
            if (kanjax != null) {
                middle = kanjax.keyword + "  -  " + kanjax.keywordPt + "\n\n" +
                        kanjax.meaning + " | " + kanjax.meaningPt + "\n"

                middle += "onYomi: " + kanjax.onYomi + " | nKunYomi: " + kanjax.kunYomi + "\n"

                bottom =
                    "jlpt: " + kanjax.jlpt + " grade: " + kanjax.grade + " frequency: " + kanjax.frequence + "\n"

                description = SpannableString(middle + bottom)
                description.setSpan(RelativeSizeSpan(1.2f), 0, middle.length, 0)
                description.setSpan(
                    RelativeSizeSpan(0.8f),
                    middle.length,
                    middle.length + bottom.length,
                    0
                )
            }
            setContentAlert(title, description)
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
                    "jlpt: " + kanjax.jlpt + " grade: " + kanjax.grade + " frequency: " + kanjax.frequence + "\n"

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
                .setHeaderColor(R.color.on_secondary)
                .setScrollable(true)
                .show()
        }

        private fun generateFurigana(furigana: String): SpannableStringBuilder {
            val furiganaBuilder = SpannableStringBuilder(furigana)
            furiganaBuilder.setSpan(
                RelativeSizeSpan(0.75f),
                0, furiganaBuilder.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            return furiganaBuilder
        }

        private fun generateClick(text: String, click: (String) -> (Unit)): ClickableSpan {
            return object : ClickableSpan() {
                override fun onClick(p0: View) {
                    click(text)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                    ds.color = VOCABULARY
                }
            }
        }

        private fun kuromojiTokenizer(text: String, vocabularyClick: (String) -> (Unit)): SpannableStringBuilder {
            val textBuilder = SpannableStringBuilder()
            textBuilder.append(text)
            for (t in mKuromojiTokenizer!!.tokenize(text)) {
                if (t.surface.isNotEmpty() && t.surface.matches(mPattern)
                ) {
                    var furigana = ""
                    for (c in t.reading)
                        furigana += JapaneseCharacter.toHiragana(c)

                    textBuilder.setSpan(
                        SuperRubySpan(
                            generateFurigana(furigana),
                            SuperReplacementSpan.Alignment.CENTER,
                            SuperReplacementSpan.Alignment.CENTER
                        ),
                        t.position, t.position + t.surface.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    textBuilder.setSpan(
                        generateClick(t.baseForm, vocabularyClick),
                        t.position, t.position + t.surface.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }

            return textBuilder
        }

        @TargetApi(26)
        private fun sudachiTokenizer(text: String, vocabularyClick: (String) -> (Unit)): SpannableStringBuilder {
            val textBuilder = SpannableStringBuilder()
            textBuilder.append(text)
            if (mSudachiTokenizer != null) {
                for (t in mSudachiTokenizer!!.tokenize(com.worksap.nlp.sudachi.Tokenizer.SplitMode.C, text)) {
                    if (t.readingForm().isNotEmpty() && t.surface().matches(mPattern)
                    ) {
                        var furigana = ""
                        for (c in t.readingForm())
                            furigana += JapaneseCharacter.toHiragana(c)

                        textBuilder.setSpan(
                            SuperRubySpan(
                                generateFurigana(furigana),
                                SuperReplacementSpan.Alignment.CENTER,
                                SuperReplacementSpan.Alignment.CENTER
                            ),
                            t.begin(), t.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        textBuilder.setSpan(
                            generateClick(t.dictionaryForm(), vocabularyClick),
                            t.begin(), t.end(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
            }

            return textBuilder
        }

        fun generateFurigana(text: String, furigana: (CharSequence) -> (Unit), vocabularyClick: (String) -> (Unit)) {
            if (text.isEmpty()) {
                furigana(text)
                return
            }

            val textBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                sudachiTokenizer(text, vocabularyClick)
            else
                kuromojiTokenizer(text, vocabularyClick)

            furigana(textBuilder)
        }

        fun generateKanjiColor(context: Context, texts: ArrayList<String>): ArrayList<SpannableString> {
            val array = arrayListOf<SpannableString>()
            for (text in texts)
                generateKanjiColor(context, text) { array.add(it) }

            return array
        }

        fun generateKanjiColor(
            text: String,
            function: (SpannableString) -> (Unit),
            callAlert: (SpannableString, SpannableString) -> (Unit)
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
                            getPopupKanjiAlert(kanji, callAlert)
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
    }
}
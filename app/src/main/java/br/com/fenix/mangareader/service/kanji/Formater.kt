package br.com.fenix.mangareader.service.kanji

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import br.com.fenix.mangareader.R
import br.com.fenix.mangareader.service.repository.KanjiRepository
import br.com.fenix.mangareader.service.tokenizers.SudachiTokenizer
import br.com.fenix.mangareader.util.constants.GeneralConsts
import br.com.fenix.mangareader.util.constants.ReaderConsts
import com.worksap.nlp.sudachi.Tokenizer
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class Formater {
    companion object KANJI {
        private val pattern = Regex(".*[\u4E00-\u9FFF].*")
        var tokenizer: Tokenizer? = null
        private var JLPT: Map<String, Int>? = null
        private var ANOTHER: Int = 0
        private var N1: Int = 0
        private var N2: Int = 0
        private var N3: Int = 0
        private var N4: Int = 0
        private var N5: Int = 0

        private fun readAll(input: InputStream?): String? {
            val isReader = InputStreamReader(input, StandardCharsets.UTF_8)
            val reader = BufferedReader(isReader)
            val sb = StringBuilder()
            while (true) {
                val line: String = reader.readLine() ?: break
                sb.append(line)
            }
            return sb.toString()
        }

        fun initialize(context: Context) = runBlocking { // this: CoroutineScope
            launch { // launch a new coroutine and continue
                try {
                    tokenizer = SudachiTokenizer(context).tokenizer
                    val repository = KanjiRepository(context)
                    JLPT = repository.getHashMap()

                    ANOTHER = context.getColor(R.color.JLPT0)
                    N1 = context.getColor(R.color.JLPT1)
                    N2 = context.getColor(R.color.JLPT2)
                    N3 = context.getColor(R.color.JLPT3)
                    N4 = context.getColor(R.color.JLPT4)
                    N5 = context.getColor(R.color.JLPT5)
                } catch (e: Exception) {
                    Log.e(GeneralConsts.TAG.LOG, "Erro ao abrir arquivo de tokenizer." + e.message)
                }
            }
        }

        fun generateFurigana(text: String, function: (String) -> (Unit)) {
            if (text.isEmpty()) {
                function(text)
                return
            }

            var furigana = text
            val replaced = mutableSetOf<String>()
            for (m in tokenizer!!.tokenize(ReaderConsts.TOKENIZER.SUDACHI.SPLIT_MODE, furigana)) {
                if (!replaced.contains(m.surface()) && m.readingForm().isNotEmpty() && m.surface()
                        .matches(pattern)
                ) {
                    furigana = furigana.replace(
                        m.surface(),
                        "{" + m.surface() + ";" + m.readingForm() + "}",
                        true
                    )
                    replaced.add(m.surface())
                }
            }

            function(furigana)
        }

        fun generateKanjiColor(text: String, function: (SpannableString) -> (Unit)) {
            if (text.isEmpty()) {
                function(SpannableString(text))
                return
            }

            val ss = SpannableString(text)
            ss.forEachIndexed { index, element ->
                val kanji = element.toString()
                if (kanji.matches(pattern)) {
                    val color = when (JLPT?.get(kanji)) {
                        1 -> N1
                        2 -> N2
                        3 -> N3
                        4 -> N4
                        5 -> N5
                        else -> ANOTHER
                    }
                    ss.setSpan(ForegroundColorSpan(color), index, index + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }

            function(ss)
        }

        fun generateFuriganaAndKanjiCollor(text: String, function: (SpannableString) -> (Unit)) {
            generateFurigana(text) {
                generateKanjiColor(it) { color ->
                    function(color)
                }
            }
        }
    }
}
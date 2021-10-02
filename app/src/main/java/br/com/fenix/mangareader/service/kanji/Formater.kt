package br.com.fenix.mangareader.service.kanji

import android.content.Context
import android.content.res.AssetManager
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.TextView
import br.com.fenix.mangareader.R
import br.com.fenix.mangareader.service.repository.KanjiRepository
import br.com.fenix.mangareader.service.tokenizers.SudachiTokenizer
import br.com.fenix.mangareader.util.constants.GeneralConsts
import br.com.fenix.mangareader.util.constants.ReaderConsts
import com.worksap.nlp.sudachi.DictionaryFactory
import com.worksap.nlp.sudachi.Tokenizer
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class Formater {
    companion object {
        private val pattern = Regex(".*[\u4E00-\u9FAF].*")
        var tokenizer: Tokenizer? = null
        private var JLPT : Map<String, Int>? = null
        private var ANOTHER: ForegroundColorSpan? = null
        private var N1: ForegroundColorSpan? = null
        private var N2: ForegroundColorSpan? = null
        private var N3: ForegroundColorSpan? = null
        private var N4: ForegroundColorSpan? = null
        private var N5: ForegroundColorSpan? = null

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

                    ANOTHER = ForegroundColorSpan(context.getColor(R.color.JLPT0))
                    N1 = ForegroundColorSpan(context.getColor(R.color.JLPT1))
                    N2 = ForegroundColorSpan(context.getColor(R.color.JLPT2))
                    N3 = ForegroundColorSpan(context.getColor(R.color.JLPT3))
                    N4 = ForegroundColorSpan(context.getColor(R.color.JLPT4))
                    N5 = ForegroundColorSpan(context.getColor(R.color.JLPT5))

                    /*val assetManager: AssetManager = context.assets
                    val dictionary: InputStream =
                        context.resources.openRawResource(R.raw.sudachi_smalldict)
                    dictionary.use { input ->
                        DictionaryFactory().create(
                            "",
                            readAll(input)
                        ).use { dict ->
                            tokenizer = dict.create()
                        }
                    }*/
                } catch (e: Exception) {
                    Log.e(GeneralConsts.TAG.LOG, "Erro ao abrir arquivo de tokenizer." + e.message)
                }
            }
        }

        val example = "サンシャイン６０の<ruby>展望台<rt>てんぼうだい</rt></ruby>が<ruby>新<rt>あたら</rt></ruby>しくなる"
        fun generateFurigana(context: Context, textView: TextView, text: String) {
            if (text.isEmpty())
                return

            val replaced = mutableSetOf<String>()
            for (m in tokenizer!!.tokenize(ReaderConsts.TOKENIZER.SUDACHI.SPLIT_MODE, text)) {
                if (m.surface().matches(pattern) && !replaced.contains(m.surface())) {
                    text.replace(
                        m.surface(),
                        "<ruby>" + m.surface() + "<rt>" + m.readingForm() + "</rt></ruby>",
                        true
                    )
                    replaced.add(m.surface())
                }
            }

            val ss = SpannableString(text)
            text.forEachIndexed { index, element ->
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
                    ss.setSpan(color, index, index + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
            textView.text = ss
        }
    }
}
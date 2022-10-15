package br.com.fenix.bilingualmangareader.view.adapter.vocabulary

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Vocabulary
import br.com.fenix.bilingualmangareader.service.listener.VocabularyCardListener

class VocabularyViewHolder(itemView: View, private val listener: VocabularyCardListener) :
    RecyclerView.ViewHolder(itemView) {

    fun bind(vocabulary: Vocabulary) {
        val content = itemView.findViewById<LinearLayout>(R.id.vocabulary_content)
        val title = itemView.findViewById<TextView>(R.id.vocabulary_title)
        val reading = itemView.findViewById<TextView>(R.id.vocabulary_reading)
        val meaning = itemView.findViewById<TextView>(R.id.vocabulary_meaning)
        val appear = itemView.findViewById<TextView>(R.id.vocabulary_appear)
        val mangaList = itemView.findViewById<RecyclerView>(R.id.vocabulary_manga_list)

        content.setOnLongClickListener {
            val clipboard = itemView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied Text", vocabulary.word + " " + vocabulary.meaning)
            clipboard.setPrimaryClip(clip)

            Toast.makeText(
                itemView.context,
                itemView.context.getString(R.string.action_copy, vocabulary.word + " " + vocabulary.meaning),
                Toast.LENGTH_LONG
            ).show()

            true
        }

        title.text = vocabulary.word
        reading.text = vocabulary.reading + (if (vocabulary.revised) '¹' else "")
        meaning.text = vocabulary.meaning
        appear.text = vocabulary.appears.toString()

        val lineAdapter = VocabularyMangaListCardAdapter()
        mangaList.adapter = lineAdapter
        val layout = GridLayoutManager(itemView.context, 1)
        layout.orientation = RecyclerView.HORIZONTAL
        mangaList.layoutManager = layout
        lineAdapter.updateList(vocabulary.vocabularyMangas)
    }

}
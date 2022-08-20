package br.com.fenix.bilingualmangareader.view.adapter.vocabulary

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Vocabulary
import br.com.fenix.bilingualmangareader.service.listener.VocabularyCardListener

class VocabularyViewHolder(itemView: View, private val listener: VocabularyCardListener) :
    RecyclerView.ViewHolder(itemView) {

    fun bind(vocabulary: Vocabulary) {
        val title = itemView.findViewById<TextView>(R.id.vocabulary_title)
        val reading = itemView.findViewById<TextView>(R.id.vocabulary_reading)
        val meaning = itemView.findViewById<TextView>(R.id.vocabulary_meaning)
        val appear = itemView.findViewById<TextView>(R.id.vocabulary_appear)
        val mangaList = itemView.findViewById<RecyclerView>(R.id.vocabulary_manga_list)

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
package br.com.fenix.bilingualmangareader.view.adapter.vocabulary

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Vocabulary
import br.com.fenix.bilingualmangareader.service.listener.VocabularyCardListener
import br.com.fenix.bilingualmangareader.util.helpers.Util
import com.google.android.material.button.MaterialButton

class VocabularyViewHolder(itemView: View, private val listener: VocabularyCardListener) :
    RecyclerView.ViewHolder(itemView) {

    companion object {
        private fun createLayout(context: Context): GridLayoutManager {
            val layout = GridLayoutManager(context, 1)
            layout.orientation = RecyclerView.HORIZONTAL
            return layout
        }

        private fun createAdapter(): VocabularyMangaListCardAdapter =
            VocabularyMangaListCardAdapter()
    }

    val layout: GridLayoutManager = createLayout(itemView.context)
    val adapter: VocabularyMangaListCardAdapter = createAdapter()

    fun bind(vocabulary: Vocabulary) {
        println("bind")
        val content = itemView.findViewById<LinearLayout>(R.id.vocabulary_content)
        val title = itemView.findViewById<TextView>(R.id.vocabulary_title)
        val reading = itemView.findViewById<TextView>(R.id.vocabulary_reading)
        val meaning = itemView.findViewById<TextView>(R.id.vocabulary_meaning)
        val appear = itemView.findViewById<TextView>(R.id.vocabulary_appear)
        val mangaList = itemView.findViewById<RecyclerView>(R.id.vocabulary_manga_list)
        val favorite = itemView.findViewById<MaterialButton>(R.id.vocabulary_favorite)

        content.setOnLongClickListener {
            val clipboard = itemView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied Text", vocabulary.word + " " + vocabulary.portuguese)
            clipboard.setPrimaryClip(clip)

            Toast.makeText(
                itemView.context,
                itemView.context.getString(R.string.action_copy, vocabulary.word + " " + vocabulary.portuguese),
                Toast.LENGTH_LONG
            ).show()

            true
        }

        title.text = Util.setVerticalText(vocabulary.word)
        reading.text = vocabulary.reading + (if (!vocabulary.revised) '¹' else "")
        meaning.text = vocabulary.portuguese
        appear.text = itemView.context.getString(R.string.vocabulary_appear, vocabulary.appears)

        favorite.setIconResource(if (vocabulary.favorite) R.drawable.ic_favorite_mark else R.drawable.ic_favorite_unmark)

        favorite.setOnClickListener {
            vocabulary.favorite = !vocabulary.favorite
            favorite.setIconResource(if (vocabulary.favorite) R.drawable.ico_animated_favorited_marked else R.drawable.ico_animated_favorited_unmarked)
            (favorite.icon as AnimatedVectorDrawable).start()
            listener.onClickFavorite(vocabulary)
        }

        mangaList.adapter = adapter
        mangaList.layoutManager = layout
        adapter.updateList(vocabulary.vocabularyMangas)
    }

}
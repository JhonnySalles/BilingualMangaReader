package br.com.fenix.bilingualmangareader.view.adapter.reader

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Pages
import br.com.fenix.bilingualmangareader.service.listener.ChapterCardListener
import com.google.android.material.card.MaterialCardView


class ChaptersViewHolder(itemView: View, private val listener: ChapterCardListener) :
    RecyclerView.ViewHolder(itemView) {

    companion object {
        var mPageSelectStroke: Int = 0
    }

    init {
        mPageSelectStroke = itemView.resources.getDimension(R.dimen.manga_chapter_selected_stroke).toInt()
    }

    fun bind(page: Pages) {
        val card = itemView.findViewById<MaterialCardView>(R.id.chapter_card)
        val image = itemView.findViewById<ImageView>(R.id.chapter_image)
        val number = itemView.findViewById<TextView>(R.id.chapter_number)

        card.strokeWidth = if (page.isSelected) mPageSelectStroke else 0

        number.text = page.page.toString()
        card.setOnClickListener { listener.onClick(page) }
        image.setImageBitmap(page.image)
    }

}
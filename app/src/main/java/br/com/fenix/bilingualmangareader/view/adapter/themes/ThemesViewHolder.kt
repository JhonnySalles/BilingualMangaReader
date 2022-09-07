package br.com.fenix.bilingualmangareader.view.adapter.themes

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.enums.Themes
import br.com.fenix.bilingualmangareader.service.listener.ThemesListener
import br.com.fenix.bilingualmangareader.util.helpers.Util
import com.google.android.material.card.MaterialCardView


class ThemesViewHolder(itemView: View, private val listener: ThemesListener) :
    RecyclerView.ViewHolder(itemView) {

    companion object {
        var mPageSelectStroke: Int = 0
    }

    init {
        mPageSelectStroke = itemView.resources.getDimension(R.dimen.config_theme_selected_stroke).toInt()
    }

    fun bind(item: Pair<Themes, Boolean>) {
        val root = itemView.findViewById<LinearLayout>(R.id.theme_root)
        val card = itemView.findViewById<MaterialCardView>(R.id.theme_card)
        val description = itemView.findViewById<TextView>(R.id.theme_name)

        card.strokeWidth = if (item.second) mPageSelectStroke else 0
        description.text = Util.themeDescription(itemView.context, item.first)

        root.setOnClickListener { listener.onClick(item) }
    }

}
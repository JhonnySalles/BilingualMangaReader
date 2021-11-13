package br.com.fenix.bilingualmangareader.view.adapter.history

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.service.listener.MangaCardListener
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import java.util.*

class HistoryHeaderViewHolder(itemView: View, private val listener: MangaCardListener) :
    RecyclerView.ViewHolder(itemView) {

    fun bind(manga: Manga) {
        val title = itemView.findViewById<TextView>(R.id.history_divider_title)
        val cardView = itemView.findViewById<LinearLayout>(R.id.history_divider)

        if (manga.lastAccess != null) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            title.text = if (manga.lastAccess!!.after(calendar.time))
                itemView.context.getString(R.string.history_today)
            else
                GeneralConsts.formatterDate(
                    manga.lastAccess!!
                )
        } else
            title.text = ""

    }

}
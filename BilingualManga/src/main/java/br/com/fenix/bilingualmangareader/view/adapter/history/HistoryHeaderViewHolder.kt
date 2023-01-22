package br.com.fenix.bilingualmangareader.view.adapter.history

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.service.listener.MangaCardListener
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class HistoryHeaderViewHolder(itemView: View, private val listener: MangaCardListener) :
    RecyclerView.ViewHolder(itemView) {

    fun bind(manga: Manga) {
        val title = itemView.findViewById<TextView>(R.id.history_divider_title)
        //val cardView = itemView.findViewById<LinearLayout>(R.id.history_divider)

        if (manga.lastAccess != null) {
            title.text = if (manga.lastAccess!!.isAfter(LocalDateTime.now().minusDays(1)))
                itemView.context.getString(R.string.history_today)
            else if (manga.lastAccess!!.isAfter(LocalDateTime.now().minusDays(7)))
                itemView.context.getString(
                    R.string.history_day_ago,
                    ChronoUnit.DAYS.between(manga.lastAccess, LocalDateTime.now()).toString()
                )
            else
                GeneralConsts.formatterDate(
                    itemView.context,
                    manga.lastAccess!!
                )
        } else
            title.text = ""

    }

}
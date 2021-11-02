package br.com.fenix.mangareader.view.adapter.history

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.mangareader.R
import br.com.fenix.mangareader.model.entity.Manga
import br.com.fenix.mangareader.service.listener.MangaCardListener
import br.com.fenix.mangareader.util.constants.GeneralConsts
import java.time.LocalDateTime

class HistoryHeaderViewHolder(itemView: View, private val listener: MangaCardListener) :
    RecyclerView.ViewHolder(itemView) {

    fun bind(manga: Manga) {
        val title = itemView.findViewById<TextView>(R.id.history_divider_title)
        val cardView = itemView.findViewById<LinearLayout>(R.id.history_divider)

        if (manga.lastAccess != null) {
            title.text = if (manga.lastAccess!!.isAfter(LocalDateTime.now().minusDays(1)))
                itemView.context.getString(R.string.history_today)
            else
                GeneralConsts.formatterDate(
                    manga.lastAccess!!
                )
        } else
            title.text = ""

    }

}
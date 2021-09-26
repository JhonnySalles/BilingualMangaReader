package br.com.fenix.mangareader.view.adapter.library

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.mangareader.R
import br.com.fenix.mangareader.model.entity.Manga
import br.com.fenix.mangareader.service.controller.ImageCoverController
import br.com.fenix.mangareader.service.listener.MangaCardListener
import br.com.fenix.mangareader.util.constants.GeneralConsts
import com.google.android.material.card.MaterialCardView
import java.time.LocalDateTime

class GridViewHolder(itemView: View, private val listener: MangaCardListener) :
    RecyclerView.ViewHolder(itemView) {

    fun bind(manga: Manga) {
        val mangaImage = itemView.findViewById<ImageView>(R.id.manga_grid_image_cover)
        val mangaTitle = itemView.findViewById<TextView>(R.id.manga_grid_text_title)
        val mangaSubTitle = itemView.findViewById<TextView>(R.id.manga_grid_sub_title)
        val mangaProgress = itemView.findViewById<ProgressBar>(R.id.manga_grid_progress)
        val cardView = itemView.findViewById<MaterialCardView>(R.id.manga_grid_card)

        cardView.setOnClickListener { listener.onClick(manga) }

        ImageCoverController.instance.setImageCover(mangaImage, manga)

        mangaTitle.text = manga.title

        if (manga.subTitle.isEmpty()) {
            val title = if (manga.lastAccess != null && manga.lastAccess != LocalDateTime.MIN)
                "${manga.bookMark} / ${manga.pages}  -  ${GeneralConsts.formaterDate(manga.lastAccess!!)}"
            else
                "${manga.bookMark} / ${manga.pages}"

            mangaSubTitle.text = title
        } else
            mangaSubTitle.text = manga.subTitle

        mangaProgress.max = manga.pages
        mangaProgress.setProgress(manga.bookMark, false)
    }

}
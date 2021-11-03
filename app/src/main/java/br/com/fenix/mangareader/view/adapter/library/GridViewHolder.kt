package br.com.fenix.mangareader.view.adapter.library

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.mangareader.R
import br.com.fenix.mangareader.model.entity.Manga
import br.com.fenix.mangareader.model.enums.LibraryType
import br.com.fenix.mangareader.service.controller.ImageCoverController
import br.com.fenix.mangareader.service.listener.MangaCardListener
import br.com.fenix.mangareader.util.constants.GeneralConsts
import br.com.fenix.mangareader.view.ui.library.LibraryFragment
import com.google.android.material.card.MaterialCardView
import java.time.LocalDateTime


class GridViewHolder(itemView: View, private val listener: MangaCardListener) :
    RecyclerView.ViewHolder(itemView) {

    companion object {
        lateinit var mImageCover: Bitmap
    }

    init {
        mImageCover = BitmapFactory.decodeResource(itemView.resources, R.mipmap.book)
    }

    fun bind(manga: Manga) {
        val mangaImage = itemView.findViewById<ImageView>(R.id.manga_grid_image_cover)
        val mangaTitle = itemView.findViewById<TextView>(R.id.manga_grid_text_title)
        val mangaSubTitle = itemView.findViewById<TextView>(R.id.manga_grid_sub_title)
        val mangaProgress = itemView.findViewById<ProgressBar>(R.id.manga_grid_progress)
        val cardView = itemView.findViewById<MaterialCardView>(R.id.manga_grid_card)
        val favorite = itemView.findViewById<ImageView>(R.id.manga_grid_favorite)

        if (manga.favorite)
            favorite.visibility = View.VISIBLE
        else
            favorite.visibility = View.GONE

        val isLandscape =
            itemView.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        when (LibraryFragment.mGridType) {
            LibraryType.GRID_MEDIUM -> if (isLandscape) cardView.layoutParams.width =
                itemView.resources.getDimension(R.dimen.manga_grid_card_layout_width_landscape_medium)
                    .toInt()
            else cardView.layoutParams.width =
                itemView.resources.getDimension(R.dimen.manga_grid_card_layout_width_medium).toInt()
            LibraryType.GRID_SMALL ->
                if (isLandscape) {
                    cardView.layoutParams.width = itemView.resources.getDimension(R.dimen.manga_grid_card_layout_width_small).toInt()
                    cardView.layoutParams.height = itemView.resources.getDimension(R.dimen.manga_grid_card_layout_height_small).toInt()
                    mangaImage.layoutParams.height = itemView.resources.getDimension(R.dimen.manga_grid_card_image_small).toInt()
                }
            else -> {
                cardView.layoutParams.width =
                    itemView.resources.getDimension(R.dimen.manga_grid_card_layout_width).toInt()
                cardView.layoutParams.height = itemView.resources.getDimension(R.dimen.manga_grid_card_layout_height).toInt()
                mangaImage.layoutParams.height = itemView.resources.getDimension(R.dimen.manga_grid_card_image).toInt()
            }
        }

        cardView.setOnClickListener { listener.onClick(manga) }
        cardView.setOnLongClickListener {
            listener.onClickLong(manga, it, layoutPosition)
            true
        }

        if (manga.thumbnail != null && manga.thumbnail!!.image != null)
            mangaImage.setImageBitmap(manga.thumbnail!!.image)
        else {
            mangaImage.setImageBitmap(mImageCover)
            ImageCoverController.instance.setImageCoverAsync(itemView.context, manga, layoutPosition)
        }

        mangaTitle.text = manga.title

        if (manga.subTitle.isEmpty()) {
            val title = if (manga.lastAccess != null)
                "${manga.bookMark} / ${manga.pages}  -  ${GeneralConsts.formatterDate(manga.lastAccess!!)}"
            else
                "${manga.bookMark} / ${manga.pages}"

            mangaSubTitle.text = title
        } else
            mangaSubTitle.text = manga.subTitle

        mangaProgress.max = manga.pages
        mangaProgress.setProgress(manga.bookMark, false)
    }

}
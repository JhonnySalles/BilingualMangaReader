package br.com.fenix.bilingualmangareader.view.adapter.library

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.model.enums.LibraryType
import br.com.fenix.bilingualmangareader.service.controller.ImageCoverController
import br.com.fenix.bilingualmangareader.service.listener.MangaCardListener
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.view.ui.library.LibraryFragment
import com.google.android.material.card.MaterialCardView


class GridViewHolder(itemView: View, private val listener: MangaCardListener) :
    RecyclerView.ViewHolder(itemView) {

    companion object {
        var mIsLandscape: Boolean = false
        var mMangaCardWidth: Int = 0
        var mMangaCardHeight: Int = 0
        var mMangaCardWidthMedium: Int = 0
        var mMangaCardWidthLandscapeMedium: Int = 0
        var mMangaCardWidthSmall: Int = 0
        var mMangaCardHeightSmall: Int = 0
        var mMangaImage: Int = 0
        var mMangaImageSmall: Int = 0
        lateinit var mDefaultImageCover: Bitmap
    }

    init {
        mIsLandscape = itemView.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        mMangaCardWidth = itemView.resources.getDimension(R.dimen.manga_grid_card_layout_width).toInt()
        mMangaCardHeight = itemView.resources.getDimension(R.dimen.manga_grid_card_layout_height).toInt()
        mMangaCardWidthMedium = itemView.resources.getDimension(R.dimen.manga_grid_card_layout_width_medium).toInt()
        mMangaCardWidthLandscapeMedium = itemView.resources.getDimension(R.dimen.manga_grid_card_layout_width_landscape_medium).toInt()
        mMangaCardWidthSmall = itemView.resources.getDimension(R.dimen.manga_grid_card_layout_width_small).toInt()
        mMangaCardHeightSmall = itemView.resources.getDimension(R.dimen.manga_grid_card_layout_height_small).toInt()
        mMangaImageSmall = itemView.resources.getDimension(R.dimen.manga_grid_card_image_small).toInt()
        mMangaImage = itemView.resources.getDimension(R.dimen.manga_grid_card_image).toInt()
        mDefaultImageCover = BitmapFactory.decodeResource(itemView.resources, R.mipmap.book)
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

        when (LibraryFragment.mGridType) {
            LibraryType.GRID_MEDIUM -> if (mIsLandscape) cardView.layoutParams.width = mMangaCardWidthLandscapeMedium
            else cardView.layoutParams.width = mMangaCardWidthMedium
            LibraryType.GRID_SMALL ->
                if (mIsLandscape) {
                    cardView.layoutParams.width = mMangaCardWidthSmall
                    cardView.layoutParams.height = mMangaCardHeightSmall
                    mangaImage.layoutParams.height = mMangaImageSmall
                }
            else -> {
                cardView.layoutParams.width = mMangaCardWidth
                cardView.layoutParams.height = mMangaCardHeight
                mangaImage.layoutParams.height = mMangaImage
            }
        }

        cardView.setOnClickListener { listener.onClick(manga) }
        cardView.setOnLongClickListener {
            listener.onClickLong(manga, it, layoutPosition)
            true
        }

        mangaImage.setImageBitmap(mDefaultImageCover)
        ImageCoverController.instance.setImageCoverAsync(manga, mangaImage)

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
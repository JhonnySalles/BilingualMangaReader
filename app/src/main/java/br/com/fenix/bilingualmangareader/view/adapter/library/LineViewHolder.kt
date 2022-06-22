package br.com.fenix.bilingualmangareader.view.adapter.library

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.MainActivity
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.service.controller.ImageCoverController
import br.com.fenix.bilingualmangareader.service.listener.MangaCardListener
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts

class LineViewHolder(itemView: View, private val listener: MangaCardListener) :
    RecyclerView.ViewHolder(itemView) {

    companion object {
        lateinit var mDefaultImageCover : Bitmap
    }

    init {
        mDefaultImageCover = BitmapFactory.decodeResource(itemView.resources, R.mipmap.book_icon)
    }

    fun bind(manga: Manga) {
        val mangaImage = itemView.findViewById<ImageView>(R.id.manga_line_image_cover)
        val mangaTitle = itemView.findViewById<TextView>(R.id.manga_line_text_title)
        val mangaSubTitle = itemView.findViewById<TextView>(R.id.manga_line_sub_title)
        val mangaProgress = itemView.findViewById<ProgressBar>(R.id.manga_line_progress)
        val cardView = itemView.findViewById<LinearLayout>(R.id.manga_line_card)
        val favorite = itemView.findViewById<ImageView>(R.id.manga_line_favorite)

        if (manga.favorite)
            favorite.visibility = View.VISIBLE
        else
            favorite.visibility = View.GONE

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
                "${manga.bookMark} / ${manga.pages}  -  ${itemView.resources.getString(R.string.library_last_access)}: ${
                    GeneralConsts.formatterDate(
                        manga.lastAccess!!
                    )
                }"
            else
                "${manga.bookMark} / ${manga.pages}"

            mangaSubTitle.text = title
        } else
            mangaSubTitle.text = manga.subTitle

        mangaProgress.max = manga.pages
        mangaProgress.setProgress(manga.bookMark, false)
    }

}
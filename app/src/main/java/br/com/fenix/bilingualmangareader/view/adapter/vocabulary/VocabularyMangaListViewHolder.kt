package br.com.fenix.bilingualmangareader.view.adapter.vocabulary

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.VocabularyManga
import br.com.fenix.bilingualmangareader.service.controller.ImageCoverController
import br.com.fenix.bilingualmangareader.view.adapter.library.GridViewHolder
import com.google.android.material.card.MaterialCardView

class VocabularyMangaListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    companion object {
        lateinit var mDefaultImageCover1: Bitmap
        lateinit var mDefaultImageCover2: Bitmap
        lateinit var mDefaultImageCover3: Bitmap
        lateinit var mDefaultImageCover4: Bitmap
        lateinit var mDefaultImageCover5: Bitmap
    }

    init {
        mDefaultImageCover1 = BitmapFactory.decodeResource(itemView.resources, R.mipmap.book_cover_1)
        mDefaultImageCover2 = BitmapFactory.decodeResource(itemView.resources, R.mipmap.book_cover_2)
        mDefaultImageCover3 = BitmapFactory.decodeResource(itemView.resources, R.mipmap.book_cover_3)
        mDefaultImageCover4 = BitmapFactory.decodeResource(itemView.resources, R.mipmap.book_cover_4)
        mDefaultImageCover5 = BitmapFactory.decodeResource(itemView.resources, R.mipmap.book_cover_5)
    }

    fun bind(vocabulary: VocabularyManga) {
        val appear = itemView.findViewById<TextView>(R.id.vocabulary_manga_list_appear)
        val card = itemView.findViewById<MaterialCardView>(R.id.vocabulary_manga_list_image_card)
        val cover = itemView.findViewById<ImageView>(R.id.vocabulary_manga_list_image_cover)
        val name = itemView.findViewById<TextView>(R.id.vocabulary_manga_list_nome)

        appear.text = vocabulary.appears.toString()
        name.text = vocabulary.manga?.name ?: ""

        val image = when ((1..5).random()) {
            1 -> GridViewHolder.mDefaultImageCover1
            2 -> GridViewHolder.mDefaultImageCover2
            3 -> GridViewHolder.mDefaultImageCover3
            4 -> GridViewHolder.mDefaultImageCover4
            else -> GridViewHolder.mDefaultImageCover5
        }

        cover.setImageBitmap(image)
        vocabulary.manga?.let { ImageCoverController.instance.setImageCoverAsync(itemView.context, it, cover) }
    }

}
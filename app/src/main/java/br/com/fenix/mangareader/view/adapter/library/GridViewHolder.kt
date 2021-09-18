package br.com.fenix.mangareader.view.adapter.library

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.mangareader.R
import br.com.fenix.mangareader.model.entity.Book
import br.com.fenix.mangareader.service.listener.BookCardListener
import com.google.android.material.card.MaterialCardView

class GridViewHolder(itemView: View, private val listener: BookCardListener) :
    RecyclerView.ViewHolder(itemView) {

    fun bind(book: Book) {
        val bookImage = itemView.findViewById<ImageView>(R.id.book_grid_image_cover)
        val bookTitle = itemView.findViewById<TextView>(R.id.book_grid_text_title)
        val bookSubTitle = itemView.findViewById<TextView>(R.id.book_grid_sub_title)
        val bookProgress = itemView.findViewById<ProgressBar>(R.id.book_grid_book_progress)
        val cardView = itemView.findViewById<MaterialCardView>(R.id.book_grid_card)

        cardView.setOnClickListener { listener.onClick(book) }

        if (book.thumbnail != null && book.thumbnail!!.image != null)
            bookImage.setImageBitmap(book.thumbnail!!.image)

        bookTitle.text = book.title
        bookSubTitle.text = book.subTitle
        bookProgress.setProgress(book.bookMark / book.pages, false)
    }

}
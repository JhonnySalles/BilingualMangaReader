package br.com.fenix.mangareader.adapter

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.mangareader.R
import br.com.fenix.mangareader.listener.BookCardListener
import br.com.fenix.mangareader.model.Book

class LineViewHolder(itemView: View, private val listener: BookCardListener) :
    RecyclerView.ViewHolder(itemView) {

    fun bind(book: Book) {
        val bookImage: ImageView = itemView.findViewById(R.id.book_line_image_cover)
        val bookTitle: TextView = itemView.findViewById(R.id.book_line_text_title)
        val bookSubTitle: TextView = itemView.findViewById(R.id.book_line_sub_title)
        val bookProgress: ProgressBar = itemView.findViewById(R.id.book_line_book_progress)
        val cardView: LinearLayout = itemView.findViewById(R.id.book_line_card)

        //cardView.setOnClickListener(listener.onClick(book.id))

        if (book.thumbnail != null && book.thumbnail!!.image != null)
            bookImage.setImageBitmap(book.thumbnail!!.image)

        bookTitle.text = book.title
        bookSubTitle.text = book.subTitle
        bookProgress.setProgress(book.bookMark / book.pages, false)
    }

}
package br.com.fenix.mangareader.view.adapter.library

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.mangareader.R
import br.com.fenix.mangareader.model.entity.Book
import br.com.fenix.mangareader.service.listener.BookCardListener
import br.com.fenix.mangareader.util.constants.GeneralConsts
import java.time.LocalDateTime

class LineViewHolder(itemView: View, private val listener: BookCardListener) :
    RecyclerView.ViewHolder(itemView) {

    fun bind(book: Book) {
        val bookImage = itemView.findViewById<ImageView>(R.id.book_line_image_cover)
        val bookTitle = itemView.findViewById<TextView>(R.id.book_line_text_title)
        val bookSubTitle = itemView.findViewById<TextView>(R.id.book_line_sub_title)
        val bookProgress = itemView.findViewById<ProgressBar>(R.id.book_line_book_progress)
        val cardView = itemView.findViewById<LinearLayout>(R.id.book_line_card)

        cardView.setOnClickListener { listener.onClick(book) }

        if (book.thumbnail != null && book.thumbnail!!.image != null)
            bookImage.setImageBitmap(book.thumbnail!!.image)

        bookTitle.text = book.title

        if (book.subTitle.isEmpty()) {
            if (book.lastAccess != null && book.lastAccess != LocalDateTime.MIN)
                bookSubTitle.text =
                    "${book.bookMark} / ${book.pages}  -  ${itemView.resources.getString(R.string.library_last_access)}: ${GeneralConsts.formaterDate(
                        book.lastAccess!!
                    )}"
            else
                bookSubTitle.text = "${book.bookMark} / ${book.pages}"
        } else
            bookSubTitle.text = book.subTitle

        bookProgress.max = book.pages
        bookProgress.setProgress(book.bookMark, false)
    }

}
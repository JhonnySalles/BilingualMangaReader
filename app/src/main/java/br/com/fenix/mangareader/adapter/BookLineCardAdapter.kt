package br.com.fenix.mangareader.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.mangareader.R
import br.com.fenix.mangareader.model.Book

class BookLineCardAdapter(private val data: ArrayList<Book>, val context: Context) :
    RecyclerView.Adapter<BookLineCardAdapter.ViewHolder>() {

    lateinit var click: ClickListener

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.book_line_card, parent, false))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    interface ClickListener {
        fun onClick(pos: Int, view: View)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        var bookImage: ImageView
        var bookTitle: TextView
        var bookSubTitle: TextView
        var bookProgress: ProgressBar
        var cardView: LinearLayout

        init {
            itemView.setOnClickListener(this)

            cardView = itemView.findViewById(R.id.book_line_card)
            bookImage = itemView.findViewById(R.id.book_line_image_cover)
            bookTitle = itemView.findViewById(R.id.book_line_text_title)
            bookSubTitle = itemView.findViewById(R.id.book_line_sub_title)
            bookProgress = itemView.findViewById(R.id.book_line_book_progress)
        }

        fun bind(book: Book) {
            if (book.tumbnail != null && book.tumbnail!!.image != null)
                bookImage.setImageBitmap(book.tumbnail!!.image)

            bookTitle.text = book.title
            bookSubTitle.text = book.subTitle
            bookProgress.setProgress(book.bookMark / book.pages, false)
        }

        override fun onClick(p0: View?) {
            click.onClick(adapterPosition, itemView)
        }
    }

}
package br.com.fenix.mangareader.adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.mangareader.R
import br.com.fenix.mangareader.activity.ReaderActivity
import br.com.fenix.mangareader.constants.GeneralConsts
import br.com.fenix.mangareader.model.Book

class BookGridCardAdapter(private val data: ArrayList<Book>, val context: Context) :
    RecyclerView.Adapter<BookGridCardAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.book_grid_card, parent, false))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        var bookImage: ImageView
        var bookTitle: TextView
        var bookSubTitle: TextView
        var bookProgress: ProgressBar
        var cardView: CardView

        init {
            itemView.setOnClickListener(this)

            cardView = itemView.findViewById(R.id.book_grid_card)
            bookImage = itemView.findViewById(R.id.book_grid_image_cover)
            bookTitle = itemView.findViewById(R.id.book_grid_text_title)
            bookSubTitle = itemView.findViewById(R.id.book_grid_sub_title)
            bookProgress = itemView.findViewById(R.id.book_grid_book_progress)
        }

        fun bind(book: Book) {
            bookImage.setImageResource(
                context.resources.getIdentifier(
                    R.mipmap.book.toString(),
                    "drawable",
                    context.packageName
                )
            )
            bookTitle.text = book.title
            bookSubTitle.text = book.subTitle
            bookProgress.setProgress(book.bookMark / book.pages, false)
        }

        override fun onClick(p0: View?) {
            val book = data[adapterPosition]
            val intent = Intent(context, ReaderActivity::class.java)
            val bundle = Bundle()
            bundle.putString(GeneralConsts.KEYS.BOOK.PATH, book.file.path)
            bundle.putInt(GeneralConsts.KEYS.BOOK.MARK, book.bookMark)
            intent.putExtras(bundle)
            context.startActivity(intent)
        }
    }
}
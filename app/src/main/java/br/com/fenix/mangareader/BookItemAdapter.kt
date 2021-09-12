package br.com.fenix.mangareader

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class BookItemAdapter(private val data: List<Book>, val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    lateinit var click: ClickListener

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(context)
        return ViewHolder(inflater.inflate(R.layout.book_item, parent, false))
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
        var cardView: CardView

        init {
            itemView.setOnClickListener(this)
            bookImage = itemView.findViewById(R.id.image_cover)
            bookTitle = itemView.findViewById(R.id.text_title)
            bookSubTitle = itemView.findViewById(R.id.sub_title)
            bookProgress = itemView.findViewById(R.id.book_progress)
            cardView = itemView.findViewById(R.id.card_view)
        }

        fun bind(book: Book) {
            bookImage.setImageResource(
                context.resources.getIdentifier(
                    R.mipmap.book.toString(),
                    "drawable",
                    context.packageName
                )
            )
            bookTitle.text = book.Title
            bookSubTitle.text = book.SubTitle
            bookProgress.setProgress(book.bookMark / book.Pages, false)
        }

        override fun onClick(p0: View?) {
            click.onClick(adapterPosition, itemView)
        }

    }


}
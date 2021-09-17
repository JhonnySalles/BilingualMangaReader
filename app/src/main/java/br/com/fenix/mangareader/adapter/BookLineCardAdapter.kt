package br.com.fenix.mangareader.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.mangareader.R
import br.com.fenix.mangareader.listener.BookCardListener
import br.com.fenix.mangareader.model.Book
import java.util.*
import kotlin.collections.ArrayList


class BookLineCardAdapter() : RecyclerView.Adapter<LineViewHolder>(), Filterable {

    private lateinit var mListener: BookCardListener
    private var mBookList: ArrayList<Book> = arrayListOf()
    private var mBookListFull: ArrayList<Book> = arrayListOf()

    override fun onBindViewHolder(holder: LineViewHolder, position: Int) {
        holder.bind(mBookList[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LineViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(parent.context)
        return LineViewHolder(inflater.inflate(R.layout.book_grid_card, parent, false), mListener)
    }

    override fun getItemCount(): Int {
        return mBookList.size
    }

    fun updateList(list: ArrayList<Book>) {
        mBookList = list
        notifyDataSetChanged()
    }

    fun attachListener(listener: BookCardListener) {
        mListener = listener
    }

    override fun getFilter(): Filter {
        return bookFilter
    }

    private val bookFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList: MutableList<Book> = ArrayList()

            if (constraint == null || constraint.length === 0) {
                filteredList.addAll(mBookListFull)
            } else {
                val filterPattern = constraint.toString().lowercase(Locale.getDefault()).trim()

                filteredList.addAll(mBookListFull.filter {
                    it.name.lowercase(Locale.getDefault()).contains(filterPattern) ||
                            it.type.lowercase(Locale.getDefault()).contains(filterPattern)
                })
            }

            val results = FilterResults()
            results.values = filteredList

            return results
        }

        override fun publishResults(constraint: CharSequence?, filterResults: FilterResults?) {
            mBookList.clear()
            mBookList.addAll(filterResults!!.values as Collection<Book>)
            notifyDataSetChanged()
        }
    }

}
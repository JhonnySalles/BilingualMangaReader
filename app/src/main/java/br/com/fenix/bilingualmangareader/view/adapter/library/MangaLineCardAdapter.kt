package br.com.fenix.bilingualmangareader.view.adapter.library

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.service.listener.MangaCardListener
import java.util.*
import kotlin.collections.MutableList


class MangaLineCardAdapter : RecyclerView.Adapter<LineViewHolder>(), Filterable {

    private lateinit var mListener: MangaCardListener
    private var mMangaList: MutableList<Manga> = mutableListOf()
    private var mMangaListFull: MutableList<Manga> = mutableListOf()

    override fun onBindViewHolder(holder: LineViewHolder, position: Int) {
        holder.bind(mMangaList[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LineViewHolder {
        val item = LayoutInflater.from(parent.context).inflate(R.layout.manga_line_card, parent, false)
        return LineViewHolder(item, mListener)
    }

    override fun getItemCount(): Int {
        return mMangaList.size
    }

    fun updateList(list: MutableList<Manga>) {
        mMangaList = list
        mMangaListFull = list.toMutableList()
        notifyDataSetChanged()
    }

    fun attachListener(listener: MangaCardListener) {
        mListener = listener
    }

    override fun getFilter(): Filter {
        return mMangaFilter
    }

    private val mMangaFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList: MutableList<Manga> = mutableListOf()

            if (constraint == null || constraint.length === 0) {
                filteredList.addAll(mMangaListFull)
            } else {
                val filterPattern = constraint.toString().lowercase(Locale.getDefault()).trim()

                filteredList.addAll(mMangaListFull.filter {
                    it.name.lowercase(Locale.getDefault()).contains(filterPattern) ||
                            it.type.lowercase(Locale.getDefault()).contains(filterPattern)
                })
            }

            val results = FilterResults()
            results.values = filteredList

            return results
        }

        override fun publishResults(constraint: CharSequence?, filterResults: FilterResults?) {
            mMangaList.clear()
            mMangaList.addAll(filterResults!!.values as Collection<Manga>)
            notifyDataSetChanged()
        }
    }

}
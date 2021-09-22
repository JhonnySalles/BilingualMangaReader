package br.com.fenix.mangareader.view.adapter.library

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.mangareader.R
import br.com.fenix.mangareader.model.entity.Manga
import br.com.fenix.mangareader.service.listener.MangaCardListener
import java.util.*
import kotlin.collections.ArrayList

class MangaGridCardAdapter : RecyclerView.Adapter<GridViewHolder>(), Filterable {

    private lateinit var mListener: MangaCardListener
    private var mMangaList: ArrayList<Manga> = arrayListOf()
    private var mMangaListFull: ArrayList<Manga> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val item =
            LayoutInflater.from(parent.context).inflate(R.layout.manga_grid_card, parent, false)
        return GridViewHolder(item, mListener)
    }

    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
        holder.bind(mMangaList[position])
    }


    override fun getItemCount(): Int {
        return mMangaList.size
    }

    fun attachListener(listener: MangaCardListener) {
        mListener = listener
    }

    fun updateList(list: ArrayList<Manga>) {
        mMangaList = list
        mMangaListFull = ArrayList(list)
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return mMangaFilter
    }

    private val mMangaFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList: MutableList<Manga> = ArrayList()

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
package br.com.fenix.mangareader.view.adapter.library

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.mangareader.R
import br.com.fenix.mangareader.model.entity.Manga
import br.com.fenix.mangareader.service.listener.MangaCardListener


class HistoryCardAdapter : RecyclerView.Adapter<HistoryViewHolder>() {

    private lateinit var mListener: MangaCardListener
    private var mHistoryList: ArrayList<Manga> = arrayListOf()

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(mHistoryList[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val item = LayoutInflater.from(parent.context).inflate(R.layout.history_card, parent, false)
        return HistoryViewHolder(item, mListener)
    }

    override fun getItemCount(): Int {
        return mHistoryList.size
    }

    fun updateList(list: ArrayList<Manga>) {
        mHistoryList = list
        notifyDataSetChanged()
    }

    fun attachListener(listener: MangaCardListener) {
        mListener = listener
    }

}
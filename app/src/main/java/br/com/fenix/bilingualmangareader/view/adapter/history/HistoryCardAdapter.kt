package br.com.fenix.bilingualmangareader.view.adapter.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.service.listener.MangaCardListener


class HistoryCardAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var mListener: MangaCardListener
    private var mHistoryList: ArrayList<Manga> = arrayListOf()

    companion object {
        private const val HEADER = 1
        private const val CONTENT = 0
    }

    override fun getItemViewType(position: Int): Int = if (mHistoryList[position].id == null) HEADER else CONTENT

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            HEADER -> {
                (holder as HistoryHeaderViewHolder).bind(mHistoryList[position])
            }
            else -> {
                (holder as HistoryViewHolder).bind(mHistoryList[position])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            HEADER -> HistoryHeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.line_card_divider_history, parent, false), mListener)
            else -> HistoryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.line_card_history, parent, false), mListener)
        }
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

    fun notifyItemChanged(manga: Manga) {
        if (mHistoryList.contains(manga))
            notifyItemChanged(mHistoryList.indexOf(manga))
    }

}
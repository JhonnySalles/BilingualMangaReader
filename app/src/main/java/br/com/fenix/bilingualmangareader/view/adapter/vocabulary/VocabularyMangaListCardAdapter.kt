package br.com.fenix.bilingualmangareader.view.adapter.vocabulary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.VocabularyManga


class VocabularyMangaListCardAdapter : RecyclerView.Adapter<VocabularyMangaListViewHolder>() {

    private var mList: List<VocabularyManga> = listOf()

    override fun onBindViewHolder(holder: VocabularyMangaListViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VocabularyMangaListViewHolder {
        val item = LayoutInflater.from(parent.context).inflate(R.layout.line_card_vocabulary_manga_list, parent, false)
        return VocabularyMangaListViewHolder(item)
    }

    override fun getItemCount(): Int {
        return mList.size
    }
    
    fun updateList(list: List<VocabularyManga>) {
        mList = list
        notifyDataSetChanged()
    }

}
package br.com.fenix.bilingualmangareader.view.adapter.vocabulary

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.VocabularyManga


class VocabularyMangaListCardAdapter : RecyclerView.Adapter<VocabularyMangaListViewHolder>() {

    companion object {
        private var mMangaList: MutableMap<Long, Bitmap?> = mutableMapOf()
        fun clearVocabularyMangaList() =
            mMangaList.clear()
    }

    private var mList: List<VocabularyManga> = listOf()

    override fun onBindViewHolder(holder: VocabularyMangaListViewHolder, position: Int) {
        holder.bind(mList[position], mMangaList)
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
        if (list.size > 10)
            notifyItemChanged(0, 10)
        else
            notifyItemChanged(0, list.size)
    }

}
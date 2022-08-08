package br.com.fenix.bilingualmangareader.view.adapter.reader

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Pages
import br.com.fenix.bilingualmangareader.service.listener.ChapterCardListener

class MangaChaptersCardAdapter : RecyclerView.Adapter<ChaptersViewHolder>() {

    private lateinit var mListener: ChapterCardListener
    private var mList: MutableList<Pages> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChaptersViewHolder {
        val item =
            LayoutInflater.from(parent.context).inflate(R.layout.chapter_card, parent, false)
        return ChaptersViewHolder(item, mListener)
    }

    override fun onBindViewHolder(holder: ChaptersViewHolder, position: Int) {
        holder.bind(mList[position])
        //holder.itemView.animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.animation_library_grid)
    }

    override fun onViewDetachedFromWindow(holder: ChaptersViewHolder) {
        //holder.itemView.clearAnimation()
        super.onViewDetachedFromWindow(holder)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun attachListener(listener: ChapterCardListener) {
        mListener = listener
    }

    fun updateList(list: MutableList<Pages>) {
        mList = list
        notifyDataSetChanged()
    }

}
package br.com.fenix.bilingualmangareader.view.adapter.library

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.service.listener.MangaCardListener


class MangaLineCardAdapter : RecyclerView.Adapter<LineViewHolder>() {

    private lateinit var mListener: MangaCardListener
    private var mMangaList: MutableList<Manga> = mutableListOf()
    var isAnimation: Boolean = true

    override fun onBindViewHolder(holder: LineViewHolder, position: Int) {
        holder.bind(mMangaList[position])
        if (isAnimation)
            holder.itemView.animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.animation_library_line)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LineViewHolder {
        val item = LayoutInflater.from(parent.context).inflate(R.layout.line_card_manga, parent, false)
        return LineViewHolder(item, mListener)
    }

    override fun onViewDetachedFromWindow(holder: LineViewHolder) {
        holder.itemView.clearAnimation()
        super.onViewDetachedFromWindow(holder)
    }

    override fun getItemCount(): Int {
        return mMangaList.size
    }

    fun removeList(manga: Manga) {
        if (mMangaList.contains(manga))
            notifyItemRemoved(mMangaList.indexOf(manga))
        mMangaList.remove(manga)
    }

    fun updateList(list: MutableList<Manga>) {
        mMangaList = list
        notifyDataSetChanged()
    }

    fun attachListener(listener: MangaCardListener) {
        mListener = listener
    }
}
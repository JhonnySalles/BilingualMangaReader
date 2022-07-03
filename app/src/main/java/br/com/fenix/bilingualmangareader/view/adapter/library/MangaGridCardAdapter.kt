package br.com.fenix.bilingualmangareader.view.adapter.library

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.service.listener.MangaCardListener

class MangaGridCardAdapter : RecyclerView.Adapter<GridViewHolder>() {

    private lateinit var mListener: MangaCardListener
    private var mMangaList: MutableList<Manga> = mutableListOf()
    var isAnimation: Boolean = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val item =
            LayoutInflater.from(parent.context).inflate(R.layout.manga_grid_card, parent, false)
        return GridViewHolder(item, mListener)
    }

    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
        holder.bind(mMangaList[position])
        if (isAnimation)
            holder.itemView.animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.animation_library_grid)
    }

    override fun onViewDetachedFromWindow(holder: GridViewHolder) {
        holder.itemView.clearAnimation()
        super.onViewDetachedFromWindow(holder)
    }

    override fun getItemCount(): Int {
        return mMangaList.size
    }

    fun attachListener(listener: MangaCardListener) {
        mListener = listener
    }

    fun removeList(manga: Manga) {
        mMangaList.remove(manga)
    }

    fun updateList(list: MutableList<Manga>) {
        mMangaList = list
        notifyDataSetChanged()
    }

}
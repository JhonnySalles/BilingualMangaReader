package br.com.fenix.bilingualmangareader.view.adapter.themes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.enums.Themes
import br.com.fenix.bilingualmangareader.service.listener.ThemesListener

class ThemesCardAdapter : RecyclerView.Adapter<ThemesViewHolder>() {

    private lateinit var mListener: ThemesListener
    private var mList: MutableList<Pair<Themes, Boolean>> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemesViewHolder {
        return ThemesViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.grid_card_theme, parent, false), mListener)
    }

    override fun onBindViewHolder(holder: ThemesViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun attachListener(listener: ThemesListener) {
        mListener = listener
    }

    fun updateList(list: MutableList<Pair<Themes, Boolean>>) {
        mList = list
        notifyDataSetChanged()
    }

}
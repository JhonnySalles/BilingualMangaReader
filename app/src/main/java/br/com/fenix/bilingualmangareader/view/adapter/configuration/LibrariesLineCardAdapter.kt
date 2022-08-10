package br.com.fenix.bilingualmangareader.view.adapter.configuration

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Library
import br.com.fenix.bilingualmangareader.service.listener.LibrariesCardListener

class LibrariesLineCardAdapter : RecyclerView.Adapter<LibrariesLineViewHolder>() {

    private lateinit var mListener: LibrariesCardListener
    private var mList: MutableList<Library> = mutableListOf()

    override fun onBindViewHolder(holder: LibrariesLineViewHolder, position: Int) {
        holder.bind(mList[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibrariesLineViewHolder {
        val item = LayoutInflater.from(parent.context).inflate(R.layout.line_card_library, parent, false)
        return LibrariesLineViewHolder(item, mListener)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun removeList(manga: Library) {
        mList.remove(manga)
    }

    fun updateList(list: MutableList<Library>) {
        mList = list
        notifyDataSetChanged()
    }

    fun attachListener(listener: LibrariesCardListener) {
        mListener = listener
    }
}
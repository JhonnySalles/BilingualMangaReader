package br.com.fenix.bilingualmangareader.view.adapter.page_link

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.PageLink
import br.com.fenix.bilingualmangareader.service.listener.PageLinkCardListener

class PageLinkCardAdapter : RecyclerView.Adapter<PageLinkViewHolder>() {

    private lateinit var mListener: PageLinkCardListener
    private var mPageLinkList: ArrayList<PageLink> = arrayListOf()

    override fun onBindViewHolder(holder: PageLinkViewHolder, position: Int) {
        holder.bind(mPageLinkList[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageLinkViewHolder {
        return PageLinkViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.page_link_card, parent, false), mListener)
    }

    override fun getItemCount(): Int {
        return mPageLinkList.size
    }

    fun updateList(list: ArrayList<PageLink>) {
        mPageLinkList = list
        notifyDataSet()
    }

    fun attachListener(listener: PageLinkCardListener) {
        mListener = listener
    }

    private fun notifyDataSet(idItem: Int? = null) {
        if (idItem != null)
            notifyItemChanged(idItem)
        else
            notifyDataSetChanged()
    }

}
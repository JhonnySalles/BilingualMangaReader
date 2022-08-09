package br.com.fenix.bilingualmangareader.view.adapter.vocabulary

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Vocabulary
import br.com.fenix.bilingualmangareader.service.listener.VocabularyCardListener


class VocabularyCardAdapter : RecyclerView.Adapter<VocabularyViewHolder>() {

    private lateinit var mListener: VocabularyCardListener
    private var mMutableList: MutableList<Vocabulary> = mutableListOf()
    var isAnimation: Boolean = true

    override fun onBindViewHolder(holder: VocabularyViewHolder, position: Int) {
        holder.bind(mMutableList[position])
        if (isAnimation)
            holder.itemView.animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.animation_library_line)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VocabularyViewHolder {
        val item = LayoutInflater.from(parent.context).inflate(R.layout.vocabulary_line_card, parent, false)
        return VocabularyViewHolder(item, mListener)
    }

    override fun onViewDetachedFromWindow(holder: VocabularyViewHolder) {
        holder.itemView.clearAnimation()
        super.onViewDetachedFromWindow(holder)
    }

    override fun getItemCount(): Int {
        return mMutableList.size
    }

    fun removeList(vocabulary: Vocabulary) {
        mMutableList.remove(vocabulary)
    }

    fun updateList(list: MutableList<Vocabulary>) {
        mMutableList = list
        notifyDataSetChanged()
    }

    fun attachListener(listener: VocabularyCardListener) {
        mListener = listener
    }
}
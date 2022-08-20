package br.com.fenix.bilingualmangareader.view.adapter.vocabulary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Vocabulary
import br.com.fenix.bilingualmangareader.service.listener.VocabularyCardListener


class VocabularyCardAdapter(var listener: VocabularyCardListener) :
    PagingDataAdapter<Vocabulary, VocabularyViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VocabularyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.line_card_vocabulary, parent, false), listener)

    override fun onBindViewHolder(holder: VocabularyViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Vocabulary>() {
            override fun areItemsTheSame(oldItem: Vocabulary, newItem: Vocabulary): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Vocabulary, newItem: Vocabulary): Boolean =
                oldItem == newItem
        }
    }
}



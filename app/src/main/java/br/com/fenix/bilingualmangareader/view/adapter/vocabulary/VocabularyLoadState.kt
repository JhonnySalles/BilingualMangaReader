package br.com.fenix.bilingualmangareader.view.adapter.vocabulary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import br.com.fenix.bilingualmangareader.R

class VocabularyLoadState : LoadStateAdapter<VocabularyLoadState.LoadStateViewHolder>() {

    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
        val params = holder.itemView.layoutParams
        if (params is StaggeredGridLayoutManager.LayoutParams) params.isFullSpan = true
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): LoadStateViewHolder {
        return LoadStateViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.loading_vocabulary, parent, false)
        )
    }

    inner class LoadStateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(loadState: LoadState) {
            val progress = itemView.findViewById<ProgressBar>(R.id.vocabulary_loading_progress)
            progress.visibility = if (loadState is LoadState.Loading) View.VISIBLE else View.GONE
        }
    }
}



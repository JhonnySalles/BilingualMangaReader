package br.com.fenix.bilingualmangareader.service.listener

import android.view.View
import br.com.fenix.bilingualmangareader.model.entity.Vocabulary

interface VocabularyCardListener {
    fun onClick(vocabulary: Vocabulary)
    fun onClickLong(vocabulary: Vocabulary, view: View, position: Int)
    fun onClickFavorite(vocabulary: Vocabulary)
}
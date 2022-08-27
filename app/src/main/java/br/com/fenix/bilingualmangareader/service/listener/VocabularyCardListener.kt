package br.com.fenix.bilingualmangareader.service.listener

import android.view.View
import br.com.fenix.bilingualmangareader.model.entity.Manga

interface VocabularyCardListener {
    fun onClick(manga: Manga)
    fun onClickLong(manga: Manga, view : View, position: Int)
}
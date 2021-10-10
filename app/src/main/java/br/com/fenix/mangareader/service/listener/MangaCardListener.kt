package br.com.fenix.mangareader.service.listener

import android.view.View
import br.com.fenix.mangareader.model.entity.Manga

interface MangaCardListener {
    fun onClick(manga: Manga)
    fun onClickLong(manga: Manga, view : View)
}
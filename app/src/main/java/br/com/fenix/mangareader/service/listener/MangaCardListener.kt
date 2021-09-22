package br.com.fenix.mangareader.service.listener

import br.com.fenix.mangareader.model.entity.Manga

interface MangaCardListener {
    fun onClick(manga: Manga)
    fun onClickLong(manga: Manga)
    fun onAddFavorite(manga: Manga)
}
package br.com.fenix.mangareader.service.listener

import br.com.fenix.mangareader.model.entity.Book

interface BookCardListener {
    fun onClick(book: Book)
    fun onClickLong(book: Book)
    fun onAddFavorite(book: Book)
}
package br.com.fenix.bilingualmangareader.service.listener

import br.com.fenix.bilingualmangareader.model.entity.Library

interface LibrariesCardListener {
    fun onClickLong(library: Library)
    fun changeEnable(library: Library)
}
package br.com.fenix.bilingualmangareader.service.listener

import br.com.fenix.bilingualmangareader.model.entity.Pages

interface ChapterCardListener {
    fun onClick(page: Pages)
}
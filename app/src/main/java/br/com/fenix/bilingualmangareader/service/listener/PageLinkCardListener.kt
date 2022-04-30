package br.com.fenix.bilingualmangareader.service.listener

import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.model.entity.PageLink

interface PageLinkCardListener {
    fun onClick(page: PageLink)
}
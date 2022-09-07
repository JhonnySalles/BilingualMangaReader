package br.com.fenix.bilingualmangareader.service.listener

import br.com.fenix.bilingualmangareader.model.enums.Themes

interface ThemesListener {
    fun onClick(theme: Pair<Themes, Boolean>)
}
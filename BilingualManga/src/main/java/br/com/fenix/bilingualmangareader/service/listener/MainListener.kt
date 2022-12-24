package br.com.fenix.bilingualmangareader.service.listener

interface MainListener {
    fun showUpButton()
    fun hideUpButton()

    fun changeLibraryTitle(library: String)
    fun clearLibraryTitle()
}
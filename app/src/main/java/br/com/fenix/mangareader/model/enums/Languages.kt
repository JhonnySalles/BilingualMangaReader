package br.com.fenix.mangareader.model.enums

enum class Languages(private val value: String) {
    PT("Portuguese"),
    EN("English"),
    JP("japanese");

    override fun toString() : String {
        return value
    }
}
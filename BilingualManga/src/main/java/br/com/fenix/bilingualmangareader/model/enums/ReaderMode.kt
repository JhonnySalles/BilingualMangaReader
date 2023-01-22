package br.com.fenix.bilingualmangareader.model.enums

enum class ReaderMode(private val value: Int) {
    ASPECT_FILL(0),
    ASPECT_FIT(1),
    FIT_WIDTH(2);

    var native_int = 0

    open fun PageViewMode(n: Int) {
        native_int = n
    }
}
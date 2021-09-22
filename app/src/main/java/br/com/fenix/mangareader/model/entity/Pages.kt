package br.com.fenix.mangareader.model.entity

data class Pages(
    var name: String,
    val number: Int,
    val hash: String,
    val texts: List<Text>,
    val vocabulary: MutableSet<Vocabulary>
)
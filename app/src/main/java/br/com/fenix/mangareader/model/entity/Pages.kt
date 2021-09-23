package br.com.fenix.mangareader.model.entity

import com.google.gson.annotations.SerializedName

data class Pages(
    @SerializedName("nomePagina")
    var name: String,
    @SerializedName("numero")
    val number: Int,
    val hash: String,
    @SerializedName("textos")
    val texts: List<Text>,
    @SerializedName("vocabularios")
    val vocabulary: MutableSet<Vocabulary>
)
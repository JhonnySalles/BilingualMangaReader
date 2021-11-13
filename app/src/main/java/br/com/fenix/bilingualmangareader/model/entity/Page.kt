package br.com.fenix.bilingualmangareader.model.entity

import com.google.gson.annotations.SerializedName

data class Page(
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
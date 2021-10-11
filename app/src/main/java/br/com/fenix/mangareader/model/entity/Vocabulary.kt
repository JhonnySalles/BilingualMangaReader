package br.com.fenix.mangareader.model.entity

import com.google.gson.annotations.SerializedName

data class Vocabulary(
    @SerializedName("palavra")
    val word: String,
    @SerializedName("significado")
    val meaning: String,
    @SerializedName("leitura")
    val reading: String,
    @SerializedName("revisado")
    val revised: Boolean
)
package br.com.fenix.bilingualmangareader.service.tracker.mal

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("picture")
    val picture: String,
    @SerializedName("gender")
    val gender: String?,
    @SerializedName("birthday")
    val birthday: String?,
    @SerializedName("location")
    val location: String?,
    @SerializedName("joined_at")
    val joinedAt: String,
    //@SerializedName("anime_statistics")
    //lateinit var anime_statistics  object or null
    @SerializedName("time_zone")
    val timeZone: String?,
    @SerializedName("is_supporter")
    val isSupporter: Boolean?
)
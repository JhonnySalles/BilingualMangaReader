package br.com.fenix.bilingualmangareader.service.tracker.mal

import com.google.gson.annotations.SerializedName

data class MalMangaDetail(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String = "",
    @SerializedName("main_picture")
    val mainPicture: MalPicture?,
    @SerializedName("alternative_titles")
    val alternativeTitles: MalAlternativeTitles?,
    @SerializedName("start_date")
    val startDate: String?,
    @SerializedName("end_date")
    val endDate: String?,
    @SerializedName("synopsis")
    val synopsis: String?,
    @SerializedName("mean")
    val mean: String?,
    @SerializedName("rank")
    val rank: String?,
    @SerializedName("popularity")
    val popularity: Int = 0,
    @SerializedName("num_list_users")
    val numListUsers: Int = 0,
    @SerializedName("num_scoring_users")
    val numScoringUsers: Int = 0,
    @SerializedName("nsfw")
    val nsfw: NSFW?,
    @SerializedName("genres")
    val genres: List<MalGenres>?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?,
    @SerializedName("media_type")
    val mediaType: MEDIA?,
    @SerializedName("status")
    val status: STATUS?,
    @SerializedName("my_list_status")
    val myListStatus: List<MalStatus>?,
    @SerializedName("num_volumes")
    val volumes: Int = 0,
    @SerializedName("num_chapters")
    val chapters: Int = 0,
    @SerializedName("authors")
    val authors: List<MalAuthors>?,
    @SerializedName("pictures")
    val pictures: List<MalPicture>?
)


data class MalPicture(
    @SerializedName("large")
    val large: String,
    @SerializedName("medium")
    val medium: String
)

data class MalAlternativeTitles(
    @SerializedName("synonyms")
    val synonyms: List<String>,
    @SerializedName("en")
    val english: String,
    @SerializedName("ja")
    val japanese: String
)

data class MalGenres(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String
)

data class MalAuthors(
    @SerializedName("node")
    val author: Author,
    @SerializedName("role")
    val role: String
)

data class Author(
    @SerializedName("id")
    val id: Int,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
)

data class MalStatus(
    @SerializedName("status")
    val status: String?,
    @SerializedName("score")
    val score: Int,
    @SerializedName("num_volumes_read")
    val numVolumesRead: Int,
    @SerializedName("num_chapters_read")
    val numChaptersRead: Int,
    @SerializedName("is_rereading")
    val isRereading: Boolean,
    @SerializedName("start_date")
    val startDate: String?,
    @SerializedName("finish_date")
    val finishDate: String?,
    @SerializedName("priority")
    val priority: Int,
    @SerializedName("num_times_reread")
    val numTimesReread: Int,
    @SerializedName("reread_value")
    val reread_value: Int,
    @SerializedName("tags")
    val tags: List<String>,
    @SerializedName("comments")
    val comments: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

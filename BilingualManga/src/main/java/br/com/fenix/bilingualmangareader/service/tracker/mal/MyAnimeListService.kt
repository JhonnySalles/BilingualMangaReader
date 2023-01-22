package br.com.fenix.bilingualmangareader.service.tracker.mal

import retrofit2.Call
import retrofit2.http.*

interface MyAnimeListService {

    companion object {
        const val FIELDS = "id,title,main_picture,alternative_titles,start_date,end_date,synopsis,mean,rank,popularity,num_list_users,num_scoring_users,nsfw,created_at,updated_at,media_type,status,genres,my_list_status,num_volumes,num_chapters,authors{first_name,last_name},pictures,background,related_anime,related_manga,recommendations,serialization{name}"
    }

    @GET("authorize")
    fun auth(
        @Query("client_id") idClient: String,
        @Query("code_challenge") challenge: String,
        @Query("response_type") authorization: String = "code"
    ): Call<OAuth>

    @POST("token")
    @FormUrlEncoded
    fun refreshToken(
        @Field("client_id") idClient: String,
        @Field("refresh_token") refreshToken: String,
        @Field("grant_type") type: String = "refresh_token"
    ): Call<OAuth>

    @POST("token")
    @FormUrlEncoded
    fun login(
        @Field("client_id") idClient: String,
        @Field("code") authCode: String,
        @Field("code_verifier") codeVerifier: String = "",
        @Field("grant_type") authorization: String = "authorization_code"
    ): Call<OAuth>

    //Example 'https://api.myanimelist.net/v2/manga/2?fields=id,title,main_picture,alternative_titles,start_date,end_date,synopsis,mean,rank,popularity,num_list_users,num_scoring_users,nsfw,created_at,updated_at,media_type,status,genres,my_list_status,num_volumes,num_chapters,authors{first_name,last_name},pictures,background,related_anime,related_manga,recommendations,serialization{name}'
    @GET("manga/{manga_id}")
    fun getMangaDetail(
        @Header("Authorization") token: String?,
        @Header("X-MAL-CLIENT-ID") idClient: String?,
        @Path("manga_id") idManga: Long,
        @Query("fields", encoded = true) fields: String = FIELDS
    ): Call<MalMangaDetail>

    // Query set in url condition
    @GET("manga")
    fun getListManga(
        @Header("Authorization") token: String?,
        @Header("X-MAL-CLIENT-ID") idClient: String?,
        @Query("q") search: String,
        @Query("limit") limit: Int = 250,
        @Query("offset") offset: Int? = null,
        @Query("fields", encoded = true) fields: String = FIELDS
    ): Call<MalMangaList>

    @GET("users/{user_name}")
    fun getUserMangaList(
        @Header("Authorization") token: String?,
        @Header("X-MAL-CLIENT-ID") idClient: String?,
        @Path("user_name") userName: String,
        @Query("q") search: String,
        @Query("status") status: String = "",
        @Query("sort") sort: String = "",
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0,
        @Query("fields", encoded = true) fields: String = FIELDS,
        @Query("nsfw") nsfw: Boolean = false
    ): Call<MalMangaList>

    @POST("manga/{manga_id}/my_list_status")
    @FormUrlEncoded
    fun updateMangaList(
        @Header("Authorization") token: String,
        @Path("manga_id") idManga: Long,
        @Field("is_rereading") isRereading: Boolean = false,
        @Field("score") score: Int = 0,
        @Field("start_date", encoded = true) startDate: String = "",
        @Field("finish_date", encoded = true) finishDate: String = "",
        @Field("num_volumes_read") volumesRead: Int = 0,
        @Field("num_chapters_read") chaptersRead: Int = 0,
        @Field("priority") priority: Int = 0,
        @Field("num_times_reread") timesReread: Int = 0,
        @Field("reread_value") reread: Int = 0,
        @Field("tags") tags: String = "",
        @Field("comments") comments: String = ""
    ): Call<MalMangaList>

}
package br.com.fenix.bilingualmangareader.service.tracker

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClient private constructor() {
    companion object {
        private lateinit var INSTANCE: Retrofit
        private lateinit var INSTANCEOAUTH: Retrofit
        private const val BASE_OAUTH_Url = "https://myanimelist.net/v1/oauth2/"
        private const val BASE_API_URL = "https://api.myanimelist.net/v2/"
        private fun getRetrofitInstance(): Retrofit {
            if (!::INSTANCE.isInitialized) {
                synchronized(RetrofitClient::class) {
                    val httpClient = OkHttpClient.Builder()
                    INSTANCE = Retrofit.Builder()
                        .baseUrl(BASE_API_URL)
                        .client(httpClient.build())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                }
            }
            return INSTANCE
        }

        fun <T> getService(service: Class<T>): T {
            return getRetrofitInstance().create(service)
        }

        fun <T> getOAuth(service: Class<T>): T {
            if (!::INSTANCEOAUTH.isInitialized) {
                synchronized(RetrofitClient::class) {
                    val httpClient = OkHttpClient.Builder()
                    INSTANCEOAUTH = Retrofit.Builder()
                        .baseUrl(BASE_OAUTH_Url)
                        .client(httpClient.build())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                }
            }
            return INSTANCEOAUTH.create(service)
        }
    }

}
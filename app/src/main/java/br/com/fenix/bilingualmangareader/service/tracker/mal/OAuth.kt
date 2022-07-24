package br.com.fenix.bilingualmangareader.service.tracker.mal

import java.io.Serializable

data class OAuth(
    val refresh_token: String,
    val access_token: String,
    val token_type: String,
    val created_at: Long = System.currentTimeMillis(),
    val expires_in: Long,
) : Serializable {

    fun isExpired() =
        System.currentTimeMillis() > created_at + (expires_in * 1000)
}

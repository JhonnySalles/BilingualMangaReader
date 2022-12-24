package br.com.fenix.bilingualmangareader.service.tracker.mal

import android.content.Context
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.service.listener.ApiListener
import br.com.fenix.bilingualmangareader.service.tracker.RetrofitClient
import br.com.fenix.bilingualmangareader.util.secrets.PkceUtil
import br.com.fenix.bilingualmangareader.util.secrets.Secrets
import org.slf4j.LoggerFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyAnimeListTracker(var mContext: Context) {

    private val mLOGGER = LoggerFactory.getLogger(MyAnimeListTracker::class.java)

    private val mMyAnimeListAuth = RetrofitClient.getOAuth(MyAnimeListService::class.java)
    private val mMyAnimeList = RetrofitClient.getService(MyAnimeListService::class.java)
    private var idClient: String = Secrets.getSecrets(mContext).getMyAnimeListClientId()
    private var token: String? = null
    private var oAuth: OAuth? = null

    private fun getIdClient() =
        if (token != null) null else idClient

    private inline fun <T> validToken(authCode: String, listener: ApiListener<T>, crossinline function: () -> Unit) {
        if (token == null || oAuth == null || oAuth!!.isExpired()) {
            val call = if (token != null)
                mMyAnimeListAuth.refreshToken(Secrets.getSecrets(mContext).getMyAnimeListClientId(), authCode, token!!)
            else
                mMyAnimeListAuth.auth(Secrets.getSecrets(mContext).getMyAnimeListClientId(), PkceUtil.generateCodeVerifier())

            call.enqueue(object : Callback<OAuth> {
                override fun onResponse(call: Call<OAuth>, response: Response<OAuth>) {
                    if (response.code() == 200) {
                        oAuth = response.body()
                        token = response.body()?.access_token ?: ""
                        if (token != null && token!!.isNotEmpty())
                            function()
                    } else
                        listener.onFailure(response.raw().toString())
                }

                override fun onFailure(call: Call<OAuth>, t: Throwable) {
                    mLOGGER.error(t.message, t.stackTrace)
                    listener.onFailure(mContext.getString(R.string.api_error))
                }
            })
        } else
            function()
    }

    fun login(email: String, passWord: String, listener: ApiListener<OAuth>) {
        val call = mMyAnimeList.login(email, passWord)
        call.enqueue(object : Callback<OAuth> {
            override fun onResponse(call: Call<OAuth>, response: Response<OAuth>) {
                if (response.code() == 200)
                    response.body()?.let { listener.onSuccess(it) }
                else
                    listener.onFailure(response.raw().toString())
            }

            override fun onFailure(call: Call<OAuth>, t: Throwable) {
                mLOGGER.error(t.message, t.stackTrace)
                listener.onFailure(mContext.getString(R.string.api_error))
            }
        })
    }

    fun updateUserManga(search: String, listener: ApiListener<List<MalMangaDetail>>) {
        validToken("", listener) {

        }
    }

    fun getListManga(search: String, listener: ApiListener<List<MalMangaDetail>>) {
        val call = mMyAnimeList.getListManga(token, getIdClient(), search, limit = 20)
        call.enqueue(object : Callback<MalMangaList> {
            override fun onResponse(call: Call<MalMangaList>, response: Response<MalMangaList>) {
                if (response.code() == 200)
                    response.body()?.let { listener.onSuccess(MalTransform.getList(it.data)) }
                else
                    listener.onFailure(response.toString())
            }

            override fun onFailure(call: Call<MalMangaList>, t: Throwable) {
                mLOGGER.error(t.message, t.stackTrace)
                listener.onFailure(mContext.getString(R.string.api_error))
            }

        })
    }

    fun getListManga(search: String, listener: ApiListener<List<MalMangaDetail>>, vararg fields: String) {
        val call = mMyAnimeList.getListManga(token, getIdClient(), search, fields = fields.joinToString(","))
        call.enqueue(object : Callback<MalMangaList> {
            override fun onResponse(call: Call<MalMangaList>, response: Response<MalMangaList>) {
                if (response.code() == 200)
                    response.body()?.let {
                        listener.onSuccess(MalTransform.getList(it.data))
                    }
                else
                    listener.onFailure(response.raw().toString())
            }

            override fun onFailure(call: Call<MalMangaList>, t: Throwable) {
                mLOGGER.error(t.message, t.stackTrace)
                listener.onFailure(mContext.getString(R.string.api_error))
            }

        })

    }

}
package br.com.fenix.bilingualmangareader.util.secrets

import android.content.Context
import android.content.res.AssetManager
import br.com.fenix.bilingualmangareader.view.ui.manga_detail.MangaDetailFragment
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.InputStream
import java.util.*


class Secrets {

    private val mLOGGER = LoggerFactory.getLogger(MangaDetailFragment::class.java)

    companion object Instance {
        private lateinit var mAssets: AssetManager
        private lateinit var INSTANCE: Secrets
        fun getSecrets(context: Context): Secrets {
            if (!::INSTANCE.isInitialized) {
                mAssets = context.assets
                INSTANCE = Secrets()
            }

            return INSTANCE
        }
    }

    private var ANIME_LIST_CLIENT_ID: String = ""

    init {
        try {
            val properties = Properties()
            val assetManager: AssetManager = mAssets
            val inputStream: InputStream = assetManager.open("secrets.properties")
            properties.load(inputStream)

            ANIME_LIST_CLIENT_ID = properties.getProperty("ANIME_LIST_CLIENT_ID")
        } catch (e: IOException) {
            e.printStackTrace()
            mLOGGER.error("Error to read secrets", e)
        }
    }

    fun getAnimeListClientId(): String {
        return ANIME_LIST_CLIENT_ID
    }

}
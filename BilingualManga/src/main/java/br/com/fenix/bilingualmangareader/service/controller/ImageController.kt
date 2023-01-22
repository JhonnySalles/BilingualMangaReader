package br.com.fenix.bilingualmangareader.service.controller

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import android.widget.ImageView
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.util.helpers.Util
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.net.URL

class ImageController private constructor() {

    companion object {
        val instance: ImageController by lazy { HOLDER.INSTANCE }
    }

    private val mLOGGER = LoggerFactory.getLogger(ImageController::class.java)

    private object HOLDER {
        val INSTANCE = ImageController()
    }

    private fun saveBitmapToCache(context: Context, key: String, bitmap: Bitmap) {
        try {
            val cacheDir = File(GeneralConsts.getCacheDir(context), GeneralConsts.CACHE_FOLDER.IMAGE)
            if (!cacheDir.exists())
                cacheDir.mkdir()

            val byte = Util.imageToByteArray(bitmap) ?: return
            val image = File(cacheDir.path + '/' + key)
            image.writeBytes(byte)
        } catch (e: Exception) {
            mLOGGER.error("Error save bitmap to cache: " + e.message, e)
        }
    }

    private fun getBitmapFromCache(context: Context, key: String): Bitmap? {
        try {
            val file = File(GeneralConsts.getCacheDir(context), GeneralConsts.CACHE_FOLDER.IMAGE + '/' + key)

            if (file.exists())
                return BitmapFactory.decodeFile(file.absolutePath)

        } catch (e: Exception) {
            mLOGGER.error("Error retrieve bitmap from cache: " + e.message, e)
        }
        return null
    }

    private fun generateHash(link: String): String =
        Util.MD5(link)

    private fun getImage(context: Context, link: String): Bitmap? {
        val hash = generateHash(link)
        var image: Bitmap? = getBitmapFromCache(context, hash)

        if (image == null) {
            try {
                val stream = URL(link).openStream()
                image = BitmapFactory.decodeStream(stream)
                if (image != null)
                    saveBitmapToCache(context, hash, image)
            } catch (e: IOException) {
                e.printStackTrace()
                mLOGGER.error("Error retrieve bitmap from link: " + e.message, e)
            }
        }
        return image
    }

    fun setImageAsync(
        context: Context,
        link: String,
        imageView: ImageView
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var image: Bitmap? = null
                val deferred = async {
                    image = getImage(context, link)
                }
                deferred.await()
                withContext(Dispatchers.Main) {
                    if (image != null) {
                        imageView.setImageBitmap(image)
                        imageView.visibility = View.VISIBLE
                    }
                }
            } catch (m: OutOfMemoryError) {
                System.gc()
                mLOGGER.error("Memory full, cleaning", m)
            } catch (e: Exception) {
                mLOGGER.error("Error to get image async", e)
            }
        }
    }
}
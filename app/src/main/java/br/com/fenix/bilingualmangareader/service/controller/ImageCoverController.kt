package br.com.fenix.bilingualmangareader.service.controller

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import android.widget.ImageView
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.service.parses.Parse
import br.com.fenix.bilingualmangareader.service.parses.ParseFactory
import br.com.fenix.bilingualmangareader.service.parses.RarParse
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.util.constants.ReaderConsts
import br.com.fenix.bilingualmangareader.util.helpers.Util
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStream


class ImageCoverController private constructor() {

    companion object {
        val instance: ImageCoverController by lazy { HOLDER.INSTANCE }
    }

    private val mLOGGER = LoggerFactory.getLogger(ImageCoverController::class.java)

    private object HOLDER {
        val INSTANCE = ImageCoverController()
    }

    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 4
    private val lru = object : LruCache<String, Bitmap> (cacheSize)  {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    private fun saveBitmapToLru(key: String, bitmap: Bitmap) {
        try {
            synchronized (instance.lru) {
                if (instance.lru.get(key) == null)
                    instance.lru.put(key, bitmap)
            }
        } catch (e: Exception) {
            mLOGGER.warn("Error save image on LruCache: " + e.message, e)
        }
    }

    private fun retrieveBitmapFromLru(key: String): Bitmap? {
        try {
            return instance.lru.get(key)
        } catch (e: Exception) {
            mLOGGER.warn("Error retrieve image from LruCache: " + e.message, e)
        }
        return null
    }

    private fun saveBitmapToCache(context: Context, key: String, bitmap: Bitmap) {
        try {
            saveBitmapToLru(key, bitmap)
            val cacheDir = File(GeneralConsts.getCacheDir(context), GeneralConsts.CACHE_FOLDER.COVERS)
            if (!cacheDir.exists())
                cacheDir.mkdir()

            val byte = Util.imageToByteArray(bitmap) ?: return
            val image = File(cacheDir.path + '/' + key)
            image.writeBytes(byte)
        } catch (e: Exception) {
            mLOGGER.error("Error save bitmap to cache: " + e.message, e)
        }
    }

    private fun retrieveBitmapFromCache(context: Context, key: String): Bitmap? {
        try {
            var image = retrieveBitmapFromLru(key)
            if (image != null) return image

            val file = File(GeneralConsts.getCacheDir(context), GeneralConsts.CACHE_FOLDER.COVERS + '/' + key)

            if (file.exists()) {
                image = BitmapFactory.decodeFile(file.absolutePath)
                saveBitmapToLru(key, image)
                return image
            }
        } catch (e: Exception) {
            mLOGGER.error("Error retrieve bitmap from cache: " + e.message, e)
        }
        return null
    }

    fun getCoverFromFile(context: Context, file: File, parse: Parse): Bitmap? {
        return getCoverFromFile(context, generateHash(file), parse)
    }

    private fun generateHash(file: File): String =
        Util.MD5(file.path + file.name)

    private fun getCoverFromFile(context: Context, hash: String, parse: Parse): Bitmap? {
        var index = 0
        for (i in 0..parse.numPages()) {
            if (Util.isImage(parse.getPagePath(i)!!)) {
                index = i
                break
            }
        }
        var stream: InputStream? = parse.getPage(index)

        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeStream(stream, null, options)
        options.inSampleSize = Util.calculateInSampleSize(
            options,
            ReaderConsts.COVER.COVER_THUMBNAIL_WIDTH,
            ReaderConsts.COVER.COVER_THUMBNAIL_HEIGHT
        )

        options.inJustDecodeBounds = false
        Util.closeInputStream(stream)
        stream = parse.getPage(index)
        val result = BitmapFactory.decodeStream(stream, null, options)

        return if (result != null) {
            saveBitmapToCache(context, hash, result)
            Util.closeInputStream(stream)
            result
        } else
            null
    }

    private fun getMangaCover(context: Context, manga: Manga): Bitmap? {
        val hash = generateHash(manga.file)
        var image = retrieveBitmapFromCache(context, hash)
        if (image == null) {
            val parse = ParseFactory.create(manga.file) ?: return image
            try {
                if (parse is RarParse) {
                    val folder = GeneralConsts.CACHE_FOLDER.RAR + '/' + Util.normalizeNameCache(manga.file.nameWithoutExtension)
                    val cacheDir = File(GeneralConsts.getCacheDir(context), folder)
                    (parse as RarParse?)!!.setCacheDirectory(cacheDir)
                }

                image = getCoverFromFile(context, hash, parse)
            } finally {
                Util.destroyParse(parse)
            }
        }

        return image
    }

    fun setImageCoverAsync(
        context: Context,
        manga: Manga,
        imageView: ImageView
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var image: Bitmap? = null
                val deferred = async {
                    image = getMangaCover(context, manga)
                }
                deferred.await()
                withContext(Dispatchers.Main) {
                    if (image != null)
                        imageView.setImageBitmap(image)
                }
            } catch (e: Exception) {
                mLOGGER.error("Error to load image async", e)
            } catch (m: OutOfMemoryError) {
                System.gc()
                mLOGGER.error("Memory full, cleaning", m)
            }
        }

    }

}
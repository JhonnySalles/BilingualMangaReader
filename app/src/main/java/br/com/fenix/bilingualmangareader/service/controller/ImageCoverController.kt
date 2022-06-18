package br.com.fenix.bilingualmangareader.service.controller

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import android.widget.ImageView
import br.com.fenix.bilingualmangareader.MainActivity
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

    var cacheSize = 12 * 1024 * 1024 // 12Mb
    private val lru: LruCache<String, Bitmap> = LruCache(cacheSize)

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

    private fun saveBitmapToCache(key: String, bitmap: Bitmap) {
        try {
            saveBitmapToLru(key, bitmap)
            val cacheDir = File(MainActivity.getAppContext().externalCacheDir, GeneralConsts.CACHEFOLDER.COVERS)
            if (!cacheDir.exists())
                cacheDir.mkdir()

            val byte = Util.imageToByteArray(bitmap) ?: return
            val image = File(cacheDir.path + '/' + key)
            image.writeBytes(byte)
        } catch (e: Exception) {
            mLOGGER.error("Error save bitmap to cache: " + e.message, e)
        }
    }

    private fun retrieveBitmapFromCache(key: String): Bitmap? {
        try {
            var image = retrieveBitmapFromLru(key)
            if (image != null) return image

            val file = File(MainActivity.getAppContext().externalCacheDir,GeneralConsts.CACHEFOLDER.COVERS + '/' + key)

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

    fun getCoverFromFile(file: File, parse: Parse): Bitmap? {
        return getCoverFromFile(generateHash(file), parse)
    }

    private fun generateHash(file: File): String =
        Util.MD5(file.path + file.name)

    private fun getCoverFromFile(hash: String, parse: Parse): Bitmap? {
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
            saveBitmapToCache(hash, result)
            Util.closeInputStream(stream)
            result
        } else
            null
    }

    private fun getMangaCover(manga: Manga): Bitmap? {
        val hash = generateHash(manga.file)
        var image = retrieveBitmapFromCache(hash)
        if (image == null) {
            val parse = ParseFactory.create(manga.file) ?: return image
            try {
                if (parse is RarParse) {
                    val folder = GeneralConsts.CACHEFOLDER.RAR + '/' + Util.normalizeNameCache(manga.file.nameWithoutExtension)
                    val cacheDir = File(MainActivity.getAppContext().externalCacheDir, folder)
                    (parse as RarParse?)!!.setCacheDirectory(cacheDir)
                }

                image = getCoverFromFile(hash, parse)
            } finally {
                Util.destroyParse(parse)
            }
        }

        return image
    }

    fun setImageCoverAsync(
        manga: Manga,
        imageView: ImageView
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var image: Bitmap? = null
                val deferred = async {
                    image = getMangaCover(manga)
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
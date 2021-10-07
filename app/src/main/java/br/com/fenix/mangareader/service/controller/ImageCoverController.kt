package br.com.fenix.mangareader.service.controller

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.collection.LruCache
import br.com.fenix.mangareader.model.entity.Cover
import br.com.fenix.mangareader.model.entity.Manga
import br.com.fenix.mangareader.service.parses.Parse
import br.com.fenix.mangareader.service.parses.ParseFactory
import br.com.fenix.mangareader.service.repository.CoverRepository
import br.com.fenix.mangareader.util.constants.ReaderConsts
import br.com.fenix.mangareader.util.helpers.Util
import kotlinx.coroutines.*
import java.io.InputStream


class ImageCoverController private constructor() {
    private object HOLDER {
        val INSTANCE = ImageCoverController()
    }

    companion object {
        val instance: ImageCoverController by lazy { HOLDER.INSTANCE }
    }

    private val lru: LruCache<Any, Any> = LruCache(1024)

    private fun saveBitmapToCache(key: String, bitmap: Bitmap) {
        try {
            instance.lru.put(key, bitmap)
        } catch (e: Exception) {
        }
    }

    private fun retrieveBitmapFromCache(key: String): Bitmap? {
        try {
            return instance.lru.get(key) as Bitmap?
        } catch (e: Exception) {
        }

        return null
    }

    private lateinit var mRepository: CoverRepository
    private var mParse: Parse? = null
    private fun getImage(manga: Manga) {
        val hash = Util.MD5(manga.file!!.path + manga.file!!.name)
        var image = retrieveBitmapFromCache(hash)
        if (image != null) {
            manga.thumbnail = Cover(
                null, manga.id!!, hash, ReaderConsts.COVER.COVER_THUMBNAIL_WIDTH,
                manga.type, image
            )
        } else {
            if (manga.id != null) {
                manga.thumbnail = mRepository.findFirstByIdManga(manga.id!!)
                if (manga.thumbnail?.image != null)
                    image = manga.thumbnail?.image
            }

            if (image == null) {
                mParse = ParseFactory.create(manga.file!!)
                var stream: InputStream? = mParse!!.getPage(0)

                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeStream(stream, null, options)
                options.inSampleSize = Util.calculateInSampleSize(
                    options,
                    ReaderConsts.COVER.COVER_THUMBNAIL_WIDTH,
                    ReaderConsts.COVER.COVER_THUMBNAIL_HEIGHT
                )

                options.inJustDecodeBounds = false
                stream?.close()
                stream = mParse!!.getPage(0)
                val result = BitmapFactory.decodeStream(stream, null, options)

                if (result != null) {
                    saveBitmapToCache(hash, result)
                    manga.thumbnail = Cover(
                        null, manga.id!!, hash, ReaderConsts.COVER.COVER_THUMBNAIL_WIDTH,
                        manga.type, result
                    )
                    mRepository.save(manga.thumbnail!!)
                }
            }
        }
    }

    private fun processList(list: List<Manga>,): List<Manga> {
        for (manga in list)
            if (manga.thumbnail == null || manga.thumbnail!!.image == null)
                getImage(manga)
        return list
    }

    fun setImageCoverAsync(
        context: Context,
        list: List<Manga>,
        updateList: (List<Manga>) -> (Unit)
    ) {
        if (!::mRepository.isInitialized)
            mRepository = CoverRepository(context)

        CoroutineScope(Dispatchers.IO).launch {
            val deferred = async { processList(list) }
            val withCovers = deferred.await()
            withContext(Dispatchers.Main) {
                updateList(withCovers)
            }
        }
    }

}
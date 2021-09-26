package br.com.fenix.mangareader.service.controller

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import androidx.collection.LruCache
import br.com.fenix.mangareader.model.entity.Cover
import br.com.fenix.mangareader.model.entity.Manga
import br.com.fenix.mangareader.service.parses.Parse
import br.com.fenix.mangareader.service.parses.ParseFactory
import br.com.fenix.mangareader.service.repository.CoverRepository
import br.com.fenix.mangareader.util.constants.ReaderConsts
import br.com.fenix.mangareader.util.helpers.Util
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.InputStream


class ImageCoverController private constructor() {
    private object HOLDER {
        val INSTANCE = ImageCoverController()
    }

    companion object {
        val instance: ImageCoverController by lazy { HOLDER.INSTANCE }
    }

    private val lru: LruCache<Any, Any> = LruCache(1024)

    private fun saveBitmapToCahche(key: String, bitmap: Bitmap) {
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
    private fun getImage(view: ImageView, manga: Manga) =
        runBlocking { // this: CoroutineScope
            launch {
                val hash = Util.MD5(manga.file!!.name)
                var image = retrieveBitmapFromCache(hash)

                mRepository = CoverRepository(view.context)
                if (manga.id != null) {
                    val cover = mRepository.findFirstByIdManga(manga.id!!)
                    if (cover?.image != null)
                        image = cover.image
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
                        saveBitmapToCahche(hash, result)
                        manga.thumbnail = Cover(
                            null, manga.id!!, hash, ReaderConsts.COVER.COVER_THUMBNAIL_WIDTH,
                            manga.type, result
                        )
                        mRepository.save(manga.thumbnail!!)
                    }
                }

                if (image != null)
                    view.setImageBitmap(image)
            }
        }

    fun setImageCover(view: ImageView, manga: Manga) {
        if (manga.thumbnail != null && manga.thumbnail!!.image != null)
            view.setImageBitmap(manga.thumbnail!!.image)
        else
            getImage(view, manga)

    }

}
package br.com.fenix.bilingualmangareader.service.controller

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import androidx.collection.LruCache
import br.com.fenix.bilingualmangareader.model.entity.Cover
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.service.parses.Parse
import br.com.fenix.bilingualmangareader.service.parses.ParseFactory
import br.com.fenix.bilingualmangareader.service.repository.CoverRepository
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.util.constants.ReaderConsts
import br.com.fenix.bilingualmangareader.util.helpers.Util
import kotlinx.coroutines.*
import java.io.File
import java.io.InputStream
import java.util.ArrayList


class ImageCoverController private constructor() {

    private var mUpdateHandler: MutableList<Handler>? = ArrayList()


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

    fun addUpdateHandler(handler: Handler) {
        mUpdateHandler!!.add(handler)
    }

    fun removeUpdateHandler(handler: Handler) {
        mUpdateHandler!!.remove(handler)
    }

    private fun notifyCoverUpdateFinished(position: Int?) {
        for (h in mUpdateHandler!!)
            if (position != null) {
                val msg = Message()
                msg.what = GeneralConsts.SCANNER.MESSAGE_COVER_UPDATE_FINISHED
                msg.data = Bundle()
                msg.data.putInt("position",position)
                h.sendMessage(msg)
            } else
                h.sendEmptyMessage(GeneralConsts.SCANNER.MESSAGE_COVER_UPDATE_FINISHED)
    }

    fun getCoverFromFile(file: File, parse: Parse): Cover? {
        return getCoverFromFile(generateHash(file), parse)
    }

    private fun generateHash(file: File): String =
        Util.MD5(file.path + file.name)

    private fun getCoverFromFile(hash: String, parse: Parse): Cover? {
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
        stream?.close()
        stream = parse.getPage(index)
        val result = BitmapFactory.decodeStream(stream, null, options)

        return if (result != null) {
            saveBitmapToCache(hash, result)
            Cover(
                null, 0, hash, ReaderConsts.COVER.COVER_THUMBNAIL_WIDTH,
                parse.getType()!!, result
            )
        } else
            null
    }

    private lateinit var mRepository: CoverRepository
    private fun getMangaCover(manga: Manga) {
        val hash = generateHash(manga.file!!)
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
                val parse = ParseFactory.create(manga.file!!) ?: return
                val cover = getCoverFromFile(hash, parse)
                if (cover != null) {
                    cover.id_manga = manga.id!!
                    manga.thumbnail = cover
                    mRepository.save(manga.thumbnail!!)
                }
            }
        }
    }

    var mErrors: Int = 0
    fun setImageCoverAsync(
        context: Context,
        manga: Manga,
        position : Int? = null
    ) {
        if (!::mRepository.isInitialized)
            mRepository = CoverRepository(context)

        try {
            CoroutineScope(Dispatchers.IO).launch {
                val deferred = async { getMangaCover(manga) }
                deferred.await()
                withContext(Dispatchers.Main) {
                    notifyCoverUpdateFinished(position)
                }
                mErrors = 0
            }
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, "Erro ao processar lista " + e.message)
            mErrors++
            if (mErrors < 3)
                setImageCoverAsync(context, manga, position)
        }
    }

}
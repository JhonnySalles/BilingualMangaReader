package br.com.fenix.mangareader

import android.graphics.Bitmap
import androidx.collection.LruCache
import java.io.File


class ImageCover private constructor() {
    private object HOLDER {
        val INSTANCE = ImageCover()
    }

    companion object {
        val instance: ImageCover by lazy { HOLDER.INSTANCE }
    }
    val lru: LruCache<Any, Any>

    init {
        lru = LruCache(1024)
    }

    fun saveBitmapToCahche(key: String, bitmap: Bitmap) {
        try {
            ImageCover.instance.lru.put(key, bitmap)
        } catch (e: Exception) {
        }
    }

    fun retrieveBitmapFromCache(key: String): Bitmap? {

        try {
            return ImageCover.instance.lru.get(key) as Bitmap?
        } catch (e: Exception) {
        }

        return null
    }

    fun getImage(file : File): Bitmap? {
        var image = retrieveBitmapFromCache(file.name)

        if (image == null) {

          //  val coverImage = null
            //if (file.extension.endsWith(".rar")) {
                //val conteudo : List<ContentDescription> = Junrar.getContentsDescription(file)

                //Junrar.extractFile(conteudo[0], coverImage)
            //}


            //saveBitmapToCahche(file.name, bitmap: Bitmap)
        }

        return image;
    }

}
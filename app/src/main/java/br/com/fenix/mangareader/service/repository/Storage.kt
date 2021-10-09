package br.com.fenix.mangareader.service.repository

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import br.com.fenix.mangareader.model.entity.Manga

class Storage(context: Context) {
    private val mRepository = MangaRepository(context)
    private val mRepositoryCover = CoverRepository(context)

    fun getPrevManga(manga: Manga): Manga? {
        val mangas: List<Manga>? = mRepository.findByFileFolder(manga.file!!.parent)
        val idx = mangas!!.indexOf(manga)
        return if (idx > 0) mangas[idx - 1] else null
    }

    fun getNextManga(manga: Manga): Manga? {
        val mangas: List<Manga>? = mRepository.findByFileFolder(manga.file!!.parent)
        val idx = mangas!!.indexOf(manga)
        return if (idx != mangas.size - 1) mangas[idx + 1] else null
    }

    fun get(idManga: Long): Manga? =
        mRepository.get(idManga)

    fun findByName(name: String): Manga? =
        mRepository.findByFileName(name)

    fun listBook(): List<Manga>? =
        mRepository.list()

    fun delete(manga: Manga) {
        mRepositoryCover.deleteAll(manga.id!!)
        mRepository.delete(manga)
    }

    fun updateBookMark(manga: Manga) {
        mRepository.updateBookMark(manga)
    }

    fun save(manga: Manga): Long {
        val id = mRepository.save(manga)
        if (manga.thumbnail != null) {
            manga.thumbnail!!.id_manga = id
            mRepositoryCover.save(manga.thumbnail!!)
        }
        return id
    }

    // Used to get the cache images
    companion object Storage {
        private val EXTERNAL_PERMS = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
        )

        fun isPermissionGranted(context: Context): Boolean {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R)
            // Valide permission on android 10 or above
                return Environment.isExternalStorageManager()
            else {
                val readExternalStoragePermission: Int = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                return readExternalStoragePermission == PackageManager.PERMISSION_GRANTED
            }
        }

        fun takePermission(context: Context, activity: Activity) =
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R) {
                try {
                    val intent: Intent =
                        Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.addCategory("android.intent.category.DEFAULT")
                    intent.data = Uri.parse(
                        String.format(
                            "package:%s",
                            context.packageName
                        )
                    )
                    activity.startActivity(intent)
                } catch (ex: Exception) {
                    val intent: Intent = Intent()
                    intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                    activity.startActivity(intent)
                }
            } else {
                ActivityCompat.requestPermissions(
                    activity,
                    EXTERNAL_PERMS,
                    101
                )
            }
    }
}
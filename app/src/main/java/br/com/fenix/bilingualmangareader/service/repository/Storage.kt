package br.com.fenix.bilingualmangareader.service.repository

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
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import java.util.*

class Storage(context: Context) {

    private val mRepository = MangaRepository(context)

    fun getPrevManga(manga: Manga): Manga? {
        val mangas: List<Manga>? = mRepository.findByFileFolder(manga.file.parent ?: "")
        val idx = mangas!!.indexOf(manga)
        return if (idx > 0) mangas[idx - 1] else null
    }

    fun getNextManga(manga: Manga): Manga? {
        val mangas: List<Manga>? = mRepository.findByFileFolder(manga.file.parent ?: "")
        val idx = mangas!!.indexOf(manga)
        return if (idx != mangas.size - 1) mangas[idx + 1] else null
    }

    fun get(idManga: Long): Manga? =
        mRepository.get(idManga)

    fun findByName(name: String): Manga? =
        mRepository.findByFileName(name)

    fun listMangas(): List<Manga>? = mRepository.list()

    fun listDeleted(): List<Manga>? = mRepository.listDeleted()

    fun delete(manga: Manga) = mRepository.delete(manga)

    fun updateBookMark(manga: Manga) {
        mRepository.updateBookMark(manga)
    }

    fun save(manga: Manga): Long {
        return if (manga.id != null) {
            mRepository.update(manga)
            manga.id!!
        } else
            mRepository.save(manga)
    }

    // Used to get the cache images
    companion object Storage {
        private val EXTERNAL_PERMS = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
        )

        fun isPermissionGranted(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            // Valid permission on android 10 or above
                Environment.isExternalStorageManager()
            else {
                val readExternalStoragePermission: Int = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                readExternalStoragePermission == PackageManager.PERMISSION_GRANTED
            }
        }

        fun takePermission(context: Context, activity: Activity) =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) try {
                val intent =
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
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                activity.startActivity(intent)
            } else {
                ActivityCompat.requestPermissions(
                    activity,
                    EXTERNAL_PERMS,
                    GeneralConsts.REQUEST.PERMISSION_FILES_ACCESS
                )
            }
    }

    fun updateLastAccess(manga: Manga) {
        manga.lastAccess = Date()
        mRepository.updateLastAcess(manga)
    }
}
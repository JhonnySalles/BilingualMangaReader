package br.com.fenix.mangareader.service.repository

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import br.com.fenix.mangareader.model.entity.Book
import java.util.ArrayList

class Storage(context: Context) {
    private val mRepository = BookRepository(context)

    fun getPrevBook(book: Book): Book? {
        val books: List<Book>? = mRepository.findByFileFolder(book.file!!.parent)
        val idx = books!!.indexOf(book)
        return if (idx > 0) books[idx - 1] else null
    }

    fun getNextBook(book: Book): Book? {
        val books: List<Book>? = mRepository.findByFileFolder(book.file!!.parent)
        val idx = books!!.indexOf(book)
        return if (idx != books.size - 1) books[idx + 1] else null
    }

    fun get(idBook: Long): Book? = mRepository.get(idBook)

    fun findByName(name: String): Book? = mRepository.findByFileName(name)

    fun listBook(withCover: Boolean) : List<Book>? = mRepository.list(withCover)

    fun delete(book: Book) {
        mRepository.delete(book)
    }

    fun updateBookMark(book: Book) {
        mRepository.updateBookMark(book)
    }

    fun save(book: Book) : Long = mRepository.save(book)

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
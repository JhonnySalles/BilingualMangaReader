package br.com.fenix.mangareader.repository

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

class Storage {
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
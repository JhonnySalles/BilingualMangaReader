package br.com.fenix.bilingualmangareader.util.helpers

import android.content.Context
import java.io.File
import java.io.InputStream

class FileUtil(val context: Context) {

    /**
     * Copies an asset file from assets to phone internal storage, if it doesn't already exist
     * Will be copied to path <prefix> + <assetName> in files directory
     * Returns true if copied, false otherwise (including if file already exists)
     */
    fun copyAssetToFilesIfNotExist(prefix: String, assetName: String): Boolean {
        val file = File(context.filesDir.absolutePath, prefix + assetName)
        if (file.exists())
            return false

        val inputStream: InputStream = context.assets.open(assetName)
        File(context.filesDir.absolutePath, prefix).mkdirs()
        // Copy in 10mb chunks to avoid going oom for larger files
        inputStream.copyTo(file.outputStream(), 10000)
        inputStream.close()
        return true
    }
}
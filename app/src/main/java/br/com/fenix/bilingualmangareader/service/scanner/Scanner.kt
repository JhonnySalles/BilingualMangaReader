package br.com.fenix.bilingualmangareader.service.scanner

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Message
import android.os.Process
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.service.controller.ImageCoverController
import br.com.fenix.bilingualmangareader.service.parses.Parse
import br.com.fenix.bilingualmangareader.service.parses.ParseFactory
import br.com.fenix.bilingualmangareader.service.parses.RarParse
import br.com.fenix.bilingualmangareader.service.repository.Storage
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.util.helpers.Util
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.ref.WeakReference

class Scanner(private val context: Context) {

    private val mLOGGER = LoggerFactory.getLogger(Scanner::class.java)

    private var mUpdateHandler: MutableList<Handler> = ArrayList()
    private var mUpdateThread: Thread? = null

    private var mIsStopped = false
    private var mIsRestarted = false

    private val mRestartHandler: Handler = RestartHandler(this)

    private inner class RestartHandler(scanner: Scanner) :
        Handler() {
        private val mScannerRef: WeakReference<Scanner> = WeakReference<Scanner>(scanner)
        override fun handleMessage(msg: Message) {
            mScannerRef.get()?.scanLibrary()
        }
    }

    // Singleton - One thread initialize only
    companion object {
        private lateinit var INSTANCE: Scanner
        fun getInstance(context: Context): Scanner {
            if (!::INSTANCE.isInitialized)
                synchronized(Scanner::class.java) { // Used for a two or many cores
                    INSTANCE = Scanner(context)
                }
            return INSTANCE
        }
    }

    fun isRunning(): Boolean {
        return mUpdateThread != null &&
                mUpdateThread!!.isAlive && mUpdateThread!!.state != Thread.State.TERMINATED && mUpdateThread!!.state != Thread.State.NEW
    }

    fun forceScanLibrary() {
        if (isRunning()) {
            mIsStopped = true
            mIsRestarted = true
        } else
            scanLibrary()
    }

    fun scanLibrary() {
        if (mUpdateThread == null || mUpdateThread!!.state == Thread.State.TERMINATED) {
            val runnable = LibraryUpdateRunnable()
            mUpdateThread = Thread(runnable)
            mUpdateThread!!.priority =
                Process.THREAD_PRIORITY_DEFAULT + Process.THREAD_PRIORITY_LESS_FAVORABLE
            mUpdateThread!!.start()
        }
    }

    fun addUpdateHandler(handler: Handler) {
        if (mUpdateHandler.contains(handler))
            removeUpdateHandler(handler)

        mUpdateHandler.add(handler)
    }

    fun removeUpdateHandler(handler: Handler) {
        mUpdateHandler.remove(handler)
    }

    private fun notifyMediaUpdatedAdd(manga: Manga) {
        val msg = Message()
        msg.obj = manga
        msg.what = GeneralConsts.SCANNER.MESSAGE_MANGA_UPDATED_ADD
        notifyHandlers(msg)
    }

    private fun notifyMediaUpdatedRemove(manga: Manga) {
        val msg = Message()
        msg.obj = manga
        msg.what = GeneralConsts.SCANNER.MESSAGE_MANGA_UPDATED_REMOVE
        notifyHandlers(msg)
    }

    private fun notifyLibraryUpdateFinished(isProcessed: Boolean) {
        val msg = Message()
        msg.obj = isProcessed
        msg.what = GeneralConsts.SCANNER.MESSAGE_MANGA_UPDATE_FINISHED
        notifyHandlers(msg, 200)
    }

    private fun notifyHandlers(msg: Message, delay: Int = -1) {
        for (h in mUpdateHandler) {
            try {
                if (h.hasMessages(msg.what, msg.obj))
                    h.removeMessages(msg.what, msg.obj)

                if (delay > -1)
                    h.sendMessageDelayed(msg, 200)
                else
                    h.sendMessage(msg)

            } catch (e: Exception) {
                mLOGGER.error("Error when notify handlers", e)
            }
        }
    }

    private fun generateCover(parse: Parse, manga: Manga) =
        ImageCoverController.instance.getCoverFromFile(context, manga.file, parse)

    private inner class LibraryUpdateRunnable : Runnable {
        override fun run() {
            var isProcess = false
            try {
                val preference: SharedPreferences = GeneralConsts.getSharedPreferences(context)
                val libraryPath = preference.getString(GeneralConsts.KEYS.LIBRARY.FOLDER, "")

                if (libraryPath == "" || !File(libraryPath).exists()) return

                val storage = Storage(context)
                val storageFiles: MutableMap<String, Manga> = HashMap()
                val storageDeletes: MutableMap<String, Manga> = HashMap()

                // create list of files available in storage
                for (c in storage.listMangas()!!)
                    storageFiles[c.title] = c

                for (c in storage.listDeleted()!!)
                    storageDeletes[c.title] = c

                var walked = false
                // search and add comics if necessary
                val file = File(libraryPath)
                file.walk().onFail { _, ioException -> mLOGGER.warn("File walk error", ioException) }
                    .filterNot { it.isDirectory }.forEach {
                        walked = true
                        if (mIsStopped) return
                        if (it.name.endsWith(".rar") ||
                            it.name.endsWith(".zip") ||
                            it.name.endsWith(".cbr") ||
                            it.name.endsWith(".cbz")
                        ) {
                            if (storageFiles.containsKey(it.name))
                                storageFiles.remove(it.name)
                            else {
                                isProcess = true
                                val parse: Parse? = ParseFactory.create(it)
                                try {
                                    if (parse is RarParse) {
                                        val cacheDir = File(GeneralConsts.getCacheDir(context), GeneralConsts.CACHE_FOLDER.RAR)
                                        (parse as RarParse?)!!.setCacheDirectory(cacheDir)
                                    }

                                    if (parse != null)
                                        if (parse.numPages() > 0) {
                                            val manga = if (storageDeletes.containsKey(it.name)) {
                                                storageFiles.remove(it.name)
                                                storageDeletes.getValue(it.name)
                                            } else Manga(
                                                null,
                                                it.name,
                                                "",
                                                it.path,
                                                it.parent,
                                                it.nameWithoutExtension,
                                                it.extension,
                                                parse.numPages()
                                            )

                                            manga.excluded = false
                                            generateCover(parse, manga)
                                            manga.id = storage.save(manga)
                                            notifyMediaUpdatedAdd(manga)
                                        }
                                } finally {
                                    Util.destroyParse(parse)
                                }
                            }
                        }
                    }

                // delete missing comics
                if (!mIsStopped && !mIsRestarted && walked)
                    for (missing in storageFiles.values) {
                        isProcess = true
                        storage.delete(missing)
                        notifyMediaUpdatedRemove(missing)
                    }
            } finally {
                mIsStopped = false
                if (mIsRestarted) {
                    mIsRestarted = false
                    mRestartHandler.sendEmptyMessageDelayed(1, 200)
                } else
                    notifyLibraryUpdateFinished(isProcess)
            }
        }
    }
}
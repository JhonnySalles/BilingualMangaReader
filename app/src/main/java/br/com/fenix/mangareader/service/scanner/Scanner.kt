package br.com.fenix.mangareader.service.scanner

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Message
import android.os.Process
import br.com.fenix.mangareader.MainActivity
import br.com.fenix.mangareader.model.entity.Manga
import br.com.fenix.mangareader.service.controller.ImageCoverController
import br.com.fenix.mangareader.service.parses.Parse
import br.com.fenix.mangareader.service.parses.ParseFactory
import br.com.fenix.mangareader.service.repository.CoverRepository
import br.com.fenix.mangareader.service.repository.Storage
import br.com.fenix.mangareader.util.constants.GeneralConsts
import kotlinx.coroutines.*
import java.io.File
import java.lang.Runnable
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.HashMap

class Scanner {

    private var mUpdateHandler: MutableList<Handler>? = ArrayList()
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
        fun getInstance(): Scanner {
            if (!::INSTANCE.isInitialized)
                synchronized(Scanner::class.java) { // Used for a two or many cores
                    INSTANCE = Scanner()
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
        mUpdateHandler!!.add(handler)
    }

    fun removeUpdateHandler(handler: Handler) {
        mUpdateHandler!!.remove(handler)
    }

    private fun notifyMediaUpdated() {
        for (h in mUpdateHandler!!)
            h.sendEmptyMessage(GeneralConsts.SCANNER.MESSAGE_MEDIA_UPDATED)
    }

    private fun notifyCoverUpdated() {
        for (h in mUpdateHandler!!)
            h.sendEmptyMessage(GeneralConsts.SCANNER.MESSAGE_COVER_UPDATED)
    }

    private fun notifyLibraryUpdateFinished() {
        for (h in mUpdateHandler!!)
            h.sendEmptyMessage(GeneralConsts.SCANNER.MESSAGE_MEDIA_UPDATE_FINISHED)
    }

    private fun generateCovers(list: MutableList<Manga>) {
        val storage = Storage(MainActivity.getAppContext())
        CoroutineScope(Dispatchers.IO).launch {
            async {
                var index = 0
                for (manga in list) {
                    index++
                    if (index > 20) {
                        index = 0
                        notifyCoverUpdated()
                    }

                    val parse = ParseFactory.create(manga.file!!) ?: continue
                    val cover = ImageCoverController.instance.getCoverFromFile(manga.file!!, parse) ?: continue
                    cover.id_manga = manga.id!!
                    manga.thumbnail = cover
                    storage.save(cover)
                }
            }
        }
    }

    private inner class LibraryUpdateRunnable : Runnable {
        override fun run() {
            try {
                val ctx: Context = MainActivity.getAppContext()
                val preference: SharedPreferences? = GeneralConsts.getSharedPreferences(ctx)
                val libraryPath = preference?.getString(GeneralConsts.KEYS.LIBRARY.FOLDER, "")

                if (libraryPath == "" || !File(libraryPath).exists()) return

                val storage = Storage(ctx)
                val storageFiles: MutableMap<String, Manga> = HashMap()
                val generateCovers: MutableList<Manga> = arrayListOf()

                // create list of files available in storage
                for (c in storage.listBook()!!)
                    storageFiles[c.path] = c

                // search and add comics if necessary
                var file = File(libraryPath)
                file.walk()
                    .filterNot { it.isDirectory }.forEach {
                        if (mIsStopped) return

                        if (it.name.endsWith(".rar") ||
                            it.name.endsWith(".zip") ||
                            it.name.endsWith(".cbr") ||
                            it.name.endsWith(".cbz")
                        ) {
                            if (storageFiles.containsKey(it.path))
                                storageFiles.remove(it.path)
                            else {
                                val parse: Parse? = ParseFactory.create(it)
                                if (parse != null)
                                    if (parse.numPages() > 0) {
                                        val manga = Manga(
                                            null,
                                            it.name,
                                            "",
                                            it.path,
                                            it.parent,
                                            it.nameWithoutExtension,
                                            it.extension,
                                            parse.numPages()
                                        )

                                        manga.id = storage.save(manga)
                                        generateCovers.add(manga)
                                        notifyMediaUpdated()
                                    }
                            }
                        }
                    }

                // delete missing comics
                for (missing in storageFiles.values)
                    storage.delete(missing)

                if (generateCovers.size > 0)
                    generateCovers(generateCovers)
            } finally {
                mIsStopped = false
                if (mIsRestarted) {
                    mIsRestarted = false
                    mRestartHandler.sendEmptyMessageDelayed(1, 200)
                } else
                    notifyLibraryUpdateFinished()
            }
        }
    }
}
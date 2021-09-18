package br.com.fenix.mangareader.service.scanner

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Message
import android.os.Process
import br.com.fenix.mangareader.MainActivity
import br.com.fenix.mangareader.model.entity.Book
import br.com.fenix.mangareader.service.parses.Parse
import br.com.fenix.mangareader.service.parses.ParseFactory
import br.com.fenix.mangareader.service.repository.Storage
import br.com.fenix.mangareader.util.constants.GeneralConsts
import br.com.fenix.mangareader.util.helpers.Util
import java.io.File
import java.lang.ref.WeakReference
import java.util.*

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

    private fun notifyLibraryUpdateFinished() {
        for (h in mUpdateHandler!!)
            h.sendEmptyMessage(GeneralConsts.SCANNER.MESSAGE_MEDIA_UPDATE_FINISHED)
    }

    private inner class LibraryUpdateRunnable : Runnable {
        override fun run() {
            try {
                val ctx: Context = MainActivity.getAppContext()
                val preference: SharedPreferences? = GeneralConsts.getSharedPreferences(ctx)
                val library = preference?.getString(GeneralConsts.KEYS.LIBRARY.FOLDER, "")

                if (library == "") return

                val storage: Storage = Storage(ctx)
                val storageFiles: MutableMap<File?, Book> = HashMap<File?, Book>()

                // create list of files available in storage
                for (c in storage.listBook()!!)
                    storageFiles[c.file] = c

                // search and add comics if necessary
                val directories: Deque<File> = ArrayDeque()
                directories.add(File(library))
                while (!directories.isEmpty()) {
                    val dir = directories.pop()
                    val files = dir.listFiles()
                    Arrays.sort(files)
                    for (file in files) {
                        if (mIsStopped) return
                        if (file.isDirectory)
                            directories.add(file)

                        if (storageFiles.containsKey(file)) {
                            storageFiles.remove(file)
                            continue
                        }
                        val parse: Parse = ParseFactory.create(file) ?: continue
                        if (parse.numPages() > 0) {
                            storage.save(
                                Book(
                                    0,
                                    file.name,
                                    "",
                                    file.path,
                                    file.nameWithoutExtension,
                                    file.extension,
                                    parse.numPages()
                                )
                            )
                            notifyMediaUpdated()
                        }
                    }
                }

                // delete missing comics
                for (missing in storageFiles.values) {
                    val coverCache: File? =
                        Util.getCacheFile(ctx, missing.file!!.absolutePath)
                    coverCache!!.delete()
                    storage.delete(missing)
                }
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
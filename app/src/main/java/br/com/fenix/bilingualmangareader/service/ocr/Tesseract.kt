package br.com.fenix.bilingualmangareader.service.ocr

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Message
import android.os.Process
import br.com.fenix.bilingualmangareader.model.enums.Languages
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.util.helpers.FileUtil
import br.com.fenix.bilingualmangareader.util.helpers.Util
import com.googlecode.tesseract.android.TessBaseAPI
import org.slf4j.LoggerFactory
import java.io.File

class Tesseract(context: Context) {

    private val mLOGGER = LoggerFactory.getLogger(GoogleVision::class.java)

    companion object {
        fun copyTessData(context: Context) {
            val mFileUtil = FileUtil(context)
            val tessData = Util.normalizeFilePath(File(GeneralConsts.getCacheDir(context), GeneralConsts.CACHEFOLDER.TESSERACT).absolutePath)
            // Load language files from asset packs
            mFileUtil.copyAssetToFilesIfNotExist("tessdata/", "eng.traineddata", tessData)
            mFileUtil.copyAssetToFilesIfNotExist("tessdata/", "jpn.traineddata", tessData)
            mFileUtil.copyAssetToFilesIfNotExist("tessdata/", "jpn_vert.traineddata", tessData)
            mFileUtil.copyAssetToFilesIfNotExist("tessdata/", "por.traineddata", tessData)
        }

        private lateinit var INSTANCE: Tesseract

        fun getInstance(context: Context): Tesseract {
            if (!::INSTANCE.isInitialized)
                INSTANCE = Tesseract(context)
            return INSTANCE
        }
    }

    init {
        copyTessData(context)
    }

    private val TESSERACT_DATA_PATH = File(GeneralConsts.getCacheDir(context), GeneralConsts.CACHEFOLDER.TESSERACT).absolutePath
    private var tesseract: TessBaseAPI? = null

    fun process(language: Languages, image: Bitmap): String? {
        tesseract = TessBaseAPI()
        val isInit =  when(language) {
            Languages.PORTUGUESE -> tesseract!!.init(TESSERACT_DATA_PATH, "por")
            Languages.ENGLISH -> tesseract!!.init(TESSERACT_DATA_PATH, "eng")
            Languages.JAPANESE -> tesseract!!.init(TESSERACT_DATA_PATH, "jpn")
            else -> false
        }

        try {
            return if (isInit) {
                val grayScale = ImageProcess.processGrayscale(image)
                tesseract!!.setImage(grayScale)
                tesseract!!.utF8Text
            } else null
        } finally {
            tesseract!!.recycle()
        }
    }

    fun processAsync(language: Languages, image: Bitmap, setText: (String?) -> (Unit)) {
        val thread = Thread(ImageProcessRunnable(language, image, ImageUpdate(setText)))
        thread.priority = Process.THREAD_PRIORITY_DEFAULT + Process.THREAD_PRIORITY_LESS_FAVORABLE
        thread.start()
    }

    private inner class ImageUpdate(private var setText: (String?) -> (Unit)) : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                1 -> setText(msg.obj as String?)
            }
        }
    }

    private inner class ImageProcessRunnable(private var language: Languages, private var image: Bitmap, private var setText: Handler) : Runnable {
        private var tesseract: TessBaseAPI? = null
        override fun run() {
            try {
                tesseract = TessBaseAPI()
                val isInit =  when(language) {
                    Languages.PORTUGUESE -> tesseract!!.init(TESSERACT_DATA_PATH, "por")
                    Languages.ENGLISH -> tesseract!!.init(TESSERACT_DATA_PATH, "eng")
                    Languages.JAPANESE -> tesseract!!.init(TESSERACT_DATA_PATH, "jpn")
                    else -> false
                }

                try {
                    if (isInit) {
                        val grayScale = ImageProcess.processGrayscale(image)
                        tesseract!!.setImage(grayScale)
                        val msg = Message()
                        msg.obj = tesseract!!.utF8Text
                        msg.what = 1
                        setText.sendMessage(msg)
                    }
                } finally {
                    tesseract!!.recycle()
                }
            } catch (e: Exception) {
                mLOGGER.error("Error to process Tesseract ocr image async: " + e.message, e)
            }
        }
    }

}
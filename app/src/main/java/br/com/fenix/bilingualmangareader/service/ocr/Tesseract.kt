package br.com.fenix.bilingualmangareader.service.ocr

import android.content.Context
import android.graphics.Bitmap
import br.com.fenix.bilingualmangareader.model.enums.Languages
import br.com.fenix.bilingualmangareader.util.helpers.FileUtil
import com.googlecode.tesseract.android.TessBaseAPI

class Tesseract(context: Context) {

    companion object {
        fun copyTessData(context: Context) {
            val mFileUtil = FileUtil(context)
            // Load language files from asset packs
            mFileUtil.copyAssetToFilesIfNotExist("tesseract/tessdata/", "eng.traineddata")
            mFileUtil.copyAssetToFilesIfNotExist("tesseract/tessdata/", "jpn.traineddata")
            mFileUtil.copyAssetToFilesIfNotExist("tesseract/tessdata/", "jpn_vert.traineddata")
            mFileUtil.copyAssetToFilesIfNotExist("tesseract/tessdata/", "por.traineddata")
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

    private val TESSERACT_DATA_PATH = context.filesDir.absolutePath + "/tesseract"
    private var tesseract: TessBaseAPI = TessBaseAPI()

    fun process(language: Languages, image: Bitmap): String? {
        val isInit =  when(language) {
            Languages.PORTUGUESE -> tesseract.init(TESSERACT_DATA_PATH, "por")
            Languages.ENGLISH -> tesseract.init(TESSERACT_DATA_PATH, "eng")
            Languages.JAPANESE -> tesseract.init(TESSERACT_DATA_PATH, "jpn")
            else -> false
        }

        try {
            return if (isInit) {
                val grayScale = ImageProcess.processGrayscale(image)
                tesseract.setImage(grayScale)
                tesseract.utF8Text
            } else null
        } finally {
            tesseract.recycle()
        }
    }

}
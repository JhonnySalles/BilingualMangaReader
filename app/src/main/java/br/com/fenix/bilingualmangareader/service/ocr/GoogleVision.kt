package br.com.fenix.bilingualmangareader.service.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import org.slf4j.LoggerFactory

class GoogleVision {

    private val mLOGGER = LoggerFactory.getLogger(GoogleVision::class.java)

    companion object {
        private lateinit var INSTANCE: GoogleVision

        fun getInstance(): GoogleVision {
            if (!::INSTANCE.isInitialized)
                INSTANCE = GoogleVision()
            return INSTANCE
        }
    }

    private fun getElements(visionText: Text): ArrayList<String> {
        val texts = arrayListOf<String>()
        for (block in visionText.textBlocks) {
            //val blockText = block.text
            //val blockCornerPoints = block.cornerPoints
            //val blockFrame = block.boundingBox
            for (line in block.lines) {
                //val lineText = line.text
                //val lineCornerPoints = line.cornerPoints
                //val lineFrame = line.boundingBox
                for (element in line.elements) {
                    texts.add(element.text)
                    //val elementText = element.text
                    //val elementCornerPoints = element.cornerPoints
                    //val elementFrame = element.boundingBox
                }
            }
        }

        return texts
    }

    private fun getBlocks(visionText: Text): ArrayList<String> {
        val texts = arrayListOf<String>()
        for (block in visionText.textBlocks)
            texts.add(block.text)

        return texts
    }

    fun process(image: Bitmap, setText: (ArrayList<String>) -> (Unit)) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val input = InputImage.fromBitmap(image, 0)
        recognizer.process(input)
            .addOnSuccessListener { visionText -> setText(getBlocks(visionText)) }
            .addOnFailureListener { e ->
                mLOGGER.error("Error to process google vision ocr: " + e.message, e)
            }
    }
}
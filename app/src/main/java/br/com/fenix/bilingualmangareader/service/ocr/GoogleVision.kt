package br.com.fenix.bilingualmangareader.service.ocr

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class GoogleVision {

    companion object {
        private lateinit var INSTANCE: GoogleVision

        fun getInstance(context: Context): GoogleVision {
            if (!::INSTANCE.isInitialized)
                INSTANCE = GoogleVision()
            return INSTANCE
        }
    }

    fun process(image: Bitmap) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val input = InputImage.fromBitmap(image, 0)
        recognizer.process(input)
            .addOnSuccessListener { visionText ->
                val resultText = visionText.text
                for (block in visionText.textBlocks) {
                    val blockText = block.text
                    val blockCornerPoints = block.cornerPoints
                    val blockFrame = block.boundingBox
                    for (line in block.lines) {
                        val lineText = line.text
                        val lineCornerPoints = line.cornerPoints
                        val lineFrame = line.boundingBox
                        for (element in line.elements) {
                            val elementText = element.text
                            val elementCornerPoints = element.cornerPoints
                            val elementFrame = element.boundingBox
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }
}
package br.com.fenix.bilingualmangareader.service.ocr

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import br.com.fenix.bilingualmangareader.R
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import org.slf4j.LoggerFactory

class GoogleVision(private var context: Context) {

    private val mLOGGER = LoggerFactory.getLogger(GoogleVision::class.java)

    companion object {
        private lateinit var INSTANCE: GoogleVision

        fun getInstance(context: Context): GoogleVision {
            if (!::INSTANCE.isInitialized)
                INSTANCE = GoogleVision(context)
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

        Toast.makeText(
            context,
            context.resources.getString(R.string.ocr_google_vision_get_request),
            Toast.LENGTH_SHORT
        ).show()

        recognizer.process(input)
            .addOnSuccessListener { visionText -> setText(getBlocks(visionText)) }
            .addOnFailureListener { e ->
                mLOGGER.error("Error to process google vision ocr: " + e.message, e)

                val msg = if (e.message != null && e.message!!.isNotEmpty())
                    e.message
                else
                    context.getString(R.string.ocr_google_vision_error)

                AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle)
                    .setTitle(context.getString(R.string.alert_title))
                    .setMessage(msg)
                    .setPositiveButton(
                        R.string.action_neutral
                    ) { _, _ -> }
                    .create()
                    .show()
            }
    }
}
package br.com.fenix.bilingualmangareader.view.components

import android.graphics.Bitmap
import br.com.fenix.bilingualmangareader.model.enums.Languages

interface OcrProcess {
    fun getImage(): Bitmap?
    fun getImage(x: Int, y: Int, width: Int, height: Int): Bitmap?
    fun getLanguage(): Languages
    fun setText(text: String?)
}
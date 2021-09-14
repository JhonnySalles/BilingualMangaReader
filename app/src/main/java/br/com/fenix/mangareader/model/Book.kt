package br.com.fenix.mangareader.model

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

class Book(Id: Int, Title: String, SubTitle: String, Archive: File, Type: String) {

    var id : Int = Id
    var title: String = Title
    var subTitle: String = SubTitle
    var pages: Int = 1
    var bookMark: Int = 0
    var file : File = Archive
    var type : String = Type
    var tumbnail: Bitmap? = null
    var update: Boolean = false

    override fun toString(): String {
        return "Book(id=$id, title='$title', subTitle='$subTitle', pages=$pages, bookMark=$bookMark, type='$type', update=$update)"
    }
}
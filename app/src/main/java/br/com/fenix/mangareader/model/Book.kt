package br.com.fenix.mangareader.model

import android.graphics.Bitmap
import java.io.File

class Book(id: Int, title: String, subTitle: String, file: File, type: String) {

    var id : Int = id
        get() {
            return id;
        }
    var title: String = title
        get() {
            return title;
        }
    var subTitle: String = subTitle
        get() {
            return subTitle
        }
        set(value) { field = value}
    var pages: Int = 0
        get() {
            return pages
        }
        set(value) { field = value}
    var bookMark: Int = 0
        get() {
            return bookMark
        }
        set(value) { field = value}
    var file : File = file
        get() {
            return file
        }
    var type : String = type
        get() {
            return type
        }
    var tumbnail: Bitmap? = null
        get() {
            return tumbnail
        }
        set(value) { field = value}

    var update: Boolean = false
        get() {
            return update
        }
        set(value) { field = value}

}
package br.com.fenix.mangareader

import android.graphics.Bitmap
import java.io.File

class Book(title: String, sub_title: String, file_path: File) {

    var Title: String = title
        get() {
            return Title;
        }
    var SubTitle: String = sub_title
        get() {
            return SubTitle
        }
        set(value) { field = value}
    var Pages: Int = 0
        get() {
            return Pages
        }
        set(value) { field = value}
    var bookMark: Int = 0
        get() {
            return bookMark
        }
        set(value) { field = value}
    var Tumbnail: Int = 0
        get() {
            return Tumbnail
        }
        set(value) { field = value}
    var file_path: File = file_path
        get() {
            return file_path
        }
    var image_cover: Bitmap? = null
        get() {
            return image_cover
        }
        set(value) { field = value}

}
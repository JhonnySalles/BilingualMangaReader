package br.com.fenix.mangareader.model

import android.graphics.Bitmap

class Cover(Id: Long, Name: String, Size: Int, Type: String, Image: Bitmap?) {

    var id: Long = Id
    var name: String = Name
    var size: Int = Size
    var type: String = Type
    var image: Bitmap? = Image
    var update: Boolean = false

    override fun toString(): String {
        return "Cover(id=$id, name='$name', size=$size, type='$type', update=$update)"
    }
}
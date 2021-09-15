package br.com.fenix.mangareader.model

import java.io.File

class Book(Id: Long, Title: String, SubTitle: String, Archive: File, Type: String) {

    var id: Long = Id
    var title: String = Title
    var subTitle: String = SubTitle
    var pages: Int = 1
    var bookMark: Int = 0
    var file: File = Archive
    var type: String = Type
    var favorite: Boolean = false
    var tumbnail: Cover? = null
    var update: Boolean = false

    override fun toString(): String {
        return "Book(id=$id, title='$title', subTitle='$subTitle', pages=$pages, bookMark=$bookMark, type='$type', update=$update)"
    }
}
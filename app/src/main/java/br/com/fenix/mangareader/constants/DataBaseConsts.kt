package br.com.fenix.mangareader.constants

class DataBaseConsts private constructor() {
    object BOOK {
        const val TABLE_NAME = "Book"

        object COLUMNS {
            const val ID = "id"
            const val TITLE = "title"
            const val SUB_TITLE = "subTitle"
            const val PAGES = "pages"
            const val BOOK_MARK = "bookMark"
            const val FILE_PATH = "path"
            const val FILE_NAME = "name"
            const val FILE_TYPE = "type"
            const val FAVORITE = "favorite"
            const val DATE_CREATE = "dateCreate"
            const val LAST_ACCESS = "lastAccess"
        }
    }

    object COVER {
        const val TABLE_NAME = "Covers"

        object COLUMNS {
            const val ID = "id"
            const val FK_ID_BOOK = "id_book"
            const val NAME = "name"
            const val SIZE = "size"
            const val TYPE = "type"
            const val IMAGE = "image"
        }
    }

    object SUBTITLES {
        const val TABLE_NAME = "SubTitles"

        object COLUMNS {
            const val ID = "id"
            const val FK_ID_BOOK = "id_book"
            const val NAME = "name"
            const val FOLDER = "folder"
            const val ROOT = "isRoot"
            const val DATE_CREATE = "dateCreate"
        }
    }

}
package br.com.fenix.mangareader.util.constants

class DataBaseConsts private constructor() {
    object MANGA {
        const val TABLE_NAME = "Manga"

        object COLUMNS {
            const val ID = "id"
            const val TITLE = "title"
            const val SUB_TITLE = "subTitle"
            const val PAGES = "pages"
            const val BOOK_MARK = "bookMark"
            const val FILE_PATH = "path"
            const val FILE_NAME = "name"
            const val FILE_TYPE = "type"
            const val FILE_FOLDER = "folder"
            const val FAVORITE = "favorite"
            const val DATE_CREATE = "dateCreate"
            const val LAST_ACCESS = "lastAccess"
        }
    }

    object COVER {
        const val TABLE_NAME = "Covers"

        object COLUMNS {
            const val ID = "id"
            const val FK_ID_MANGA = "id_manga"
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
            const val FK_ID_MANGA = "id_manga"
            const val LANGUAGE = "language"
            const val CHAPTER_KEY = "chapterKey"
            const val PAGE_KEY = "pageKey"
            const val PAGE = "pageCount"
            const val FILE_PATH = "path"
            const val DATE_CREATE = "dateCreate"
        }
    }

    object JLPT {
        const val TABLE_NAME = "JLPT"

        object COLUMNS {
            const val ID = "id"
            const val KANJI = "kanji"
            const val LEVEL = "level"
        }
    }

}
package br.com.fenix.bilingualmangareader.util.constants

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
            const val EXCLUDED = "excluded"
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

    object KANJAX {
        const val TABLE_NAME = "KANJAX"

        object COLUMNS {
            const val ID = "id"
            const val KANJI = "kanji"
            const val KEYWORD = "keyword"
            const val MEANING = "meaning"
            const val KOOHII = "koohii"
            const val KOOHII2 = "kohii2"
            const val ONYOMI = "onyomi"
            const val KUNYOMI = "kunyomi"
            const val ONWORDS = "onwords"
            const val KUNWORDS = "kunwords"
            const val JLPT = "jlpt"
            const val GRADE = "grade"
            const val FREQUENCE = "frequence"
            const val STROKES = "strokes"
            const val VARIANTS = "variants"
            const val RADICAL = "radical"
            const val PARTS = "parts"
            const val UTF8 = "utf8"
            const val SJIS = "sjis"
            const val KEYWORDS_PT = "keywords_pt"
            const val MEANING_PT = "meaning_pt"
        }
    }

    object FILELINK {
        const val TABLE_NAME = "FileLink"

        object COLUMNS {
            const val ID = "id"
            const val FK_ID_MANGA = "id_manga"
            const val PAGES = "pages"
            const val FILE_PATH = "path"
            const val FILE_NAME = "name"
            const val FILE_TYPE = "type"
            const val FILE_FOLDER = "folder"
            const val LANGUAGE = "language"
            const val DATE_CREATE = "dateCreate"
            const val LAST_ACCESS = "lastAccess"
        }
    }

    object PAGESLINK {
        const val TABLE_NAME = "PagesLink"

        object COLUMNS {
            const val ID = "id"
            const val FK_ID_FILE = "id_file"
            const val MANGA_PAGE = "manga_page"
            const val MANGA_PAGES = "manga_pages"
            const val MANGA_PAGE_NAME = "manga_page_name"
            const val FILE_LINK_PAGE = "file_link_page"
            const val FILE_LINK_PAGES = "file_link_pages"
            const val FILE_LINK_PAGE_NAME = "file_link_page_name"
            const val FILE_RIGHT_LINK_PAGE = "file_right_link_page"
            const val FILE_RIGHT_LINK_PAGE_NAME = "file_right_link_page_name"
            const val NOT_LINKED = "not_linked"
            const val DUAL_IMAGE = "dual_image"
        }
    }



}
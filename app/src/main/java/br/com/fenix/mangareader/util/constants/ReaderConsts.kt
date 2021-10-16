package br.com.fenix.mangareader.util.constants

import com.worksap.nlp.sudachi.Tokenizer

class ReaderConsts {

    object READER {
        const val MAX_PAGE_HEIGHT = 1600
        const val MAX_PAGE_WIDTH = 2000
        const val MAX_RECENT_COUNT = 5
    }

    object COVER {
        const val COVER_THUMBNAIL_HEIGHT = 300
        const val COVER_THUMBNAIL_WIDTH = 200
    }

    object STATES {
        const val STATE_FULLSCREEN = "STATE_FULLSCREEN"
        const val STATE_NEW_COMIC = "STATE_NEW_COMIC"
        const val STATE_NEW_COMIC_TITLE = "STATE_NEW_COMIC_TITLE"
    }

    object TOKENIZER {
        object SUDACHI {
            val DICTIONARY_NAME = "sudachi_smalldict.json"
            val SPLIT_MODE = Tokenizer.SplitMode.C
        }
    }

}
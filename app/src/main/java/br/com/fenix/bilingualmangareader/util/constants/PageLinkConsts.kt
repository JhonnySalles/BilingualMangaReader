package br.com.fenix.bilingualmangareader.util.constants

class PageLinkConsts {

    object VALUES {
        const val PAGE_EMPTY: Int = -1
    }

    object MESSAGES {
        const val MESSAGE_PAGES_LINK_IMAGE_START = 0
        const val MESSAGE_PAGES_LINK_IMAGE_UPDATED = 1
        const val MESSAGE_PAGES_LINK_IMAGE_ADDED = 2
        const val MESSAGE_PAGES_LINK_IMAGE_REMOVED = 3
        const val MESSAGE_PAGES_LINK_IMAGE_FINISHED = 4
        const val MESSAGE_PAGES_LINK_IMAGE_LOAD_ERROR = 5
    }

    object TAG {
        const val PAGE_LINK_RIGHT = "page_link_right"
        const val PAGE_LINK_LEFT = "page_link_left"
        const val PAGE_NOT_LINK = "page_not_link"
    }

    object CLIPDATA {
        const val PAGE_LINK = 0
        const val PAGE_TYPE = 1
        const val IMAGE_NAME = 2
    }
}
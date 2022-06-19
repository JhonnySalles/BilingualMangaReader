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
        const val MESSAGE_PAGES_LINK_ALL_IMAGES_LOADED = 9
        
        const val MESSAGE_PAGES_LINK_REORDER_AUTO_PAGES_START = 10
        const val MESSAGE_PAGES_LINK_REORDER_AUTO_PAGES_FINISHED = 11
        const val MESSAGE_PAGES_LINK_REORDER_DOUBLE_PAGES_START = 12
        const val MESSAGE_PAGES_LINK_REORDER_DOUBLE_PAGES_FINISHED = 13
        const val MESSAGE_PAGES_LINK_REORDER_SIMPLE_PAGES_START = 14
        const val MESSAGE_PAGES_LINK_REORDER_SIMPLE_PAGES_FINISHED = 15
        const val MESSAGE_PAGES_LINK_REORDER_SORTED_PAGES_START = 16
        const val MESSAGE_PAGES_LINK_REORDER_SORTED_PAGES_FINISHED = 17

        const val MESSAGE_PAGES_LINK_ITEM_CHANGE = 20
        const val MESSAGE_PAGES_LINK_ITEM_ADD = 21
        const val MESSAGE_PAGES_LINK_ITEM_REMOVE = 22
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
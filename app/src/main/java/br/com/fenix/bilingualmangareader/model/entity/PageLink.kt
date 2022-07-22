package br.com.fenix.bilingualmangareader.model.entity

import android.graphics.Bitmap
import androidx.room.*
import br.com.fenix.bilingualmangareader.util.constants.DataBaseConsts
import br.com.fenix.bilingualmangareader.util.constants.PageLinkConsts
import java.io.Serializable

@Entity(
    tableName = DataBaseConsts.PAGESLINK.TABLE_NAME,
    indices = [Index(value = [DataBaseConsts.PAGESLINK.COLUMNS.FK_ID_FILE])]
)
class PageLink(
    id: Long?,
    idFile: Long?,
    mangaPage: Int,
    mangaPages: Int,
    mangaPageName: String,
    mangaPagePath: String,
    fileLinkLeftPage: Int = PageLinkConsts.VALUES.PAGE_EMPTY,
    fileLinkLeftPages: Int = 0,
    fileLinkLeftPageName: String = "",
    fileLinkLeftPagePath: String = "",
    fileLinkRightPage: Int = PageLinkConsts.VALUES.PAGE_EMPTY,
    fileLinkRightPageName: String = "",
    fileLinkRightPagePath: String = "",
    isNotLinked: Boolean = false,
    isDualImage: Boolean = false,
    isMangaDualPage: Boolean = false,
    isFileLeftDualPage: Boolean = false,
    isFileRightDualPage: Boolean = false
) : Serializable {

    constructor(
        id: Long?,
        idFile: Long?,
        mangaPage: Int,
        mangaPages: Int,
        mangaPageName: String,
        mangaPagePath: String,
        fileLinkLeftPage: Int,
        fileLinkLeftPages: Int,
        fileLinkLeftPageName: String,
        fileLinkLeftPagePath: String,
        fileLinkRightPage: Int,
        fileLinkRightPageName: String,
        fileLinkRightPagePath: String,
        isNotLinked: Boolean,
        isDualImage: Boolean,
        isMangaDualPage: Boolean,
        isFileLeftDualPage: Boolean,
        isFileRightDualPage: Boolean,
        imageMangaPage: Bitmap? = null,
        imageFileLinkPage: Bitmap? = null,
        imageRightFileLinkPage: Bitmap? = null
    ) : this(
        id,
        idFile,
        mangaPage,
        mangaPages,
        mangaPageName,
        mangaPagePath,
        fileLinkLeftPage,
        fileLinkLeftPages,
        fileLinkLeftPageName,
        fileLinkLeftPagePath,
        fileLinkRightPage,
        fileLinkRightPageName,
        fileLinkRightPagePath,
        isNotLinked,
        isDualImage,
        isMangaDualPage,
        isFileLeftDualPage,
        isFileRightDualPage
    ) {
        this.imageMangaPage = imageMangaPage
        this.imageLeftFileLinkPage = imageFileLinkPage
        this.imageRightFileLinkPage = imageRightFileLinkPage
    }

    constructor(
        idFile: Long?,
        mangaPage: Int,
        mangaPages: Int,
        mangaPageName: String,
        mangaPagePath: String,
        fileLinkLeftPage: Int,
        fileLinkLeftPages: Int,
        fileLinkLeftPageName: String,
        fileLinkLeftPagePath: String,
        isFileLeftDualPage: Boolean,
        isFileRightDualPage: Boolean,
        imageFileLinkPage: Bitmap? = null,
        imageRightFileLinkPage: Bitmap? = null
    ) : this(
        null,
        idFile,
        mangaPage,
        mangaPages,
        mangaPageName,
        mangaPagePath,
        fileLinkLeftPage,
        fileLinkLeftPages,
        fileLinkLeftPageName,
        fileLinkLeftPagePath,
        isNotLinked = mangaPage == PageLinkConsts.VALUES.PAGE_EMPTY,
        isFileLeftDualPage = isFileLeftDualPage,
        isFileRightDualPage = isFileRightDualPage
    ) {
        this.imageMangaPage = null
        this.imageLeftFileLinkPage = imageFileLinkPage
        this.imageRightFileLinkPage = imageRightFileLinkPage
    }

    constructor(
        idFile: Long?,
        mangaPage: Int,
        mangaPages: Int,
        mangaPageName: String,
        mangaPagePath: String,
        isMangaDualPage: Boolean = false,
        imageMangaPage: Bitmap? = null
    ) : this(null, idFile, mangaPage, mangaPages, mangaPageName, mangaPagePath, isMangaDualPage = isMangaDualPage) {
        this.imageMangaPage = imageMangaPage
        this.imageLeftFileLinkPage = null
        this.imageRightFileLinkPage = null
    }

    constructor(
        idFile: Long?,
        mangaPage: Int,
        mangaPages: Int,
        mangaPageName: String,
        mangaPagePath: String,
        fileLinkLeftPage: Int,
        fileLinkLeftPages: Int,
        fileLinkLeftPageName: String,
        fileLinkLeftPagePath: String,
        isNotLinked: Boolean = false,
        isMangaDualPage: Boolean = false,
        isFileLeftDualPage: Boolean = false,
        imageMangaPage: Bitmap? = null,
        imageFileLinkPage: Bitmap? = null
    ) : this(
        null,
        idFile,
        mangaPage,
        mangaPages,
        mangaPageName,
        mangaPagePath,
        fileLinkLeftPage,
        fileLinkLeftPages,
        fileLinkLeftPageName,
        fileLinkLeftPagePath,
        isNotLinked = isNotLinked
    ) {
        this.imageMangaPage = imageMangaPage
        this.imageLeftFileLinkPage = imageFileLinkPage
        this.imageRightFileLinkPage = null
        this.isMangaDualPage = isMangaDualPage
        this.isFileLeftDualPage = isFileLeftDualPage
    }

    constructor(
        idFile: Long?, isNotLinked: Boolean, fileLinkLeftPage: Int, fileLinkLeftPages: Int, fileLinkLeftPageName: String, fileLinkLeftPagePath: String,
        isFileLeftDualPage: Boolean = false, imageFileLinkPage: Bitmap? = null
    ) : this(
        null, idFile, PageLinkConsts.VALUES.PAGE_EMPTY, 0, "", "", fileLinkLeftPage, fileLinkLeftPages,
        fileLinkLeftPageName, fileLinkLeftPagePath, isNotLinked = isNotLinked
    ) {
        this.imageMangaPage = null
        this.imageLeftFileLinkPage = imageFileLinkPage
        this.imageRightFileLinkPage = null
        this.isFileLeftDualPage = isFileLeftDualPage
    }

    constructor(pageLink: PageLink) : this(
        pageLink.id, pageLink.mangaPage, pageLink.mangaPages, pageLink.mangaPageName,
        pageLink.mangaPagePath, pageLink.isMangaDualPage, pageLink.imageMangaPage
    )

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.ID)
    var id: Long? = id

    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.FK_ID_FILE)
    var idFile: Long? = idFile

    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.MANGA_PAGE)
    val mangaPage: Int = mangaPage

    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.MANGA_PAGES)
    val mangaPages: Int = mangaPages

    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.MANGA_PAGE_NAME)
    val mangaPageName: String = mangaPageName

    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.MANGA_PAGE_PATH)
    val mangaPagePath: String = mangaPagePath

    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.FILE_LINK_PAGE)
    var fileLinkLeftPage: Int = fileLinkLeftPage

    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.FILE_LINK_PAGES)
    var fileLinkLeftPages: Int = fileLinkLeftPages

    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.FILE_LINK_PAGE_NAME)
    var fileLinkLeftPageName: String = fileLinkLeftPageName

    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.FILE_LINK_PAGE_PATH)
    var fileLinkLeftPagePath: String = fileLinkLeftPagePath

    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.FILE_RIGHT_LINK_PAGE)
    var fileLinkRightPage: Int = fileLinkRightPage

    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.FILE_RIGHT_LINK_PAGE_NAME)
    var fileLinkRightPageName: String = fileLinkRightPageName

    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.FILE_RIGHT_LINK_PAGE_PATH)
    var fileLinkRightPagePath: String = fileLinkRightPagePath

    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.NOT_LINKED)
    var isNotLinked: Boolean = isNotLinked

    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.DUAL_IMAGE)
    var isDualImage: Boolean = isDualImage

    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.MANGA_DUAL_PAGE)
    var isMangaDualPage: Boolean = isMangaDualPage

    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.FILE_LEFT_DUAL_PAGE)
    var isFileLeftDualPage: Boolean = isFileLeftDualPage

    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.FILE_RIGHT_DUAL_PAGE)
    var isFileRightDualPage: Boolean = isFileRightDualPage

    @Ignore
    var imageMangaPage: Bitmap? = null

    @Ignore
    var imageLeftFileLinkPage: Bitmap? = null

    @Ignore
    var imageRightFileLinkPage: Bitmap? = null

    fun merge(another: PageLink) {
        this.fileLinkLeftPage = another.fileLinkLeftPage
        this.fileLinkLeftPages = another.fileLinkLeftPages
        this.fileLinkLeftPageName = another.fileLinkLeftPageName
        this.fileLinkLeftPagePath = another.fileLinkLeftPagePath
        this.fileLinkRightPage = another.fileLinkRightPage
        this.fileLinkRightPageName = another.fileLinkRightPageName
        this.fileLinkRightPagePath = another.fileLinkRightPagePath
        this.imageLeftFileLinkPage = another.imageLeftFileLinkPage
        this.imageRightFileLinkPage = another.imageRightFileLinkPage
        this.isNotLinked = another.isNotLinked
        this.isDualImage = another.isDualImage
        this.isFileLeftDualPage = another.isFileLeftDualPage
        this.isFileRightDualPage = another.isFileRightDualPage
    }

    fun addLeftPageLink(another: PageLink) {
        this.addLeftPageLink(
            another.fileLinkLeftPage, another.fileLinkLeftPages, another.fileLinkLeftPageName,
            another.fileLinkLeftPagePath, another.isFileLeftDualPage, another.imageLeftFileLinkPage
        )
    }

    fun addLeftFromRightPageLink(another: PageLink) {
        this.addLeftPageLink(
            another.fileLinkRightPage, another.fileLinkLeftPages, another.fileLinkRightPageName,
            another.fileLinkRightPagePath, another.isFileRightDualPage, another.imageRightFileLinkPage
        )
    }

    fun addLeftPageLink(page: Int, pages: Int, pageName: String, pagePath: String, isFileLeftDualPage: Boolean, image: Bitmap?) {
        this.fileLinkLeftPage = page
        this.fileLinkLeftPages = pages
        this.fileLinkLeftPageName = pageName
        this.fileLinkLeftPagePath = pagePath
        this.imageLeftFileLinkPage = image
        this.isFileLeftDualPage = isFileLeftDualPage
    }

    fun addRightFromLeftPageLink(another: PageLink) {
        this.addRightPageLink(
            another.fileLinkLeftPage, another.fileLinkLeftPageName, another.fileLinkLeftPagePath,
            another.isFileLeftDualPage, another.imageLeftFileLinkPage
        )
    }

    fun addRightPageLink(another: PageLink) {
        this.addRightPageLink(
            another.fileLinkRightPage, another.fileLinkRightPageName, another.fileLinkRightPagePath,
            another.isFileRightDualPage, another.imageRightFileLinkPage
        )
    }

    fun addRightPageLink(page: Int, pageName: String, pagePath: String, isFileRightDualPage: Boolean, image: Bitmap?) {
        if (this.fileLinkLeftPage == PageLinkConsts.VALUES.PAGE_EMPTY) {
            this.fileLinkLeftPage = page
            this.fileLinkLeftPageName = pageName
            this.fileLinkLeftPagePath = pagePath
            this.imageLeftFileLinkPage = image
            this.isFileLeftDualPage = isFileRightDualPage
        } else {
            this.fileLinkRightPage = page
            this.fileLinkRightPageName = pageName
            this.fileLinkRightPagePath = pagePath
            this.imageRightFileLinkPage = image
            this.isFileRightDualPage = isFileRightDualPage
        }
        this.isDualImage = this.fileLinkRightPage != PageLinkConsts.VALUES.PAGE_EMPTY
    }

    fun movePageLinkRightToLeft() {
        this.fileLinkLeftPage = this.fileLinkRightPage
        this.fileLinkLeftPageName = this.fileLinkRightPageName
        this.fileLinkLeftPagePath = this.fileLinkRightPagePath
        this.imageLeftFileLinkPage = this.imageRightFileLinkPage
        this.isFileLeftDualPage = this.isFileRightDualPage
        this.clearRightPageLink()
    }

    fun clearPageLink() {
        this.fileLinkLeftPage = PageLinkConsts.VALUES.PAGE_EMPTY
        this.fileLinkLeftPages = 0
        this.fileLinkLeftPageName = ""
        this.fileLinkLeftPagePath = ""
        this.fileLinkRightPage = PageLinkConsts.VALUES.PAGE_EMPTY
        this.fileLinkRightPageName = ""
        this.fileLinkRightPagePath = ""
        this.imageLeftFileLinkPage = null
        this.imageRightFileLinkPage = null
        this.isFileLeftDualPage = false
        this.isFileRightDualPage = false
        this.isNotLinked = false
        this.isDualImage = false
    }

    fun clearLeftPageLink(canMoved: Boolean = false): Boolean {
        val moved = if (canMoved && this.fileLinkRightPage != PageLinkConsts.VALUES.PAGE_EMPTY) {
            this.fileLinkLeftPage = this.fileLinkRightPage
            this.fileLinkLeftPageName = this.fileLinkRightPageName
            this.fileLinkLeftPagePath = this.fileLinkRightPagePath
            this.imageLeftFileLinkPage = this.imageRightFileLinkPage
            this.isFileLeftDualPage = this.isFileRightDualPage
            this.clearRightPageLink()
            true
        } else {
            this.fileLinkLeftPage = PageLinkConsts.VALUES.PAGE_EMPTY
            this.fileLinkLeftPages = 0
            this.fileLinkLeftPageName = ""
            this.fileLinkLeftPagePath = ""
            this.imageLeftFileLinkPage = null
            this.isFileLeftDualPage = false
            false
        }
        return moved
    }

    fun clearRightPageLink() {
        this.fileLinkRightPage = PageLinkConsts.VALUES.PAGE_EMPTY
        this.fileLinkRightPageName = ""
        this.fileLinkRightPagePath = ""
        this.imageRightFileLinkPage = null
        this.isDualImage = false
        this.isFileRightDualPage = false
    }
}
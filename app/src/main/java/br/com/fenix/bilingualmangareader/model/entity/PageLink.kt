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
class PageLink(id: Long?, idFile: Long?, mangaPage: Int, mangaPages: Int, mangaPageName: String, mangaPagePath: String, fileLinkPage: Int = PageLinkConsts.VALUES.PAGE_EMPTY,
               fileLinkPages: Int = 0, fileLinkPageName: String = "", fileLinkPagePath: String = "", fileRightLinkPage: Int = PageLinkConsts.VALUES.PAGE_EMPTY,
               fileRightLinkPageName: String = "", fileRightLinkPagePath: String = "", notLinked: Boolean = false, dualImage: Boolean = false, isMangaDualPage: Boolean = false,
               isFileLeftDualPage: Boolean = false, isFileRightDualPage: Boolean = false) : Serializable {

    constructor(id: Long?, idFile: Long?, mangaPage: Int, mangaPages: Int, mangaPageName: String, mangaPagePath: String, fileLinkPage: Int, fileLinkPages: Int,
                fileLinkPageName: String, fileLinkPagePath: String, fileRightLinkPage: Int, fileRightLinkPageName: String, fileRightLinkPagePath: String,
                notLinked: Boolean, dualImage: Boolean, isMangaDualPage: Boolean, isFileLeftDualPage: Boolean, isFileRightDualPage: Boolean, imageMangaPage: Bitmap? = null,
                imageFileLinkPage: Bitmap? = null, imageRightFileLinkPage: Bitmap? = null
    ) : this(id, idFile, mangaPage, mangaPages, mangaPageName, mangaPagePath, fileLinkPage, fileLinkPages, fileLinkPageName, fileLinkPagePath, fileRightLinkPage,
             fileRightLinkPageName, fileRightLinkPagePath, notLinked, dualImage, isMangaDualPage, isFileLeftDualPage, isFileRightDualPage) {
        this.imageMangaPage = imageMangaPage
        this.imageLeftFileLinkPage = imageFileLinkPage
        this.imageRightFileLinkPage = imageRightFileLinkPage
    }

    constructor(idFile: Long?, mangaPage: Int, mangaPages: Int, mangaPageName: String, mangaPagePath: String, fileLinkPage: Int, fileLinkPages: Int, fileLinkPageName: String,
                fileLinkPagePath: String, isFileLeftDualPage: Boolean, isFileRightDualPage: Boolean, imageFileLinkPage: Bitmap? = null, imageRightFileLinkPage: Bitmap? = null
    ) : this(null, idFile, mangaPage, mangaPages, mangaPageName, mangaPagePath, fileLinkPage, fileLinkPages, fileLinkPageName, fileLinkPagePath,
             notLinked = mangaPage == PageLinkConsts.VALUES.PAGE_EMPTY, isFileLeftDualPage = isFileLeftDualPage, isFileRightDualPage = isFileRightDualPage) {
        this.imageMangaPage = null
        this.imageLeftFileLinkPage = imageFileLinkPage
        this.imageRightFileLinkPage = imageRightFileLinkPage
    }

    constructor(idFile: Long?, mangaPage: Int, mangaPages: Int, mangaPageName: String, mangaPagePath: String, isMangaDualPage: Boolean = false, imageMangaPage: Bitmap? = null
    ) : this(null, idFile, mangaPage, mangaPages, mangaPageName, mangaPagePath, isMangaDualPage = isMangaDualPage) {
        this.imageMangaPage = imageMangaPage
        this.imageLeftFileLinkPage = null
        this.imageRightFileLinkPage = null
    }

    constructor(idFile: Long?, mangaPage: Int, mangaPages: Int, mangaPageName: String, mangaPagePath: String, fileLinkPage: Int, fileLinkPages: Int,
                fileLinkPageName: String, fileLinkPagePath: String, notLinked:Boolean = false, isMangaDualPage: Boolean = false, isFileLeftDualPage: Boolean = false,
                imageMangaPage: Bitmap? = null, imageFileLinkPage: Bitmap? = null
    ) : this(null, idFile, mangaPage, mangaPages, mangaPageName, mangaPagePath, fileLinkPage, fileLinkPages, fileLinkPageName, fileLinkPagePath, notLinked = notLinked) {
        this.imageMangaPage = imageMangaPage
        this.imageLeftFileLinkPage = imageFileLinkPage
        this.imageRightFileLinkPage = null
        this.isMangaDualPage = isMangaDualPage
        this.isFileLeftDualPage = isFileLeftDualPage
    }

    constructor(idFile: Long?, notLinked:Boolean, fileLinkPage: Int, fileLinkPages: Int, fileLinkPageName: String, fileLinkPagePath: String,
                isFileLeftDualPage: Boolean = false, imageFileLinkPage: Bitmap? = null
    ) : this(null, idFile, PageLinkConsts.VALUES.PAGE_EMPTY, 0, "", "", fileLinkPage, fileLinkPages,
        fileLinkPageName, fileLinkPagePath, notLinked = notLinked) {
        this.imageMangaPage = null
        this.imageLeftFileLinkPage = imageFileLinkPage
        this.imageRightFileLinkPage = null
        this.isFileLeftDualPage = isFileLeftDualPage
    }

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
    var fileLinkPage: Int = fileLinkPage
    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.FILE_LINK_PAGES)
    var fileLinkPages: Int = fileLinkPages
    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.FILE_LINK_PAGE_NAME)
    var fileLinkPageName: String = fileLinkPageName
    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.FILE_LINK_PAGE_PATH)
    var fileLinkPagePath: String = fileLinkPagePath
    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.FILE_RIGHT_LINK_PAGE)
    var fileRightLinkPage: Int = fileRightLinkPage
    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.FILE_RIGHT_LINK_PAGE_NAME)
    var fileRightLinkPageName: String = fileRightLinkPageName
    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.FILE_RIGHT_LINK_PAGE_PATH)
    var fileRightLinkPagePath: String = fileRightLinkPagePath
    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.NOT_LINKED)
    var notLinked: Boolean = notLinked
    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.DUAL_IMAGE)
    var dualImage: Boolean = dualImage
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
        this.fileLinkPage = another.fileLinkPage
        this.fileLinkPages = another.fileLinkPages
        this.fileLinkPageName = another.fileLinkPageName
        this.fileLinkPagePath = another.fileLinkPagePath
        this.fileRightLinkPage = another.fileRightLinkPage
        this.fileRightLinkPageName = another.fileRightLinkPageName
        this.fileRightLinkPagePath = another.fileRightLinkPagePath
        this.imageLeftFileLinkPage = another.imageLeftFileLinkPage
        this.imageRightFileLinkPage = another.imageRightFileLinkPage
        this.notLinked = another.notLinked
        this.dualImage = another.dualImage
        this.isFileLeftDualPage = another.isFileLeftDualPage
        this.isFileRightDualPage = another.isFileRightDualPage
    }

    fun addLeftFileLinkImage(another: PageLink) {
        this.addLeftFileLinkImage(another.fileLinkPage, another.fileLinkPages, another.fileLinkPageName,
            another.fileLinkPagePath, another.isFileLeftDualPage, another.imageLeftFileLinkPage)
    }

    fun addLeftFileLinkImage(page: Int, pages: Int, pageName: String, pagePath: String, isFileLeftDualPage: Boolean, image: Bitmap?) {
        this.fileLinkPage = page
        this.fileLinkPages = pages
        this.fileLinkPageName = pageName
        this.fileLinkPagePath = pagePath
        this.imageLeftFileLinkPage = image
        this.isFileLeftDualPage = isFileLeftDualPage
    }

    fun addRightFileLinkImage(another: PageLink) {
        this.addRightFileLinkImage(another.fileRightLinkPage, another.fileRightLinkPageName, another.fileRightLinkPagePath,
            another.isFileRightDualPage, another.imageRightFileLinkPage)
    }

    fun addRightFileLinkImage(page: Int, pageName: String, pagePath: String, isFileRightDualPage: Boolean, image: Bitmap?) {
        if (this.fileLinkPage == PageLinkConsts.VALUES.PAGE_EMPTY) {
            this.fileLinkPage = page
            this.fileLinkPageName = pageName
            this.fileLinkPagePath = pagePath
            this.imageLeftFileLinkPage = image
            this.isFileLeftDualPage = isFileRightDualPage
        } else {
            this.fileRightLinkPage = page
            this.fileRightLinkPageName = pageName
            this.fileRightLinkPagePath = pagePath
            this.imageRightFileLinkPage = image
            this.isFileRightDualPage = isFileRightDualPage
        }
        this.dualImage = this.fileRightLinkPage != PageLinkConsts.VALUES.PAGE_EMPTY
    }

    fun moveFileLinkRightToLeft() {
        this.fileLinkPage = this.fileRightLinkPage
        this.fileLinkPageName = this.fileRightLinkPageName
        this.fileLinkPagePath = this.fileRightLinkPagePath
        this.imageLeftFileLinkPage = this.imageRightFileLinkPage
        this.isFileLeftDualPage = this.isFileRightDualPage
        this.clearRightFileLink()
    }

    fun clearFileLink() {
        this.fileLinkPage = PageLinkConsts.VALUES.PAGE_EMPTY
        this.fileLinkPages = 0
        this.fileLinkPageName = ""
        this.fileLinkPagePath = ""
        this.fileRightLinkPage = PageLinkConsts.VALUES.PAGE_EMPTY
        this.fileRightLinkPageName = ""
        this.fileRightLinkPagePath = ""
        this.imageLeftFileLinkPage = null
        this.imageRightFileLinkPage = null
        this.isFileLeftDualPage = false
        this.isFileRightDualPage = false
        this.notLinked = false
        this.dualImage = false
    }

    fun clearLeftFileLink(canMoved: Boolean = false) : Boolean {
        val moved = if (canMoved && this.fileRightLinkPage != PageLinkConsts.VALUES.PAGE_EMPTY) {
            this.fileLinkPage = this.fileRightLinkPage
            this.fileLinkPageName = this.fileRightLinkPageName
            this.fileLinkPagePath = this.fileRightLinkPagePath
            this.imageLeftFileLinkPage = this.imageRightFileLinkPage
            this.isFileLeftDualPage = this.isFileRightDualPage
            this.clearRightFileLink()
            true
        } else {
            this.fileLinkPage = PageLinkConsts.VALUES.PAGE_EMPTY
            this.fileLinkPages = 0
            this.fileLinkPageName = ""
            this.fileLinkPagePath = ""
            this.imageLeftFileLinkPage = null
            this.isFileLeftDualPage = false
            false
        }
        return moved
    }

    fun clearRightFileLink() {
        this.fileRightLinkPage = PageLinkConsts.VALUES.PAGE_EMPTY
        this.fileRightLinkPageName = ""
        this.fileRightLinkPagePath = ""
        this.imageRightFileLinkPage = null
        this.dualImage = false
        this.isFileRightDualPage = false
    }
}
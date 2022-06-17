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
class PageLink(id: Long?, idFile: Long?, mangaPage: Int, mangaPages: Int, mangaPageName: String, fileLinkPage: Int = PageLinkConsts.VALUES.PAGE_EMPTY,
               fileLinkPages: Int = 0, fileLinkPageName: String = "", fileRightLinkPage: Int = PageLinkConsts.VALUES.PAGE_EMPTY,
               fileRightLinkPageName: String = "", notLinked: Boolean = false, dualImage : Boolean = false) : Serializable {

    constructor(id: Long?, idFile: Long?, mangaPage: Int, mangaPages: Int, mangaPageName: String, fileLinkPage: Int, fileLinkPages: Int,
                fileLinkPageName: String, fileRightLinkPage: Int, fileRightLinkPageName: String, notLinked: Boolean, dualImage : Boolean,
                imageMangaPage: Bitmap? = null, imageFileLinkPage: Bitmap? = null, imageRightFileLinkPage: Bitmap? = null
    ) : this(id, idFile, mangaPage, mangaPages, mangaPageName, fileLinkPage, fileLinkPages, fileLinkPageName, fileRightLinkPage,
            fileRightLinkPageName, notLinked, dualImage) {
        this.imageMangaPage = imageMangaPage
        this.imageLeftFileLinkPage = imageFileLinkPage
        this.imageRightFileLinkPage = imageRightFileLinkPage
    }

    constructor(idFile: Long?, mangaPage: Int, mangaPages: Int, fileLinkPage: Int, fileLinkPages: Int, mangaPageName: String,
                fileLinkPageName: String, imageFileLinkPage: Bitmap? = null, imageRightFileLinkPage: Bitmap? = null
    ) : this(null, idFile, mangaPage, mangaPages, mangaPageName, fileLinkPage, fileLinkPages, fileLinkPageName,
            notLinked = mangaPage == PageLinkConsts.VALUES.PAGE_EMPTY) {
        this.imageMangaPage = null
        this.imageLeftFileLinkPage = imageFileLinkPage
        this.imageRightFileLinkPage = imageRightFileLinkPage
    }

    constructor(idFile: Long?, mangaPage: Int, mangaPages: Int, mangaPageName: String, imageMangaPage: Bitmap? = null
    ) : this(null, idFile, mangaPage, mangaPages, mangaPageName) {
        this.imageMangaPage = imageMangaPage
        this.imageLeftFileLinkPage = null
        this.imageRightFileLinkPage = null
    }

    constructor(idFile: Long?, mangaPage: Int, mangaPages: Int, mangaPageName: String, fileLinkPage: Int, fileLinkPages: Int,
                fileLinkPageName: String, notLinked:Boolean, imageMangaPage: Bitmap? = null, imageFileLinkPage: Bitmap? = null
    ) : this(null, idFile, mangaPage, mangaPages, mangaPageName, fileLinkPage, fileLinkPages, fileLinkPageName, notLinked = notLinked) {
        this.imageMangaPage = imageMangaPage
        this.imageLeftFileLinkPage = imageFileLinkPage
        this.imageRightFileLinkPage = null
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
    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.FILE_LINK_PAGE)
    var fileLinkPage: Int = fileLinkPage
    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.FILE_LINK_PAGES)
    var fileLinkPages: Int = fileLinkPages
    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.FILE_LINK_PAGE_NAME)
    var fileLinkPageName: String = fileLinkPageName
    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.FILE_RIGHT_LINK_PAGE)
    var fileRightLinkPage: Int = fileRightLinkPage
    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.FILE_RIGHT_LINK_PAGE_NAME)
    var fileRightLinkPageName: String = fileRightLinkPageName
    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.NOT_LINKED)
    var notLinked: Boolean = notLinked
    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.DUAL_IMAGE)
    var dualImage: Boolean = dualImage
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
        this.fileRightLinkPage = another.fileRightLinkPage
        this.fileRightLinkPageName = another.fileRightLinkPageName
        this.imageLeftFileLinkPage = another.imageLeftFileLinkPage
        this.imageRightFileLinkPage = another.imageRightFileLinkPage
        this.notLinked = another.notLinked
        this.dualImage = another.dualImage
    }

    fun addLeftFileLinkImage(page: Int, pages: Int, pageName : String, image: Bitmap?) {
        this.fileLinkPage = page
        this.fileLinkPages = pages
        this.fileLinkPageName = pageName
        this.imageLeftFileLinkPage = image
    }

    fun addRightFileLinkImage(page: Int, pageName : String, image: Bitmap?) {
        if (this.fileLinkPage == PageLinkConsts.VALUES.PAGE_EMPTY) {
            this.fileLinkPage = page
            this.fileLinkPageName = pageName
            this.imageLeftFileLinkPage = image
        } else {
            this.fileRightLinkPage = page
            this.fileRightLinkPageName = pageName
            this.imageRightFileLinkPage = image
        }
        this.dualImage = this.fileRightLinkPage != PageLinkConsts.VALUES.PAGE_EMPTY
    }

    fun moveFileLinkRightToLeft() {
        this.fileLinkPage = this.fileRightLinkPage
        this.fileLinkPageName = this.fileRightLinkPageName
        this.imageLeftFileLinkPage = this.imageRightFileLinkPage
        this.fileRightLinkPage = PageLinkConsts.VALUES.PAGE_EMPTY
        this.fileRightLinkPageName = ""
        this.imageRightFileLinkPage = null
        this.dualImage = false
    }

    fun clearFileLink() {
        this.fileLinkPage = PageLinkConsts.VALUES.PAGE_EMPTY
        this.fileLinkPages = 0
        this.fileLinkPageName = ""
        this.fileRightLinkPage = PageLinkConsts.VALUES.PAGE_EMPTY
        this.fileRightLinkPageName = ""
        this.imageLeftFileLinkPage = null
        this.imageRightFileLinkPage = null
        this.notLinked = false
        this.dualImage = false
    }

    fun clearLeftFileLink(canMoved: Boolean = false) : Boolean {
        val moved = if (canMoved && this.fileRightLinkPage != PageLinkConsts.VALUES.PAGE_EMPTY) {
            this.fileLinkPage = this.fileRightLinkPage
            this.fileLinkPageName = this.fileRightLinkPageName
            this.imageLeftFileLinkPage = this.imageRightFileLinkPage
            this.clearRightFileLink()
            this.dualImage = false
            true
        } else {
            this.fileLinkPage = PageLinkConsts.VALUES.PAGE_EMPTY
            this.fileLinkPages = 0
            this.fileLinkPageName = ""
            this.imageLeftFileLinkPage = null
            false
        }
        return moved
    }

    fun clearRightFileLink() {
        this.fileRightLinkPage = PageLinkConsts.VALUES.PAGE_EMPTY
        this.fileRightLinkPageName = ""
        this.imageRightFileLinkPage = null
        this.dualImage = false
    }
}
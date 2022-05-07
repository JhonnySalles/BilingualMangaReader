package br.com.fenix.bilingualmangareader.model.entity

import android.graphics.Bitmap
import androidx.room.*
import br.com.fenix.bilingualmangareader.util.constants.DataBaseConsts
import java.time.LocalDateTime

@Entity(
    tableName = DataBaseConsts.PAGESLINK.TABLE_NAME,
    indices = [Index(value = [DataBaseConsts.PAGESLINK.COLUMNS.FK_ID_FILE])]
)
class PageLink(id: Long?, idFile: Long?, mangaPage: Int, mangaPages: Int, fileLinkPage: Int, fileLinkPages: Int, mangaPageName: String, fileLinkPageName: String) {

    constructor(
        id: Long?, idFile: Long?, mangaPage: Int, mangaPages: Int, fileLinkPage: Int, fileLinkPages: Int, mangaPageName: String, fileLinkPageName: String,
        imageMangaPage: Bitmap? = null, imageFileLinkPage: Bitmap? = null
    ) : this(id, idFile, mangaPage, mangaPages, fileLinkPage, fileLinkPages, mangaPageName, fileLinkPageName) {
        this.imageMangaPage = imageMangaPage
        this.imageFileLinkPage = imageFileLinkPage
    }

    constructor(idFile: Long?, mangaPage: Int, mangaPages: Int, fileLinkPage: Int, fileLinkPages: Int, mangaPageName: String,
                fileLinkPageName: String, imageFileLinkPage: Bitmap? = null
    ) : this(0L, idFile, mangaPage, mangaPages, fileLinkPage, fileLinkPages, mangaPageName, fileLinkPageName) {
        this.imageMangaPage = null
        this.imageFileLinkPage = imageFileLinkPage
    }

    constructor(idFile: Long?, mangaPage: Int, mangaPages: Int, mangaPageName: String, imageMangaPage: Bitmap? = null
    ) : this(0L, idFile, mangaPage, mangaPages, -1, 0, mangaPageName, "") {
        this.imageMangaPage = imageMangaPage
        this.imageFileLinkPage = null
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
    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.FILE_LINK_PAGE)
    var fileLinkPage: Int = fileLinkPage
    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.FILE_LINK_PAGES)
    var fileLinkPages: Int = fileLinkPages
    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.MANGA_PAGE_NAME)
    val mangaPageName: String = mangaPageName
    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.FILE_LINK_PAGE_NAME)
    var fileLinkPageName: String = fileLinkPageName
    @Ignore
    var imageMangaPage: Bitmap? = null
    @Ignore
    var imageFileLinkPage: Bitmap? = null
    @Ignore
    var isFileLinkLoading: Boolean = false

    fun clearFileLInk() {
        this.fileLinkPage = -1
        this.fileLinkPages = 0
        this.fileLinkPageName = ""
        this.imageFileLinkPage = null
    }
}
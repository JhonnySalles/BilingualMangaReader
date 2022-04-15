package br.com.fenix.bilingualmangareader.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.fenix.bilingualmangareader.util.constants.DataBaseConsts

@Entity(
    tableName = DataBaseConsts.PAGESLINK.TABLE_NAME,
    indices = [Index(value = [DataBaseConsts.PAGESLINK.COLUMNS.FK_ID_FILE])]
)
data class PageLink(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.ID)
    val id: Long,
    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.FK_ID_FILE)
    var idFile: Long?,
    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.MANGA_PAGE)
    val mangaPage: Int,
    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.MANGA_PAGES)
    val mangaPages: Int,
    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.FILE_LINK_PAGE)
    val fileLinkPage: Int,
    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.FILE_LINK_PAGES)
    val fileLinkPages: Int,
    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.MANGA_PAGE_NAME)
    val mangaPageName: String,
    @ColumnInfo(name = DataBaseConsts.PAGESLINK.COLUMNS.FILE_LINK_PAGE_NAME)
    val fileLinkPageName: String,
)
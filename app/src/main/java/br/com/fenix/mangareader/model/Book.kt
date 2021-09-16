package br.com.fenix.mangareader.model

import androidx.room.*
import br.com.fenix.mangareader.constants.DataBaseConsts
import java.io.File
import java.time.LocalDateTime

@Entity(tableName = DataBaseConsts.BOOK.TABLE_NAME, indices = [Index(value = [DataBaseConsts.BOOK.COLUMNS.FILE_NAME, DataBaseConsts.BOOK.COLUMNS.TITLE])])
class Book(Id: Long, Title: String, SubTitle: String, Archive: File, Type: String) {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DataBaseConsts.BOOK.COLUMNS.ID)
    var id: Long = Id

    @ColumnInfo(name = DataBaseConsts.BOOK.COLUMNS.TITLE)
    var title: String = Title

    @ColumnInfo(name = DataBaseConsts.BOOK.COLUMNS.SUB_TITLE)
    var subTitle: String = SubTitle

    @ColumnInfo(name = DataBaseConsts.BOOK.COLUMNS.PAGES)
    var pages: Int = 1

    @ColumnInfo(name = DataBaseConsts.BOOK.COLUMNS.BOOK_MARK)
    var bookMark: Int = 0

    @ColumnInfo(name = DataBaseConsts.BOOK.COLUMNS.FILE_TYPE)
    var type: String = Type

    @ColumnInfo(name = DataBaseConsts.BOOK.COLUMNS.FAVORITE)
    var favorite: Boolean = false

    @ColumnInfo(name = DataBaseConsts.BOOK.COLUMNS.LAST_ACCESS)
    var lastAccess: LocalDateTime? = LocalDateTime.MIN

    var tumbnail: Cover? = null

    @Ignore
    var file: File = Archive
    @Ignore
    var update: Boolean = false

    override fun toString(): String {
        return "Book(id=$id, title='$title', subTitle='$subTitle', pages=$pages, bookMark=$bookMark, type='$type', update=$update)"
    }
}
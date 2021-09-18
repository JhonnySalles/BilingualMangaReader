package br.com.fenix.mangareader.model.entity

import androidx.room.*
import br.com.fenix.mangareader.util.constants.DataBaseConsts
import java.io.File
import java.time.LocalDateTime

@Entity(
    tableName = DataBaseConsts.BOOK.TABLE_NAME,
    indices = [Index(value = [DataBaseConsts.BOOK.COLUMNS.FILE_NAME, DataBaseConsts.BOOK.COLUMNS.TITLE])]
)
class Book(id: Long?, title: String, subTitle: String, path: String, name: String, type: String) {

    constructor(
        id: Long?, title: String, subTitle: String,
        path: String, name: String, type: String, pages: Int,
        bookMark: Int, favorite: Boolean,
        dateCreate: LocalDateTime?, lastAccess: LocalDateTime?
    ) : this(id, title, subTitle, path, name, type) {
        this.pages = pages
        this.bookMark = bookMark
        this.favorite = favorite
        this.dateCreate = dateCreate
        this.lastAccess = lastAccess
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DataBaseConsts.BOOK.COLUMNS.ID)
    var id: Long? = id

    @ColumnInfo(name = DataBaseConsts.BOOK.COLUMNS.TITLE)
    var title: String = title

    @ColumnInfo(name = DataBaseConsts.BOOK.COLUMNS.SUB_TITLE)
    var subTitle: String = subTitle

    @ColumnInfo(name = DataBaseConsts.BOOK.COLUMNS.PAGES)
    var pages: Int = 1

    @ColumnInfo(name = DataBaseConsts.BOOK.COLUMNS.BOOK_MARK)
    var bookMark: Int = 0

    @ColumnInfo(name = DataBaseConsts.BOOK.COLUMNS.FILE_PATH)
    var path: String = path

    @Ignore
    var file: File? = null

    @ColumnInfo(name = DataBaseConsts.BOOK.COLUMNS.FILE_NAME)
    var name: String = name

    @ColumnInfo(name = DataBaseConsts.BOOK.COLUMNS.FILE_TYPE)
    var type: String = type

    @ColumnInfo(name = DataBaseConsts.BOOK.COLUMNS.FAVORITE)
    var favorite: Boolean = false

    @ColumnInfo(name = DataBaseConsts.BOOK.COLUMNS.DATE_CREATE)
    var dateCreate: LocalDateTime? = LocalDateTime.MIN

    @ColumnInfo(name = DataBaseConsts.BOOK.COLUMNS.LAST_ACCESS)
    var lastAccess: LocalDateTime? = LocalDateTime.MIN

    @Ignore
    var thumbnail: Cover? = null

    @Ignore
    var update: Boolean = false

    override fun toString(): String {
        return "Book(id=$id, title='$title', subTitle='$subTitle', pages=$pages, bookMark=$bookMark, type='$type', update=$update)"
    }
}
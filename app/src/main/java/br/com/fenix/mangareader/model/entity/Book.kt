package br.com.fenix.mangareader.model.entity

import androidx.room.*
import br.com.fenix.mangareader.util.constants.DataBaseConsts
import java.io.File
import java.io.Serializable
import java.time.LocalDateTime

@Entity(
    tableName = DataBaseConsts.BOOK.TABLE_NAME,
    indices = [Index(value = [DataBaseConsts.BOOK.COLUMNS.FILE_NAME, DataBaseConsts.BOOK.COLUMNS.TITLE])]
)
class Book(id: Long?, title: String, subTitle: String, path: String, folder: String, name: String, type: String, pages: Int) : Serializable {

    constructor(
        id: Long?, title: String, subTitle: String,
        path: String, folder: String, name: String, type: String,
        pages: Int, bookMark: Int, favorite: Boolean,
        dateCreate: LocalDateTime?, lastAccess: LocalDateTime?
    ) : this(id, title, subTitle, path, folder, name, type, pages) {
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
    var pages: Int = pages

    @ColumnInfo(name = DataBaseConsts.BOOK.COLUMNS.BOOK_MARK)
    var bookMark: Int = 0

    @ColumnInfo(name = DataBaseConsts.BOOK.COLUMNS.FILE_PATH)
    var path: String = path

    @Ignore
    var file: File? = File(path)

    @ColumnInfo(name = DataBaseConsts.BOOK.COLUMNS.FILE_NAME)
    var name: String = name

    @ColumnInfo(name = DataBaseConsts.BOOK.COLUMNS.FILE_TYPE)
    var type: String = type

    @ColumnInfo(name = DataBaseConsts.BOOK.COLUMNS.FILE_FOLDER)
    var folder: String = folder

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

    @Ignore
    var subTitles : List<SubTitle> = arrayListOf()

    override fun toString(): String {
        return "Book(id=$id, title='$title', subTitle='$subTitle', pages=$pages, bookMark=$bookMark, type='$type', update=$update)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Book

        if (id != other.id) return false
        if (name != other.name) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + name.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }
}
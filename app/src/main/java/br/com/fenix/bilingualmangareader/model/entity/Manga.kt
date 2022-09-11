package br.com.fenix.bilingualmangareader.model.entity

import androidx.room.*
import br.com.fenix.bilingualmangareader.util.constants.DataBaseConsts
import java.io.File
import java.io.Serializable
import java.time.LocalDateTime

@Entity(
    tableName = DataBaseConsts.MANGA.TABLE_NAME,
    indices = [Index(value = [DataBaseConsts.MANGA.COLUMNS.FILE_NAME, DataBaseConsts.MANGA.COLUMNS.TITLE])]
)
class Manga(id: Long?, title: String, subTitle: String, path: String, folder: String, name: String, type: String, pages: Int, fkLibrary: Long?) : Serializable {

    constructor(
        id: Long?, title: String, subTitle: String,
        path: String, folder: String, name: String, type: String,
        pages: Int, bookMark: Int, favorite: Boolean, hasSubtitle: Boolean,
        dateCreate: LocalDateTime?, lastAccess: LocalDateTime?,
        lastAlteration : LocalDateTime?, fkLibrary: Long?,
        sort: LocalDateTime? = null
    ) : this(id, title, subTitle, path, folder, name, type, pages, fkLibrary) {
        this.bookMark = bookMark
        this.favorite = favorite
        this.hasSubtitle = hasSubtitle
        this.dateCreate = dateCreate
        this.lastAccess = lastAccess
        this.lastAccess = lastAlteration
        this.sort = sort
    }

    constructor(
        id: Long?, title: String, subTitle: String,
        path: String, folder: String, name: String, type: String,
        pages: Int, bookMark: Int, favorite: Boolean, hasSubtitle: Boolean,
        dateCreate: LocalDateTime?, lastAccess: LocalDateTime?,
        lastAlteration: LocalDateTime?, fkLibrary: Long?,
        excluded: Boolean = false
    ) : this(id, title, subTitle, path, folder, name, type, pages, fkLibrary) {
        this.bookMark = bookMark
        this.favorite = favorite
        this.hasSubtitle = hasSubtitle
        this.dateCreate = dateCreate
        this.lastAccess = lastAccess
        this.lastAlteration = lastAlteration
        this.excluded = excluded
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DataBaseConsts.MANGA.COLUMNS.ID)
    var id: Long? = id

    @ColumnInfo(name = DataBaseConsts.MANGA.COLUMNS.TITLE)
    var title: String = title

    @ColumnInfo(name = DataBaseConsts.MANGA.COLUMNS.SUB_TITLE)
    var subTitle: String = subTitle

    @ColumnInfo(name = DataBaseConsts.MANGA.COLUMNS.PAGES)
    var pages: Int = pages

    @ColumnInfo(name = DataBaseConsts.MANGA.COLUMNS.BOOK_MARK)
    var bookMark: Int = 0

    @ColumnInfo(name = DataBaseConsts.MANGA.COLUMNS.FILE_PATH)
    var path: String = path

    @Ignore
    var fileName: String = title

    @Ignore
    var file: File = File(path)

    @ColumnInfo(name = DataBaseConsts.MANGA.COLUMNS.FILE_NAME)
    var name: String = name

    @ColumnInfo(name = DataBaseConsts.MANGA.COLUMNS.FILE_TYPE)
    var type: String = type

    @ColumnInfo(name = DataBaseConsts.MANGA.COLUMNS.FILE_FOLDER)
    var folder: String = folder

    @ColumnInfo(name = DataBaseConsts.MANGA.COLUMNS.FAVORITE)
    var favorite: Boolean = false

    @ColumnInfo(name = DataBaseConsts.MANGA.COLUMNS.HAS_SUBTITLE)
    var hasSubtitle: Boolean = false

    @ColumnInfo(name = DataBaseConsts.MANGA.COLUMNS.DATE_CREATE)
    var dateCreate: LocalDateTime? = LocalDateTime.now()

    @ColumnInfo(name = DataBaseConsts.MANGA.COLUMNS.LAST_ACCESS)
    var lastAccess: LocalDateTime? = null

    @ColumnInfo(name = DataBaseConsts.MANGA.COLUMNS.LAST_ALTERATION)
    var lastAlteration: LocalDateTime? = LocalDateTime.now()

    @ColumnInfo(name = DataBaseConsts.MANGA.COLUMNS.EXCLUDED)
    var excluded: Boolean = false

    @ColumnInfo(name = DataBaseConsts.MANGA.COLUMNS.FK_ID_LIBRARY)
    var fkLibrary: Long? = fkLibrary

    @Ignore
    var library: Library = Library(null)

    @Ignore
    var update: Boolean = false

    @Ignore
    var subTitles : List<SubTitle> = arrayListOf()

    @Ignore
    var sort: LocalDateTime? = null

    override fun toString(): String {
        return "Book(id=$id, title='$title', subTitle='$subTitle', pages=$pages, bookMark=$bookMark, type='$type', update=$update)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Manga

        if (id != other.id) return false
        if (path != other.path) return false
        if (name != other.name) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + path.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    fun update(manga: Manga) {
        this.bookMark = manga.bookMark
        this.favorite = manga.favorite
        this.lastAccess = manga.lastAccess
        this.hasSubtitle = manga.hasSubtitle
    }
}
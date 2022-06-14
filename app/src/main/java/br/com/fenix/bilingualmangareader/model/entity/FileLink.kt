package br.com.fenix.bilingualmangareader.model.entity

import androidx.room.*
import br.com.fenix.bilingualmangareader.model.enums.Languages
import br.com.fenix.bilingualmangareader.service.parses.Parse
import br.com.fenix.bilingualmangareader.service.parses.ParseFactory
import br.com.fenix.bilingualmangareader.util.constants.DataBaseConsts
import java.io.File
import java.io.Serializable
import java.time.LocalDateTime

@Entity(
    tableName = DataBaseConsts.FILELINK.TABLE_NAME,
    indices = [Index(value = [DataBaseConsts.FILELINK.COLUMNS.FK_ID_MANGA, DataBaseConsts.FILELINK.COLUMNS.FILE_NAME])]
)
class FileLink(id: Long?, idManga: Long, pages: Int, path: String, name: String, type: String, folder: String,
               language: Languages, dateCreate: LocalDateTime?, lastAccess: LocalDateTime?) : Serializable {

    constructor(
        id: Long?, idManga: Long, pages: Int, path: String, name: String, type: String, folder: String, language: Languages
    ) : this(id, idManga, pages, path, name, type, folder, language, LocalDateTime.now(), LocalDateTime.now())

    constructor(
        manga: Manga, parseManga : Parse?, pages: Int, path: String, name: String, type: String, folder: String
    ) : this(null, manga.id!!, pages, path, name, type, folder, Languages.PORTUGUESE, LocalDateTime.now(), LocalDateTime.now()) {
        this.manga = manga
        this.parseManga = parseManga
    }

    constructor( manga: Manga ) : this(null, manga.id!!, 0, "", "", "", "", Languages.PORTUGUESE,
        LocalDateTime.now(), LocalDateTime.now()) {
        this.manga = manga
    }


    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DataBaseConsts.FILELINK.COLUMNS.ID)
    var id: Long? = id

    @ColumnInfo(name = DataBaseConsts.FILELINK.COLUMNS.FK_ID_MANGA)
    var idManga: Long = idManga

    @ColumnInfo(name = DataBaseConsts.FILELINK.COLUMNS.PAGES)
    var pages: Int = pages

    @ColumnInfo(name = DataBaseConsts.FILELINK.COLUMNS.FILE_PATH)
    var path: String = path

    @ColumnInfo(name = DataBaseConsts.FILELINK.COLUMNS.FILE_NAME)
    var name: String = name

    @ColumnInfo(name = DataBaseConsts.FILELINK.COLUMNS.FILE_TYPE)
    var type: String = type

    @ColumnInfo(name = DataBaseConsts.FILELINK.COLUMNS.FILE_FOLDER)
    var folder: String = folder

    @ColumnInfo(name = DataBaseConsts.FILELINK.COLUMNS.DATE_CREATE)
    var dateCreate: LocalDateTime? = dateCreate

    @ColumnInfo(name = DataBaseConsts.FILELINK.COLUMNS.LAST_ACCESS)
    var lastAccess: LocalDateTime? = lastAccess

    @ColumnInfo(name = DataBaseConsts.FILELINK.COLUMNS.LANGUAGE)
    var language: Languages = language

    @Ignore
    var manga: Manga? = null

    @Ignore
    var file: File = File(path)

    @Ignore
    var pagesLink: List<PageLink>? = null

    @Ignore
    var pagesNotLink: List<PageLink>? = null

    @Ignore
    var parseManga = if (manga != null) ParseFactory.create(manga!!.path) else null

    @Ignore
    var parseFileLink = if (path.isNotEmpty()) ParseFactory.create(path) else null

    override fun toString(): String {
        return "FileLink(id=$id, idManga=$idManga, pages=$pages, path='$path', name='$name', type='$type', folder='$folder')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileLink

        if (id != other.id) return false
        if (idManga != other.idManga) return false
        if (pages != other.pages) return false
        if (path != other.path) return false
        if (name != other.name) return false
        if (type != other.type) return false
        if (folder != other.folder) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + idManga.hashCode()
        result = 31 * result + pages
        result = 31 * result + path.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + folder.hashCode()
        return result
    }

    fun addManga(manga : Manga) {
        idManga = manga.id?: 0
        lastAccess = LocalDateTime.now()
        this.manga = manga
    }

}
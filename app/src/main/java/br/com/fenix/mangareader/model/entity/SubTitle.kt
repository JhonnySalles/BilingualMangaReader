package br.com.fenix.mangareader.model.entity

import androidx.room.*
import br.com.fenix.mangareader.model.enums.Languages
import br.com.fenix.mangareader.util.constants.DataBaseConsts
import java.io.File
import java.util.*

@Entity(
    tableName = DataBaseConsts.SUBTITLES.TABLE_NAME,
    indices = [Index(value = [DataBaseConsts.SUBTITLES.COLUMNS.LANGUAGE, DataBaseConsts.SUBTITLES.COLUMNS.FK_ID_MANGA])]
)
data class SubTitle(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DataBaseConsts.SUBTITLES.COLUMNS.ID)
    var id: Long? = null,

    @ColumnInfo(name = DataBaseConsts.SUBTITLES.COLUMNS.FK_ID_MANGA)
    var id_manga: Long = 0,

    @ColumnInfo(name = DataBaseConsts.SUBTITLES.COLUMNS.LANGUAGE)
    var language: Languages,

    @ColumnInfo(name = DataBaseConsts.SUBTITLES.COLUMNS.CHAPTER_KEY)
    var chapterKey: String = "",

    @ColumnInfo(name = DataBaseConsts.SUBTITLES.COLUMNS.PAGE_KEY)
    var pageKey: String = "",

    @ColumnInfo(name = DataBaseConsts.SUBTITLES.COLUMNS.PAGE)
    var pageCount: Int = 0,

    @ColumnInfo(name = DataBaseConsts.SUBTITLES.COLUMNS.FILE_PATH)
    var path: String = "",

    @ColumnInfo(name = DataBaseConsts.SUBTITLES.COLUMNS.DATE_CREATE)
    var dateCreate: Date? = Date(),

    @Ignore
    var file: File = File(path),

    @Ignore
    var chapter: Chapter? = null,

    @Ignore
    var update: Boolean = false
) {

    constructor(
        id: Long? = 0,
        id_manga: Long = 0,
        language: Languages,
        chapterKey: String = "",
        pageKey: String = "",
        pageCount: Int = 0,
        path: String = "",
        dateCreate: Date? = Date(),
    ) : this(
        id, id_manga, language, chapterKey, pageKey, pageCount, path, dateCreate,
        File(path)
    )

    constructor(
        id_manga: Long = 0,
        language: Languages,
        chapterKey: String = "",
        pageKey: String = "",
        pageCount: Int = 0,
        path: String = "",
        chapter: Chapter?
    ) : this(
        null, id_manga, language, chapterKey, pageKey, pageCount, path, Date(),
        File(path)
    ) {
        this.chapter = chapter
    }

    override fun toString(): String {
        return "SubTitle(id=$id, id_manga=$id_manga, language=$language, chapterKey='$chapterKey', pageKey=$pageKey, pageCount=$pageCount, path='$path', dateCreate=$dateCreate, update=$update)"
    }

}
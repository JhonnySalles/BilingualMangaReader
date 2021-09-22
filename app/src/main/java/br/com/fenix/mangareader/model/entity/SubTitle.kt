package br.com.fenix.mangareader.model.entity

import androidx.room.*
import br.com.fenix.mangareader.model.enums.Languages
import br.com.fenix.mangareader.util.constants.DataBaseConsts
import java.io.File
import java.time.LocalDateTime

@Entity(
    tableName = DataBaseConsts.SUBTITLES.TABLE_NAME,
    indices = [Index(value = [DataBaseConsts.SUBTITLES.COLUMNS.FILE_NAME])]
)
data class SubTitle(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DataBaseConsts.SUBTITLES.COLUMNS.ID)
    var id: Long = 0,

    @ColumnInfo(name = DataBaseConsts.SUBTITLES.COLUMNS.FK_ID_MANGA)
    var id_manga: Long = 0,

    @ColumnInfo(name = DataBaseConsts.SUBTITLES.COLUMNS.LANGUAGE)
    var language: Languages,

    @ColumnInfo(name = DataBaseConsts.SUBTITLES.COLUMNS.IN_ROOT)
    var inRoot: Boolean = false,

    @ColumnInfo(name = DataBaseConsts.SUBTITLES.COLUMNS.VOLUME)
    var isVolume: Boolean = false,

    @ColumnInfo(name = DataBaseConsts.SUBTITLES.COLUMNS.CHAPTER)
    var isChapter: Boolean = false,

    @ColumnInfo(name = DataBaseConsts.SUBTITLES.COLUMNS.FILE_PATH)
    var path: String = "",

    @ColumnInfo(name = DataBaseConsts.SUBTITLES.COLUMNS.FILE_NAME)
    var name: String = "",

    @ColumnInfo(name = DataBaseConsts.SUBTITLES.COLUMNS.FILE_FOLDER)
    var folder: String = "",

    @ColumnInfo(name = DataBaseConsts.SUBTITLES.COLUMNS.DATE_CREATE)
    var dateCreate: LocalDateTime? = LocalDateTime.MIN,

    @Ignore
    var file: File = File(path),

    @Ignore
    var volume: Volume? = null,

    @Ignore
    var chapter: Chapter? = null,

    @Ignore
    var update: Boolean = false
) {

    constructor(
        id: Long = 0,
        id_manga: Long = 0,
        language: Languages,
        inRoot: Boolean = false,
        isVolume: Boolean = false,
        isChapter: Boolean = false,
        path: String = "",
        name: String = "",
        folder: String = "",
        dateCreate: LocalDateTime? = LocalDateTime.MIN,
       ) : this(id, id_manga, language, inRoot, isVolume, isChapter, path, name, folder, dateCreate,
    File(path)) {
    }

    override fun toString(): String {
        return "SubTitle(id=$id, id_manga=$id_manga, language=$language, inRoot=$inRoot, isVolume=$isVolume, isChapter=$isChapter, path='$path', name='$name', folder='$folder', dateCreate=$dateCreate, update=$update)"
    }
}
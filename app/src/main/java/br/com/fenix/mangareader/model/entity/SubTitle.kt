package br.com.fenix.mangareader.model.entity

import androidx.room.*
import br.com.fenix.mangareader.util.constants.DataBaseConsts
import org.intellij.lang.annotations.Language
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

    @ColumnInfo(name = DataBaseConsts.SUBTITLES.COLUMNS.FK_ID_BOOK)
    var id_book: Long = 0,

    @ColumnInfo(name = DataBaseConsts.SUBTITLES.COLUMNS.LANGUAGE)
    var language: Language,

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
    var volume: Volume,

    @Ignore
    var chapter: Chapter,

    @Ignore
    var update: Boolean = false
) {

    override fun toString(): String {
        return "SubTitle(id=$id, id_book=$id_book, language=$language, inRoot=$inRoot, isVolume=$isVolume, isChapter=$isChapter, path='$path', name='$name', folder='$folder', dateCreate=$dateCreate, update=$update)"
    }
}
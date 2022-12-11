package br.com.fenix.bilingualmangareader.model.entity

import androidx.room.*
import br.com.fenix.bilingualmangareader.util.constants.DataBaseConsts

@Entity(
    tableName = DataBaseConsts.MANGA_VOCABULARY.TABLE_NAME,
    foreignKeys = [ForeignKey(
        entity = Manga::class,
        parentColumns = arrayOf(DataBaseConsts.MANGA.COLUMNS.ID),
        childColumns = arrayOf(DataBaseConsts.MANGA_VOCABULARY.COLUMNS.ID_MANGA),
        onDelete = ForeignKey.NO_ACTION,
        onUpdate = ForeignKey.NO_ACTION
    ), ForeignKey(
        entity = Vocabulary::class,
        parentColumns = arrayOf(DataBaseConsts.VOCABULARY.COLUMNS.ID),
        childColumns = arrayOf(DataBaseConsts.MANGA_VOCABULARY.COLUMNS.ID_VOCABULARY),
        onDelete = ForeignKey.NO_ACTION,
        onUpdate = ForeignKey.NO_ACTION
    )]
)
data class VocabularyManga(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DataBaseConsts.MANGA_VOCABULARY.COLUMNS.ID)
    var id: Long?,
    @ColumnInfo(name = DataBaseConsts.MANGA_VOCABULARY.COLUMNS.ID_VOCABULARY)
    val idVocabulary: Long,
    @ColumnInfo(name = DataBaseConsts.MANGA_VOCABULARY.COLUMNS.ID_MANGA)
    val idManga: Long,
    @ColumnInfo(name = DataBaseConsts.MANGA_VOCABULARY.COLUMNS.APPEARS)
    var appears: Int
) {
    @Ignore
    var manga: Manga? = null
}
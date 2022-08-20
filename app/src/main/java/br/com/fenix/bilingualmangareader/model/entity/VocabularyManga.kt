package br.com.fenix.bilingualmangareader.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import br.com.fenix.bilingualmangareader.util.constants.DataBaseConsts

@Entity(
    tableName = DataBaseConsts.MANGA_VOCABULARY.TABLE_NAME
)
data class VocabularyManga(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DataBaseConsts.MANGA_VOCABULARY.COLUMNS.ID)
    var id: Long?,
    @ColumnInfo(name = DataBaseConsts.MANGA_VOCABULARY.COLUMNS.ID_VOCABULARY)
    val idVocabulary: Long,
    @ColumnInfo(name = DataBaseConsts.MANGA_VOCABULARY.COLUMNS.ID_MANGA)
    val idManga: Long
) {

    @Ignore
    var appears: Int = 1

    @Ignore
    var manga: Manga? = null
}
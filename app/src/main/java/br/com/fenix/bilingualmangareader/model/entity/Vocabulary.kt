package br.com.fenix.bilingualmangareader.model.entity

import androidx.room.*
import br.com.fenix.bilingualmangareader.util.constants.DataBaseConsts
import com.google.gson.annotations.SerializedName

@Entity(
    tableName = DataBaseConsts.VOCABULARY.TABLE_NAME,
    indices = [Index(value = [DataBaseConsts.VOCABULARY.COLUMNS.WORD, DataBaseConsts.VOCABULARY.COLUMNS.BASIC_FORM])]
)
data class Vocabulary(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DataBaseConsts.VOCABULARY.COLUMNS.ID)
    var id: Long?,

    @ColumnInfo(name = DataBaseConsts.VOCABULARY.COLUMNS.WORD)
    @SerializedName("palavra")
    val word: String,

    @ColumnInfo(name = DataBaseConsts.VOCABULARY.COLUMNS.MEANING)
    @SerializedName("significado")
    val meaning: String,

    @ColumnInfo(name = DataBaseConsts.VOCABULARY.COLUMNS.READING)
    @SerializedName("leitura")
    val reading: String,

    @ColumnInfo(name = DataBaseConsts.VOCABULARY.COLUMNS.BASIC_FORM)
    val basicForm: String?,

    @ColumnInfo(name = DataBaseConsts.VOCABULARY.COLUMNS.REVISED)
    @SerializedName("revisado")
    val revised: Boolean,

    @ColumnInfo(name = DataBaseConsts.VOCABULARY.COLUMNS.FAVORITE)
    var favorite: Boolean
) {

    @Ignore
    var vocabularyMangas: List<VocabularyManga> = listOf()

    @Ignore
    var appears: Int = 0
}
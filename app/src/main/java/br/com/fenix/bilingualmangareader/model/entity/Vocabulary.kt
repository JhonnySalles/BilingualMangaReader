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

    @ColumnInfo(name = DataBaseConsts.VOCABULARY.COLUMNS.PORTUGUESE)
    @SerializedName("portugues")
    val portuguese: String?,

    @ColumnInfo(name = DataBaseConsts.VOCABULARY.COLUMNS.ENGLISH)
    @SerializedName("ingles")
    val english: String?,

    @ColumnInfo(name = DataBaseConsts.VOCABULARY.COLUMNS.READING)
    @SerializedName("leitura")
    val reading: String?,

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vocabulary

        if (id != other.id) return false
        if (word != other.word) return false
        if (basicForm != other.basicForm) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + word.hashCode()
        result = 31 * result + (basicForm?.hashCode() ?: 0)
        return result
    }
}
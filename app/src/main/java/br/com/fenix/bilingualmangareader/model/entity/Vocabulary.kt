package br.com.fenix.bilingualmangareader.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
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
    val revised: Boolean
)
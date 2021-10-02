package br.com.fenix.mangareader.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.fenix.mangareader.util.constants.DataBaseConsts

@Entity(
    tableName = DataBaseConsts.JLPT.TABLE_NAME,
    indices = [Index(value = [DataBaseConsts.JLPT.COLUMNS.KANJI, DataBaseConsts.JLPT.COLUMNS.LEVEL])]
)
data class KanjiJLPT(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DataBaseConsts.JLPT.COLUMNS.ID)
    var id: Long? = null,

    @ColumnInfo(name = DataBaseConsts.JLPT.COLUMNS.KANJI)
    var kanji: String,

    @ColumnInfo(name = DataBaseConsts.JLPT.COLUMNS.LEVEL)
    var level: Int
) {
    override fun toString(): String {
        return "KanjiJLPT(id=$id, kanji='$kanji', leve='$level')"
    }
}
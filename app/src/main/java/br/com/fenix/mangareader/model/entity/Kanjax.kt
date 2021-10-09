package br.com.fenix.mangareader.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.fenix.mangareader.util.constants.DataBaseConsts

@Entity(
    tableName = DataBaseConsts.KANJAX.TABLE_NAME,
    indices = [Index(value = [DataBaseConsts.KANJAX.COLUMNS.ID, DataBaseConsts.KANJAX.COLUMNS.KANJI, DataBaseConsts.KANJAX.COLUMNS.KEYWORD, DataBaseConsts.KANJAX.COLUMNS.KEYWORDS_PT])]
)
data class Kanjax(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DataBaseConsts.KANJAX.COLUMNS.ID)
    var id: Long? = null,

    @ColumnInfo(name = DataBaseConsts.KANJAX.COLUMNS.KANJI)
    var kanji: String,

    @ColumnInfo(name = DataBaseConsts.KANJAX.COLUMNS.KEYWORD)
    var keyword: String,

    @ColumnInfo(name = DataBaseConsts.KANJAX.COLUMNS.MEANING)
    var meaning: String,

    @ColumnInfo(name = DataBaseConsts.KANJAX.COLUMNS.KOOHII)
    var koohii: String,

    @ColumnInfo(name = DataBaseConsts.KANJAX.COLUMNS.KOOHII2)
    var koohii2: String,

    @ColumnInfo(name = DataBaseConsts.KANJAX.COLUMNS.ONYOMI)
    var onYomi: String,

    @ColumnInfo(name = DataBaseConsts.KANJAX.COLUMNS.KUNYOMI)
    var kunYomi: String,

    @ColumnInfo(name = DataBaseConsts.KANJAX.COLUMNS.ONWORDS)
    var onWords: String,

    @ColumnInfo(name = DataBaseConsts.KANJAX.COLUMNS.KUNWORDS)
    var kunWords: String,

    @ColumnInfo(name = DataBaseConsts.KANJAX.COLUMNS.JLPT)
    var jlpt: Int,

    @ColumnInfo(name = DataBaseConsts.KANJAX.COLUMNS.GRADE)
    var grade: Int,

    @ColumnInfo(name = DataBaseConsts.KANJAX.COLUMNS.FREQUENCE)
    var frequence: Int,

    @ColumnInfo(name = DataBaseConsts.KANJAX.COLUMNS.STROKES)
    var strokes: Int,

    @ColumnInfo(name = DataBaseConsts.KANJAX.COLUMNS.VARIANTS)
    var variants: String,

    @ColumnInfo(name = DataBaseConsts.KANJAX.COLUMNS.RADICAL)
    var radical: String,

    @ColumnInfo(name = DataBaseConsts.KANJAX.COLUMNS.PARTS)
    var parts: String,

    @ColumnInfo(name = DataBaseConsts.KANJAX.COLUMNS.UTF8)
    var utf8: String,

    @ColumnInfo(name = DataBaseConsts.KANJAX.COLUMNS.SJIS)
    var sjis: String,

    @ColumnInfo(name = DataBaseConsts.KANJAX.COLUMNS.KEYWORDS_PT)
    var keywordPt: String,

    @ColumnInfo(name = DataBaseConsts.KANJAX.COLUMNS.MEANING_PT)
    var meaningPt: String,

    ) {

    override fun toString(): String {
        return "Kanjax(id=$id, kanji='$kanji', keyword='$keyword', meaning='$meaning', onYomi='$onYomi', kunYomi='$kunYomi', jlpt=$jlpt, utf8='$utf8', sjis='$sjis', keywordPt='$keywordPt', meaningPt='$meaningPt')"
    }
}
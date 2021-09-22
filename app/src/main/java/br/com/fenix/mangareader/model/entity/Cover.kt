package br.com.fenix.mangareader.model.entity

import android.graphics.Bitmap
import androidx.room.*
import br.com.fenix.mangareader.util.constants.DataBaseConsts

@Entity(
    tableName = DataBaseConsts.COVER.TABLE_NAME,
    indices = [Index(value = [DataBaseConsts.COVER.COLUMNS.NAME])]
)
data class Cover(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DataBaseConsts.COVER.COLUMNS.ID)
    var id: Long = 0,

    @ColumnInfo(name = DataBaseConsts.COVER.COLUMNS.FK_ID_MANGA)
    var id_manga: Long = 0,

    @ColumnInfo(name = DataBaseConsts.COVER.COLUMNS.NAME)
    var name: String = "",

    @ColumnInfo(name = DataBaseConsts.COVER.COLUMNS.SIZE)
    var size: Int = 0,

    @ColumnInfo(name = DataBaseConsts.COVER.COLUMNS.TYPE)
    var type: String = "",

    @ColumnInfo(name = DataBaseConsts.COVER.COLUMNS.IMAGE)
    var image: Bitmap? = null,

    @Ignore
    var update: Boolean = false
) {

    override fun toString(): String {
        return "Cover(id=$id, name='$name', size=$size, type='$type', update=$update)"
    }
}
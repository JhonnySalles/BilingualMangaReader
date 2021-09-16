package br.com.fenix.mangareader.common

import android.graphics.Bitmap
import androidx.room.TypeConverter
import br.com.fenix.mangareader.common.Util
import java.time.LocalDateTime

class Converters {

    @TypeConverter
    fun fromBase64(image: String): Bitmap {
        return Util.decodeImageBase64(image)
    }

    @TypeConverter
    fun bitmapToBase64(image: Bitmap): String {
        return Util.encodeImageBase64(image)
    }

    @TypeConverter
    fun fromLocalDateTime(dateTime: String): LocalDateTime {
        return LocalDateTime.parse(dateTime)
    }

    @TypeConverter
    fun localDateTimeToString(dateTime: LocalDateTime): String {
        return dateTime.toString()
    }

}
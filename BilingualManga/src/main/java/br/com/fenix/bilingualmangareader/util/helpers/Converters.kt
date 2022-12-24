package br.com.fenix.bilingualmangareader.util.helpers

import android.graphics.Bitmap
import androidx.room.TypeConverter
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import java.text.SimpleDateFormat
import java.util.*

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
    fun fromLocalDateTime(dateTime: String?): Date? {
        if (dateTime == null)
            return null
        return SimpleDateFormat(GeneralConsts.PATTERNS.DATE_TIME_PATTERN, Locale.getDefault()).parse(dateTime)
    }

    @TypeConverter
    fun localDateTimeToString(dateTime: Date?): String? {
        if (dateTime == null)
            return null

        return SimpleDateFormat(GeneralConsts.PATTERNS.DATE_TIME_PATTERN, Locale.getDefault()).format(dateTime)
    }

}
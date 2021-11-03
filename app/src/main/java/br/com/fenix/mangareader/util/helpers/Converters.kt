package br.com.fenix.mangareader.util.helpers

import android.graphics.Bitmap
import androidx.room.TypeConverter
import br.com.fenix.mangareader.util.constants.GeneralConsts
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
    fun fromDate(dateTime: String?): Date? {
        if (dateTime == null)
            return null
        return SimpleDateFormat(GeneralConsts.DEFAULT.DATE_TIME_PATTERN, Locale.getDefault()).parse(dateTime)
    }

    @TypeConverter
    fun localDateToString(dateTime: Date?): String? {
        if (dateTime == null)
            return null
        return SimpleDateFormat(GeneralConsts.DEFAULT.DATE_TIME_PATTERN, Locale.getDefault()).format(dateTime);
    }

}
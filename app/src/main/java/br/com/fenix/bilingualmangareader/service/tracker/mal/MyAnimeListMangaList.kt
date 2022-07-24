package br.com.fenix.bilingualmangareader.service.tracker.mal

import com.google.gson.annotations.SerializedName
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

data class MalMangaList(
    @SerializedName("data")
    val data: List<Node>,
    @SerializedName("padding")
    val padding: Padding,
)

data class Padding(
    @SerializedName("previous")
    val previous: String,
    @SerializedName("next")
    val next: String
)

data class Node(
    @SerializedName("node")
    val item: MalMangaDetail
)

class MalTransform {
    companion object {
        fun getList(list: List<Node>): List<MalMangaDetail> =
            list.map { it.item }

        private val formatDates = listOf("yyyy-MM-dd", "yyyy-MM", "yyyy")
        fun getDate(date: String): Date? {
            for (format in formatDates) {
                try {
                    return SimpleDateFormat(format).parse(date)
                } catch (e: ParseException) {
                }
            }
            return null
        }
    }
}


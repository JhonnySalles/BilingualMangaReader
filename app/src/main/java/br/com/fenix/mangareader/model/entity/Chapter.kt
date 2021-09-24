package br.com.fenix.mangareader.model.entity

import br.com.fenix.mangareader.model.enums.Languages
import com.google.gson.annotations.SerializedName
import org.intellij.lang.annotations.Language

data class Chapter(
    @SerializedName("manga")
    var manga: String,
    @SerializedName("volume")
    var volume: Float,
    @SerializedName("capitulo")
    val chapter: Float,
    @SerializedName("lingua")
    var language: Languages,
    val scan: String,
    @SerializedName("paginas")
    val pages: List<Pages>,
    val extra: Boolean,
    val raw: Boolean,
    @SerializedName("vocabulario")
    val vocabulary: MutableSet<Vocabulary>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Chapter

        if (manga != other.manga) return false
        if (volume != other.volume) return false
        if (chapter != other.chapter) return false
        if (language != other.language) return false
        if (scan != other.scan) return false
        if (extra != other.extra) return false
        if (raw != other.raw) return false

        return true
    }

    override fun hashCode(): Int {
        var result = manga.hashCode()
        result = 31 * result + volume.hashCode()
        result = 31 * result + chapter.hashCode()
        result = 31 * result + language.hashCode()
        result = 31 * result + scan.hashCode()
        result = 31 * result + extra.hashCode()
        result = 31 * result + raw.hashCode()
        return result
    }
}

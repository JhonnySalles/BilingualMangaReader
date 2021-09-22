package br.com.fenix.mangareader.model.entity

import br.com.fenix.mangareader.model.enums.Languages

data class Volume(
    var manga: String,
    val volume: Int,
    val language: Languages,
    val chapters: List<Chapter>,
    val vocabulary: MutableSet<Vocabulary?>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Volume

        if (manga != other.manga) return false
        if (volume != other.volume) return false
        if (language != other.language) return false
        if (chapters != other.chapters) return false

        return true
    }

    override fun hashCode(): Int {
        var result = manga.hashCode()
        result = 31 * result + volume
        result = 31 * result + language.hashCode()
        result = 31 * result + chapters.hashCode()
        return result
    }
}


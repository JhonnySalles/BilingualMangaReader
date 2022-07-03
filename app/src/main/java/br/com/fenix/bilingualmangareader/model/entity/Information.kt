package br.com.fenix.bilingualmangareader.model.entity

import com.kttdevelopment.mal4j.manga.MangaPreview
import java.util.*

class Information(
    link: String, imageLink: String?, title: String, alternativeTitles: String, synopsis: String, synonyms: String, volumes: String,
    chapters: String, status: String, startDate: Date?, endDate: Date?, genres: String, authors: String
) {
    companion object {
        val MY_ANIME_LIST = "MyAnimeList"
    }

    constructor() : this("", null, "", "", "", "", "", "", "", null, null, "", "")

    constructor(manga: MangaPreview) : this() {
        setManga(manga)
    }

    var link: String = link
    var imageLink: String? = imageLink
    var title: String = title
    var alternativeTitles: String = alternativeTitles
    var synopsis: String = synopsis
    var synonyms: String = synonyms
    var volumes: String = volumes
    var chapters: String = chapters
    var status: String = status
    var startDate: Date? = startDate
    var endDate: Date? = endDate
    var genres: String = genres
    var authors: String = authors
    var origin: String = ""

    fun setManga(manga: MangaPreview) {
        this.link = "https://myanimelist.net/manga/" + manga.id
        this.imageLink = manga.mainPicture.mediumURL
        this.title = manga.title
        this.alternativeTitles = ""
        if (manga.alternativeTitles.english.isNotEmpty())
            this.alternativeTitles += manga.alternativeTitles.english + ", "
        if (manga.alternativeTitles.japanese.isNotEmpty())
            this.alternativeTitles += manga.alternativeTitles.japanese + ", "
        if (manga.alternativeTitles.synonyms.isNotEmpty())
            this.alternativeTitles += manga.alternativeTitles.synonyms + ", "

        this.alternativeTitles = this.alternativeTitles.substringBeforeLast(",").plus(".")

        this.synopsis = manga.synopsis
        this.synonyms = manga.synopsis
        this.volumes = manga.volumes.toString()
        this.chapters = manga.chapters.toString()
        this.status = manga.status.name
        this.startDate = manga.startDate
        this.endDate = manga.endDate
        this.genres = manga.genres.joinToString { it.name }
        this.authors = manga.authors.joinToString { it.firstName + " " + it.lastName + "(" + it.role + ")" }
        this.origin = MY_ANIME_LIST
    }
}
package br.com.fenix.bilingualmangareader.service.tracker.mal

import com.google.gson.annotations.SerializedName

enum class NSFW {
    @SerializedName("white")
    WHITE,

    @SerializedName("gray")
    GRAY,

    @SerializedName("black")
    BLACK
}

enum class STATUS {
    @SerializedName("finished")
    FINISHED,

    @SerializedName("currently_publishing")
    CURRENTLY_PUBLISHING,

    @SerializedName("not_yet_published")
    NOT_YET_PUBLISHED
}

enum class MEDIA {
    @SerializedName("unknown")
    UNKNOWN,

    @SerializedName("manga")
    MANGA,

    @SerializedName("novel")
    NOVEL,

    @SerializedName("one_shot")
    ONE_SHOT,

    @SerializedName("doujinshi")
    DOUJINSHI,

    @SerializedName("manhwa")
    MANHWA,

    @SerializedName("manhua")
    MANHUA,

    @SerializedName("oel")
    OEL,
}

enum class RANKING {
    @SerializedName("all")
    ALL,

    @SerializedName("manga")
    MANGA,

    @SerializedName("novels")
    NOVELS,

    @SerializedName("oneshots")
    ONESHOTS,

    @SerializedName("doujin")
    DOUJIN,

    @SerializedName("manhwa")
    MANHWA,

    @SerializedName("manhua")
    MANHUA,

    @SerializedName("bypopularity")
    BYPOPULARITY,

    @SerializedName("favorite")
    FAVORITE
}


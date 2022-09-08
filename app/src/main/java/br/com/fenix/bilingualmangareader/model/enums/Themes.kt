package br.com.fenix.bilingualmangareader.model.enums

import br.com.fenix.bilingualmangareader.R

enum class Themes(private val value: Int) {
    ORIGINAL(R.style.Theme_MangaReader),
    BLOOD_RED(R.style.Theme_MangaReader_BloodRed),
    BLUE(R.style.Theme_MangaReader_Blue),
    FOREST_GREEN(R.style.Theme_MangaReader_ForestGreen),
    GREEN(R.style.Theme_MangaReader_Green),
    NEON_BLUE(R.style.Theme_MangaReader_NeonBlue),
    NEON_GREEN(R.style.Theme_MangaReader_NeonGreen),
    OCEAN_BLUE(R.style.Theme_MangaReader_OceanBlue),
    PINK(R.style.Theme_MangaReader_Pink),
    RED(R.style.Theme_MangaReader_Red);

    open fun getValue() : Int = this.value

}
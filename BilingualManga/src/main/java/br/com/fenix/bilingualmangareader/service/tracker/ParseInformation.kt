package br.com.fenix.bilingualmangareader.service.tracker

import br.com.fenix.bilingualmangareader.model.entity.Information
import br.com.fenix.bilingualmangareader.service.tracker.mal.MalMangaDetail

class ParseInformation {
    companion object {
        fun <T> getInformation(list: List<T>): MutableList<Information> {
            val newList = mutableListOf<Information>()
            for (item in list)
                newList.add(getInformation(item))

            newList.removeIf {
                it.title.isEmpty()
            }

            return newList
        }

        fun <T> getInformation(item: T): Information {
            return when (item) {
                is MalMangaDetail -> Information(item as MalMangaDetail)
                else -> Information()
            }
        }

        fun getInformation(item: MalMangaDetail): Information {
            return Information(item)
        }
    }
}
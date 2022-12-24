package br.com.fenix.bilingualmangareader.utils

import android.content.Context
import android.graphics.BitmapFactory
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.service.controller.ImageCoverController
import br.com.fenix.bilingualmangareader.service.parses.ParseFactory
import br.com.fenix.bilingualmangareader.service.parses.RarParse
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.util.helpers.Util
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class TestUtils {

    companion object TestUtils {

        private val TEST_FILE_PATH = "storage/emulated/0/Manga/Manga of test.cbr"
        private val DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss"

        private fun getCoverMipmap(index: Int = 0): Int {
            return if (index > 5 || index < 0)
                getCoverMipmap((0..5).random())
            else when (index) {
                1 -> R.mipmap.book_cover_1
                2 -> R.mipmap.book_cover_2
                3 -> R.mipmap.book_cover_3
                4 -> R.mipmap.book_cover_4
                5 -> R.mipmap.book_cover_5
                else -> R.mipmap.book_cover_1
            }
        }

        fun generateCovers(context: Context, mangas: ArrayList<Manga>) {
            for ((index, manga) in mangas.withIndex())
                generateCovers(context, manga, index)
        }

        fun generateCovers(context: Context, manga: Manga, index: Int = 0) {
            val minMap = getCoverMipmap(index)
            val cover = BitmapFactory.decodeResource(context.resources, minMap)
            ImageCoverController.instance.saveCoverToCache(context, manga, cover)
        }

        fun getManga(context: Context, filePath: String = ""): Manga {
            val mangaPath = filePath.ifEmpty { TEST_FILE_PATH }
            val manga = Manga(
                1,
                Util.getNameFromPath(mangaPath),
                "",
                mangaPath,
                Util.getFolderFromPath(mangaPath),
                Util.getNameWithoutExtensionFromPath(mangaPath),
                Util.getExtensionFromPath(mangaPath),
                10,
                5,
                (1..2).random() > 1,
                SimpleDateFormat(DATE_TIME_PATTERN, Locale.getDefault()).parse("29/06/2022 00:41:00"),
                Date(),
                Date(),
                GeneralConsts.KEYS.LIBRARY.DEFAULT,
                (1..5).random() > 2
            )

            if (filePath.isNotEmpty()) {
                val parse = ParseFactory.create(filePath) ?: return manga
                try {
                    if (parse is RarParse) {
                        val folder = GeneralConsts.CACHE_FOLDER.LINKED + '/' + Util.normalizeNameCache(manga.name)
                        val cacheDir = File(GeneralConsts.getCacheDir(context), folder)
                        (parse as RarParse?)!!.setCacheDirectory(cacheDir)
                    }

                    manga.pages = parse.numPages()
                    ImageCoverController.instance.getCoverFromFile(context, manga.file, parse)
                } finally {
                    Util.destroyParse(parse)
                }
            } else
                generateCovers(context, manga)

            return manga
        }

        fun getManga(): Manga {
            return Manga(
                1,
                Util.getNameFromPath(TEST_FILE_PATH),
                "",
                TEST_FILE_PATH,
                Util.getFolderFromPath(TEST_FILE_PATH),
                Util.getNameWithoutExtensionFromPath(TEST_FILE_PATH),
                Util.getExtensionFromPath(TEST_FILE_PATH),
                10,
                5,
                (1..2).random() > 1,
                SimpleDateFormat(DATE_TIME_PATTERN, Locale.getDefault()).parse("29/06/2022 00:41:00"),
                Date(),
                Date(),
                GeneralConsts.KEYS.LIBRARY.DEFAULT,
                (1..5).random() > 2
            )
        }

        fun getArrayMangas(context: Context): ArrayList<Manga> {
            val mangas = getArrayMangas()
            generateCovers(context, mangas)
            return mangas
        }

        fun getArrayMangas(): ArrayList<Manga> {
            val array = arrayListOf<Manga>()

            for (i in 0 until 10)
                array.add(
                    Manga(
                        i.toLong(),
                        Util.getNameFromPath(TEST_FILE_PATH),
                        "",
                        TEST_FILE_PATH,
                        Util.getFolderFromPath(TEST_FILE_PATH),
                        Util.getNameWithoutExtensionFromPath(TEST_FILE_PATH),
                        Util.getExtensionFromPath(TEST_FILE_PATH),
                        125,
                        (0..100).random(),
                        i in 2..5,
                        SimpleDateFormat(DATE_TIME_PATTERN, Locale.getDefault()).parse("29/06/2022 00:41:00"),
                        Date(),
                        Date(),
                        GeneralConsts.KEYS.LIBRARY.DEFAULT,
                        i in 4..6
                    )
                )

            return array
        }

    }
}
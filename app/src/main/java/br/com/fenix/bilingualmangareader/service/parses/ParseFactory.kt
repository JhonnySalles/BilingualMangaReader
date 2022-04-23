package br.com.fenix.bilingualmangareader.service.parses

import android.util.Log
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.util.helpers.Util
import java.io.File
import java.io.IOException
import java.util.*

class ParseFactory {

    companion object Factory {
        fun create(file: String): Parse? {
            return create(File(file))
        }

        fun create(file: File): Parse? {
            var parser: Parse? = null
            val fileName = file.absolutePath.lowercase(Locale.getDefault())
            if (file.isDirectory)
                parser = DirectoryParse()

            when {
                Util.isZip(fileName) -> parser = ZipParse()
                Util.isRar(fileName) -> parser = RarParse()
                Util.isTarball(fileName) -> parser = TarParse()
                Util.isSevenZ(fileName) -> parser = SevenZipParse()
            }

            return tryParse(parser, file)
        }

        private fun tryParse(parser: Parse?, file: File): Parse? {
            if (parser == null)
                return null

            try {
                parser.parse(file)
            } catch (e: Exception) {
                Log.e(GeneralConsts.TAG.LOG, "Error when parse: " + e.message)
                return null
            }
            return if (parser is DirectoryParse && parser.numPages() < 4) null else parser
        }
    }
}
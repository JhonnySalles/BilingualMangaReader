package br.com.fenix.bilingualmangareader.service.parses

import br.com.fenix.bilingualmangareader.util.helpers.Util
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

class ParseFactory {

    companion object Factory {
        private val mLOGGER = LoggerFactory.getLogger(ParseFactory::class.java)
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
                mLOGGER.warn("Error when parse: " + e.message + " - File: " + file.name, e)
                return null
            }
            return if (parser is DirectoryParse && parser.numPages() < 4) null else parser
        }
    }
}
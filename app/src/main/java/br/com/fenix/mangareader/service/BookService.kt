package br.com.fenix.mangareader.service

import android.app.Application
import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import br.com.fenix.mangareader.constants.DataBaseConsts
import br.com.fenix.mangareader.constants.GeneralConsts
import br.com.fenix.mangareader.model.Book
import br.com.fenix.mangareader.model.Cover
import java.io.File

class BookService(application: Application) : BaseService<Book>(application) {

    private val coverService: CoverService = CoverService(application)

    private fun getContents(book: Book): ContentValues {
        val contentsValues = ContentValues()
        contentsValues.put(DataBaseConsts.BOOK.COLUMNS.TITLE, book.title)
        contentsValues.put(DataBaseConsts.BOOK.COLUMNS.SUB_TITLE, book.subTitle)
        contentsValues.put(DataBaseConsts.BOOK.COLUMNS.PAGES, book.pages)
        contentsValues.put(DataBaseConsts.BOOK.COLUMNS.BOOK_MARK, book.bookMark)
        contentsValues.put(DataBaseConsts.BOOK.COLUMNS.FILE_PATH, book.file.absolutePath)
        contentsValues.put(DataBaseConsts.BOOK.COLUMNS.FILE_NAME, book.file.name)
        contentsValues.put(DataBaseConsts.BOOK.COLUMNS.FILE_TYPE, book.file.extension)
        contentsValues.put(DataBaseConsts.BOOK.COLUMNS.FAVORITE, book.favorite)
        contentsValues.put(DataBaseConsts.BOOK.COLUMNS.DATE_CREATE, book.title)
        return contentsValues
    }

    private fun getProjection(): Array<String> {
        return arrayOf(
            DataBaseConsts.BOOK.COLUMNS.ID,
            DataBaseConsts.BOOK.COLUMNS.TITLE,
            DataBaseConsts.BOOK.COLUMNS.SUB_TITLE,
            DataBaseConsts.BOOK.COLUMNS.PAGES,
            DataBaseConsts.BOOK.COLUMNS.BOOK_MARK,
            DataBaseConsts.BOOK.COLUMNS.FILE_PATH,
            DataBaseConsts.BOOK.COLUMNS.FILE_NAME,
            DataBaseConsts.BOOK.COLUMNS.FILE_TYPE,
            DataBaseConsts.BOOK.COLUMNS.FAVORITE,
            DataBaseConsts.BOOK.COLUMNS.DATE_CREATE
        )
    }

    private fun parseToBook(cursor: Cursor): Book {
        val archive =
            File(cursor.getString(cursor.getColumnIndexOrThrow(DataBaseConsts.BOOK.COLUMNS.FILE_PATH)))
        val book = Book(
            cursor.getLong(cursor.getColumnIndexOrThrow(DataBaseConsts.BOOK.COLUMNS.ID)),
            cursor.getString(cursor.getColumnIndexOrThrow(DataBaseConsts.BOOK.COLUMNS.TITLE)),
            cursor.getString(cursor.getColumnIndexOrThrow(DataBaseConsts.BOOK.COLUMNS.SUB_TITLE)),
            archive,
            cursor.getString(cursor.getColumnIndexOrThrow(DataBaseConsts.BOOK.COLUMNS.FILE_TYPE))
        )
        book.pages = cursor.getInt(cursor.getColumnIndexOrThrow(DataBaseConsts.BOOK.COLUMNS.PAGES))
        book.bookMark =
            cursor.getInt(cursor.getColumnIndexOrThrow(DataBaseConsts.BOOK.COLUMNS.BOOK_MARK))
        book.favorite =
            cursor.getInt(cursor.getColumnIndexOrThrow(DataBaseConsts.BOOK.COLUMNS.FAVORITE)) == 1
        book.tumbnail = coverService.findFirstByIdBook(book.id)
        return book
    }

    private fun saveCover(idBook: Long, cover: Cover?) {
        if (idBook > 0 && cover != null && cover.update)
            coverService.saveOrUpdate(idBook, cover)
    }

    override fun save(book: Book) {
        val contents: ContentValues = getContents(book)
        book.id = mDBRepository.save(DataBaseConsts.BOOK.TABLE_NAME, contents)
        saveCover(book.id, book.tumbnail)
    }

    override fun update(book: Book) {
        val contents: ContentValues = getContents(book)
        val selection = DataBaseConsts.BOOK.COLUMNS.ID + " = ?"
        val args = arrayOf(book.id.toString())
        mDBRepository.update(DataBaseConsts.BOOK.TABLE_NAME, contents, selection, args)
        saveCover(book.id, book.tumbnail)
    }

    override fun delete(book: Book) {
        coverService.deleteByIdBook(book.id)
        val selection = DataBaseConsts.BOOK.COLUMNS.ID + " = ?"
        val args = arrayOf(book.id.toString())
        mDBRepository.delete(DataBaseConsts.BOOK.TABLE_NAME, selection, args)
    }

    override fun list(): List<Book>? {
        var cursor: Cursor? =
            mDBRepository.query(DataBaseConsts.BOOK.TABLE_NAME, getProjection(), null, null)
        val books: MutableList<Book> = ArrayList()
        return try {
            if (cursor != null && cursor.count > 0) {
                if (cursor.moveToNext())
                    books.add(parseToBook(cursor))
            }
            books
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, e.message.toString())
            books
        } finally {
            cursor?.close()
        }
    }

    override fun get(id: Long): Book? {
        val selection = DataBaseConsts.BOOK.COLUMNS.ID + " = ?"
        val args = arrayOf(id.toString())
        var cursor: Cursor? = mDBRepository.query(
            DataBaseConsts.BOOK.TABLE_NAME, getProjection(),
            selection, args
        )
        return try {
            if (cursor != null && cursor.moveToFirst()) {
                parseToBook(cursor)
            } else
                null
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, e.message.toString())
            null
        } finally {
            cursor?.close()
        }
    }

    fun findByFileName(name: String): Book? {
        val selection = DataBaseConsts.BOOK.COLUMNS.FILE_NAME + " = ?"
        val args = arrayOf(name)
        var cursor: Cursor? = mDBRepository.query(
            DataBaseConsts.BOOK.TABLE_NAME, getProjection(),
            selection, args
        )
        return try {
            if (cursor != null && cursor.moveToFirst()) {
                parseToBook(cursor)
            } else
                null
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, e.message.toString())
            null
        } finally {
            cursor?.close()
        }
    }
}
package br.com.fenix.mangareader.view.managers

import android.graphics.BitmapFactory
import android.net.Uri
import br.com.fenix.mangareader.service.parses.Parse
import com.squareup.picasso.Picasso
import com.squareup.picasso.Request
import com.squareup.picasso.RequestHandler

class MangaHandler(private var mParse: Parse?) : RequestHandler() {
    private val HANDLER_URI = "localcomic"

    override fun canHandleRequest(request: Request): Boolean {
        return HANDLER_URI == request.uri.scheme
    }

    override fun load(request: Request, networkPolicy: Int): Result {
        val pageNum = request.uri.fragment!!.toInt()
        return Result(BitmapFactory.decodeStream(mParse!!.getPage(pageNum)!!), Picasso.LoadedFrom.MEMORY)
    }

    fun getPageUri(pageNum: Int): Uri? {
        return Uri.Builder()
            .scheme(HANDLER_URI)
            .authority("")
            .fragment(pageNum.toString())
            .build()
    }
}
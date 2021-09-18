package br.com.fenix.mangareader.managers

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import br.com.fenix.mangareader.service.parses.Parse
import com.squareup.picasso.Picasso
import com.squareup.picasso.Request
import com.squareup.picasso.RequestHandler
import java.io.InputStream

class BookHandler(mParse: Parse?) : RequestHandler() {
    val HANDLER_URI = "localcomic"
    var mParse: Parse? = mParse

    override fun canHandleRequest(request: Request): Boolean {
        return HANDLER_URI == request.uri.scheme
    }

    override fun load(request: Request, networkPolicy: Int): RequestHandler.Result? {
         val pageNum = request.uri.fragment!!.toInt()
        val stream: InputStream = mParse!!.getPage(pageNum)!!
        val bitmap : Bitmap = BitmapFactory.decodeStream(stream);
        return RequestHandler.Result(bitmap, Picasso.LoadedFrom.DISK)
    }

    fun getPageUri(pageNum: Int): Uri? {
        return Uri.Builder()
            .scheme(HANDLER_URI)
            .authority("")
            .fragment(Integer.toString(pageNum))
            .build()
    }
}
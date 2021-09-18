package br.com.fenix.mangareader.view.ui.reader

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.com.fenix.mangareader.R
import br.com.fenix.mangareader.util.constants.GeneralConsts

class ReaderActivity : AppCompatActivity() {

    private var bookPath: String = ""
    private var bookMark: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reader)

        val bundle = intent.extras

        if (bundle != null) {
            bookPath = bundle.getString(GeneralConsts.KEYS.BOOK.PATH)!!
            bookMark = bundle.getInt(GeneralConsts.KEYS.BOOK.MARK)
        }
    }
}
package br.com.fenix.mangareader.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.com.fenix.mangareader.R
import br.com.fenix.mangareader.model.Consts

class ReaderActivity : AppCompatActivity() {

    private var bookPath : String = ""
    private var bookMark : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reader)

        val bundle = intent.extras

        if (bundle != null) {
            bookPath = bundle.getString(Consts.getKeyBookPath())!!
            bookMark = bundle.getInt(Consts.getKeyBookMark())
        }
    }
}
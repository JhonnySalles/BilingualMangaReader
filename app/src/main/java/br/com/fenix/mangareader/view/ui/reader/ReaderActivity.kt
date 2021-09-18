package br.com.fenix.mangareader.view.ui.reader

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import br.com.fenix.mangareader.R
import br.com.fenix.mangareader.model.entity.Book
import br.com.fenix.mangareader.util.constants.GeneralConsts
import java.io.File

class ReaderActivity : AppCompatActivity() {

    private lateinit var mReaderTitle : TextView
    private lateinit var mReaderProgress : SeekBar
    private lateinit var mNavReader : LinearLayout
    private lateinit var mTolbar : Toolbar
    private lateinit var mTolbarTitle : TextView
    private var bookMark: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reader)

        mTolbar = findViewById(R.id.toolbar_reader)
        mTolbarTitle = findViewById(R.id.tolbar_title_custom)
        setSupportActionBar(mTolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(true);

        mReaderTitle = findViewById(R.id.nav_reader_title)
        mReaderProgress = findViewById(R.id.nav_reader_progress)
        mNavReader = findViewById(R.id.nav_reader)

        val bundle = intent.extras

        if (bundle != null) {
            mTolbarTitle.text = bundle.getString(GeneralConsts.KEYS.BOOK.NAME)
            bookMark = bundle.getInt(GeneralConsts.KEYS.BOOK.MARK)
        }

        if (savedInstanceState == null) {
            if (Intent.ACTION_VIEW == intent.action) {
                val fragment: ReaderFragment? = ReaderFragment.create(File(intent.data!!.path))
                setFragment(fragment)
            } else {
                val extras = intent.extras
                val book = (extras!!.getSerializable(GeneralConsts.KEYS.OBJECT.BOOK) as Book?)!!
                mReaderTitle.text = book.bookMark.toString()
                mTolbarTitle.text = book.title
                var fragment: ReaderFragment? = ReaderFragment.create(book.file)
                setFragment(fragment)
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    fun setFragment(fragment: Fragment?) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.root_frame_reader, fragment!!)
            .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
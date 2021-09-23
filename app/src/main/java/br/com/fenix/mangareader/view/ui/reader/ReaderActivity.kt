package br.com.fenix.mangareader.view.ui.reader

import android.content.Intent
import android.content.SharedPreferences
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
import br.com.fenix.mangareader.model.entity.Manga
import br.com.fenix.mangareader.model.enums.PageMode
import br.com.fenix.mangareader.model.enums.ReaderMode
import br.com.fenix.mangareader.util.constants.GeneralConsts
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.io.File

class ReaderActivity : AppCompatActivity() {

    private lateinit var mReaderTitle: TextView
    private lateinit var mReaderProgress: SeekBar
    private lateinit var mNavReader: LinearLayout
    private lateinit var mTolbar: Toolbar
    private lateinit var mTolbarTitle: TextView
    private lateinit var mMenuPopup: FrameLayout
    private var mBookMark: Int = 0

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
        mMenuPopup = findViewById(R.id.menu_popup)
        BottomSheetBehavior.from(mMenuPopup).apply {
            peekHeight = 200
            this.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        val bundle = intent.extras

        if (bundle != null) {
            mTolbarTitle.text = bundle.getString(GeneralConsts.KEYS.MANGA.NAME)
            mBookMark = bundle.getInt(GeneralConsts.KEYS.MANGA.MARK)
        }

        if (savedInstanceState == null) {
            if (Intent.ACTION_VIEW == intent.action) {
                val fragment: ReaderFragment? = ReaderFragment.create(File(intent.data!!.path))
                setFragment(fragment)
            } else {
                val extras = intent.extras
                val manga = (extras!!.getSerializable(GeneralConsts.KEYS.OBJECT.MANGA) as Manga?)
                var fragment: ReaderFragment?
                if (manga != null) {
                    mReaderTitle.text = manga.bookMark.toString()
                    mTolbarTitle.text = manga.title
                    fragment = ReaderFragment.create(manga)
                } else {
                    val file = (extras!!.getSerializable(GeneralConsts.KEYS.OBJECT.FILE) as File?)
                    fragment = if (file != null)
                        ReaderFragment.create(file)
                    else
                        ReaderFragment.create()

                    mTolbarTitle.text = file?.name
                }
                setFragment(fragment)
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setFragment(fragment: Fragment?) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.root_frame_reader, fragment!!)
            .commit()
    }

    private fun optionsSave(any: Any?) {
        if (any == null)
            return

        val sharedPreferences: SharedPreferences? =
            GeneralConsts.getSharedPreferences(applicationContext)
        when (any) {
            is PageMode -> sharedPreferences?.edit()
                ?.putString(GeneralConsts.KEYS.READER.PAGE_MODE, any.toString())
                ?.commit()
            is ReaderMode -> sharedPreferences?.edit()
                ?.putString(GeneralConsts.KEYS.READER.READER_MODE, any.toString())
                ?.commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.reading_left_to_right -> optionsSave(PageMode.Comics)
            R.id.reading_left_to_right -> optionsSave(PageMode.Manga)
            R.id.view_mode_aspect_fill -> optionsSave(ReaderMode.ASPECT_FILL)
            R.id.view_mode_aspect_fit -> optionsSave(ReaderMode.ASPECT_FIT)
            R.id.view_mode_fit_width -> optionsSave(ReaderMode.FIT_WIDTH)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
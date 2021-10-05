package br.com.fenix.mangareader.view.ui.reader

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import br.com.fenix.mangareader.R
import br.com.fenix.mangareader.model.entity.Manga
import br.com.fenix.mangareader.model.enums.PageMode
import br.com.fenix.mangareader.model.enums.ReaderMode
import br.com.fenix.mangareader.service.controller.SubTitleController
import br.com.fenix.mangareader.service.kanji.Formater
import br.com.fenix.mangareader.service.repository.MangaRepository
import br.com.fenix.mangareader.util.constants.GeneralConsts
import br.com.fenix.mangareader.view.ui.reader.FloatingSubtitleReader.Companion.canDrawOverlays
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import java.io.File
import java.util.*


class ReaderActivity : AppCompatActivity() {

    private lateinit var mReaderTitle: TextView
    private lateinit var mReaderProgress: SeekBar
    private lateinit var mNavReader: LinearLayout
    private lateinit var mToolbar: Toolbar
    private lateinit var mToolbarTitle: TextView
    private lateinit var mMenuPopup: FrameLayout
    private lateinit var mPopupView: ViewPager
    private lateinit var mBottomSheet: BottomSheetBehavior<FrameLayout>

    private lateinit var mPopupReaderColorFilterFragment: PopupReaderColorFilter
    private lateinit var mPopupSubtitleConfigurationFragment: PopupSubtitleConfiguration
    private lateinit var mPopupSubtitleReaderFragment: PopupSubtitleReader

    private lateinit var mFloatingSubtitleReader: FloatingSubtitleReader

    private lateinit var mRepository: MangaRepository
    private lateinit var mManga: Manga
    private var mBookMark: Int = 0

    companion object {
        private lateinit var mPopupTab: TabLayout
        private lateinit var mToolbarSubTitle: TextView
        fun selectTabReader() = mPopupTab.selectTab(mPopupTab.getTabAt(0), true)
        fun setSubtitle(text: String) {
            if (::mToolbarSubTitle.isInitialized)
                mToolbarSubTitle.text = text
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reader)

        Formater.initialize(applicationContext)

        mToolbar = findViewById(R.id.toolbar_reader)
        mToolbarTitle = findViewById(R.id.tolbar_title_custom)
        mToolbarSubTitle = findViewById(R.id.tolbar_subtitle_custom)
        setSupportActionBar(mToolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(true)

        mReaderTitle = findViewById(R.id.nav_reader_title)
        mReaderProgress = findViewById(R.id.nav_reader_progress)
        mNavReader = findViewById(R.id.nav_reader)
        mMenuPopup = findViewById(R.id.menu_popup)

        findViewById<ImageView>(R.id.menu_close).setOnClickListener {
            mMenuPopup.visibility = View.INVISIBLE
        }
        findViewById<ImageView>(R.id.menu_floating_touch).setOnClickListener { menuFloat() }
        findViewById<Button>(R.id.btn_popup_subtitle).setOnClickListener {
            mBottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
            mMenuPopup.visibility = View.VISIBLE
        }
        findViewById<Button>(R.id.btn_popup_open_floating).setOnClickListener { menuFloat() }
        findViewById<Button>(R.id.btn_screen_rotate).setOnClickListener {
            requestedOrientation = if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE )
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            else
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

        mBottomSheet = BottomSheetBehavior.from(mMenuPopup).apply {
            peekHeight = 195
            this.state = BottomSheetBehavior.STATE_COLLAPSED
            mBottomSheet = this
        }

        findViewById<ImageView>(R.id.menu_popup_touch).setOnClickListener {
            if (mBottomSheet.state == BottomSheetBehavior.STATE_COLLAPSED)
                mBottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
            else
                mBottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        mPopupTab = findViewById(R.id.popup_tab)
        mPopupView = findViewById(R.id.popup_view_pager)

        mPopupReaderColorFilterFragment = PopupReaderColorFilter()
        mPopupSubtitleConfigurationFragment = PopupSubtitleConfiguration()
        mPopupSubtitleReaderFragment = PopupSubtitleReader()

        mFloatingSubtitleReader = FloatingSubtitleReader(applicationContext)

        mPopupTab.setupWithViewPager(mPopupView)

        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, 0)
        viewPagerAdapter.addFragment(
            mPopupSubtitleReaderFragment,
            resources.getString(R.string.popup_reading_tab_item_subtitle)
        )
        viewPagerAdapter.addFragment(
            mPopupSubtitleConfigurationFragment,
            resources.getString(R.string.popup_reading_tab_item_subtitle_import)
        )
        viewPagerAdapter.addFragment(
            mPopupReaderColorFilterFragment,
            resources.getString(R.string.popup_reading_tab_item_brightness)
        )
        mPopupView.adapter = viewPagerAdapter

        mRepository = MangaRepository(applicationContext)
        val bundle = intent.extras

        if (bundle != null) {
            mToolbarTitle.text = bundle.getString(GeneralConsts.KEYS.MANGA.NAME)
            mBookMark = bundle.getInt(GeneralConsts.KEYS.MANGA.MARK)
        }

        if (savedInstanceState == null) {
            if (Intent.ACTION_VIEW == intent.action) {
                val fragment: ReaderFragment = ReaderFragment.create(File(intent.data!!.path!!))
                setFragment(fragment)
            } else {
                val extras = intent.extras
                val manga = (extras!!.getSerializable(GeneralConsts.KEYS.OBJECT.MANGA) as Manga?)
                val fragment: ReaderFragment?
                if (manga != null) {
                    mManga = manga
                    mReaderTitle.text = manga.bookMark.toString()
                    mToolbarTitle.text = manga.title

                    fragment = ReaderFragment.create(manga)
                } else {
                    val file = (extras.getSerializable(GeneralConsts.KEYS.OBJECT.FILE) as File?)
                    fragment = if (file != null)
                        ReaderFragment.create(file)
                    else
                        ReaderFragment.create()

                    mToolbarTitle.text = file?.name
                }
                setFragment(fragment)
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onStop() {
        if (mFloatingSubtitleReader != null && mFloatingSubtitleReader.isShowing)
            mFloatingSubtitleReader.dismiss()

        super.onStop()
    }

    override fun onDestroy() {
        if (mFloatingSubtitleReader != null && mFloatingSubtitleReader.isShowing)
            mFloatingSubtitleReader.dismiss()

        super.onDestroy()
    }

    fun setFragment(fragment: Fragment?) {
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
                ?.apply()
            is ReaderMode -> sharedPreferences?.edit()
                ?.putString(GeneralConsts.KEYS.READER.READER_MODE, any.toString())
                ?.apply()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.reading_left_to_right -> optionsSave(PageMode.Comics)
            R.id.reading_right_to_left -> optionsSave(PageMode.Manga)
            R.id.view_mode_aspect_fill -> optionsSave(ReaderMode.ASPECT_FILL)
            R.id.view_mode_aspect_fit -> optionsSave(ReaderMode.ASPECT_FIT)
            R.id.view_mode_fit_width -> optionsSave(ReaderMode.FIT_WIDTH)
            R.id.menu_popup_open_floating -> menuFloat()
            R.id.menu_reader_favorite -> changeFavorite(item)
            R.id.menu_popup_subtitle -> mMenuPopup.visibility =
                if (mMenuPopup.visibility == View.INVISIBLE) {
                    mBottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
                    View.VISIBLE
                } else View.INVISIBLE
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val favoriteItem = menu.findItem(R.id.menu_reader_favorite)
        val icon = if (::mManga.isInitialized && mManga.favorite)
            ContextCompat.getDrawable(this, R.drawable.ic_favorite_mark)
        else
            ContextCompat.getDrawable(this, R.drawable.ic_favorite_unmark)
        icon?.setTint(getColor(R.color.onSecondary))
        favoriteItem.icon = icon
        return super.onPrepareOptionsMenu(menu)
    }

    private fun changeFavorite(item: MenuItem) {
        mManga.favorite = !mManga.favorite

        val icon = if (mManga.favorite)
            ContextCompat.getDrawable(this, R.drawable.ic_favorite_mark)
        else
            ContextCompat.getDrawable(this, R.drawable.ic_favorite_unmark)
        icon?.setTint(getColor(R.color.onSecondary))
        item.icon = icon
        mRepository.update(mManga)
    }

    private fun menuFloat() {
        mMenuPopup.visibility = View.INVISIBLE

        if (mFloatingSubtitleReader.isShowing)
            mFloatingSubtitleReader.dismiss()
        else {
            if (canDrawOverlays(applicationContext)) {
                val mSubTitleController = SubTitleController.getInstance(applicationContext)
                mSubTitleController.textSelected.observe(this, {
                    mFloatingSubtitleReader.updateText(it)
                })
                mFloatingSubtitleReader.show()
            } else
                startManageDrawOverlaysPermission()

            //startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            5 -> {
                if (canDrawOverlays(applicationContext))
                    mFloatingSubtitleReader.show()
                else
                    Toast.makeText(
                        application,
                        getString(R.string.floating_reading_not_permission),
                        Toast.LENGTH_SHORT
                    ).show()
            }
        }
    }

    private fun startManageDrawOverlaysPermission() {
        Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${applicationContext.packageName}")
        ).let {
            startActivityForResult(it, 5)
        }
    }

    inner class ViewPagerAdapter(fm: FragmentManager, behavior: Int) :
        FragmentPagerAdapter(fm, behavior) {
        private val fragments: MutableList<Fragment> = ArrayList()
        private val fragmentTitle: MutableList<String> = ArrayList()
        fun addFragment(fragment: Fragment, title: String) {
            fragments.add(fragment)
            fragmentTitle.add(title)
        }

        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }

        override fun getPageTitle(position: Int): CharSequence {
            return fragmentTitle[position]
        }
    }
}
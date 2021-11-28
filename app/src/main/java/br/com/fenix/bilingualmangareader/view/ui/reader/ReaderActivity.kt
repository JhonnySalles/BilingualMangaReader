package br.com.fenix.bilingualmangareader.view.ui.reader

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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.model.enums.PageMode
import br.com.fenix.bilingualmangareader.model.enums.ReaderMode
import br.com.fenix.bilingualmangareader.service.controller.SubTitleController
import br.com.fenix.bilingualmangareader.service.kanji.Formatter
import br.com.fenix.bilingualmangareader.service.repository.MangaRepository
import br.com.fenix.bilingualmangareader.service.repository.SubTitleRepository
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.view.ui.reader.FloatingSubtitleReader.Companion.canDrawOverlays
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import java.io.File
import java.util.*


class ReaderActivity : AppCompatActivity() {

    private val mViewModel: ReaderViewModel by viewModels()
    private lateinit var mReaderTitle: TextView
    private lateinit var mReaderProgress: SeekBar
    private lateinit var mNavReader: LinearLayout
    private lateinit var mToolbar: Toolbar
    private lateinit var mToolbarTitle: TextView
    private lateinit var mMenuPopup: FrameLayout
    private lateinit var mPopupView: ViewPager
    private lateinit var mBottomSheet: BottomSheetBehavior<FrameLayout>

    private lateinit var mPopupReaderColorFilterFragment: PopupReaderColorFilterFragment
    private lateinit var mPopupSubtitleConfigurationFragment: PopupSubtitleConfiguration
    private lateinit var mPopupSubtitleReaderFragment: PopupSubtitleReader
    private lateinit var mPopupSubtitleVocabularyFragment: PopupSubtitleVocabulary

    private lateinit var mFloatingSubtitleReader: FloatingSubtitleReader

    private lateinit var mRepository: MangaRepository
    private var mManga: Manga? = null
    private var mBookMark: Int = 0
    private var isTabletOrLandscape : Boolean = false

    companion object {
        private lateinit var mPopupTab: TabLayout
        private lateinit var mToolbarSubTitle: TextView
        fun selectTabReader() =
            mPopupTab.selectTab(mPopupTab.getTabAt(0), true)

        fun setSubtitle(text: String) {
            if (::mToolbarSubTitle.isInitialized)
                mToolbarSubTitle.text = text
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reader)

        Formatter.initializeAsync(applicationContext)

        mToolbar = findViewById(R.id.toolbar_reader)
        mToolbarTitle = findViewById(R.id.toolbar_title_custom)
        mToolbarSubTitle = findViewById(R.id.toolbar_subtitle_custom)
        setSupportActionBar(mToolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(true)

        mReaderTitle = findViewById(R.id.nav_reader_title)
        mReaderProgress = findViewById(R.id.nav_reader_progress)
        mNavReader = findViewById(R.id.nav_reader)
        mMenuPopup = findViewById(R.id.menu_popup)

        if (findViewById<ImageView>(R.id.menu_popup_touch) == null)
            isTabletOrLandscape = true
        else {
            mBottomSheet = BottomSheetBehavior.from(mMenuPopup).apply {
                peekHeight = 195
                this.state = BottomSheetBehavior.STATE_COLLAPSED
                mBottomSheet = this
            }
            mBottomSheet.isDraggable = false

            findViewById<ImageView>(R.id.menu_popup_touch).setOnClickListener {
                if (mBottomSheet.state == BottomSheetBehavior.STATE_COLLAPSED)
                    mBottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
                else
                    mBottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }

        findViewById<ImageView>(R.id.menu_close).setOnClickListener {
            mMenuPopup.visibility = View.GONE
        }
        findViewById<ImageView>(R.id.menu_floating_touch).setOnClickListener { menuFloat() }
        findViewById<Button>(R.id.btn_popup_subtitle).setOnClickListener {
            if (!isTabletOrLandscape)
                mBottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
            mMenuPopup.visibility = View.VISIBLE
        }
        findViewById<Button>(R.id.btn_popup_open_floating).setOnClickListener { menuFloat() }
        findViewById<Button>(R.id.btn_screen_rotate).setOnClickListener {
            requestedOrientation = if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            else
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

        mPopupTab = findViewById(R.id.popup_tab)
        mPopupView = findViewById(R.id.popup_view_pager)

        mPopupReaderColorFilterFragment = PopupReaderColorFilterFragment()
        mPopupSubtitleConfigurationFragment = PopupSubtitleConfiguration()
        mPopupSubtitleReaderFragment = PopupSubtitleReader()
        mPopupSubtitleVocabularyFragment = PopupSubtitleVocabulary()
        mPopupSubtitleVocabularyFragment.setBackground(R.color.onPrimary)

        mFloatingSubtitleReader = FloatingSubtitleReader(applicationContext)

        mPopupTab.setupWithViewPager(mPopupView)

        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, 0)
        viewPagerAdapter.addFragment(
            mPopupSubtitleReaderFragment,
            resources.getString(R.string.popup_reading_tab_item_subtitle)
        )
        viewPagerAdapter.addFragment(
            mPopupSubtitleVocabularyFragment,
            resources.getString(R.string.popup_reading_tab_item_subtitle_vocabulary)
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
                val fragment: ReaderFragment = if (manga != null) {
                    mManga = manga
                    mReaderTitle.text = manga.bookMark.toString()
                    mToolbarTitle.text = manga.title

                    ReaderFragment.create(manga)
                } else
                    ReaderFragment.create()
                setFragment(fragment)
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSaveInstanceState(savedInstanceState : Bundle) {
        if (mManga != null)
            savedInstanceState.putSerializable(GeneralConsts.KEYS.OBJECT.MANGA, mManga)

        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onRestoreInstanceState(savedInstanceState : Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val manga = (savedInstanceState.getSerializable(GeneralConsts.KEYS.OBJECT.MANGA) as Manga?)
        if (manga != null)
            mManga = manga
    }

    private var mLastFloatingShowing = false
    override fun onResume() {
        if (mLastFloatingShowing)
            mFloatingSubtitleReader.show()

        super.onResume()
    }

    override fun onStop() {
        if (::mFloatingSubtitleReader.isInitialized) {
            mLastFloatingShowing = mFloatingSubtitleReader.isShowing
            if (mFloatingSubtitleReader.isShowing)
                mFloatingSubtitleReader.dismiss()
        }

        super.onStop()
    }

    override fun onDestroy() {
        if (::mFloatingSubtitleReader.isInitialized && mFloatingSubtitleReader.isShowing)
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

        val sharedPreferences: SharedPreferences =
            GeneralConsts.getSharedPreferences(applicationContext)
        when (any) {
            is PageMode -> sharedPreferences.edit()
                .putString(GeneralConsts.KEYS.READER.PAGE_MODE, any.toString())
                .apply()
            is ReaderMode -> sharedPreferences.edit()
                .putString(GeneralConsts.KEYS.READER.READER_MODE, any.toString())
                .apply()
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
        val icon = if (mManga != null && mManga!!.favorite)
            ContextCompat.getDrawable(this, R.drawable.ic_favorite_mark)
        else
            ContextCompat.getDrawable(this, R.drawable.ic_favorite_unmark)
        icon?.setTint(getColor(R.color.onSecondary))
        favoriteItem.icon = icon
        return super.onPrepareOptionsMenu(menu)
    }

    private fun changeFavorite(item: MenuItem) {
        if (mManga == null)
            return

        mManga?.favorite = !mManga!!.favorite

        val icon = if (mManga!!.favorite)
            ContextCompat.getDrawable(this, R.drawable.ic_favorite_mark)
        else
            ContextCompat.getDrawable(this, R.drawable.ic_favorite_unmark)
        icon?.setTint(getColor(R.color.onSecondary))
        item.icon = icon
        mRepository.update(mManga!!)
    }

    private fun prepareMenuFloat() {
        val mSubTitleController = SubTitleController.getInstance(applicationContext)

        mSubTitleController.pageSelected.observe(this, {
            mFloatingSubtitleReader.updatePage(it)
        })
        mSubTitleController.textSelected.observe(this, {
            mFloatingSubtitleReader.updateText(it)
        })

        mSubTitleController.forceExpandFloatingPopup.observe(this, {
            if (mFloatingSubtitleReader.isShowing)
                mFloatingSubtitleReader.expanded(true)
        })

        if (mSubTitleController.mManga != null && mSubTitleController.mManga!!.id != null && mSubTitleController.textSelected.value == null) {
            val mSubtitleRepository = SubTitleRepository(applicationContext)
            val lastSubtitle = mSubtitleRepository.findByIdManga(mSubTitleController.mManga!!.id!!)
            if (lastSubtitle != null)
                mSubTitleController.initialize(lastSubtitle.chapterKey, lastSubtitle.pageKey)
        }
    }

    private fun menuFloat() {
        mMenuPopup.visibility = View.INVISIBLE

        if (mFloatingSubtitleReader.isShowing)
            mFloatingSubtitleReader.dismiss()
        else {
            if (canDrawOverlays(applicationContext)) {
                prepareMenuFloat()
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
                if (canDrawOverlays(applicationContext)) {
                    prepareMenuFloat()
                    mFloatingSubtitleReader.show()
                } else
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
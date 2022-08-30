package br.com.fenix.bilingualmangareader.view.ui.reader

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Library
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.model.entity.Pages
import br.com.fenix.bilingualmangareader.model.enums.*
import br.com.fenix.bilingualmangareader.service.controller.ImageCoverController
import br.com.fenix.bilingualmangareader.service.controller.SubTitleController
import br.com.fenix.bilingualmangareader.service.kanji.Formatter
import br.com.fenix.bilingualmangareader.service.listener.ChapterCardListener
import br.com.fenix.bilingualmangareader.service.ocr.GoogleVision
import br.com.fenix.bilingualmangareader.service.ocr.OcrProcess
import br.com.fenix.bilingualmangareader.service.repository.LibraryRepository
import br.com.fenix.bilingualmangareader.service.repository.MangaRepository
import br.com.fenix.bilingualmangareader.service.repository.Storage
import br.com.fenix.bilingualmangareader.service.repository.SubTitleRepository
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.util.helpers.FileUtil
import br.com.fenix.bilingualmangareader.util.helpers.LibraryUtil
import br.com.fenix.bilingualmangareader.util.helpers.Util
import br.com.fenix.bilingualmangareader.view.adapter.reader.MangaChaptersCardAdapter
import br.com.fenix.bilingualmangareader.view.components.ComponentsUtil
import br.com.fenix.bilingualmangareader.view.ui.pages_link.PagesLinkActivity
import br.com.fenix.bilingualmangareader.view.ui.pages_link.PagesLinkViewModel
import br.com.fenix.bilingualmangareader.view.ui.window.FloatingButtons
import br.com.fenix.bilingualmangareader.view.ui.window.FloatingSubtitleReader
import br.com.fenix.bilingualmangareader.view.ui.window.FloatingWindowOcr
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import org.slf4j.LoggerFactory
import java.io.File


class ReaderActivity : AppCompatActivity(), OcrProcess {

    private val mLOGGER = LoggerFactory.getLogger(ReaderActivity::class.java)

    private val mViewModel: ReaderViewModel by viewModels()

    private lateinit var mReaderTitle: TextView
    private lateinit var mReaderProgress: SeekBar
    private lateinit var mNavReader: LinearLayout
    private lateinit var mToolbar: Toolbar
    private lateinit var mToolbarTitle: TextView
    private lateinit var mToolbarSubTitle: TextView
    private lateinit var mToolbarTitleContent: LinearLayout
    private lateinit var mSubToolbar: LinearLayout
    private lateinit var mLanguageOcrDescription: TextView
    private lateinit var mMenuPopupTranslate: FrameLayout
    private lateinit var mPopupTranslateView: ViewPager
    private lateinit var mMenuPopupColor: FrameLayout
    private lateinit var mPopupColorView: ViewPager
    private lateinit var mPopupColorTab: TabLayout
    private lateinit var mBottomSheetTranslate: BottomSheetBehavior<FrameLayout>
    private lateinit var mBottomSheetColor: BottomSheetBehavior<FrameLayout>

    private lateinit var mPopupReaderColorFilterFragment: PopupReaderColorFilterFragment
    private lateinit var mPopupSubtitleConfigurationFragment: PopupSubtitleConfiguration
    private lateinit var mPopupSubtitleReaderFragment: PopupSubtitleReader
    private lateinit var mPopupSubtitleVocabularyFragment: PopupSubtitleVocabulary

    private lateinit var mFloatingSubtitleReader: FloatingSubtitleReader
    private lateinit var mFloatingWindowOcr: FloatingWindowOcr
    private lateinit var mFloatingButtons: FloatingButtons

    private lateinit var mClockAndBattery: LinearLayout
    private lateinit var mBattery: TextView
    private lateinit var mTouchView: ConstraintLayout

    private lateinit var mChapterContent: LinearLayout
    private lateinit var mChapterList: RecyclerView

    private var mHandler = Handler(Looper.getMainLooper())
    private val mMonitoringBattery = Runnable { getBatteryPercent() }
    private val mDismissTouchView = Runnable { closeViewTouch() }

    private lateinit var mPreferences: SharedPreferences
    private lateinit var mStorage: Storage
    private lateinit var mRepository: MangaRepository
    private lateinit var mSubtitleController: SubTitleController
    private lateinit var mLibrary: Library
    private var mFragment: ReaderFragment? = null
    private var mManga: Manga? = null
    private var mMenuPopupBottomSheet: Boolean = false

    companion object {
        private lateinit var mPopupTranslateTab: TabLayout
        fun selectTabReader() =
            mPopupTranslateTab.selectTab(mPopupTranslateTab.getTabAt(0), true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reader)

        Formatter.initializeAsync(applicationContext)

        mSubtitleController = SubTitleController.getInstance(applicationContext)

        if (savedInstanceState == null)
            mSubtitleController.clearExternalSubtitlesSelected()

        mToolbar = findViewById(R.id.toolbar_reader)
        mToolbarTitle = findViewById(R.id.toolbar_title_custom)
        mToolbarSubTitle = findViewById(R.id.toolbar_subtitle_custom)
        mToolbarTitleContent = findViewById(R.id.toolbar_title_content)
        mSubToolbar = findViewById(R.id.sub_toolbar)
        mLanguageOcrDescription = findViewById(R.id.ocr_language)
        mLanguageOcrDescription.setOnClickListener { choiceLanguage { mViewModel.mLanguageOcr = it } }

        setSupportActionBar(mToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        mReaderTitle = findViewById(R.id.nav_reader_title)
        mReaderProgress = findViewById(R.id.nav_reader_progress)
        mNavReader = findViewById(R.id.nav_reader)
        mMenuPopupTranslate = findViewById(R.id.menu_popup_translate)
        mMenuPopupColor = findViewById(R.id.menu_popup_color)

        if (findViewById<ImageView>(R.id.menu_translate_touch) == null)
            mMenuPopupBottomSheet = true
        else {
            mBottomSheetTranslate = BottomSheetBehavior.from(mMenuPopupTranslate).apply {
                peekHeight = 195
                this.state = BottomSheetBehavior.STATE_COLLAPSED
                mBottomSheetTranslate = this
            }
            mBottomSheetTranslate.isDraggable = false

            findViewById<ImageView>(R.id.menu_translate_touch).setOnClickListener {
                if (mBottomSheetTranslate.state == BottomSheetBehavior.STATE_COLLAPSED)
                    mBottomSheetTranslate.state = BottomSheetBehavior.STATE_EXPANDED
                else
                    mBottomSheetTranslate.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }

        findViewById<ImageView>(R.id.menu_translate_close).setOnClickListener {
            mMenuPopupTranslate.visibility = View.GONE
        }
        findViewById<ImageView>(R.id.menu_color_close).setOnClickListener {
            mMenuPopupColor.visibility = View.GONE
        }
        findViewById<ImageView>(R.id.menu_translate_floating_touch).setOnClickListener { openFloatingSubtitle() }
        findViewById<Button>(R.id.btn_menu_file_link).setOnClickListener { openFileLink() }
        findViewById<Button>(R.id.btn_popup_subtitle).setOnClickListener {
            mMenuPopupColor.visibility = View.GONE
            if (!mMenuPopupBottomSheet)
                mBottomSheetTranslate.state = BottomSheetBehavior.STATE_EXPANDED
            mMenuPopupTranslate.visibility = View.VISIBLE
        }
        findViewById<Button>(R.id.btn_popup_color).setOnClickListener {
            mMenuPopupTranslate.visibility = View.GONE
            if (!mMenuPopupBottomSheet)
                mBottomSheetColor.state = BottomSheetBehavior.STATE_EXPANDED
            mMenuPopupColor.visibility = View.VISIBLE
        }
        findViewById<Button>(R.id.btn_floating_popup).setOnClickListener { openFloatingSubtitle() }
        findViewById<Button>(R.id.btn_floating_buttons).setOnClickListener { openFloatingButtons() }
        findViewById<Button>(R.id.btn_screen_rotate).setOnClickListener {
            requestedOrientation = if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            else
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

        val buttonOCR = findViewById<Button>(R.id.btn_menu_ocr)
        buttonOCR.setOnClickListener {
            showMenuFromButton(buttonOCR, it)
        }

        findViewById<Button>(R.id.btn_menu_page_linked).setOnClickListener { mSubtitleController.drawPageLinked() }

        mLibrary = LibraryUtil.getDefault(this)
        mStorage = Storage(applicationContext)
        findViewById<MaterialButton>(R.id.nav_previous_file).setOnClickListener { switchManga(false) }
        findViewById<MaterialButton>(R.id.nav_next_file).setOnClickListener { switchManga(true) }

        mToolbarTitleContent.setOnClickListener { dialogPageIndex() }
        mToolbarTitleContent.setOnLongClickListener {
            mManga?.let { FileUtil(this).copyName(it) }
            true
        }

        mPopupTranslateTab = findViewById(R.id.popup_translate_tab)
        mPopupTranslateView = findViewById(R.id.popup_translate_view_pager)

        mClockAndBattery = findViewById(R.id.container_clock_battery)
        mBattery = findViewById(R.id.txt_battery)
        mTouchView = findViewById(R.id.container_touch_demonstration)

        mPopupReaderColorFilterFragment = PopupReaderColorFilterFragment()
        mPopupSubtitleConfigurationFragment = PopupSubtitleConfiguration()
        mPopupSubtitleReaderFragment = PopupSubtitleReader()
        mPopupSubtitleVocabularyFragment = PopupSubtitleVocabulary()
        mPopupSubtitleVocabularyFragment.setBackground(R.color.on_primary)

        mFloatingButtons = FloatingButtons(applicationContext, this)
        mFloatingSubtitleReader = FloatingSubtitleReader(applicationContext, this)
        mFloatingWindowOcr = FloatingWindowOcr(applicationContext, this)
        prepareFloatingSubtitle()

        mPopupTranslateTab.setupWithViewPager(mPopupTranslateView)

        val viewTranslatePagerAdapter = ViewPagerAdapter(supportFragmentManager, 0)
        viewTranslatePagerAdapter.addFragment(
            mPopupSubtitleReaderFragment,
            resources.getString(R.string.popup_reading_tab_item_subtitle)
        )
        viewTranslatePagerAdapter.addFragment(
            mPopupSubtitleVocabularyFragment,
            resources.getString(R.string.popup_reading_tab_item_subtitle_vocabulary)
        )
        viewTranslatePagerAdapter.addFragment(
            mPopupSubtitleConfigurationFragment,
            resources.getString(R.string.popup_reading_tab_item_subtitle_import)
        )

        mPopupTranslateView.adapter = viewTranslatePagerAdapter

        mPopupColorTab = findViewById(R.id.popup_color_tab)
        mPopupColorView = findViewById(R.id.popup_color_view_pager)

        if (findViewById<ImageView>(R.id.menu_color_touch) != null) {
            mBottomSheetColor = BottomSheetBehavior.from(mMenuPopupColor).apply {
                peekHeight = 195
                this.state = BottomSheetBehavior.STATE_COLLAPSED
                mBottomSheetColor = this
            }
            mBottomSheetColor.isDraggable = false

            findViewById<ImageView>(R.id.menu_color_touch).setOnClickListener {
                if (mBottomSheetColor.state == BottomSheetBehavior.STATE_COLLAPSED)
                    mBottomSheetColor.state = BottomSheetBehavior.STATE_EXPANDED
                else
                    mBottomSheetColor.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }

        mPopupColorTab.setupWithViewPager(mPopupColorView)

        val viewColorPagerAdapter = ViewPagerAdapter(supportFragmentManager, 0)
        viewColorPagerAdapter.addFragment(
            mPopupReaderColorFilterFragment,
            resources.getString(R.string.popup_reading_tab_item_brightness)
        )
        mPopupColorView.adapter = viewColorPagerAdapter

        mRepository = MangaRepository(applicationContext)

        mPreferences = GeneralConsts.getSharedPreferences(this)
        mClockAndBattery.visibility = if (mPreferences.getBoolean(GeneralConsts.KEYS.READER.SHOW_CLOCK_AND_BATTERY, false))
            View.VISIBLE
        else
            View.GONE

        getBatteryPercent()

        mTouchView.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (mHandler.hasCallbacks(mDismissTouchView))
                    mHandler.removeCallbacks(mDismissTouchView)
            } else {
                mHandler.removeCallbacks(mDismissTouchView)
            }

            closeViewTouch()
        }

        mChapterContent = findViewById(R.id.container_chapters_list)
        mChapterList = findViewById(R.id.chapters_list_covers)
        mChapterContent.visibility = View.GONE
        prepareChapters()

        val bundle = intent.extras
        if (bundle != null) {
            val name = bundle.getString(GeneralConsts.KEYS.MANGA.NAME) ?: ""
            val bookMark = if (bundle.containsKey(GeneralConsts.KEYS.MANGA.PAGE_NUMBER))
                bundle.getInt(GeneralConsts.KEYS.MANGA.PAGE_NUMBER)
            else
                bundle.getInt(GeneralConsts.KEYS.MANGA.MARK)
            changePage(name, "", bookMark)
        } else
            changePage("", "", 0)

        if (savedInstanceState == null) {
            mViewModel.clearChapter()
            if (Intent.ACTION_VIEW == intent.action) {
                if (intent.extras != null && intent.extras!!.containsKey(GeneralConsts.KEYS.MANGA.ID)) {
                    val manga = mRepository.get(intent.extras!!.getLong(GeneralConsts.KEYS.MANGA.ID))
                    manga?.fkLibrary?.let {
                        val library = LibraryRepository(this)
                        mLibrary = library.get(it)?: mLibrary
                    }
                    initialize(manga)
                } else
                    intent.data?.path?.let {
                        val file = File(it)
                        initialize(file, 0)
                    }
            } else {
                val extras = intent.extras

                if (extras != null)
                    mLibrary = extras.getSerializable(GeneralConsts.KEYS.OBJECT.LIBRARY) as Library

                val manga = if (extras != null) (extras.getSerializable(GeneralConsts.KEYS.OBJECT.MANGA) as Manga?) else null
                manga?.let {
                    it.bookMark = extras?.getInt(GeneralConsts.KEYS.MANGA.MARK) ?: 0
                }

                initialize(manga)
            }
        } else
            mFragment = supportFragmentManager.findFragmentById(R.id.root_frame_reader) as ReaderFragment?
    }

    private fun initialize(manga: Manga?) {
        val fragment: ReaderFragment = if (manga != null) {
            setManga(manga)
            ReaderFragment.create(mLibrary, manga)
        } else
            ReaderFragment.create()

        val fileLink: PagesLinkViewModel by viewModels()
        mSubtitleController.setFileLink(fileLink.getFileLink(manga))
        setFragment(fragment)
    }

    private fun initialize(file: File?, page: Int) {
        val fragment: ReaderFragment = if (file != null) {
            changePage(file.name, "", page)
            ReaderFragment.create(mLibrary, file)
        } else
            ReaderFragment.create()

        mSubtitleController.setFileLink(null)
        setFragment(fragment)
    }

    private fun switchManga(isNext: Boolean = true) {
        if (mManga == null) return

        val changeManga = if (isNext)
            mStorage.getNextManga(mLibrary, mManga!!)
        else
            mStorage.getPrevManga(mLibrary, mManga!!)

        if (changeManga == null) {
            val content = if (isNext) R.string.switch_next_comic_last_comic else R.string.switch_prev_comic_first_comic
            AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle)
                .setTitle(getString(R.string.switch_next_comic_not_found))
                .setMessage(content)
                .setPositiveButton(
                    R.string.action_neutral
                ) { _, _ ->
                }
                .create().show()
            return
        }

        val title = if (isNext) R.string.switch_next_comic else R.string.switch_prev_comic

        val dialog: AlertDialog =
            AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle)
                .setTitle(title)
                .setMessage(changeManga.file.name)
                .setPositiveButton(
                    R.string.switch_action_positive
                ) { _, _ ->
                    changeManga(changeManga)
                }
                .setNegativeButton(
                    R.string.switch_action_negative
                ) { _, _ -> }
                .create()
        dialog.show()
    }

    fun changeManga(manga: Manga) {
        setManga(manga)

        mSubtitleController.clearExternalSubtitlesSelected()
        val fileLink: PagesLinkViewModel by viewModels()
        mSubtitleController.setFileLink(fileLink.getFileLink(manga))

        setFragment(ReaderFragment.create(mLibrary, manga))
    }

    fun setLanguage(language: Languages) {
        mViewModel.mLanguageOcr = language
        mLanguageOcrDescription.text = getString(R.string.languages_description, Util.languageToString(this, language))
        mSubToolbar.visibility = View.VISIBLE
    }

    private fun changeShowBatteryClock(enabled: Boolean) {
        mClockAndBattery.visibility = if (enabled)
            View.VISIBLE
        else
            View.GONE

        optionsSave(enabled)
    }

    fun changePage(title: String, text: String, page: Int) {
        mReaderTitle.text = if (page > -1) "$page/${mManga?.pages ?: ""}" else ""
        mToolbarTitle.text = title
        mToolbarSubTitle.text = text
        mViewModel.selectPage(page)
    }

    private fun setManga(manga: Manga) {
        changePage(manga.title, "", manga.bookMark)
        mViewModel.clearChapter()
        mManga = manga
        mRepository.updateLastAccess(manga)
        setShortCutManga()
    }

    private fun setShortCutManga() {
        try {
            val shortcut = getSystemService(ShortcutManager::class.java)
            val lasts = mRepository.getLastedRead()
            val list = mutableListOf<ShortcutInfo>()

            lasts.first?.let {
                list.add(generateInfo("comic1", it))
            }

            lasts.second?.let {
                list.add(generateInfo("comic2", it))
            }

            shortcut.dynamicShortcuts.clear()
            shortcut.dynamicShortcuts = list
        } catch (e: Exception) {
            mLOGGER.warn("Error generate shortcut: " + e.message, e)
        }
    }

    private fun generateInfo(id: String, manga: Manga) : ShortcutInfo {
        val image = ImageCoverController.instance.getMangaCover(this, manga, true)
        val icon = if (image != null) Icon.createWithAdaptiveBitmap(image) else Icon.createWithResource(this, R.drawable.ic_shortcut_book)

        val intent = Intent(this, ReaderActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.data = Uri.parse(manga.path)
        intent.extras

        val bundle = Bundle()
        bundle.putLong(GeneralConsts.KEYS.MANGA.ID, manga.id ?: -1)
        intent.putExtras(bundle)

        return ShortcutInfo.Builder(this, id)
            .setShortLabel(manga.title)
            .setIcon(icon)
            .setIntent(intent)
            .build()
    }

    private fun dialogPageIndex() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.root_frame_reader) ?: return
        val parse = (currentFragment as ReaderFragment).mParse ?: return

        val paths = parse.getPagePaths()

        if (paths.isEmpty()) {
            AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle)
                .setTitle(resources.getString(R.string.reading_page_index))
                .setMessage(resources.getString(R.string.reading_page_empty))
                .setPositiveButton(
                    R.string.action_neutral
                ) { _, _ -> }
                .create()
                .show()
            return
        }

        val items = paths.keys.toTypedArray()

        val title = LinearLayout(this)
        title.orientation = LinearLayout.VERTICAL
        title.setPadding(resources.getDimensionPixelOffset(R.dimen.page_link_page_index_title_padding))
        val name = TextView(this)
        name.text = mToolbarTitle.text
        name.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.title_index_dialog_size))
        name.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        title.addView(name)
        val index = TextView(this)
        index.text = resources.getString(R.string.reading_page_index)
        index.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.title_small_index_dialog_size))
        index.setTextColor(ContextCompat.getColor(this, R.color.on_secondary))
        title.addView(index)
        title.setOnLongClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied Text", mToolbarTitle.text)
            clipboard.setPrimaryClip(clip)

            Toast.makeText(
                this,
                getString(R.string.action_copy, mToolbarTitle.text),
                Toast.LENGTH_LONG
            ).show()

            true
        }

        MaterialAlertDialogBuilder(this, R.style.AppCompatMaterialAlertDialogStyle)
            .setCustomTitle(title)
            .setItems(items) { _, selected ->
                val pageNumber = paths[items[selected]]
                if (pageNumber != null)
                    currentFragment.setCurrentPage(pageNumber + 1)
            }
            .show()
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putSerializable(GeneralConsts.KEYS.OBJECT.LIBRARY, mLibrary)

        if (mManga != null)
            savedInstanceState.putSerializable(GeneralConsts.KEYS.OBJECT.MANGA, mManga)

        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        mLibrary = savedInstanceState.getSerializable(GeneralConsts.KEYS.OBJECT.LIBRARY) as Library

        val manga = (savedInstanceState.getSerializable(GeneralConsts.KEYS.OBJECT.MANGA) as Manga?)
        if (manga != null) {
            mManga = manga
            changePage(manga.title, "", manga.bookMark)
        }
    }

    private var mLastFloatingWindowOcr = false
    private var mLastFloatingShowing = false
    override fun onResume() {
        super.onResume()
        if (mLastFloatingWindowOcr)
            mFloatingWindowOcr.show()

        if (mLastFloatingShowing)
            mFloatingSubtitleReader.show()
    }

    override fun onStop() {
        if (::mFloatingSubtitleReader.isInitialized) {
            mLastFloatingShowing = mFloatingSubtitleReader.isShowing
            if (mFloatingSubtitleReader.isShowing)
                mFloatingSubtitleReader.dismiss()
        }

        if (::mFloatingWindowOcr.isInitialized) {
            mLastFloatingWindowOcr = mFloatingWindowOcr.isShowing
            if (mFloatingWindowOcr.isShowing)
                mFloatingWindowOcr.dismiss()
        }

        if (::mFloatingButtons.isInitialized && mFloatingButtons.isShowing)
            mFloatingButtons.dismiss()

        super.onStop()
    }

    override fun onDestroy() {
        if (::mFloatingSubtitleReader.isInitialized)
            mFloatingSubtitleReader.destroy()

        if (::mFloatingWindowOcr.isInitialized)
            mFloatingWindowOcr.destroy()

        if (::mFloatingButtons.isInitialized)
            mFloatingButtons.destroy()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (mHandler.hasCallbacks(mMonitoringBattery))
                mHandler.removeCallbacks(mMonitoringBattery)

            if (mHandler.hasCallbacks(mDismissTouchView))
                mHandler.removeCallbacks(mDismissTouchView)
        } else {
            mHandler.removeCallbacks(mMonitoringBattery)
            mHandler.removeCallbacks(mDismissTouchView)
        }

        super.onDestroy()
    }

    fun setFragment(fragment: Fragment) {
        mFragment = if (fragment is ReaderFragment) fragment else null
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.root_frame_reader, fragment)
            .commit()
    }

    private fun optionsSave(any: Any?) {
        if (any == null)
            return

        when (any) {
            is PageMode -> mPreferences.edit()
                .putString(GeneralConsts.KEYS.READER.PAGE_MODE, any.toString())
                .apply()
            is ReaderMode -> mPreferences.edit()
                .putString(GeneralConsts.KEYS.READER.READER_MODE, any.toString())
                .apply()
            is Boolean -> mPreferences.edit()
                .putBoolean(GeneralConsts.KEYS.READER.SHOW_CLOCK_AND_BATTERY, any)
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
            R.id.menu_item_popup_open_floating -> openFloatingSubtitle()
            R.id.menu_item_reader_favorite -> changeFavorite(item)
            R.id.menu_item_popup_subtitle -> mMenuPopupTranslate.visibility =
                if (mMenuPopupTranslate.visibility == View.GONE) {
                    mMenuPopupColor.visibility = View.GONE
                    if (!mMenuPopupBottomSheet)
                        mBottomSheetTranslate.state = BottomSheetBehavior.STATE_EXPANDED
                    View.VISIBLE
                } else View.GONE
            R.id.menu_item_popup_color -> mMenuPopupColor.visibility =
                if (mMenuPopupTranslate.visibility == View.GONE) {
                    mMenuPopupTranslate.visibility = View.GONE
                    if (!mMenuPopupBottomSheet)
                        mBottomSheetColor.state = BottomSheetBehavior.STATE_EXPANDED
                    View.VISIBLE
                } else View.GONE
            R.id.menu_item_file_link -> openFileLink()
            R.id.menu_item_open_kaku -> {
                val launchIntent = packageManager.getLaunchIntentForPackage("ca.fuwafuwa.kaku")
                launchIntent?.let { startActivity(it) } ?: Toast.makeText(
                    application,
                    getString(R.string.open_app_kaku_not_founded),
                    Toast.LENGTH_SHORT
                ).show()
            }
            R.id.menu_item_floating_buttons -> openFloatingButtons()
            R.id.menu_item_view_touch_screen -> openViewTouch()
            R.id.menu_item_show_clock_and_battery -> {
                item.isChecked = !item.isChecked
                changeShowBatteryClock(item.isChecked)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showMenuFromButton(button: Button, view: View) {
        when (button.id) {
            R.id.btn_menu_ocr -> {
                val popup = PopupMenu(this, view)
                popup.menuInflater.inflate(R.menu.menu_ocr, popup.menu)
                popup.setOnMenuItemClickListener { menuItem: MenuItem ->
                    when (menuItem.itemId) {
                        R.id.menu_popup_ocr_tesseract -> openTesseract()
                        R.id.menu_popup_ocr_google_vision -> openGoogleVisionOcr()
                    }
                    true
                }
                popup.setOnDismissListener {
                }
                popup.show()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val favoriteItem = menu.findItem(R.id.menu_item_reader_favorite)
        val icon = if (mManga != null && mManga!!.favorite)
            ContextCompat.getDrawable(this, R.drawable.ic_favorite_mark)
        else
            ContextCompat.getDrawable(this, R.drawable.ic_favorite_unmark)
        icon?.setTint(getColor(R.color.on_secondary))
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
        icon?.setTint(getColor(R.color.on_secondary))
        item.icon = icon
        mRepository.update(mManga!!)
    }

    private fun prepareFloatingSubtitle() {
        mSubtitleController.pageSelected.observe(this) {
            mFloatingSubtitleReader.updatePage(it)
        }

        mSubtitleController.textSelected.observe(this) {
            mFloatingSubtitleReader.updateText(it)
        }

        mSubtitleController.forceExpandFloatingPopup.observe(this) {
            if (mFloatingSubtitleReader.isShowing)
                mFloatingSubtitleReader.expanded(true)
        }

        mViewModel.ocrItem.observe(this) {
            mFloatingSubtitleReader.updateOcrList(it)
        }

        if (mSubtitleController.mManga != null && mSubtitleController.mManga!!.id != null && mSubtitleController.textSelected.value == null) {
            val mSubtitleRepository = SubTitleRepository(applicationContext)
            val lastSubtitle = mSubtitleRepository.findByIdManga(mSubtitleController.mManga!!.id!!)
            if (lastSubtitle != null)
                mSubtitleController.initialize(lastSubtitle.chapterKey, lastSubtitle.pageKey)
        }
    }

    private fun prepareChapters() {
        val lineAdapter = MangaChaptersCardAdapter()
        mChapterList.adapter = lineAdapter
        val layout = GridLayoutManager(this, 1)
        layout.orientation = RecyclerView.HORIZONTAL
        mChapterList.layoutManager = layout

        val listener = object : ChapterCardListener {
            override fun onClick(page: Pages) {
                mFragment?.setCurrentPage(page.page)
            }
        }

        lineAdapter.attachListener(listener)
        mViewModel.chapters.observe(this) { lineAdapter.updateList(it) }
    }

    fun openFileLink() {
        if (mManga != null) {
            val intent = Intent(applicationContext, PagesLinkActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable(GeneralConsts.KEYS.OBJECT.LIBRARY, mLibrary)
            bundle.putSerializable(GeneralConsts.KEYS.OBJECT.MANGA, mManga)
            bundle.putInt(GeneralConsts.KEYS.MANGA.PAGE_NUMBER, mReaderProgress.progress)
            intent.putExtras(bundle)
            startActivity(intent)
        } else
            AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle)
                .setTitle(getString(R.string.page_link_manga_empty))
                .setMessage(getString(R.string.page_link_manga_empty_description))
                .setPositiveButton(
                    R.string.action_neutral
                ) { _, _ -> }
                .create()
                .show()
    }

    private fun verifySubtitle() {
        if (!mViewModel.mIsAlertSubtitle && !mSubtitleController.isNotEmpty) {
            mViewModel.mIsAlertSubtitle = true
            val message = getString(
                if (mSubtitleController.isSelected) R.string.popup_reading_subtitle_selected_empty
                else R.string.popup_reading_subtitle_embedded_empty
            )

            AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle)
                .setTitle(getString(R.string.popup_reading_subtitle_empty))
                .setMessage(message)
                .setPositiveButton(
                    R.string.action_neutral
                ) { _, _ -> }
                .create()
                .show()
        }
    }

    fun openFloatingSubtitle() {
        mMenuPopupTranslate.visibility = View.INVISIBLE
        mMenuPopupColor.visibility = View.INVISIBLE

        if (mFloatingSubtitleReader.isShowing)
            mFloatingSubtitleReader.dismiss()
        else {
            if (ComponentsUtil.canDrawOverlays(applicationContext)) {
                verifySubtitle()
                mFloatingSubtitleReader.show()
            } else
                startManageDrawOverlaysPermission(GeneralConsts.REQUEST.PERMISSION_DRAW_OVERLAYS_FLOATING_SUBTITLE)
        }
    }

    private fun openFloatingButtons() {
        if (mFloatingButtons.isShowing)
            mFloatingButtons.dismiss()
        else {
            if (ComponentsUtil.canDrawOverlays(applicationContext))
                mFloatingButtons.show()
            else
                startManageDrawOverlaysPermission(GeneralConsts.REQUEST.PERMISSION_DRAW_OVERLAYS_FLOATING_BUTTONS)
        }
    }

    private fun openViewTouch() {
        mFragment?.setFullscreen(true)

        mTouchView.alpha = 0.0f
        mTouchView.animate().alpha(1.0f).setDuration(300L)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    mTouchView.visibility = View.VISIBLE
                }
            })

        mHandler.postDelayed(mDismissTouchView, 5000)
    }

    fun touchPosition(position: Position): Boolean {
        if (position != Position.BOTTOM && mChapterContent.visibility == View.VISIBLE) {
            chapterVisibility(false)
            return true
        }

        return when (position) {
            Position.CORNER_TOP_RIGHT -> {
                mFragment?.changeAspect(mToolbar, ReaderMode.FIT_WIDTH)
                true
            }
            Position.CORNER_TOP_LEFT -> {
                mFragment?.changeAspect(mToolbar, ReaderMode.ASPECT_FIT)
                true
            }
            Position.CORNER_BOTTOM_RIGHT -> {
                mFragment?.hitEnding()
                true
            }
            Position.CORNER_BOTTOM_LEFT -> {
                mFragment?.hitBeginning()
                true
            }
            Position.BOTTOM -> {
                val initial = mFragment?.getCurrentPage() ?: 0
                val loaded = mViewModel.loadChapter(
                    mManga,
                    initial
                ) { page -> if (!mChapterList.isComputingLayout) mChapterList.adapter?.notifyItemChanged(page) }
                chapterVisibility(true)
                mFragment?.let {
                    if (loaded)
                        mChapterList.scrollToPosition(it.getCurrentPage() - 1)
                    else
                        mChapterList.smoothScrollToPosition(it.getCurrentPage() - 1)

                    mViewModel.selectPage(it.getCurrentPage())
                }
                true
            }
            else -> false
        }
    }

    private fun closeViewTouch() {
        mTouchView.alpha = 1.0f
        mTouchView.animate().alpha(0.0f).setDuration(300L)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    mTouchView.visibility = View.GONE
                }
            })
    }

    private fun chapterVisibility(isVisible: Boolean) {
        val visibility = if (isVisible) View.VISIBLE else View.GONE
        val finalAlpha = if (isVisible) 1.0f else 0.0f
        val initialAlpha = if (isVisible) 0.0f else 1.0f

        if (isVisible) {
            mChapterContent.visibility = visibility
            mChapterContent.alpha = initialAlpha
        }

        mChapterContent.animate().alpha(finalAlpha).setDuration(300L)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    mChapterContent.visibility = visibility
                }
            })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GeneralConsts.REQUEST.PERMISSION_DRAW_OVERLAYS_FLOATING_SUBTITLE -> {
                if (ComponentsUtil.canDrawOverlays(applicationContext)) {
                    verifySubtitle()
                    mFloatingSubtitleReader.show()
                } else
                    Toast.makeText(
                        application,
                        getString(R.string.floating_reading_not_permission),
                        Toast.LENGTH_SHORT
                    ).show()
            }
            GeneralConsts.REQUEST.PERMISSION_DRAW_OVERLAYS_FLOATING_BUTTONS -> {
                if (ComponentsUtil.canDrawOverlays(applicationContext))
                    mFloatingButtons.show()
                else
                    Toast.makeText(
                        application,
                        getString(R.string.floating_reading_not_permission),
                        Toast.LENGTH_SHORT
                    ).show()
            }
            GeneralConsts.REQUEST.PERMISSION_DRAW_OVERLAYS_FLOATING_OCR -> {
                if (ComponentsUtil.canDrawOverlays(applicationContext)) {
                    mFloatingWindowOcr.show()
                    mFloatingSubtitleReader.forceZIndex()
                } else
                    Toast.makeText(
                        application,
                        getString(R.string.floating_reading_not_permission),
                        Toast.LENGTH_SHORT
                    ).show()
            }
        }
    }

    private fun startManageDrawOverlaysPermission(requestCode: Int) {
        Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${applicationContext.packageName}")
        ).let {
            startActivityForResult(it, requestCode)
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

    private fun choiceLanguage(selected: (language: Languages) -> (Unit)) {
        val mapLanguage = Util.getLanguages(this)
        val items = mapLanguage.keys.filterNot { it == Util.googleLang }.toTypedArray()

        MaterialAlertDialogBuilder(this, R.style.AppCompatMaterialAlertDialogStyle)
            .setTitle(getString(R.string.languages_choice))
            .setItems(items) { _, selectItem ->
                val language = mapLanguage[items[selectItem]]
                if (language != null) {
                    setLanguage(language)
                    selected(language)
                }
            }
            .show()
    }

    private fun openTesseract() {
        if (mFloatingWindowOcr.isShowing)
            mFloatingWindowOcr.dismiss()
        else {
            if (mViewModel.mLanguageOcr == null)
                choiceLanguage {
                    mViewModel.mLanguageOcr = it
                    openFloatingWindow()
                }
            else
                openFloatingWindow()
        }
    }

    //Force floating subtitle always on top
    private fun openFloatingWindow() {
        if (ComponentsUtil.canDrawOverlays(applicationContext)) {
            mFloatingWindowOcr.show()
            mFloatingSubtitleReader.forceZIndex()
        } else
            startManageDrawOverlaysPermission(GeneralConsts.REQUEST.PERMISSION_DRAW_OVERLAYS_FLOATING_OCR)
    }

    private fun openGoogleVisionOcr() {
        val image = getImage() ?: return
        GoogleVision.getInstance(this).process(image) { setText(it) }
    }

    override fun getImage(): Bitmap? {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.root_frame_reader) ?: return null
        val view = (currentFragment as ReaderFragment).getCurrencyImageView() ?: return null
        return view.drawable.toBitmap()
    }

    override fun getImage(x: Int, y: Int, width: Int, height: Int): Bitmap? {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.root_frame_reader) ?: return null
        val view = (currentFragment as ReaderFragment).getCurrencyImageView() ?: return null
        view.isDrawingCacheEnabled = true
        view.buildDrawingCache()
        val screenshot = view.drawingCache
        val image = Bitmap.createBitmap(screenshot, x, y, width, height, null, false).copy(screenshot.config, true)
        view.isDrawingCacheEnabled = false
        return image
    }

    override fun getLanguage(): Languages {
        return mViewModel.mLanguageOcr ?: Languages.JAPANESE
    }

    override fun setText(text: String?) {
        if (::mFloatingSubtitleReader.isInitialized) {
            mViewModel.mIsAlertSubtitle = true
            mViewModel.addOcrItem(text)
            mFloatingSubtitleReader.updateTextOcr(text)
            mFloatingSubtitleReader.showWithoutDismiss()
        }
    }

    override fun setText(text: ArrayList<String>) {
        if (::mFloatingSubtitleReader.isInitialized) {
            mViewModel.mIsAlertSubtitle = true
            mViewModel.addOcrItem(text)
            mFloatingSubtitleReader.showWithoutDismiss()
            mFloatingSubtitleReader.changeLayout(false)
        }
    }

    override fun clearList() {
        mViewModel.clearOcrItem()
    }

    private fun getBatteryPercent() {
        try {
            val percent = (getSystemService(BATTERY_SERVICE) as BatteryManager).getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            mBattery.text = getString(R.string.percent, percent)
        } finally {
            mHandler.postDelayed(mMonitoringBattery, 60000)
        }
    }

}
package br.com.fenix.bilingualmangareader.view.ui.reader

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
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
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.model.enums.Languages
import br.com.fenix.bilingualmangareader.model.enums.PageMode
import br.com.fenix.bilingualmangareader.model.enums.ReaderMode
import br.com.fenix.bilingualmangareader.service.controller.SubTitleController
import br.com.fenix.bilingualmangareader.service.kanji.Formatter
import br.com.fenix.bilingualmangareader.service.ocr.GoogleVision
import br.com.fenix.bilingualmangareader.service.ocr.OcrProcess
import br.com.fenix.bilingualmangareader.service.repository.MangaRepository
import br.com.fenix.bilingualmangareader.service.repository.Storage
import br.com.fenix.bilingualmangareader.service.repository.SubTitleRepository
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.util.helpers.Util
import br.com.fenix.bilingualmangareader.view.ui.pages_link.PagesLinkActivity
import br.com.fenix.bilingualmangareader.view.ui.pages_link.PagesLinkViewModel
import br.com.fenix.bilingualmangareader.view.ui.window.FloatingSubtitleReader
import br.com.fenix.bilingualmangareader.view.ui.window.FloatingSubtitleReader.Companion.canDrawOverlays
import br.com.fenix.bilingualmangareader.view.ui.window.FloatingWindowOcr
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import java.io.File


class ReaderActivity : AppCompatActivity(), OcrProcess {

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

    private lateinit var mStorage: Storage
    private lateinit var mRepository: MangaRepository
    private lateinit var mSubtitleController: SubTitleController
    private var mManga: Manga? = null
    private var mBookMark: Int = 0
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
        mSubtitleController.clearExternalSubtitlesSelected()

        mToolbar = findViewById(R.id.toolbar_reader)
        mToolbarTitle = findViewById(R.id.toolbar_title_custom)
        mToolbarSubTitle = findViewById(R.id.toolbar_subtitle_custom)
        mToolbarTitleContent = findViewById(R.id.toolbar_title_content)
        mSubToolbar = findViewById(R.id.sub_toolbar)
        mLanguageOcrDescription = findViewById(R.id.ocr_language)
        mLanguageOcrDescription.setOnClickListener { choiceLanguage { mViewModel.mLanguageOcr = it } }

        setSupportActionBar(mToolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(true)

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
        findViewById<Button>(R.id.btn_popup_open_floating).setOnClickListener { openFloatingSubtitle() }
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

        mStorage = Storage(applicationContext)
        findViewById<MaterialButton>(R.id.nav_previous_file).setOnClickListener { switchManga(false) }
        findViewById<MaterialButton>(R.id.nav_next_file).setOnClickListener { switchManga(true) }

        mToolbarTitleContent.setOnClickListener { dialogPageIndex() }

        mPopupTranslateTab = findViewById(R.id.popup_translate_tab)
        mPopupTranslateView = findViewById(R.id.popup_translate_view_pager)

        mPopupReaderColorFilterFragment = PopupReaderColorFilterFragment()
        mPopupSubtitleConfigurationFragment = PopupSubtitleConfiguration()
        mPopupSubtitleReaderFragment = PopupSubtitleReader()
        mPopupSubtitleVocabularyFragment = PopupSubtitleVocabulary()
        mPopupSubtitleVocabularyFragment.setBackground(R.color.onPrimary)

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

        val bundle = intent.extras
        if (bundle != null) {
            mToolbarTitle.text = bundle.getString(GeneralConsts.KEYS.MANGA.NAME)
            mBookMark = bundle.getInt(GeneralConsts.KEYS.MANGA.MARK)
        }

        if (savedInstanceState == null) {
            if (Intent.ACTION_VIEW == intent.action) {
                val file = File(intent.data!!.path!!)
                val fragment: ReaderFragment = ReaderFragment.create(file)
                setTitles(file.name, "")
                mSubtitleController.setFileLink(null)
                setFragment(fragment)
            } else {
                val extras = intent.extras
                val manga = (extras!!.getSerializable(GeneralConsts.KEYS.OBJECT.MANGA) as Manga?)
                val fragment: ReaderFragment = if (manga != null) {
                    mManga = manga
                    setTitles(manga.title, manga.bookMark.toString())
                    ReaderFragment.create(manga)
                } else
                    ReaderFragment.create()

                val fileLink: PagesLinkViewModel by viewModels()
                mSubtitleController.setFileLink(fileLink.getFileLink(manga))

                setFragment(fragment)
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun switchManga(isNext: Boolean = true) {
        if (mManga == null) return

        val changeManga = if (isNext)
            mStorage.getNextManga(mManga!!)
        else
            mStorage.getPrevManga(mManga!!)

        if (changeManga == null) return

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
        setTitles(manga.title, manga.bookMark.toString())
        setManga(manga)

        val fileLink: PagesLinkViewModel by viewModels()
        mSubtitleController.setFileLink(fileLink.getFileLink(manga))

        setFragment(ReaderFragment.create(manga))
    }

    fun setTitles(title: String, page: String) {
        mReaderTitle.text = page
        mToolbarTitle.text = title
    }

    fun setLanguage(language: Languages) {
        mViewModel.mLanguageOcr = language
        mLanguageOcrDescription.text = getString(R.string.languages_description) + ": " + Util.languageToString(this, language)
        mSubToolbar.visibility = View.VISIBLE
    }

    fun setSubtitle(text: String) {
        mToolbarSubTitle.text = text
    }

    fun setManga(manga: Manga) {
        mManga = manga
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
        name.setTextColor(ContextCompat.getColor(this, R.color.textPrimary))
        title.addView(name)
        val index = TextView(this)
        index.text = resources.getString(R.string.reading_page_index)
        index.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.title_small_index_dialog_size))
        index.setTextColor(ContextCompat.getColor(this, R.color.onSecondary))
        title.addView(index)

        MaterialAlertDialogBuilder(this, R.style.AppCompatMaterialAlertDialogStyle)
            .setCustomTitle(title)
            .setItems(items) { _, selected ->
                val pageNumber = paths[items[selected]]
                if (pageNumber != null)
                    currentFragment.setCurrentPage(pageNumber)
            }
            .show()
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        if (mManga != null)
            savedInstanceState.putSerializable(GeneralConsts.KEYS.OBJECT.MANGA, mManga)

        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val manga = (savedInstanceState.getSerializable(GeneralConsts.KEYS.OBJECT.MANGA) as Manga?)
        if (manga != null)
            mManga = manga
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

        super.onStop()
    }

    override fun onDestroy() {
        if (::mFloatingSubtitleReader.isInitialized)
            mFloatingSubtitleReader.destroy()

        if (::mFloatingWindowOcr.isInitialized)
            mFloatingWindowOcr.destroy()

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
            GeneralConsts.getSharedPreferences(this)
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

    private fun openFileLink() {
        if (mManga != null) {
            val intent = Intent(applicationContext, PagesLinkActivity::class.java)
            val bundle = Bundle()
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

    private fun openFloatingSubtitle() {
        mMenuPopupTranslate.visibility = View.INVISIBLE
        mMenuPopupColor.visibility = View.INVISIBLE

        if (mFloatingSubtitleReader.isShowing)
            mFloatingSubtitleReader.dismiss()
        else {
            if (canDrawOverlays(applicationContext)) {
                verifySubtitle()
                mFloatingSubtitleReader.show()
            } else
                startManageDrawOverlaysPermission()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GeneralConsts.REQUEST.PERMISSION_DRAW_OVERLAYS -> {
                if (canDrawOverlays(applicationContext)) {
                    verifySubtitle()
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
            startActivityForResult(it, GeneralConsts.REQUEST.PERMISSION_DRAW_OVERLAYS)
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
            .setItems(items) { _, selected ->
                val language = mapLanguage[items[selected]]
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

    //Force floating subtitle aways on top
    private fun openFloatingWindow() {
        mFloatingWindowOcr.show()
        mFloatingSubtitleReader.forceZIndex()
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

    override fun setText(texts: ArrayList<String>) {
        if (::mFloatingSubtitleReader.isInitialized) {
            mViewModel.mIsAlertSubtitle = true
            mViewModel.addOcrItem(texts)
            mFloatingSubtitleReader.showWithoutDismiss()
            mFloatingSubtitleReader.changeLayout(false)
        }
    }

    override fun clearList() {
        mViewModel.clearOcrItem()
    }

}
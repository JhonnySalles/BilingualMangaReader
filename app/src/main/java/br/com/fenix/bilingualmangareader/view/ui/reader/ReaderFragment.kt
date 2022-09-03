package br.com.fenix.bilingualmangareader.view.ui.reader

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.SparseArray
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Library
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.model.enums.PageMode
import br.com.fenix.bilingualmangareader.model.enums.Position
import br.com.fenix.bilingualmangareader.model.enums.ReaderMode
import br.com.fenix.bilingualmangareader.service.controller.SubTitleController
import br.com.fenix.bilingualmangareader.service.parses.Parse
import br.com.fenix.bilingualmangareader.service.parses.ParseFactory
import br.com.fenix.bilingualmangareader.service.parses.RarParse
import br.com.fenix.bilingualmangareader.service.repository.Storage
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.util.constants.ReaderConsts
import br.com.fenix.bilingualmangareader.util.helpers.LibraryUtil
import br.com.fenix.bilingualmangareader.util.helpers.Util
import br.com.fenix.bilingualmangareader.view.components.PageImageView
import br.com.fenix.bilingualmangareader.view.components.PageViewPager
import br.com.fenix.bilingualmangareader.view.managers.MangaHandler
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.button.MaterialButton
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import com.squareup.picasso.RequestHandler
import com.squareup.picasso.Target
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.ref.WeakReference
import kotlin.math.max
import kotlin.math.min


class ReaderFragment : Fragment(), View.OnTouchListener {

    private val mLOGGER = LoggerFactory.getLogger(ReaderFragment::class.java)

    private val mViewModel: ReaderViewModel by activityViewModels()

    private lateinit var mRoot: CoordinatorLayout
    private lateinit var mToolbarTop: AppBarLayout
    private lateinit var mPageNavLayout: LinearLayout
    private lateinit var mPopupSubtitle: FrameLayout
    private lateinit var mPopupColor: FrameLayout
    private lateinit var mToolbarBottom: LinearLayout
    private lateinit var mPageSeekBar: SeekBar
    private lateinit var mPageNavTextView: TextView
    private lateinit var mPagerAdapter: ComicPagerAdapter
    private lateinit var mPreferences: SharedPreferences
    private lateinit var mGestureDetector: GestureDetector
    private lateinit var mViewPager: PageViewPager
    private lateinit var mPreviousButton: MaterialButton
    private lateinit var mNextButton: MaterialButton

    private var mResourceViewMode: HashMap<Int, ReaderMode> = HashMap()
    private var mIsFullscreen = false
    private var mFileName: String? = null
    var mReaderMode: ReaderMode = ReaderMode.FIT_WIDTH
    var mUseMagnifierType = false
    var mIsLeftToRight = false

    var mParse: Parse? = null
    lateinit var mPicasso: Picasso
    private lateinit var mComicHandler: MangaHandler
    var mTargets = SparseArray<Target>()

    private lateinit var mLibrary: Library
    private var mManga: Manga? = null
    private var mNewManga: Manga? = null
    private var mNewMangaTitle = 0
    private lateinit var mStorage: Storage
    private lateinit var mSubtitleController: SubTitleController

    init {
        mResourceViewMode[R.id.view_mode_aspect_fill] = ReaderMode.ASPECT_FILL
        mResourceViewMode[R.id.view_mode_aspect_fit] = ReaderMode.ASPECT_FIT
        mResourceViewMode[R.id.view_mode_fit_width] = ReaderMode.FIT_WIDTH
    }

    companion object {
        var mCurrentPage = 0
        private var mCacheFolderIndex = 0
        private val mCacheFolder = arrayOf(
            GeneralConsts.CACHE_FOLDER.A,
            GeneralConsts.CACHE_FOLDER.B,
            GeneralConsts.CACHE_FOLDER.C,
            GeneralConsts.CACHE_FOLDER.D
        )

        fun create(): ReaderFragment {
            if (mCacheFolderIndex >= 2)
                mCacheFolderIndex = 0
            else
                mCacheFolderIndex += 1

            val fragment = ReaderFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }

        fun create(library: Library, path: File): ReaderFragment {
            if (mCacheFolderIndex >= 2)
                mCacheFolderIndex = 0
            else
                mCacheFolderIndex += 1

            val fragment = ReaderFragment()
            val args = Bundle()
            args.putSerializable(GeneralConsts.KEYS.OBJECT.LIBRARY, library)
            args.putSerializable(GeneralConsts.KEYS.OBJECT.FILE, path)
            fragment.arguments = args
            return fragment
        }

        fun create(library: Library, manga: Manga): ReaderFragment {
            if (mCacheFolderIndex >= 2)
                mCacheFolderIndex = 0
            else
                mCacheFolderIndex += 1

            val fragment = ReaderFragment()
            val args = Bundle()
            args.putSerializable(GeneralConsts.KEYS.OBJECT.LIBRARY, library)
            args.putSerializable(GeneralConsts.KEYS.OBJECT.MANGA, manga)
            fragment.arguments = args
            return fragment
        }
    }

    private var mCurrentFragment: FrameLayout? = null
    fun getCurrencyImageView(): PageImageView? {
        if (mCurrentFragment == null)
            return null
        return mCurrentFragment?.findViewById(R.id.page_image_view) as PageImageView
    }

    private fun onRefresh() {
        if (!::mViewPager.isInitialized)
            return

        if (mTargets.size() > 0) {
            loadImage(mTargets[mViewPager.currentItem] as MyTarget)
            for (i in 0 until mTargets.size()) {
                if (mViewPager.currentItem != i)
                    loadImage(mTargets[mTargets.keyAt(i)] as MyTarget)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setFullscreen(fullscreen = true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mCurrentPage = 0
        mStorage = Storage(requireContext())
        mLibrary = LibraryUtil.getDefault(requireContext())

        val bundle: Bundle? = arguments
        if (bundle != null) {
            mLibrary = bundle.getSerializable(GeneralConsts.KEYS.OBJECT.LIBRARY) as Library

            mManga = bundle.getSerializable(GeneralConsts.KEYS.OBJECT.MANGA) as Manga?
            val file: File? = if (mManga != null) {
                mManga?.file
                if (mManga?.file != null)
                    mManga?.file
                else
                    File(mManga?.path!!)
            } else
                bundle.getSerializable(GeneralConsts.KEYS.OBJECT.FILE) as File?

            if (file != null) {
                if (mManga == null)
                    mManga = mStorage.findByName(file.name)

                if (mManga != null) {
                    mCurrentPage = mManga!!.bookMark
                    mStorage.updateLastAccess(mManga!!)
                }

                mParse = ParseFactory.create(file)
                if (mParse != null) {
                    mSubtitleController = SubTitleController.getInstance(requireContext())
                    mSubtitleController.getListChapter(mManga, mParse!!)
                    mSubtitleController.mReaderFragment = this
                    mFileName = file.name
                    mCurrentPage = max(1, min(mCurrentPage, mParse!!.numPages()))
                    mComicHandler = MangaHandler(mParse)
                    mPicasso = Picasso.Builder(requireContext())
                        .addRequestHandler((mComicHandler as RequestHandler))
                        .build()
                } else
                    mLOGGER.info("Error in open file.")
            } else {
                mLOGGER.info("File not founded.")
                AlertDialog.Builder(requireActivity(), R.style.AppCompatAlertDialogStyle)
                    .setTitle(getString(R.string.manga_excluded))
                    .setMessage(getString(R.string.file_not_found))
                    .setPositiveButton(
                        R.string.action_neutral
                    ) { _, _ -> }
                    .create()
                    .show()
            }

            mPagerAdapter = ComicPagerAdapter()
            mGestureDetector = GestureDetector(requireActivity(), MyTouchListener())

            mPreferences = GeneralConsts.getSharedPreferences(requireContext())
            mReaderMode = ReaderMode.valueOf(
                mPreferences.getString(
                    GeneralConsts.KEYS.READER.READER_MODE,
                    ReaderMode.FIT_WIDTH.toString()
                ).toString()
            )

            mUseMagnifierType = mPreferences.getBoolean(
                GeneralConsts.KEYS.READER.USE_MAGNIFIER_TYPE,
                false
            )

            mIsLeftToRight = PageMode.valueOf(
                mPreferences.getString(
                    GeneralConsts.KEYS.READER.PAGE_MODE,
                    PageMode.Comics.toString()
                )!!
            ) == PageMode.Comics

            // workaround: extract rar archive
            if (mParse is RarParse) {
                val child = mCacheFolder[mCacheFolderIndex]
                val cacheDir = File(GeneralConsts.getCacheDir(requireContext()), child)
                if (!cacheDir.exists()) {
                    cacheDir.mkdir()
                } else {
                    if (cacheDir.listFiles() != null)
                        for (f in cacheDir.listFiles()!!)
                            f.delete()
                }
                (mParse as RarParse?)!!.setCacheDirectory(cacheDir)
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_reader, container, false)

        if (mParse == null) {
            val imageError = view.findViewById<ImageView>(R.id.image_error)
            imageError.visibility = View.VISIBLE
            return view
        }

        mRoot = requireActivity().findViewById(R.id.root_activity_reader)
        mToolbarTop = requireActivity().findViewById(R.id.toolbar_reader_top)
        mPopupSubtitle = requireActivity().findViewById(R.id.menu_popup_translate)
        mPopupColor = requireActivity().findViewById(R.id.menu_popup_color)
        mPageNavLayout = requireActivity().findViewById(R.id.nav_reader)
        mToolbarBottom = requireActivity().findViewById(R.id.toolbar_reader_bottom)
        mPreviousButton = requireActivity().findViewById(R.id.nav_previous_file)
        mNextButton = requireActivity().findViewById(R.id.nav_next_file)

        (mPageNavLayout.findViewById<View>(R.id.nav_reader_progress) as SeekBar).also {
            mPageSeekBar = it
        }
        mPageSeekBar.max = (mParse?.numPages() ?: 2) - 1

        mPageSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    if (mIsLeftToRight) setCurrentPage(progress + 1) else setCurrentPage(
                        mPageSeekBar.max - progress + 1
                    )
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                mPicasso.pauseTag(this@ReaderFragment.requireActivity())
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                mPicasso.resumeTag(this@ReaderFragment.requireActivity())
            }
        })
        mPageNavTextView = mPageNavLayout.findViewById<View>(R.id.nav_reader_title) as TextView
        mViewPager = view.findViewById<View>(R.id.fragment_reader) as PageViewPager
        mViewPager.adapter = mPagerAdapter
        mViewPager.offscreenPageLimit = ReaderConsts.READER.OFF_SCREEN_PAGE_LIMIT
        mViewPager.setOnTouchListener(this)
        mViewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                if (mIsLeftToRight)
                    setCurrentPage(position + 1)
                else
                    setCurrentPage(mViewPager.adapter!!.count - position)
            }
        })
        mViewPager.setOnSwipeOutListener(object : PageViewPager.OnSwipeOutListener {
            override fun onSwipeOutAtStart() {
                if (mIsLeftToRight) hitBeginning() else hitEnding()
            }

            override fun onSwipeOutAtEnd() {
                if (mIsLeftToRight) hitEnding() else hitBeginning()
            }
        })
        if (mCurrentPage != -1)
            setCurrentPage(mCurrentPage)

        if (savedInstanceState != null) {
            val fullscreen = savedInstanceState.getBoolean(ReaderConsts.STATES.STATE_FULLSCREEN)
            setFullscreen(fullscreen)
            val newComicId = savedInstanceState.getLong(ReaderConsts.STATES.STATE_NEW_COMIC)
            val titleRes = savedInstanceState.getInt(ReaderConsts.STATES.STATE_NEW_COMIC_TITLE)
            confirmSwitch(mStorage.get(newComicId), titleRes)
        } else {
            setFullscreen(true)
        }
        requireActivity().title = mFileName
        updateSeekBar()

        mViewModel.filters.observe(viewLifecycleOwner) { onRefresh() }

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_reader, menu)
        when (mReaderMode) {
            ReaderMode.ASPECT_FILL -> menu.findItem(R.id.view_mode_aspect_fill).isChecked = true
            ReaderMode.ASPECT_FIT -> menu.findItem(R.id.view_mode_aspect_fit).isChecked = true
            ReaderMode.FIT_WIDTH -> menu.findItem(R.id.view_mode_fit_width).isChecked = true
        }
        if (mIsLeftToRight)
            menu.findItem(R.id.reading_left_to_right).isChecked = true
        else
            menu.findItem(R.id.reading_right_to_left).isChecked = true

        menu.findItem(R.id.menu_item_use_magnifier_type).isChecked = mUseMagnifierType
        menu.findItem(R.id.menu_item_show_clock_and_battery).isChecked = mPreferences.getBoolean(GeneralConsts.KEYS.READER.SHOW_CLOCK_AND_BATTERY, false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(ReaderConsts.STATES.STATE_FULLSCREEN, isFullscreen())
        outState.putLong(
            ReaderConsts.STATES.STATE_NEW_COMIC,
            (if (mNewManga != null) mNewManga!!.id else -1)!!
        )
        outState.putInt(
            ReaderConsts.STATES.STATE_NEW_COMIC_TITLE,
            if (mNewManga != null) mNewMangaTitle else -1
        )
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        if (mManga != null) {
            mManga?.bookMark = getCurrentPage()
            mStorage.updateBookMark(mManga!!)
        }
        super.onPause()
    }

    override fun onDestroy() {
        mSubtitleController.mReaderFragment = null
        Util.destroyParse(mParse)
        mPicasso.shutdown()
        super.onDestroy()
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        v?.performClick()
        return mGestureDetector.onTouchEvent(event)
    }

    fun getCurrentPage(): Int {
        return when {
            mIsLeftToRight -> if (::mViewPager.isInitialized) mViewPager.currentItem.plus(1) else 1
            ::mViewPager.isInitialized && mViewPager.adapter != null -> (mViewPager.adapter!!.count - mViewPager.currentItem)
            else -> 1
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.view_mode_aspect_fill, R.id.view_mode_aspect_fit, R.id.view_mode_fit_width -> {
                item.isChecked = true
                mReaderMode = mResourceViewMode[item.itemId] ?: ReaderMode.FIT_WIDTH
                updatePageViews(mViewPager) {
                    if (mReaderMode === ReaderMode.ASPECT_FILL) it.setTranslateToRightEdge(
                        !mIsLeftToRight
                    )
                    it.setViewMode(mReaderMode)
                }
            }
            R.id.reading_left_to_right, R.id.reading_right_to_left -> {
                item.isChecked = true
                val page = getCurrentPage()
                mIsLeftToRight = item.itemId == R.id.reading_left_to_right
                setCurrentPage(page, false)
                mViewPager.adapter?.notifyDataSetChanged()
                updateSeekBar()
            }
            R.id.menu_item_use_magnifier_type -> {
                item.isChecked = !item.isChecked
                mUseMagnifierType = item.isChecked
                updatePageViews(mViewPager) {
                    it.useMagnifierType = mUseMagnifierType
                }

                with(mPreferences.edit()) {
                    this.putBoolean(
                        GeneralConsts.KEYS.READER.USE_MAGNIFIER_TYPE,
                        mUseMagnifierType
                    )
                    this.commit()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun changeAspect(toolbar: Toolbar, mode: ReaderMode) {
        mReaderMode = mode
        updatePageViews(mViewPager) {
            if (mReaderMode === ReaderMode.ASPECT_FILL) it.setTranslateToRightEdge(
                !mIsLeftToRight
            )
            it.setViewMode(mReaderMode)
        }

        val id: Int = mResourceViewMode.filterValues { it == mode }.keys.first()
        val menuItem: MenuItem = toolbar.menu.findItem(id)
        menuItem.isChecked = true
    }

    fun setCurrentPage(page: Int) {
        setCurrentPage(page, true)
    }

    private fun setCurrentPage(page: Int, animated: Boolean) {
        if (mIsLeftToRight) {
            mViewPager.currentItem = page - 1
            mPageSeekBar.progress = page - 1
        } else {
            mViewPager.setCurrentItem(mViewPager.adapter!!.count - page, animated)
            mPageSeekBar.progress = mViewPager.adapter!!.count - page
        }
        val navPage: String = StringBuilder()
            .append(page).append("/").append(mParse?.numPages() ?: 1)
            .toString()
        mPageNavTextView.text = navPage
        mCurrentPage = page - 1

        if (mCurrentPage < 0)
            mCurrentPage = 0

        if (mManga != null)
            mSubtitleController.changeSubtitleInReader(mManga!!, mCurrentPage)

        (requireActivity() as ReaderActivity).changePage(mManga?.title ?: "", mParse?.getPagePath(mCurrentPage) ?: "", mCurrentPage + 1)
    }

    inner class ComicPagerAdapter : PagerAdapter() {
        override fun isViewFromObject(view: View, o: Any): Boolean {
            return view === o
        }

        override fun getCount(): Int {
            return mParse?.numPages() ?: 1
        }

        override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
            if (mCurrentFragment !== `object`)
                mCurrentFragment = `object` as FrameLayout
            super.setPrimaryItem(container, position, `object`)
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val inflater =
                requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val layout: View = inflater.inflate(R.layout.fragment_reader_page, container, false)
            val pageImageView: PageImageView =
                layout.findViewById<View>(R.id.page_image_view) as PageImageView
            if (mReaderMode === ReaderMode.ASPECT_FILL) pageImageView.setTranslateToRightEdge(!mIsLeftToRight)
            pageImageView.setViewMode(mReaderMode)
            pageImageView.useMagnifierType = mUseMagnifierType
            pageImageView.setOnTouchListener(this@ReaderFragment)
            container.addView(layout)
            val t = MyTarget(layout, position)
            loadImage(t)
            mTargets.put(position, t)
            return layout
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            val layout = `object` as View
            mPicasso.cancelRequest(mTargets[position])
            mTargets.delete(position)
            container.removeView(layout)
            val iv = layout.findViewById<View>(R.id.page_image_view) as ImageView
            val drawable = iv.drawable
            if (drawable is BitmapDrawable) {
                val bm = drawable.bitmap
                bm?.recycle()
            }
        }
    }

    fun loadImage(t: Target, position: Int, resize: Boolean = true) {
        try {
            val request = mPicasso.load(mComicHandler.getPageUri(position))
                .memoryPolicy(MemoryPolicy.NO_STORE)
                .tag(requireActivity())

            if (resize)
                request.resize(ReaderConsts.READER.MAX_PAGE_WIDTH, ReaderConsts.READER.MAX_PAGE_HEIGHT)
                    .centerInside()
                    .onlyScaleDown()

            request.transform(mViewModel.filters.value!!)
                .into(t)
        } catch (e: Exception) {
            mLOGGER.error("Error in open image: " + e.message, e)
        }

    }

    fun loadImage(t: Target, path: Uri, resize: Boolean = true) {
        try {
            val request = mPicasso.load(path)
                .memoryPolicy(MemoryPolicy.NO_STORE)
                .tag(requireActivity())

            if (resize)
                request.resize(ReaderConsts.READER.MAX_PAGE_WIDTH, ReaderConsts.READER.MAX_PAGE_HEIGHT)
                    .centerInside()
                    .onlyScaleDown()

            request.transform(mViewModel.filters.value!!)
                .into(t)
        } catch (e: Exception) {
            mLOGGER.error("Error in open image: " + e.message, e)
        }

    }

    fun loadImage(t: MyTarget) {
        val pos: Int = if (mIsLeftToRight)
            t.position
        else
            mViewPager.adapter!!.count - t.position - 1

        try {
            mPicasso.load(mComicHandler.getPageUri(pos))
                .memoryPolicy(MemoryPolicy.NO_STORE)
                .tag(requireActivity())
                .resize(ReaderConsts.READER.MAX_PAGE_WIDTH, ReaderConsts.READER.MAX_PAGE_HEIGHT)
                .centerInside()
                .onlyScaleDown()
                .transform(mViewModel.filters.value!!)
                .into(t)
        } catch (e: Exception) {
            mLOGGER.error("Error in open image: " + e.message, e)
        }

    }

    inner class MyTarget(layout: View, val position: Int) : Target,
        View.OnClickListener {
        private val mLayout: WeakReference<View> = WeakReference(layout)

        private fun setVisibility(imageView: Int, progressBar: Int, reloadButton: Int) {
            val layout = mLayout.get() ?: return
            layout.findViewById<View>(R.id.page_image_view).visibility = imageView
            layout.findViewById<View>(R.id.load_progress_bar).visibility = progressBar
            layout.findViewById<View>(R.id.reload_Button).visibility = reloadButton
        }

        override fun onBitmapLoaded(bitmap: Bitmap, from: LoadedFrom) {
            val layout = mLayout.get() ?: return
            setVisibility(View.VISIBLE, View.GONE, View.GONE)
            val iv = layout.findViewById<View>(R.id.page_image_view) as ImageView
            iv.setImageBitmap(bitmap)
        }

        override fun onBitmapFailed(p0: Exception, errorDrawable: Drawable?) {
            val layout = mLayout.get() ?: return
            setVisibility(View.GONE, View.GONE, View.VISIBLE)
            val ib = layout.findViewById<View>(R.id.reload_Button) as ImageButton
            ib.setOnClickListener(this)
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {

        }

        override fun onClick(v: View) {
            mLayout.get() ?: return
            setVisibility(View.GONE, View.VISIBLE, View.GONE)
            loadImage(this)
        }

    }

    inner class MyTouchListener : SimpleOnGestureListener() {

        override fun onLongPress(e: MotionEvent?) {
            super.onLongPress(e)
            if (e == null) return
            val view: PageImageView = getCurrencyImageView() ?: return
            val coordinator = view.getPointerCoordinate(e)
            mSubtitleController.selectTextByCoordinate(coordinator)
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            if (!isFullscreen()) {
                setFullscreen(fullscreen = true)
                return true
            }

            val position = getPosition(e)
            if ((requireActivity() as ReaderActivity).touchPosition(position))
                return true

            if (position == Position.LEFT || position == Position.RIGHT) {
                val view = getCurrencyImageView()
                view?.let {
                    if (it.autoScroll(position == Position.LEFT))
                        return true
                }
            }

            when (position) {
                Position.LEFT -> {
                    if (mIsLeftToRight) {
                        if (getCurrentPage() == 1) hitBeginning() else setCurrentPage(getCurrentPage() - 1)
                    } else {
                        if (getCurrentPage() == mViewPager.adapter!!.count
                        ) hitEnding() else setCurrentPage(getCurrentPage() + 1)
                    }
                }
                Position.RIGHT -> {
                    if (mIsLeftToRight) {
                        if (getCurrentPage() == mViewPager.adapter!!.count
                        ) hitEnding() else setCurrentPage(getCurrentPage() + 1)
                    } else {
                        if (getCurrentPage() == 1) hitBeginning() else setCurrentPage(getCurrentPage() - 1)
                    }
                }
                Position.CENTER -> setFullscreen(fullscreen = false)
                else -> setFullscreen(fullscreen = false)
            }

            return true
        }
    }

    private fun getPosition(e: MotionEvent): Position {
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        val horizontalSize = resources.getDimensionPixelSize(R.dimen.reader_touch_demonstration_initial_horizontal)
        val horizontal = (if (isLandscape) horizontalSize * 1.2 else horizontalSize * 3).toFloat()

        val x = e.x
        val y = e.y
        val divider = if (isLandscape) 5 else 3

        val height = Resources.getSystem().displayMetrics.heightPixels
        val width = Resources.getSystem().displayMetrics.widthPixels

        if (x < width / divider) {
            return if (y <= horizontal)
                Position.CORNER_TOP_LEFT
            else if (y >= (height - horizontal))
                Position.CORNER_BOTTOM_LEFT
            else
                Position.LEFT
        } else if (x > width / divider * (divider - 1)) {
            return if (y <= horizontal)
                Position.CORNER_TOP_RIGHT
            else if (y >= (height - horizontal))
                Position.CORNER_BOTTOM_RIGHT
            else
                Position.RIGHT
        } else {
            return if (y <= horizontal)
                Position.TOP
            else if (y >= (height - horizontal))
                Position.BOTTOM
            else
                Position.CENTER
        }
    }

    private fun updatePageViews(parentView: ViewGroup, change: (PageImageView) -> (Unit)) {
        for (i in 0 until parentView.childCount) {
            val child = parentView.getChildAt(i)
            if (child is ViewGroup) {
                updatePageViews(child, change)
            } else if (child is PageImageView) {
                val view: PageImageView = child
                change(view)
            }
        }
    }

    private fun getActionBar(): ActionBar? {
        return (requireActivity() as AppCompatActivity).supportActionBar
    }

    private val windowInsetsController by lazy { WindowInsetsControllerCompat(requireActivity().window, mViewPager) }

    fun setFullscreen(fullscreen: Boolean) {
        mIsFullscreen = fullscreen
        val w: Window = requireActivity().window
        if (fullscreen) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                windowInsetsController.let {
                    it.hide(WindowInsets.Type.systemBars())
                    it.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
                WindowCompat.setDecorFitsSystemWindows(w, true)
                w.setDecorFitsSystemWindows(false)
            } else {
                getActionBar()?.hide()
                @Suppress("DEPRECATION")
                mViewPager.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN // Hide top iu
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // Hide navigator
                        or View.SYSTEM_UI_FLAG_IMMERSIVE // Force navigator hide
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY // Force top iu hide
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // Force full screen
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE // Stable transition on fullscreen and immersive
                        )

                Handler(Looper.getMainLooper()).postDelayed({
                    w.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    w.addFlags(ContextCompat.getColor(requireContext(), R.color.transparent))
                }, 300)
            }

            mPopupSubtitle.visibility = View.GONE
            mPopupColor.visibility = View.GONE
            mRoot.fitsSystemWindows = false
            changeContentsVisibility(fullscreen)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                windowInsetsController.let {
                    it.show(WindowInsets.Type.systemBars())
                }
                //w.setDecorFitsSystemWindows(true)
                WindowCompat.setDecorFitsSystemWindows(w, false)
            } else {
                getActionBar()?.show()
                @Suppress("DEPRECATION")
                mViewPager.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)

                Handler(Looper.getMainLooper()).postDelayed({
                    w.clearFlags(ContextCompat.getColor(requireContext(), R.color.transparent))
                    w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                }, 300)
            }

            w.statusBarColor = resources.getColor(R.color.black)
            w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            w.navigationBarColor = resources.getColor(R.color.black)

            //mRoot.fitsSystemWindows = true
            changeContentsVisibility(fullscreen)
        }
    }

    private val duration = 200L
    private fun changeContentsVisibility(isFullScreen: Boolean) {
        val visibility = if (isFullScreen) View.GONE else View.VISIBLE
        val finalAlpha = if (isFullScreen) 0.0f else 1.0f
        val initialAlpha = if (isFullScreen) 1.0f else 0.0f
        val initialTranslation = if (isFullScreen) 0f else -50f
        val finalTranslation = if (isFullScreen) -50f else 0f

        if (!isFullScreen) {
            mPageNavLayout.visibility = visibility
            mToolbarBottom.visibility = visibility
            mToolbarTop.visibility = visibility
            mNextButton.visibility = visibility
            mPreviousButton.visibility = visibility

            mPageNavLayout.alpha = initialAlpha
            mToolbarTop.alpha = initialAlpha
            mToolbarBottom.alpha = initialAlpha
            mNextButton.alpha = initialAlpha
            mPreviousButton.alpha = initialAlpha

            mToolbarTop.translationY = initialTranslation
            mToolbarBottom.translationY = (initialTranslation*-1)
        }

        mPageNavLayout.animate().alpha(finalAlpha).setDuration(duration)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    mPageNavLayout.visibility = visibility
                }
            })

        mToolbarBottom.animate().alpha(finalAlpha).translationY(finalTranslation *-1)
            .setDuration(duration).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    mToolbarBottom.visibility = visibility
                }
            })

        mToolbarTop.animate().alpha(finalAlpha).translationY(finalTranslation)
            .setDuration(duration).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    mToolbarTop.visibility = visibility
                }
            })

        mNextButton.animate().alpha(finalAlpha).setDuration(duration)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    mNextButton.visibility = visibility
                }
            })

        mPreviousButton.animate().alpha(finalAlpha).setDuration(duration)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    mPreviousButton.visibility = visibility
                }
            })
    }

    fun isFullscreen(): Boolean =
        mIsFullscreen

    fun hitBeginning() {
        if (mManga != null) {
            val c: Manga? = mStorage.getPrevManga(mLibrary, mManga!!)
            confirmSwitch(c, R.string.switch_prev_comic)
        }
    }

    fun hitEnding() {
        if (mManga != null) {
            val c: Manga? = mStorage.getNextManga(mLibrary, mManga!!)
            confirmSwitch(c, R.string.switch_next_comic)
        }
    }

    private fun confirmSwitch(newManga: Manga?, titleRes: Int) {
        if (newManga == null) return
        var confirm = false
        mNewManga = newManga
        mNewMangaTitle = titleRes
        val dialog: AlertDialog =
            AlertDialog.Builder(requireActivity(), R.style.AppCompatAlertDialogStyle)
                .setTitle(titleRes)
                .setMessage(newManga.file.name)
                .setPositiveButton(
                    R.string.switch_action_positive
                ) { _, _ ->
                    confirm = true
                    val activity = requireActivity() as ReaderActivity
                    activity.changeManga(mNewManga!!)
                }
                .setNegativeButton(
                    R.string.switch_action_negative
                ) { _, _ -> }
                .setOnDismissListener {
                    if (!confirm) {
                        mNewManga = null
                        setFullscreen(fullscreen = true)
                    }
                }
                .create()
        dialog.show()
    }

    private fun updateSeekBar() {
        val seekRes: Int =
            if (mIsLeftToRight) R.drawable.reader_nav_progress else R.drawable.reader_nav_progress_inverse
        val d: Drawable? = ContextCompat.getDrawable(requireActivity(), seekRes)
        val bounds = mPageSeekBar.progressDrawable.bounds
        mPageSeekBar.progressDrawable = d
        mPageSeekBar.progressDrawable.bounds = bounds
    }
}
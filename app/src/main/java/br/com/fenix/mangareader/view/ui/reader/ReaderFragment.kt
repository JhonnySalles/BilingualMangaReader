package br.com.fenix.mangareader.view.ui.reader

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.SparseArray
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import br.com.fenix.mangareader.R
import br.com.fenix.mangareader.model.entity.Manga
import br.com.fenix.mangareader.model.enums.PageMode
import br.com.fenix.mangareader.model.enums.ReaderMode
import br.com.fenix.mangareader.service.controller.SubTitleController
import br.com.fenix.mangareader.service.parses.Parse
import br.com.fenix.mangareader.service.parses.ParseFactory
import br.com.fenix.mangareader.service.parses.RarParse
import br.com.fenix.mangareader.service.repository.Storage
import br.com.fenix.mangareader.util.constants.GeneralConsts
import br.com.fenix.mangareader.util.constants.ReaderConsts
import br.com.fenix.mangareader.view.managers.MangaHandler
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import com.squareup.picasso.RequestHandler
import com.squareup.picasso.Target
import java.io.File
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.max
import kotlin.math.min

class ReaderFragment() : Fragment(), View.OnTouchListener {

    var mViewPager: PageViewPager? = null
    var mPageNavLayout: LinearLayout? = null
    var mPopupSubtitle: FrameLayout? = null
    var mPageSeekBar: SeekBar? = null
    var mPageNavTextView: TextView? = null
    var mPagerAdapter: ComicPagerAdapter? = null
    var mPreferences: SharedPreferences? = null
    var mGestureDetector: GestureDetector? = null

    private var mResourceViewMode: HashMap<Int, ReaderMode>? = null
    var mIsFullscreen = false
    var mCurrentPage = 0
    var mFilename: String? = null
    var mReaderMode: ReaderMode? = null
    var mIsLeftToRight = false

    var mParse: Parse? = null
    var mPicasso: Picasso? = null
    var mComicHandler: MangaHandler? = null
    val mTargets = SparseArray<Target>()

    private var mManga: Manga? = null
    var mNewManga: Manga? = null
    var mNewMangaTitle = 0
    private lateinit var mStorage: Storage

    init {
        mResourceViewMode = HashMap<Int, ReaderMode>();
        mResourceViewMode!![R.id.view_mode_aspect_fill] = ReaderMode.ASPECT_FILL
        mResourceViewMode!![R.id.view_mode_aspect_fit] = ReaderMode.ASPECT_FIT
        mResourceViewMode!![R.id.view_mode_fit_width] = ReaderMode.FIT_WIDTH
    }

    companion object {
        private var mCacheFolder = 0
        private const val mCacheFolder1 = "a"
        private const val mCacheFolder2 = "b"
        private const val mCacheFolder3 = "c"

        fun create(): ReaderFragment {
            if (mCacheFolder >= 2)
                mCacheFolder = 0
            else
                mCacheFolder += 1

            val fragment = ReaderFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }

        fun create(path: File): ReaderFragment {
            if (mCacheFolder >= 2)
                mCacheFolder = 0
            else
                mCacheFolder += 1

            val fragment = ReaderFragment()
            val args = Bundle()
            args.putSerializable(GeneralConsts.KEYS.OBJECT.FILE, path)
            fragment.arguments = args
            return fragment
        }

        fun create(manga: Manga): ReaderFragment {
            if (mCacheFolder >= 2)
                mCacheFolder = 0
            else
                mCacheFolder += 1

            val fragment = ReaderFragment()
            val args = Bundle()
            args.putSerializable(GeneralConsts.KEYS.OBJECT.MANGA, manga)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mStorage = Storage(requireContext())
        val bundle: Bundle? = arguments
        if (bundle != null) {
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

                if (mManga != null)
                    mCurrentPage = mManga!!.bookMark

                mParse = ParseFactory.create(file)
                if (mParse != null) {
                    SubTitleController.getListChapter(requireContext(), mParse!!)
                    mFilename = file.name
                    mCurrentPage = max(1, min(mCurrentPage, mParse!!.numPages()))
                    mComicHandler = MangaHandler(mParse)
                    mPicasso = Picasso.Builder(requireContext())
                        .addRequestHandler((mComicHandler as RequestHandler))
                        .build()
                } else
                    Log.e(GeneralConsts.TAG.LOG, "Erro ao abrir o arquivo.")
            } else
                Log.e(GeneralConsts.TAG.LOG, "Arquivo não encontrado.")

            mPagerAdapter = ComicPagerAdapter()
            mGestureDetector = GestureDetector(requireActivity(), MyTouchListener())

            mPreferences = GeneralConsts.getSharedPreferences(requireContext())
            mReaderMode = ReaderMode.valueOf(
                mPreferences!!.getString(
                    GeneralConsts.KEYS.READER.READER_MODE,
                    ReaderMode.FIT_WIDTH.toString()
                )
                    .toString()
            )

            mIsLeftToRight = PageMode.valueOf(
                mPreferences!!.getString(
                    GeneralConsts.KEYS.READER.PAGE_MODE,
                    PageMode.Comics.toString()
                )!!
            ) == PageMode.Comics

            // workaround: extract rar achive
            if (mParse is RarParse) {
                val child = when (mCacheFolder) {
                    0 -> mCacheFolder1
                    1 -> mCacheFolder2
                    else -> mCacheFolder3
                }
                val cacheDir = File(requireActivity().externalCacheDir, child)
                if (!cacheDir.exists()) {
                    cacheDir.mkdir()
                } else {
                    for (f in cacheDir.listFiles())
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

        mPopupSubtitle = requireActivity().findViewById(R.id.menu_popup)
        mPageNavLayout = requireActivity().findViewById(R.id.nav_reader)
        (mPageNavLayout!!.findViewById<View>(R.id.nav_reader_progress) as SeekBar).also {
            mPageSeekBar = it
        }
        mPageSeekBar!!.max = (mParse?.numPages() ?: 2) - 1

        mPageSeekBar!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    if (mIsLeftToRight) setCurrentPage(progress + 1) else setCurrentPage(
                        mPageSeekBar!!.max - progress + 1
                    )
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                mPicasso!!.pauseTag(this@ReaderFragment.requireActivity())
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                mPicasso!!.resumeTag(this@ReaderFragment.requireActivity())
            }
        })
        mPageNavTextView = mPageNavLayout!!.findViewById<View>(R.id.nav_reader_title) as TextView
        mViewPager = view.findViewById<View>(R.id.fragment_reader) as PageViewPager
        mViewPager!!.adapter = mPagerAdapter
        mViewPager!!.offscreenPageLimit = 3
        mViewPager!!.setOnTouchListener(this)
        mViewPager!!.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                if (mIsLeftToRight)
                    setCurrentPage(position + 1)
                else
                    setCurrentPage(mViewPager!!.adapter!!.count - position)
            }
        })
        mViewPager!!.setOnSwipeOutListener(object : PageViewPager.OnSwipeOutListener {
            override fun onSwipeOutAtStart() {
                if (mIsLeftToRight) hitBeginning() else hitEnding()
            }

            override fun onSwipeOutAtEnd() {
                if (mIsLeftToRight) hitEnding() else hitBeginning()
            }
        })
        if (mCurrentPage != -1) {
            setCurrentPage(mCurrentPage)
            mCurrentPage = -1
        }
        if (savedInstanceState != null) {
            val fullscreen = savedInstanceState.getBoolean(ReaderConsts.STATES.STATE_FULLSCREEN)
            setFullscreen(fullscreen)
            val newComicId = savedInstanceState.getLong(ReaderConsts.STATES.STATE_NEW_COMIC)
            val titleRes = savedInstanceState.getInt(ReaderConsts.STATES.STATE_NEW_COMIC_TITLE)
            confirmSwitch(mStorage.get(newComicId), titleRes)
        } else {
            setFullscreen(true)
        }
        requireActivity().title = mFilename
        updateSeekBar()
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
        try {
            mParse!!.destroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mPicasso?.shutdown()
        super.onDestroy()
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return mGestureDetector!!.onTouchEvent(event)
    }

    fun getCurrentPage(): Int {
        return when {
            mIsLeftToRight -> mViewPager?.currentItem?.plus(1) ?: 1
            mViewPager != null && mViewPager!!.adapter != null -> (mViewPager!!.adapter!!.count - mViewPager!!.currentItem)
            else -> 1
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val editor = mPreferences!!.edit()
        when (item.itemId) {
            R.id.view_mode_aspect_fill, R.id.view_mode_aspect_fit, R.id.view_mode_fit_width -> {
                item.isChecked = true
                mReaderMode = mResourceViewMode!![item.itemId]
                //editor.putInt(Constants.SETTINGS_PAGE_VIEW_MODE, mReaderMode!!.native_int)
                editor.apply()
                updatePageViews(mViewPager!!)
            }
            R.id.reading_left_to_right, R.id.reading_right_to_left -> {
                item.isChecked = true
                val page = getCurrentPage()
                mIsLeftToRight = item.itemId == R.id.reading_left_to_right
                //editor.putBoolean(Constants.SETTINGS_READING_LEFT_TO_RIGHT, mIsLeftToRight)
                editor.apply()
                setCurrentPage(page, false)
                mViewPager!!.adapter!!.notifyDataSetChanged()
                updateSeekBar()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun setCurrentPage(page: Int) {
        setCurrentPage(page, true)
    }

    private fun setCurrentPage(page: Int, animated: Boolean) {
        if (mIsLeftToRight) {
            mViewPager!!.currentItem = page - 1
            mPageSeekBar!!.progress = page - 1
        } else {
            mViewPager!!.setCurrentItem(mViewPager!!.adapter!!.count - page, animated)
            mPageSeekBar!!.progress = mViewPager!!.adapter!!.count - page
        }
        val navPage: String = StringBuilder()
            .append(page).append("/").append(mParse?.numPages() ?: 1)
            .toString()
        mPageNavTextView!!.text = navPage
        mCurrentPage = page - 1

        if (mCurrentPage < 0)
            mCurrentPage = 0

        if (mManga != null)
            SubTitleController.changeSubtitleInReader(requireContext(), mManga!!, mCurrentPage)
    }

    inner class ComicPagerAdapter : PagerAdapter() {
        override fun isViewFromObject(view: View, o: Any): Boolean {
            return view === o
        }

        override fun getCount(): Int {
            return mParse?.numPages() ?: 1
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val inflater =
                requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val layout: View = inflater.inflate(R.layout.fragment_reader_page, container, false)
            val pageImageView: PageImageView =
                layout.findViewById<View>(R.id.pageImageView) as PageImageView
            if (mReaderMode === ReaderMode.ASPECT_FILL) pageImageView.setTranslateToRightEdge(!mIsLeftToRight)
            pageImageView.setViewMode(mReaderMode)
            pageImageView.setOnTouchListener(this@ReaderFragment)
            container.addView(layout)
            val t = MyTarget(layout, position)
            loadImage(t)
            mTargets.put(position, t)
            return layout
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            val layout = `object` as View
            mPicasso!!.cancelRequest(mTargets[position])
            mTargets.delete(position)
            container.removeView(layout)
            val iv = layout.findViewById<View>(R.id.pageImageView) as ImageView
            val drawable = iv.drawable
            if (drawable is BitmapDrawable) {
                val bm = drawable.bitmap
                bm?.recycle()
            }
        }
    }

    fun loadImage(t: MyTarget) {
        val pos: Int = if (mIsLeftToRight)
            t.position
        else
            mViewPager!!.adapter!!.count - t.position - 1

        try {
            mPicasso!!.load(mComicHandler?.getPageUri(pos))
                .memoryPolicy(MemoryPolicy.NO_STORE)
                .tag(requireActivity())
                .resize(ReaderConsts.READER.MAX_PAGE_WIDTH, ReaderConsts.READER.MAX_PAGE_HEIGHT)
                .centerInside()
                .onlyScaleDown()
                .into(t)
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, "Erro ao carregar a imagem: " + e.message)
            Log.e(GeneralConsts.TAG.LOG, e.stackTraceToString())
        }

    }

    inner class MyTarget(layout: View, val position: Int) : Target,
        View.OnClickListener {
        private val mLayout: WeakReference<View> = WeakReference(layout);

        private fun setVisibility(imageView: Int, progressBar: Int, reloadButton: Int) {
            val layout = mLayout.get()
            layout!!.findViewById<View>(R.id.pageImageView).visibility = imageView
            layout.findViewById<View>(R.id.pageProgressBar).visibility = progressBar
            layout.findViewById<View>(R.id.reloadButton).visibility = reloadButton
        }

        override fun onBitmapLoaded(bitmap: Bitmap, from: LoadedFrom) {
            val layout = mLayout.get() ?: return
            setVisibility(View.VISIBLE, View.GONE, View.GONE)
            val iv = layout.findViewById<View>(R.id.pageImageView) as ImageView
            iv.setImageBitmap(bitmap)
        }

        override fun onBitmapFailed(p0: Exception, errorDrawable: Drawable?) {
            val layout = mLayout.get() ?: return
            setVisibility(View.GONE, View.GONE, View.VISIBLE)
            val ib = layout.findViewById<View>(R.id.reloadButton) as ImageButton
            ib.setOnClickListener(this)
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {

        }

        override fun onClick(v: View) {
            val layout = mLayout.get() ?: return
            setVisibility(View.GONE, View.VISIBLE, View.GONE)
            loadImage(this)
        }

    }

    inner class MyTouchListener : SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            if (!isFullscreen()) {
                setFullscreen(fullscreen = true, animated = true)
                return true
            }
            val x = e.x

            // tap left edge
            if (x < mViewPager!!.width.toFloat() / 3) {
                if (mIsLeftToRight) {
                    if (getCurrentPage() == 1) hitBeginning() else setCurrentPage(getCurrentPage() - 1)
                } else {
                    if (getCurrentPage() == mViewPager!!.adapter!!.count
                    ) hitEnding() else setCurrentPage(getCurrentPage() + 1)
                }
            } else if (x > mViewPager!!.width.toFloat() / 3 * 2) {
                if (mIsLeftToRight) {
                    if (getCurrentPage() == mViewPager!!.adapter!!.count
                    ) hitEnding() else setCurrentPage(getCurrentPage() + 1)
                } else {
                    if (getCurrentPage() == 1) hitBeginning() else setCurrentPage(getCurrentPage() - 1)
                }
            } else setFullscreen(fullscreen = false, animated = true)
            return true
        }
    }

    private fun updatePageViews(parentView: ViewGroup) {
        for (i in 0 until parentView.childCount) {
            val child = parentView.getChildAt(i)
            if (child is ViewGroup) {
                updatePageViews(child)
            } else if (child is PageImageView) {
                val view: PageImageView = child as PageImageView
                if (mReaderMode === ReaderMode.ASPECT_FILL) view.setTranslateToRightEdge(
                    !mIsLeftToRight
                )
                view.setViewMode(mReaderMode)
            }
        }
    }

    private fun getActionBar(): ActionBar? {
        return (requireActivity() as AppCompatActivity).supportActionBar
    }

    private fun setFullscreen(fullscreen: Boolean) {
        setFullscreen(fullscreen, false)
    }

    fun setFullscreen(fullscreen: Boolean, animated: Boolean) {
        mIsFullscreen = fullscreen
        val actionBar: ActionBar? = getActionBar()
        if (fullscreen) {
            actionBar?.hide()
            var flag = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
            flag = flag or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            flag = flag or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            flag = flag or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

            mViewPager!!.setSystemUiVisibility(flag)
            mPageNavLayout!!.visibility = View.INVISIBLE
            mPopupSubtitle!!.visibility = View.INVISIBLE
        } else {
            actionBar?.show()
            var flag = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            flag = flag or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            mViewPager!!.setSystemUiVisibility(flag)
            mPageNavLayout!!.visibility = View.VISIBLE
            mPopupSubtitle!!.visibility = View.VISIBLE

            // status bar & navigation bar background won't show in some cases

            Handler().postDelayed({
                val w: Window = requireActivity().window
                w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            }, 300)

        }
    }

    fun isFullscreen(): Boolean {
        return mIsFullscreen
    }

    fun hitBeginning() {
        if (mManga != null) {
            val c: Manga? = mStorage.getPrevManga(mManga!!)
            confirmSwitch(c, R.string.switch_prev_comic)
        }
    }

    fun hitEnding() {
        if (mManga != null) {
            val c: Manga? = mStorage.getNextManga(mManga!!)
            confirmSwitch(c, R.string.switch_next_comic)
        }
    }

    private fun confirmSwitch(newManga: Manga?, titleRes: Int) {
        if (newManga == null) return
        mNewManga = newManga
        mNewMangaTitle = titleRes
        val dialog: AlertDialog =
            AlertDialog.Builder(requireActivity(), R.style.AppCompatAlertDialogStyle)
                .setTitle(titleRes)
                .setMessage(newManga.file!!.name)
                .setPositiveButton(
                    R.string.switch_action_positive
                ) { _, _ ->
                    val activity = requireActivity() as ReaderActivity
                    activity.setFragment(create(mNewManga!!))
                }
                .setNegativeButton(
                    R.string.switch_action_negative
                ) { _, _ -> mNewManga = null }
                .create()
        dialog.show()
    }

    private fun updateSeekBar() {
        val seekRes: Int =
            if (mIsLeftToRight) R.drawable.reader_nav_progress else R.drawable.reader_nav_progress_inverse
        val d: Drawable = requireActivity().resources.getDrawable(seekRes)
        val bounds = mPageSeekBar!!.progressDrawable.bounds
        mPageSeekBar!!.progressDrawable = d
        mPageSeekBar!!.progressDrawable.bounds = bounds
    }
}
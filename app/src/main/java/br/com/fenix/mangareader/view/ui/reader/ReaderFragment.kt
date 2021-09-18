package br.com.fenix.mangareader.view.ui.reader

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
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
import br.com.fenix.mangareader.managers.BookHandler
import br.com.fenix.mangareader.model.entity.Book
import br.com.fenix.mangareader.model.enums.ReaderMode
import br.com.fenix.mangareader.service.parses.Parse
import br.com.fenix.mangareader.service.parses.ParseFactory
import br.com.fenix.mangareader.service.parses.RarParse
import br.com.fenix.mangareader.service.repository.Storage
import br.com.fenix.mangareader.util.constants.GeneralConsts
import br.com.fenix.mangareader.util.constants.ReaderConsts
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import com.squareup.picasso.RequestHandler
import com.squareup.picasso.Target
import java.io.File
import java.lang.ref.WeakReference
import java.util.*

class ReaderFragment() : Fragment(), View.OnTouchListener {
    val RESULT = 1

    val RESULT_CURRENT_PAGE = "fragment.reader.currentpage"

    val STATE_FULLSCREEN = "STATE_FULLSCREEN"
    val STATE_NEW_COMIC = "STATE_NEW_COMIC"
    val STATE_NEW_COMIC_TITLE = "STATE_NEW_COMIC_TITLE"

    var mViewPager: PageViewPager? = null
    var mPageNavLayout: LinearLayout? = null
    var mPageSeekBar: SeekBar? = null
    var mPageNavTextView: TextView? = null
    var mPagerAdapter: ComicPagerAdapter? = null
    var mPreferences: SharedPreferences? = null
    var mGestureDetector: GestureDetector? = null

    private var RESOURCE_VIEW_MODE: HashMap<Int, ReaderMode>? = null
    var mIsFullscreen = false
    var mCurrentPage = 0
    var mFilename: String? = null
    var mPageViewMode: ReaderMode? = null
    var mIsLeftToRight = false
    val mStartingX = 0f

    var mParse: Parse? = null
    var mPicasso: Picasso? = null
    var mComicHandler: BookHandler? = null
    val mTargets = SparseArray<Target>()

    private var mBook: Book? = null
    var mNewBook: Book? = null
    var mNewBookTitle = 0
    private lateinit var mStorage: Storage

    init {
        RESOURCE_VIEW_MODE = HashMap<Int, ReaderMode>();
        RESOURCE_VIEW_MODE!![R.id.view_mode_aspect_fill] = ReaderMode.ASPECT_FILL
        RESOURCE_VIEW_MODE!![R.id.view_mode_aspect_fit] = ReaderMode.ASPECT_FIT
        RESOURCE_VIEW_MODE!![R.id.view_mode_fit_width] = ReaderMode.FIT_WIDTH
    }

    companion object {
        fun create(comicId: Long): ReaderFragment? {
            val fragment = ReaderFragment()
            val args = Bundle()
            //args.putLong(PARAM_HANDLER, idBook)
            fragment.setArguments(args)
            return fragment
        }

        fun create(comicpath: File?): ReaderFragment? {
            val fragment = ReaderFragment()
            val args = Bundle()
            args.putSerializable(GeneralConsts.KEYS.OBJECT.FILE, comicpath)
            fragment.setArguments(args)
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mStorage = Storage(requireContext())
        val bundle: Bundle? = arguments
        if (bundle != null) {
            var file: File? = bundle.getSerializable(GeneralConsts.KEYS.OBJECT.FILE) as File?

            if (file != null) {
                mBook = mStorage.findByName(file.name)

                if (mBook != null)
                    mCurrentPage = mBook!!.bookMark

                mParse = ParseFactory.create(file)
                mFilename = file.name
            }

            mCurrentPage = Math.max(1, Math.min(mCurrentPage, mParse!!.numPages()))
            mComicHandler = BookHandler(mParse)
            mPicasso = Picasso.Builder(requireActivity())
                .addRequestHandler(( mComicHandler as RequestHandler))
                .build()
            mPagerAdapter = ComicPagerAdapter()
            mGestureDetector = GestureDetector(requireActivity(), MyTouchListener())
            // mPreferences = requireActivity().getSharedPreferences(Constants.SETTINGS_NAME, 0)
            //val viewModeInt = mPreferences!!.getInt(
            //  Constants.SETTINGS_PAGE_VIEW_MODE,
            // ReaderMode.ASPECT_FIT.native_int
            //)
            //mPageViewMode = ReaderMode.values().get(viewModeInt)
            mIsLeftToRight =
                true // mPreferences!!.getBoolean(Constants.SETTINGS_READING_LEFT_TO_RIGHT, true)

            // workaround: extract rar achive
            if (mParse is RarParse) {
                val cacheDir: File = File(requireActivity().getExternalCacheDir(), "c")
                if (!cacheDir.exists()) {
                    cacheDir.mkdir()
                } else {
                    for (f in cacheDir.listFiles()) {
                        f.delete()
                    }
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
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_reader, container, false)
        mPageNavLayout = requireActivity().findViewById(R.id.nav_reader)
        mPageSeekBar = mPageNavLayout!!.findViewById<View>(R.id.nav_reader_progress) as SeekBar
        mPageSeekBar!!.max = mParse!!.numPages() - 1
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
            val fullscreen = savedInstanceState.getBoolean(STATE_FULLSCREEN)
            setFullscreen(fullscreen)
            val newComicId = savedInstanceState.getLong(STATE_NEW_COMIC)
            if (newComicId != null) {
                val titleRes = savedInstanceState.getInt(STATE_NEW_COMIC_TITLE)
                confirmSwitch(mStorage.get(newComicId), titleRes)
            }
        } else {
            setFullscreen(true)
        }
        requireActivity().title = mFilename
        updateSeekBar()
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_reader, menu)
        when (mPageViewMode) {
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
        outState.putBoolean(STATE_FULLSCREEN, isFullscreen())
        outState.putLong(STATE_NEW_COMIC, (if (mNewBook != null) mNewBook!!.id else -1)!!)
        outState.putInt(STATE_NEW_COMIC_TITLE, if (mNewBook != null) mNewBookTitle else -1)
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        if (mBook != null)
            mBook!!.bookMark = getCurrentPage()

        super.onPause()
    }

    override fun onDestroy() {
        try {
            mParse!!.destroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mPicasso!!.shutdown()
        super.onDestroy()
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return mGestureDetector!!.onTouchEvent(event)
    }

    fun getCurrentPage(): Int {
        return if (mIsLeftToRight) mViewPager!!.currentItem + 1 else mViewPager!!.adapter!!
            .getCount() - mViewPager!!.currentItem
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val editor = mPreferences!!.edit()
        when (item.itemId) {
            R.id.view_mode_aspect_fill, R.id.view_mode_aspect_fit, R.id.view_mode_fit_width -> {
                item.isChecked = true
                mPageViewMode = RESOURCE_VIEW_MODE!![item.itemId]
                //editor.putInt(Constants.SETTINGS_PAGE_VIEW_MODE, mPageViewMode!!.native_int)
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

    open fun setCurrentPage(page: Int) {
        setCurrentPage(page, true)
    }

    open fun setCurrentPage(page: Int, animated: Boolean) {
        if (mIsLeftToRight) {
            mViewPager!!.setCurrentItem(page - 1)
            mPageSeekBar!!.progress = page - 1
        } else {
            mViewPager!!.setCurrentItem(mViewPager!!.getAdapter()!!.getCount() - page, animated)
            mPageSeekBar!!.progress = mViewPager!!.getAdapter()!!.getCount() - page
        }
        val navPage: String = StringBuilder()
            .append(page).append("/").append(mParse!!.numPages())
            .toString()
        mPageNavTextView!!.text = navPage
    }

    inner class ComicPagerAdapter : PagerAdapter() {
        override fun isViewFromObject(view: View, o: Any): Boolean {
            return view === o
        }

        override fun getCount(): Int {
            return mParse!!.numPages()
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val inflater = requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val layout: View = inflater.inflate(R.layout.fragment_reader_page, container, false)
            val pageImageView: PageImageView = layout.findViewById<View>(R.id.pageImageView) as PageImageView
            if (mPageViewMode === ReaderMode.ASPECT_FILL) pageImageView.setTranslateToRightEdge(!mIsLeftToRight)
            pageImageView.setViewMode(mPageViewMode)
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

    open fun loadImage(t: MyTarget) {
        val pos: Int
        pos = if (mIsLeftToRight) {
            t.position
        } else {
            mViewPager!!.adapter!!.count - t.position - 1
        }

        val mImageView = ImageView(context)

       mPicasso!!.load(mComicHandler!!.getPageUri(pos))
            .memoryPolicy(MemoryPolicy.NO_STORE)
            .tag(requireActivity())
            .resize(ReaderConsts.READER.MAX_PAGE_WIDTH, ReaderConsts.READER.MAX_PAGE_HEIGHT)
            .centerInside()
            .onlyScaleDown()
            .into(mImageView)
    }

    inner class MyTarget(layout: View, position: Int) : Target,
        View.OnClickListener {
        private val mLayout: WeakReference<View> = WeakReference(layout);
        val position: Int = position

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

        override fun onPrepareLoad(placeHolderDrawable: Drawable) {
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
                setFullscreen(true, true)
                return true
            }
            val x = e.x

            // tap left edge
            if (x < mViewPager!!.getWidth() as Float / 3) {
                if (mIsLeftToRight) {
                    if (getCurrentPage() == 1) hitBeginning() else setCurrentPage(getCurrentPage() - 1)
                } else {
                    if (getCurrentPage() == mViewPager!!.adapter!!.count
                    ) hitEnding() else setCurrentPage(getCurrentPage() + 1)
                }
            } else if (x > mViewPager!!.getWidth() as Float / 3 * 2) {
                if (mIsLeftToRight) {
                    if (getCurrentPage() == mViewPager!!.adapter!!.count
                    ) hitEnding() else setCurrentPage(getCurrentPage() + 1)
                } else {
                    if (getCurrentPage() == 1) hitBeginning() else setCurrentPage(getCurrentPage() - 1)
                }
            } else setFullscreen(false, true)
            return true
        }
    }

    open fun updatePageViews(parentView: ViewGroup) {
        for (i in 0 until parentView.childCount) {
            val child = parentView.getChildAt(i)
            if (child is ViewGroup) {
                updatePageViews(child)
            } else if (child is PageImageView) {
                val view: PageImageView = child as PageImageView
                if (mPageViewMode === ReaderMode.ASPECT_FILL) view.setTranslateToRightEdge(
                    !mIsLeftToRight
                )
                view.setViewMode(mPageViewMode)
            }
        }
    }

    open fun getActionBar(): ActionBar? {
        return (requireActivity() as AppCompatActivity).getSupportActionBar()
    }

    open fun setFullscreen(fullscreen: Boolean) {
        setFullscreen(fullscreen, false)
    }

    open fun setFullscreen(fullscreen: Boolean, animated: Boolean) {
        mIsFullscreen = fullscreen
        val actionBar: ActionBar? = getActionBar()
        if (fullscreen) {
            if (actionBar != null) actionBar.hide()
            var flag = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
            flag = flag or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            flag = flag or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            flag = flag or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

            mViewPager!!.setSystemUiVisibility(flag)
            mPageNavLayout!!.visibility = View.INVISIBLE
        } else {
            if (actionBar != null) actionBar.show()
            var flag = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            flag = flag or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            mViewPager!!.setSystemUiVisibility(flag)
            mPageNavLayout!!.visibility = View.VISIBLE

            // status bar & navigation bar background won't show in some cases

            Handler().postDelayed({
                val w: Window = requireActivity().window
                w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            }, 300)

        }
    }

    open fun isFullscreen(): Boolean {
        return mIsFullscreen
    }

    open fun hitBeginning() {
        if (mBook != null) {
            val c: Book? = mStorage.getPrevBook(mBook!!)
            confirmSwitch(c, R.string.switch_prev_comic)
        }
    }

    open fun hitEnding() {
        if (mBook != null) {
            val c: Book? = mStorage.getNextBook(mBook!!)
            confirmSwitch(c, R.string.switch_next_comic)
        }
    }

    open fun confirmSwitch(newBook: Book?, titleRes: Int) {
        if (newBook == null) return
        mNewBook = newBook
        mNewBookTitle = titleRes
        val dialog: AlertDialog =
            AlertDialog.Builder(requireActivity(), R.style.AppCompatAlertDialogStyle)
                .setTitle(titleRes)
                .setMessage(newBook.file!!.name)
                .setPositiveButton(R.string.switch_action_positive,
                    DialogInterface.OnClickListener { dialog, which ->
                        //val activity = requireActivity() as ReaderActivity
                        //activity.setFragment(ReaderFragment(mNewBook!!.id))
                    })
                .setNegativeButton(R.string.switch_action_negative,
                    DialogInterface.OnClickListener { dialog, which -> mNewBook = null })
                .create()
        dialog.show()
    }

    open fun updateSeekBar() {
        val seekRes: Int =
            if (mIsLeftToRight) R.drawable.reader_nav_progress else R.drawable.reader_nav_progress_inverse
        val d: Drawable = requireActivity().resources.getDrawable(seekRes)
        val bounds = mPageSeekBar!!.progressDrawable.bounds
        mPageSeekBar!!.progressDrawable = d
        mPageSeekBar!!.progressDrawable.bounds = bounds
    }
}
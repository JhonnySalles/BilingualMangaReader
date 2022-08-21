package br.com.fenix.bilingualmangareader.view.ui.pages_link

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Point
import android.graphics.Rect
import android.os.*
import android.util.TypedValue
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.FileLink
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.model.entity.PageLink
import br.com.fenix.bilingualmangareader.model.enums.Languages
import br.com.fenix.bilingualmangareader.model.enums.LoadFile
import br.com.fenix.bilingualmangareader.model.enums.Pages
import br.com.fenix.bilingualmangareader.service.controller.SubTitleController
import br.com.fenix.bilingualmangareader.service.listener.PageLinkCardListener
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.util.constants.PageLinkConsts
import br.com.fenix.bilingualmangareader.util.helpers.Util
import br.com.fenix.bilingualmangareader.view.adapter.page_link.PageLinkCardAdapter
import br.com.fenix.bilingualmangareader.view.adapter.page_link.PageNotLinkCardAdapter
import br.com.fenix.bilingualmangareader.view.components.ComponentsUtil
import br.com.fenix.bilingualmangareader.view.ui.menu.MenuActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputLayout
import org.slf4j.LoggerFactory
import java.lang.ref.WeakReference


class PagesLinkFragment : Fragment() {

    private val mLOGGER = LoggerFactory.getLogger(PagesLinkFragment::class.java)

    private val mViewModel: PagesLinkViewModel by viewModels()
    private lateinit var mRoot: ConstraintLayout
    private lateinit var mImageLoading: CircularProgressIndicator
    private lateinit var mScrollUp: FloatingActionButton
    private lateinit var mScrollDown: FloatingActionButton
    private lateinit var mRecyclerPageLink: RecyclerView
    private lateinit var mRecyclerPageNotLink: RecyclerView
    private lateinit var mPageNotLinkContent: ConstraintLayout
    private lateinit var mPageNotLinkIcon: ImageView
    private lateinit var mContent: LinearLayout
    private lateinit var mFileLink: TextInputLayout
    private lateinit var mFileLinkAutoComplete: AutoCompleteTextView
    private lateinit var mFileLinkLanguage: TextInputLayout
    private lateinit var mFileLinkLanguageAutoComplete: AutoCompleteTextView
    private lateinit var mContentButton: LinearLayout
    private lateinit var mSave: Button
    private lateinit var mRefresh: MaterialButton
    private lateinit var mFullScreen: MaterialButton
    private lateinit var mListener: PageLinkCardListener
    private lateinit var mButtonsGroup: MaterialButtonToggleGroup
    private lateinit var mAutoProcess: MaterialButton
    private lateinit var mReorderPages: MaterialButton
    private lateinit var mSinglePages: MaterialButton
    private lateinit var mDualPages: MaterialButton
    private lateinit var mHelp: MaterialButton
    private lateinit var mDelete: MaterialButton
    private lateinit var mPagesIndex: MaterialButton
    private lateinit var mForceImageReload: MaterialButton
    private lateinit var mToolbar: androidx.appcompat.widget.Toolbar
    private lateinit var mMangaName: TextView

    private lateinit var mMapLanguage: HashMap<String, Languages>
    private val mImageLoadHandler: Handler = ImageLoadHandler(this)
    private var mShowScrollButton: Boolean = true
    private var mHandler = Handler(Looper.getMainLooper())

    private val mDismissUpButton = Runnable { mScrollUp.hide() }
    private val mDismissDownButton = Runnable { mScrollDown.hide() }
    private val mReduceSizeGroupButton = Runnable {
        ComponentsUtil.changeWidthAnimateSize(mButtonsGroup, mCollapseButtonsGroupSize, true)
        changColorButton(requireContext().getColor(R.color.file_link_buttons))
    }

    private var mUseDualPageCalculate = false
    private var mAutoReorderPages: Boolean = true
    private var mInDrag: Boolean = false
    private var mIsTabletOrLandscape: Boolean = false
    private var mPageSelected: Int = 0
    private lateinit var mCollapseButtonsGroupSize: ConstraintLayout.LayoutParams
    private lateinit var mExpandedButtonsGroupSize: ConstraintLayout.LayoutParams

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_pages_link, container, false)

        mRoot = root.findViewById(R.id.pages_link_root)
        mRecyclerPageLink = root.findViewById(R.id.pages_link_pages_linked_recycler)
        mRecyclerPageNotLink = root.findViewById(R.id.pages_link_pages_not_linked_recycler)
        mPageNotLinkContent = root.findViewById(R.id.pages_link_content_pages_not_linked)
        mPageNotLinkIcon = root.findViewById(R.id.pages_link_icon_pages_not_linked)

        mContent = root.findViewById(R.id.pages_link_content)
        mFileLink = root.findViewById(R.id.pages_link_file_link_text)
        mFileLinkAutoComplete = root.findViewById(R.id.pages_link_file_link_autocomplete)
        mFileLinkLanguage = root.findViewById(R.id.pages_link_language_combo)
        mFileLinkLanguageAutoComplete = root.findViewById(R.id.pages_link_language_autocomplete)
        mPagesIndex = root.findViewById(R.id.pages_link_pages_index)
        mMangaName = root.findViewById(R.id.pages_link_name_manga)

        mContentButton = root.findViewById(R.id.pages_link_buttons_content)
        mSave = root.findViewById(R.id.pages_link_save_button)
        mRefresh = root.findViewById(R.id.pages_link_refresh_button)
        mFullScreen = root.findViewById(R.id.pages_link_full_screen_button)

        mImageLoading = root.findViewById(R.id.pages_link_loading_progress)
        mScrollUp = root.findViewById(R.id.pages_link_scroll_up)
        mScrollDown = root.findViewById(R.id.pages_link_scroll_down)

        mButtonsGroup = root.findViewById(R.id.page_link_buttons_group)
        mAutoProcess = root.findViewById(R.id.pages_link_auto_process_button)
        mReorderPages = root.findViewById(R.id.pages_link_reorder_button)
        mSinglePages = root.findViewById(R.id.pages_link_single_page_button)
        mDualPages = root.findViewById(R.id.pages_link_dual_page_button)
        mHelp = root.findViewById(R.id.pages_link_help_button)
        mDelete = root.findViewById(R.id.file_link_delete_button)
        mForceImageReload = root.findViewById(R.id.pages_link_force_image_reload)
        mToolbar = root.findViewById(R.id.toolbar_manga_pages_link)

        (requireActivity() as PagesLinkActivity).setActionBar(mToolbar)

        if (mHelp.tag.toString().compareTo("not_used", true) != 0) {
            mExpandedButtonsGroupSize = mButtonsGroup.layoutParams as ConstraintLayout.LayoutParams
            mButtonsGroup.layoutParams = ConstraintLayout.LayoutParams(mButtonsGroup.layoutParams as ConstraintLayout.LayoutParams)
            mButtonsGroup.layoutParams.width = resources.getDimension(R.dimen.page_link_buttons_size).toInt()
            mCollapseButtonsGroupSize = mButtonsGroup.layoutParams as ConstraintLayout.LayoutParams
        }

        mScrollUp.visibility = View.GONE
        mScrollDown.visibility = View.GONE

        mScrollUp.setOnClickListener { mRecyclerPageLink.smoothScrollToPosition(0) }
        mScrollDown.setOnClickListener {
            mRecyclerPageLink.smoothScrollToPosition((mRecyclerPageLink.adapter as RecyclerView.Adapter).itemCount)
        }

        mRecyclerPageLink.setOnScrollChangeListener { _, _, _, _, yOld ->
            if (mShowScrollButton) {
                if (yOld > 20) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (mHandler.hasCallbacks(mDismissDownButton))
                            mHandler.removeCallbacks(mDismissDownButton)
                    } else
                        mHandler.removeCallbacks(mDismissDownButton)

                    mScrollDown.hide()
                } else if (yOld < -20) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (mHandler.hasCallbacks(mDismissUpButton))
                            mHandler.removeCallbacks(mDismissUpButton)
                    } else
                        mHandler.removeCallbacks(mDismissUpButton)

                    mScrollUp.hide()
                }

                if (yOld > 150) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (mHandler.hasCallbacks(mDismissUpButton))
                            mHandler.removeCallbacks(mDismissUpButton)
                    } else
                        mHandler.removeCallbacks(mDismissUpButton)

                    mHandler.postDelayed(mDismissUpButton, 3000)
                    mScrollUp.show()
                } else if (yOld < -150) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (mHandler.hasCallbacks(mDismissDownButton))
                            mHandler.removeCallbacks(mDismissDownButton)
                    } else
                        mHandler.removeCallbacks(mDismissDownButton)

                    mHandler.postDelayed(mDismissDownButton, 3000)
                    mScrollDown.show()
                }
            } else {
                mScrollUp.hide()
                mScrollDown.hide()
            }
        }

        mFileLinkAutoComplete.setOnClickListener {
            choiceSelectManga()
        }

        val languages = resources.getStringArray(R.array.languages)
        mMapLanguage = hashMapOf(
            languages[0] to Languages.PORTUGUESE,
            languages[1] to Languages.ENGLISH,
            languages[2] to Languages.JAPANESE
        )

        mFileLinkLanguageAutoComplete.setAdapter(ArrayAdapter(requireContext(), R.layout.list_item, mMapLanguage.keys.toTypedArray()))
        mFileLinkLanguageAutoComplete.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                val language = if (parent.getItemAtPosition(position).toString().isNotEmpty() &&
                    mMapLanguage.containsKey(parent.getItemAtPosition(position).toString())
                )
                    mMapLanguage[parent.getItemAtPosition(position).toString()]
                else
                    null

                mViewModel.setLanguage(language)
            }

        mUseDualPageCalculate = GeneralConsts.getSharedPreferences(requireContext())
            .getBoolean(GeneralConsts.KEYS.PAGE_LINK.USE_DUAL_PAGE_CALCULATE, false)

        mSave.setOnClickListener { save() }
        mRefresh.setOnClickListener { refresh() }

        mAutoProcess.setOnClickListener { mViewModel.autoReorderDoublePages(Pages.LINKED, true) }
        mReorderPages.setOnClickListener { mViewModel.reorderBySortPages() }
        mSinglePages.setOnClickListener { mViewModel.reorderSimplePages() }
        mDualPages.setOnClickListener { mViewModel.reorderDoublePages(mUseDualPageCalculate) }
        mDelete.setOnClickListener {
            AlertDialog.Builder(requireActivity(), R.style.AppCompatAlertDialogStyle)
                .setTitle(getString(R.string.library_menu_delete))
                .setMessage(getString(R.string.page_link_delete_description))
                .setPositiveButton(
                    R.string.action_positive
                ) { _, _ ->
                    mViewModel.delete { index, type -> notifyItemChanged(type, index) }
                }
                .setNegativeButton(
                    R.string.action_negative
                ) { _, _ -> }
                .create().show()
        }

        mHelp.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (mHandler.hasCallbacks(mReduceSizeGroupButton))
                    mHandler.removeCallbacks(mReduceSizeGroupButton)
            } else
                mHandler.removeCallbacks(mReduceSizeGroupButton)

            mHandler.postDelayed(mReduceSizeGroupButton, 5000)

            ComponentsUtil.changeWidthAnimateSize(mButtonsGroup, mExpandedButtonsGroupSize, false)
            changColorButton(requireContext().getColor(R.color.file_link_buttons_expanded))
        }

        mFullScreen.setOnClickListener {
            val visible = mContent.visibility == View.GONE

            if (mHelp.tag.toString().compareTo("not_used", true) != 0)
                ComponentsUtil.changeAnimateVisibility(mHelp, visible)

            ComponentsUtil.changeAnimateVisibility(
                arrayListOf(mContent, mSave, mRefresh, mButtonsGroup, mToolbar),
                visible
            )

            val image = if (visible)
                R.drawable.ic_fullscreen
            else
                R.drawable.ic_fullscreen_exit

            mFullScreen.icon = ContextCompat.getDrawable(requireContext(), image)
        }

        mPagesIndex.setOnClickListener { openMenuIndexes() }

        mForceImageReload.setOnClickListener { mViewModel.reLoadImages() }

        mListener = object : PageLinkCardListener {
            override fun onClick(page: PageLink) {}

            override fun onClickLong(view: View, page: PageLink, origin: Pages, position: Int): Boolean {
                mAutoReorderPages = false
                val pageLink = if (origin == Pages.NOT_LINKED) mViewModel.getPageNotLink(page) else mViewModel.getPageLink(page)
                val item = ClipData.Item(pageLink)
                val name = if (origin == Pages.DUAL_PAGE) page.fileLinkRightPageName else page.fileLinkLeftPageName
                val dragData = ClipData(position.toString(), arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN), item)
                dragData.addItem(ClipData.Item(origin.name))
                dragData.addItem(ClipData.Item(name))
                val myShadow = View.DragShadowBuilder(view)

                view.startDragAndDrop(
                    dragData,
                    myShadow,
                    view,
                    0
                )
                view.visibility = View.INVISIBLE
                return true
            }

            override fun onDropItem(origin: Pages, destiny: Pages, dragIndex: String, drop: PageLink) {
                when {
                    origin == Pages.DUAL_PAGE || destiny == Pages.DUAL_PAGE -> {
                        val pageLink = if (origin == Pages.NOT_LINKED)
                            mViewModel.getPageNotLink(Integer.valueOf(dragIndex))
                        else
                            mViewModel.getPageLink(Integer.valueOf(dragIndex))
                        mViewModel.onMoveDualPage(origin, pageLink, destiny, drop)
                    }
                    origin == Pages.LINKED && destiny == Pages.LINKED -> mViewModel.onMove(
                        mViewModel.getPageLink(Integer.valueOf(dragIndex)),
                        drop
                    )
                    origin == Pages.LINKED && destiny == Pages.NOT_LINKED -> {
                        mViewModel.onNotLinked(mViewModel.getPageLink(Integer.valueOf(dragIndex)))
                    }
                    origin == Pages.NOT_LINKED && destiny == Pages.LINKED -> mViewModel.fromNotLinked(
                        mViewModel.getPageNotLink(
                            Integer.valueOf(
                                dragIndex
                            )
                        ), drop
                    )
                }
            }

            override fun onDragScrolling(pointScreen: Point) {
                onPageLinkScrolling(pointScreen)
            }
        }

        mRecyclerPageLink.setOnDragListener { _, dragEvent ->
            when (dragEvent.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    mInDrag = true
                    mShowScrollButton = false
                    true
                }

                DragEvent.ACTION_DRAG_ENDED -> {
                    mInDrag = false
                    mShowScrollButton = true
                    true
                }
                else -> true
            }
        }


        mRecyclerPageNotLink.setOnDragListener { _, dragEvent ->
            when (dragEvent.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    mPageNotLinkContent.background = AppCompatResources.getDrawable(requireContext(), R.drawable.file_linked_rounded_border)
                    mPageNotLinkIcon.visibility = View.VISIBLE
                    mShowScrollButton = false
                    dragEvent.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
                }

                DragEvent.ACTION_DRAG_ENTERED -> {
                    mPageNotLinkContent.background = AppCompatResources.getDrawable(requireContext(), R.drawable.file_linked_background_selected)
                    mPageNotLinkIcon.visibility = View.VISIBLE
                    true
                }

                DragEvent.ACTION_DRAG_EXITED -> {
                    mPageNotLinkContent.background = AppCompatResources.getDrawable(requireContext(), R.drawable.file_linked_rounded_border)
                    mPageNotLinkIcon.visibility = View.VISIBLE
                    true
                }

                DragEvent.ACTION_DROP -> {
                    when (val origin = Pages.valueOf(dragEvent.clipData.getItemAt(PageLinkConsts.CLIPDATA.PAGE_TYPE).text.toString())) {
                        Pages.LINKED -> mViewModel.onNotLinked(
                            mViewModel.getPageLink(
                                Integer.valueOf(
                                    dragEvent.clipData.getItemAt(
                                        PageLinkConsts.CLIPDATA.PAGE_LINK
                                    ).text.toString()
                                )
                            )
                        )
                        Pages.DUAL_PAGE -> {
                            val pageLink = mViewModel.getPageLink(Integer.valueOf(dragEvent.clipData.getItemAt(PageLinkConsts.CLIPDATA.PAGE_LINK).text.toString()))
                            mViewModel.onMoveDualPage(origin, pageLink, Pages.NOT_LINKED, pageLink)
                        }
                        else -> {}
                    }
                    true
                }

                DragEvent.ACTION_DRAG_ENDED -> {
                    mPageNotLinkContent.background = AppCompatResources.getDrawable(requireContext(), R.drawable.file_linked_background)
                    mPageNotLinkIcon.visibility = View.INVISIBLE
                    mShowScrollButton = true

                    val v = dragEvent.localState as View
                    if (!dragEvent.result || v.tag.toString().compareTo(PageLinkConsts.TAG.PAGE_LINK_RIGHT, true) != 0)
                        v.visibility = View.VISIBLE
                    true
                }

                else -> true
            }
        }

        observer()
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapterPageLink = PageLinkCardAdapter()
        mRecyclerPageLink.adapter = adapterPageLink
        mRecyclerPageLink.layoutManager = LinearLayoutManager(requireContext())
        adapterPageLink.attachListener(mListener)

        mIsTabletOrLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE ||
                mRecyclerPageNotLink.tag.toString().compareTo("vertical", true) == 0

        val adapterPageNotLink = PageNotLinkCardAdapter()
        mRecyclerPageNotLink.adapter = adapterPageNotLink
        val orientation = if (mIsTabletOrLandscape) RecyclerView.VERTICAL else RecyclerView.HORIZONTAL
        mRecyclerPageNotLink.layoutManager = LinearLayoutManager(requireContext(), orientation, false)
        adapterPageNotLink.attachListener(mListener)

        if (savedInstanceState != null) {
            val fileLink = savedInstanceState.getSerializable(GeneralConsts.KEYS.OBJECT.PAGE_LINK)
            if (fileLink != null)
                mViewModel.reload(fileLink as FileLink) { index, type -> notifyItemChanged(type, index) }
            else
                mViewModel.reLoadImages(Pages.ALL, true, isCloseThreads = true)
            mMangaName.text = mViewModel.getMangaName()
        } else {
            val bundle = this.arguments
            if (bundle != null && bundle.containsKey(GeneralConsts.KEYS.OBJECT.MANGA)) {
                mViewModel.loadManga(bundle[GeneralConsts.KEYS.OBJECT.MANGA] as Manga) { index, type -> notifyItemChanged(type, index) }
                mMangaName.text = mViewModel.getMangaName()
                mPageSelected = bundle.getInt(GeneralConsts.KEYS.MANGA.PAGE_NUMBER, 0)
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int, resultData: Intent?
    ) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GeneralConsts.REQUEST.OPEN_PAGE_LINK) {
                resultData?.data?.also { uri ->
                    try {
                        mAutoReorderPages = true
                        val path = Util.normalizeFilePath(uri.path.toString())
                        val loaded = mViewModel.readFileLink(path) { index, type -> notifyItemChanged(type, index) }
                        if (loaded != LoadFile.LOADED) {
                            val msg = if (loaded == LoadFile.ERROR_FILE_WRONG) getString(R.string.page_link_load_file_wrong) else getString(
                                R.string.page_link_load_error
                            )
                            AlertDialog.Builder(requireContext(), R.style.AppCompatAlertDialogStyle)
                                .setTitle(msg)
                                .setMessage(path)
                                .setPositiveButton(
                                    R.string.action_neutral
                                ) { _, _ -> }
                                .create()
                                .show()
                        }
                    } catch (e: Exception) {
                        mLOGGER.warn("Error when open file: " + e.message, e)
                    }
                }

            } else if (requestCode == GeneralConsts.REQUEST.SELECT_MANGA) {
                resultData?.extras?.also {
                    if (it.containsKey(GeneralConsts.KEYS.OBJECT.MANGA)) {
                        val link = it.getSerializable(GeneralConsts.KEYS.OBJECT.MANGA) as Manga
                        val loaded = mViewModel.readFileLink(link.path) { index, type -> notifyItemChanged(type, index) }
                        if (loaded != LoadFile.LOADED) {
                            val msg = if (loaded == LoadFile.ERROR_FILE_WRONG) getString(R.string.page_link_load_file_wrong) else getString(
                                R.string.page_link_load_error
                            )
                            AlertDialog.Builder(requireContext(), R.style.AppCompatAlertDialogStyle)
                                .setTitle(msg)
                                .setMessage(link.path)
                                .setPositiveButton(
                                    R.string.action_neutral
                                ) { _, _ -> }
                                .create()
                                .show()
                        }
                    }
                }
            }
        } else {
            mFileLinkAutoComplete.setText("")
            mViewModel.clearFileLink { index, type -> notifyItemChanged(type, index) }
        }
    }

    private fun onPageLinkScrolling(point: Point) {
        val recycler = Rect()
        mRecyclerPageLink.getGlobalVisibleRect(recycler)

        val space = recycler.height() / 4
        val spaceTop = recycler.top + space
        val spaceBottom = recycler.bottom - space

        val padding = if (point.y < spaceTop) {
            val fast = (space / 4)
            if (point.y < (recycler.top + fast)) -600
            else if (point.y < (recycler.top + (fast * 2))) -350
            else -150
        } else if (point.y > spaceBottom) {
            val fast = (space / 4)
            if (point.y > (recycler.bottom - fast)) +600
            else if (point.y > (recycler.bottom - (fast * 2))) +350
            else +150
        } else 0

        if (padding != 0)
            mRecyclerPageLink.smoothScrollBy(0, padding)
    }

    private fun observer() {
        mViewModel.pagesLink.observe(viewLifecycleOwner) {
            (mRecyclerPageLink.adapter as PageLinkCardAdapter).updateList(it)
        }

        mViewModel.pagesLinkNotLinked.observe(viewLifecycleOwner) {
            (mRecyclerPageNotLink.adapter as PageNotLinkCardAdapter).updateList(it)
        }

        mViewModel.fileLink.observe(viewLifecycleOwner) {
            val description = it?.name ?: ""
            mFileLinkAutoComplete.setText(description)
        }

        mViewModel.language.observe(viewLifecycleOwner) {
            var description = ""

            if (it != null) {
                for (language in mMapLanguage.entries)
                    if (language.value.compareTo(it) == 0)
                        description = language.key
            }

            mFileLinkLanguageAutoComplete.setText(description, false)
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun notifyItemChanged(type: Pages, index: Int?, add: Boolean = false, remove: Boolean = false) {
        when {
            type == Pages.NOT_LINKED && add && index != null && index > -1 -> (mRecyclerPageNotLink.adapter as PageNotLinkCardAdapter).notifyItemInserted(
                index
            )
            type == Pages.NOT_LINKED && remove && index != null && index > -1 -> (mRecyclerPageNotLink.adapter as PageNotLinkCardAdapter).notifyItemRemoved(
                index
            )
            type == Pages.NOT_LINKED && index != null && index > -1 -> (mRecyclerPageNotLink.adapter as PageNotLinkCardAdapter).notifyItemChanged(
                index
            )
            type == Pages.NOT_LINKED && (index == null || index == -1) -> (mRecyclerPageNotLink.adapter as PageNotLinkCardAdapter).notifyDataSetChanged()

            type != Pages.NOT_LINKED && add && index != null && index > -1 -> (mRecyclerPageLink.adapter as PageLinkCardAdapter).notifyItemInserted(
                index
            )
            type != Pages.NOT_LINKED && remove && index != null && index > -1 -> (mRecyclerPageLink.adapter as PageLinkCardAdapter).notifyItemRemoved(
                index
            )
            type != Pages.NOT_LINKED && index != null && index > -1 -> (mRecyclerPageLink.adapter as PageLinkCardAdapter).notifyItemChanged(
                index
            )
            type != Pages.NOT_LINKED && (index == null || index == -1) -> (mRecyclerPageLink.adapter as PageLinkCardAdapter).notifyDataSetChanged()
        }
    }

    private fun enableContent(enabled: Boolean) {
        mAutoReorderPages = false
        mRecyclerPageLink.isEnabled = enabled
        mRecyclerPageNotLink.isEnabled = enabled
        mFileLink.isEnabled = enabled
        mFileLinkLanguage.isEnabled = enabled
        mSave.isEnabled = enabled
        mRefresh.isEnabled = enabled
        mButtonsGroup.isEnabled = enabled
        mPagesIndex.isEnabled = enabled
    }

    private fun changColorButton(color: Int) {
        mAutoProcess.setBackgroundColor(color)
        mReorderPages.setBackgroundColor(color)
        mSinglePages.setBackgroundColor(color)
        mDualPages.setBackgroundColor(color)
        mHelp.setBackgroundColor(color)
        mRefresh.setBackgroundColor(color)
        mDelete.setBackgroundColor(color)
    }

    private fun save() {
        enableContent(false)

        mViewModel.save()
        Toast.makeText(
            requireContext(),
            getString(R.string.page_link_saved),
            Toast.LENGTH_SHORT
        ).show()

        enableContent(true)
    }

    private fun refresh() {
        enableContent(false)

        mViewModel.restoreBackup { index, type -> notifyItemChanged(type, index) }
        Toast.makeText(
            requireContext(),
            getString(R.string.page_link_refreshed),
            Toast.LENGTH_SHORT
        ).show()

        enableContent(true)
    }

    override fun onResume() {
        super.onResume()
        mViewModel.addImageLoadHandler(mImageLoadHandler)
        processImageLoading(isVerify = true)
        mRecyclerPageLink.scrollToPosition(mPageSelected)
    }

    override fun onPause() {
        mPageSelected = (mRecyclerPageLink.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
        mViewModel.removeImageLoadHandler(mImageLoadHandler)
        super.onPause()
    }

    override fun onStop() {
        SubTitleController.getInstance(requireContext()).setFileLink(mViewModel.getFileLink(null))
        super.onStop()
    }

    override fun onDestroy() {
        mViewModel.endThread()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (mHandler.hasCallbacks(mDismissUpButton))
                mHandler.removeCallbacks(mDismissUpButton)
            if (mHandler.hasCallbacks(mDismissDownButton))
                mHandler.removeCallbacks(mDismissDownButton)
            if (mHandler.hasCallbacks(mReduceSizeGroupButton))
                mHandler.removeCallbacks(mReduceSizeGroupButton)
        } else {
            mHandler.removeCallbacks(mDismissUpButton)
            mHandler.removeCallbacks(mDismissDownButton)
            mHandler.removeCallbacks(mReduceSizeGroupButton)
        }

        mViewModel.onDestroy()
        super.onDestroy()
    }

    private fun openMenuIndexes() {
        val fileLinkLoaded = mViewModel.fileLink.value?.path?.isNotEmpty() ?: false
        if (!fileLinkLoaded)
            openMenuIndexes(true)
        else {
            val (manga, linked) = mViewModel.getFilesNames()
            val items = arrayOf(manga, linked)
            MaterialAlertDialogBuilder(requireActivity(), R.style.AppCompatAlertDialogStyleFileChoice)
                .setTitle(resources.getString(R.string.page_link_select_file_page_index))
                .setItems(items) { _, selected ->
                    val itemSelected = items[selected]
                    openMenuIndexes(itemSelected == manga)
                }
                .show()
        }
    }

    private fun openMenuIndexes(isMangaIndexes: Boolean) {
        val paths = mViewModel.getPagesIndex(isMangaIndexes)

        if (paths.isEmpty()) {
            AlertDialog.Builder(requireContext(), R.style.AppCompatAlertDialogStyle)
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

        val title = LinearLayout(requireContext())
        title.orientation = LinearLayout.VERTICAL
        title.setPadding(resources.getDimensionPixelOffset(R.dimen.page_link_page_index_title_padding))
        val (manga, file) = mViewModel.getFilesNames()
        val name = TextView(requireContext())
        name.text = if (isMangaIndexes) manga else file
        name.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.title_index_dialog_size))
        name.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
        title.addView(name)
        val index = TextView(requireContext())
        index.text = resources.getString(R.string.reading_page_index)
        index.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.title_small_index_dialog_size))
        index.setTextColor(ContextCompat.getColor(requireContext(), R.color.on_secondary))
        title.addView(index)

        MaterialAlertDialogBuilder(requireActivity(), R.style.AppCompatAlertDialogStyle)
            .setCustomTitle(title)
            .setItems(items) { _, selected ->
                val pageIndex = paths[items[selected]]
                if (pageIndex != null)
                    mRecyclerPageLink.smoothScrollToPosition(pageIndex)
            }
            .show()
    }

    private fun choiceSelectManga() {
        val origins = requireContext().resources.getStringArray(R.array.origin_manga)
        MaterialAlertDialogBuilder(requireActivity(), R.style.AppCompatAlertDialogStyle)
            .setTitle(R.string.page_link_select_origin_manga)
            .setItems(origins) { _, selected ->
                val origin = origins[selected]
                if (origin == origins[0])
                    openLibrarySelectManga()
                else
                    openIntentSelectManga()
            }
            .show()
    }

    private fun openLibrarySelectManga() {
        val intent = Intent(requireContext(), MenuActivity::class.java)
        val bundle = Bundle()
        bundle.putInt(GeneralConsts.KEYS.FRAGMENT.ID, R.id.frame_select_manga)
        bundle.putString(GeneralConsts.KEYS.MANGA.NAME, mViewModel.getMangaName())
        intent.putExtras(bundle)
        requireActivity().overridePendingTransition(R.anim.fade_in_fragment_add_enter, R.anim.fade_out_fragment_remove_exit)
        startActivityForResult(intent, GeneralConsts.REQUEST.SELECT_MANGA, null)
    }

    private fun openIntentSelectManga() {
        val intent = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/zip|application/x-cbz|application/rar|application/x-cbr|application/x-rar-compressed|" +
                        "application/x-zip-compressed|application/cbr|application/cbz|*/*"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } else
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/*"
                putExtra(
                    Intent.EXTRA_MIME_TYPES, arrayOf(
                        "application/zip", "application/x-cbz", "application/rar", "application/x-cbr",
                        "application/x-rar-compressed", "application/x-zip-compressed", "application/cbr", "application/cbz"
                    )
                )
            }
        startActivityForResult(intent, GeneralConsts.REQUEST.OPEN_PAGE_LINK)
    }

    private fun processImageLoading(isInitial: Boolean = false, isEnding: Boolean = false, isVerify: Boolean = false) {
        if (isInitial) {
            mImageLoading.isIndeterminate = true
            mImageLoading.visibility = View.VISIBLE
        } else if (isEnding || isVerify) {
            mImageLoading.isIndeterminate = isVerify
            val progress = mViewModel.imageThreadLoadingProgress()
            mImageLoading.visibility = if (isVerify && progress > 0 || isEnding && progress > 1)
                View.VISIBLE
            else
                View.INVISIBLE
        } else {
            val (progress, size) = mViewModel.getProgress()
            if (progress == -1)
                mImageLoading.isIndeterminate = true
            else {
                mImageLoading.isIndeterminate = false
                mImageLoading.max = size
                mImageLoading.progress = progress
            }
        }
    }

    private fun processImages(isInitial: Boolean = false, isEnding: Boolean = false, message: String = "") {
        processImageLoading(isInitial, isEnding)

        val enabled = if (isInitial)
            false
        else
            isEnding

        enableContent(enabled)

        if (message.isNotEmpty())
            Toast.makeText(
                requireContext(),
                message,
                Toast.LENGTH_SHORT
            ).show()
    }

    private inner class ImageLoadHandler(fragment: PagesLinkFragment) : Handler() {
        private val mOwner: WeakReference<PagesLinkFragment> = WeakReference(fragment)
        override fun handleMessage(msg: Message) {
            val imageLoad = msg.obj as PagesLinkViewModel.ImageLoad
            when (msg.what) {
                PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_START -> processImageLoading(true)
                PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_UPDATED -> {
                    processImageLoading()
                    notifyItemChanged(imageLoad.type, imageLoad.index)
                }
                PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_LOAD_ERROR -> mHandler.postDelayed(
                    { mViewModel.reLoadImages(imageLoad.type) },
                    300L
                )
                PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_LOAD_ERROR_ENABLE_MANUAL -> mForceImageReload.visibility = View.VISIBLE
                PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_ADDED -> notifyItemChanged(imageLoad.type, imageLoad.index, add = true)
                PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_REMOVED -> notifyItemChanged(
                    imageLoad.type,
                    imageLoad.index,
                    remove = true
                )
                PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_IMAGE_FINISHED -> {
                    processImageLoading(isEnding = true)
                    mViewModel.reLoadImages(imageLoad.type, true)
                }
                PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_ALL_IMAGES_LOADED -> {
                    processImageLoading(isEnding = true)
                    if (mAutoReorderPages) {
                        mAutoReorderPages = false
                        mViewModel.autoReorderDoublePages(imageLoad.type, isNotify = false)
                    }

                    mForceImageReload.visibility = if (!mViewModel.allImagesLoaded()) View.VISIBLE else View.GONE
                }
                PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_ITEM_CHANGE -> notifyItemChanged(imageLoad.type, imageLoad.index)
                PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_ITEM_ADD -> notifyItemChanged(imageLoad.type, imageLoad.index, add = true)
                PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_ITEM_REMOVE -> notifyItemChanged(imageLoad.type, imageLoad.index, remove = true)
                PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_REORDER_AUTO_PAGES_START -> processImages(isInitial = true)
                PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_REORDER_AUTO_PAGES_FINISHED -> processImages(
                    isEnding = true,
                    message = getString(R.string.page_link_process_reorder_auto_done)
                )
                PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_REORDER_DOUBLE_PAGES_START -> processImages(isInitial = true)
                PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_REORDER_DOUBLE_PAGES_FINISHED -> processImages(
                    isEnding = true,
                    message = getString(R.string.page_link_process_reorder_dual_pages_done)
                )
                PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_REORDER_SIMPLE_PAGES_START -> processImages(isInitial = true)
                PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_REORDER_SIMPLE_PAGES_FINISHED -> processImages(
                    isEnding = true,
                    message = getString(R.string.page_link_process_reorder_single_page_done)
                )
                PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_REORDER_SORTED_PAGES_START -> processImages(isInitial = true)
                PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_REORDER_SORTED_PAGES_FINISHED -> processImages(
                    isEnding = true,
                    message = getString(R.string.page_link_process_reorder_sorted_page_done)
                )

            }
        }
    }
}
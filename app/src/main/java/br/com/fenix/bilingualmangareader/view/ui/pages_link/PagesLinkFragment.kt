package br.com.fenix.bilingualmangareader.view.ui.pages_link

import android.app.Activity
import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import android.content.res.Configuration
import android.os.*
import android.util.Log
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.FileLink
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.model.entity.PageLink
import br.com.fenix.bilingualmangareader.model.enums.LoadFile
import br.com.fenix.bilingualmangareader.model.enums.Pages
import br.com.fenix.bilingualmangareader.service.controller.SubTitleController
import br.com.fenix.bilingualmangareader.service.listener.PageLinkCardListener
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.util.constants.PageLinkConsts
import br.com.fenix.bilingualmangareader.util.helpers.Util
import br.com.fenix.bilingualmangareader.view.adapter.page_link.PageLinkCardAdapter
import br.com.fenix.bilingualmangareader.view.adapter.page_link.PageNotLinkCardAdapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import java.lang.ref.WeakReference


class PagesLinkFragment : Fragment() {

    private val mViewModel: PagesLinkViewModel by viewModels()
    private lateinit var mRoot : ConstraintLayout
    private lateinit var mScrollUp: FloatingActionButton
    private lateinit var mScrollDown: FloatingActionButton
    private lateinit var mRecyclePageLink: RecyclerView
    private lateinit var mRecyclePageNotLink: RecyclerView
    private lateinit var mFileLink: TextInputLayout
    private lateinit var mFileLinkAutoComplete: AutoCompleteTextView
    private lateinit var mSave: Button
    private lateinit var mRefresh : Button
    private lateinit var mFullScreen: MaterialButton
    private lateinit var mListener: PageLinkCardListener

    private val mImageLoadHandler: Handler = ImageLoadHandler(this)
    private var mShowScrollButton : Boolean = true
    private var mHandler = Handler(Looper.getMainLooper())
    private val mDismissUpButton = Runnable { mScrollUp.hide() }
    private val mDismissDownButton = Runnable { mScrollDown.hide() }
    private var mOpenedIntent : Boolean = false
    private var mInDrag : Boolean = false
    private var mIsTabletOrLandscape: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_pages_link, container, false)

        mRoot = root.findViewById(R.id.root_pages_link)
        mRecyclePageLink = root.findViewById(R.id.rv_pages_linked)
        mRecyclePageNotLink = root.findViewById(R.id.rv_pages_not_linked)
        mFileLink = root.findViewById(R.id.txt_file_link)
        mFileLinkAutoComplete = root.findViewById(R.id.menu_autocomplete_file_link)
        mSave = root.findViewById(R.id.btn_file_link_save)
        mRefresh = root.findViewById(R.id.btn_file_link_refresh)
        mFullScreen = root.findViewById(R.id.btn_file_link_full_screen)

        mScrollUp = root.findViewById(R.id.pages_link_scroll_up)
        mScrollDown = root.findViewById(R.id.pages_link_scroll_down)

        mScrollUp.visibility = View.GONE
        mScrollDown.visibility = View.GONE

        mScrollUp.setOnClickListener { mRecyclePageLink.smoothScrollToPosition(0) }
        mScrollDown.setOnClickListener {
            var position = if (mViewModel.pagesLink.value != null)
                mViewModel.pagesLink.value!!.size
            else 0

            mRecyclePageLink.smoothScrollToPosition(position)
        }

        mRecyclePageLink.setOnScrollChangeListener { _, _, _, _, yOld ->
            if (mShowScrollButton) {
                if (yOld > 200) {
                    if (mHandler.hasCallbacks(mDismissDownButton)) {
                        mHandler.removeCallbacks(mDismissDownButton)
                        mScrollDown.hide()
                    }
                    mHandler.removeCallbacks(mDismissUpButton)
                    mHandler.postDelayed(mDismissUpButton, 3000)
                    mScrollUp.show()
                } else if (yOld < -200) {
                    if (mHandler.hasCallbacks(mDismissUpButton)) {
                        mHandler.removeCallbacks(mDismissUpButton)
                        mScrollUp.hide()
                    }
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
            mFileLinkAutoComplete.setText("")
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
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/zip", "application/x-cbz", "application/rar", "application/x-cbr",
                        "application/x-rar-compressed", "application/x-zip-compressed", "application/cbr", "application/cbz"))
                }
            mOpenedIntent = true
            startActivityForResult(intent, GeneralConsts.REQUEST.OPEN_PAGE_LINK)
        }

        mSave.setOnClickListener { save() }
        mRefresh.setOnClickListener { refresh() }

        mFullScreen.setOnClickListener {
            val image = if (mFileLink.visibility == View.GONE) {
                mFileLink.visibility = View.VISIBLE
                mSave.visibility = View.VISIBLE
                mRefresh.visibility = View.VISIBLE
                R.drawable.ic_fullscreen
            } else {
                mFileLink.visibility = View.GONE
                mSave.visibility = View.GONE
                mRefresh.visibility = View.GONE
                R.drawable.ic_fullscreen_exit
            }
            mFullScreen.icon = ContextCompat.getDrawable(context!!, image)
        }

        mListener = object : PageLinkCardListener {
            override fun onClick(page: PageLink) { }

            override fun onClickLong(view : View, page: PageLink, origin : Pages): Boolean {
                val pageLink = if (origin == Pages.NOT_LINKED) mViewModel.getPageNotLink(page) else mViewModel.getPageLink(page)
                val item = ClipData.Item(pageLink)
                val name = if (origin == Pages.DUAL_PAGE) page.fileRightLinkPageName else page.fileLinkPageName
                val dragData = ClipData( name, arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN), item)
                dragData.addItem(ClipData.Item(origin.name))
                val myShadow = View.DragShadowBuilder(view)

                view.startDragAndDrop(dragData,
                    myShadow,
                    view,
                    0
                )
                view.visibility = View.INVISIBLE
                return true
            }

            override fun onDropItem(origin : Pages, destiny : Pages, dragIndex: String, drop: PageLink) {
                when {
                    origin == Pages.DUAL_PAGE || destiny == Pages.DUAL_PAGE -> {
                        val index = if (origin == Pages.NOT_LINKED)
                            mViewModel.getPageNotLinkIndex(drop)
                        else if (destiny == Pages.NOT_LINKED)
                                mViewModel.getPageNotLinkLastIndex() + 1
                        else null

                        val pageLink = if (origin == Pages.NOT_LINKED)
                            mViewModel.getPageNotLink(Integer.valueOf(dragIndex))
                        else
                            mViewModel.getPageLink(Integer.valueOf(dragIndex))
                        mViewModel.onMoveDualPage(origin, pageLink, destiny, drop)
                    }
                    origin == Pages.LINKED && destiny == Pages.LINKED -> mViewModel.onMove(mViewModel.getPageLink(Integer.valueOf(dragIndex)), drop)
                    origin == Pages.LINKED && destiny == Pages.NOT_LINKED -> {
                        mViewModel.onNotLinked(mViewModel.getPageLink(Integer.valueOf(dragIndex)))
                    }
                    origin == Pages.NOT_LINKED && destiny == Pages.LINKED -> {
                        val index = mViewModel.getPageNotLinkIndex(drop)
                        mViewModel.fromNotLinked(mViewModel.getPageNotLink(Integer.valueOf(dragIndex)), drop)
                    }
                }
            }

            override fun onDragScrolling(pointScreen: IntArray) {
                onPageLinkScrolling(pointScreen)
            }
        }

        mRecyclePageLink.setOnDragListener { _, dragEvent ->
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


        mRecyclePageNotLink.setOnDragListener { _, dragEvent ->

            when (dragEvent.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    mRecyclePageNotLink.background = requireContext().getDrawable(R.drawable.file_linked_rounded_border)
                    mShowScrollButton = false
                    dragEvent.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
                }

                DragEvent.ACTION_DRAG_ENTERED -> {
                    mRecyclePageNotLink.background = requireContext().getDrawable(R.drawable.file_linked_background_selected)
                    true
                }

                DragEvent.ACTION_DRAG_EXITED -> {
                    mRecyclePageNotLink.background = requireContext().getDrawable(R.drawable.file_linked_rounded_border)
                    true
                }

                DragEvent.ACTION_DROP -> {
                    when (val origin = Pages.valueOf(dragEvent.clipData.getItemAt(1).text.toString())) {
                        Pages.LINKED -> mViewModel.onNotLinked(mViewModel.getPageLink(Integer.valueOf(dragEvent.clipData.getItemAt(0).text.toString())))
                        Pages.DUAL_PAGE -> {
                            val pageLink = mViewModel.getPageLink(Integer.valueOf(dragEvent.clipData.getItemAt(0).text.toString()))
                            mViewModel.onMoveDualPage(origin, pageLink, Pages.NOT_LINKED, pageLink)
                        }
                    }
                    true
                }

                DragEvent.ACTION_DRAG_ENDED -> {
                    mRecyclePageNotLink.background = requireContext().getDrawable(R.drawable.file_linked_background)
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
        mRecyclePageLink.adapter = adapterPageLink
        mRecyclePageLink.layoutManager = LinearLayoutManager(requireContext())
        adapterPageLink.attachListener(mListener)

        mIsTabletOrLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE or
                mRecyclePageNotLink.tag.toString().compareTo("vertical", true)

        val adapterPageNotLink = PageNotLinkCardAdapter()
        mRecyclePageNotLink.adapter = adapterPageNotLink
        val orientation = if (mIsTabletOrLandscape) RecyclerView.VERTICAL else RecyclerView.HORIZONTAL
        mRecyclePageNotLink.layoutManager = LinearLayoutManager(requireContext(), orientation, false)
        adapterPageNotLink.attachListener(mListener)

        if (savedInstanceState != null) {
            val fileLink = savedInstanceState.getSerializable(GeneralConsts.KEYS.OBJECT.PAGELINK)
            if (fileLink != null)
                mViewModel.reload(fileLink as FileLink) { index, type -> notifyItemChanged(type, index)  }
        } else {
            val bundle = this.arguments
            if (bundle != null && bundle.containsKey(GeneralConsts.KEYS.OBJECT.MANGA))
                mViewModel.loadManga(bundle[GeneralConsts.KEYS.OBJECT.MANGA] as Manga) { index, type -> notifyItemChanged(type, index)  }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (!mOpenedIntent) {
            val fileLink = mViewModel.getFileLink(isBackup = true)
            outState.putSerializable(GeneralConsts.KEYS.OBJECT.PAGELINK, fileLink)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int, resultData: Intent?
    ) {
        mOpenedIntent = false
        if (requestCode == GeneralConsts.REQUEST.OPEN_PAGE_LINK) {
            if (resultCode == Activity.RESULT_OK) {
                resultData?.data?.also { uri ->
                    try {
                        val path = Util.normalizeFilePath(uri.path.toString())
                        val loaded = mViewModel.readFileLink(path) { index, type -> notifyItemChanged(type, index)  }
                        if (loaded != LoadFile.LOADED) {
                            val msg = if (loaded == LoadFile.ERROR_FILE_WRONG) getString(R.string.page_link_load_file_wrong) else getString(R.string.page_link_load_error)
                            AlertDialog.Builder(requireContext(), R.style.AppCompatAlertDialogStyle)
                                .setTitle(msg)
                                .setMessage(path)
                                .setNeutralButton(
                                    R.string.action_neutral
                                ) { _, _ -> }
                                .create()
                                .show()
                        }
                    } catch (e: Exception) {
                        Log.e(GeneralConsts.TAG.LOG, "Error when open file: " + e.message)
                    }
                }
            } else
                mViewModel.clearFileLink { index, type -> notifyItemChanged(type, index)  }
        }
    }

    private fun onPageLinkScrolling(pointScreen : IntArray) {
        val (x, y) = pointScreen
        val divider = 3
        val padding = if (y < (mRecyclePageLink.height.toFloat() / divider)) - 150
        else if (y > (mRecyclePageLink.height.toFloat() / divider * (divider - 1))) + 150
        else 0

        if (padding != 0)
            mRecyclePageLink.smoothScrollBy(0, padding)
    }

    private fun observer() {
        mViewModel.pagesLink.observe(viewLifecycleOwner) {
            (mRecyclePageLink.adapter as PageLinkCardAdapter).updateList(it)
        }

        mViewModel.pagesLinkNotLinked.observe(viewLifecycleOwner) {
            (mRecyclePageNotLink.adapter as PageNotLinkCardAdapter).updateList(it)
        }

        mViewModel.fileLink.observe(viewLifecycleOwner) {
            if (it != null)
                mFileLinkAutoComplete.setText(it.path)
            else
                mFileLinkAutoComplete.setText("")
        }
    }

    private fun notifyItemChanged(type : Pages, index : Int?, add: Boolean = false, remove: Boolean = false) {
        when {
            type == Pages.NOT_LINKED && add && index != null && index > -1 -> (mRecyclePageNotLink.adapter as PageNotLinkCardAdapter).notifyItemInserted(index)
            type == Pages.NOT_LINKED && remove && index != null && index > -1 -> (mRecyclePageNotLink.adapter as PageNotLinkCardAdapter).notifyItemRemoved(index)
            type == Pages.NOT_LINKED && index != null && index > -1 -> (mRecyclePageNotLink.adapter as PageNotLinkCardAdapter).notifyItemChanged(index)
            type == Pages.NOT_LINKED && (index == null || index == -1) -> (mRecyclePageNotLink.adapter as PageNotLinkCardAdapter).notifyDataSetChanged()

            type != Pages.NOT_LINKED && add && index != null && index > -1 -> (mRecyclePageLink.adapter as PageLinkCardAdapter).notifyItemInserted(index)
            type != Pages.NOT_LINKED && remove && index != null && index > -1 -> (mRecyclePageLink.adapter as PageLinkCardAdapter).notifyItemRemoved(index)
            type != Pages.NOT_LINKED && index != null && index > -1 -> (mRecyclePageLink.adapter as PageLinkCardAdapter).notifyItemChanged(index)
            type != Pages.NOT_LINKED && (index == null || index == -1) -> (mRecyclePageLink.adapter as PageLinkCardAdapter).notifyDataSetChanged()
        }
    }

    private fun save() {
        mSave.isEnabled = false
        mRefresh.isEnabled = false

        mViewModel.save()
        Toast.makeText(
            requireContext(),
            getString(R.string.page_link_saved),
            Toast.LENGTH_SHORT
        ).show()

        mSave.isEnabled = true
        mRefresh.isEnabled = true
    }

    private fun refresh() {
        mSave.isEnabled = false
        mRefresh.isEnabled = false

        mViewModel.restoreBackup { index, type -> notifyItemChanged(type, index)  }
        Toast.makeText(
            requireContext(),
            getString(R.string.page_link_refreshed),
            Toast.LENGTH_SHORT
        ).show()

        mSave.isEnabled = true
        mRefresh.isEnabled = true
    }

    override fun onResume() {
        super.onResume()
        mViewModel.addImageLoadHandler(mImageLoadHandler)
    }

    override fun onPause() {
        mViewModel.removeImageLoadHandler(mImageLoadHandler)
        super.onPause()
    }

    override fun onStop() {
        SubTitleController.getInstance(requireContext()).setFileLink(mViewModel.getFileLink(null))
        super.onStop()
    }

    override fun onDestroy() {
        mViewModel.endThread()
        if (mHandler.hasCallbacks(mDismissUpButton))
            mHandler.removeCallbacks(mDismissUpButton)
        if (mHandler.hasCallbacks(mDismissDownButton))
            mHandler.removeCallbacks(mDismissDownButton)

        super.onDestroy()
    }

    private inner class ImageLoadHandler(fragment: PagesLinkFragment) : Handler() {
        private val mOwner: WeakReference<PagesLinkFragment> = WeakReference(fragment)
        override fun handleMessage(msg: Message) {
            val imageLoad = msg.obj as PagesLinkViewModel.ImageLoad
            when (msg.what) {
                PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_UPDATED -> notifyItemChanged(imageLoad.type, imageLoad.index)
                PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_LOAD_IMAGE_ERROR -> mViewModel.reLoadImages()
                PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_ADDED -> notifyItemChanged(imageLoad.type, imageLoad.index, add = true)
                PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_REMOVED -> notifyItemChanged(imageLoad.type, imageLoad.index, remove = true)
            }
        }
    }
}
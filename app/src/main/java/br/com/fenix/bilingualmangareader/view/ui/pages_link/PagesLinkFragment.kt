package br.com.fenix.bilingualmangareader.view.ui.pages_link

import android.app.Activity
import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.core.os.postDelayed
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.model.entity.PageLink
import br.com.fenix.bilingualmangareader.model.enums.LoadFile
import br.com.fenix.bilingualmangareader.model.enums.Pages
import br.com.fenix.bilingualmangareader.service.controller.SubTitleController
import br.com.fenix.bilingualmangareader.service.listener.PageLinkCardListener
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.util.helpers.Util
import br.com.fenix.bilingualmangareader.view.adapter.library.LineViewHolder
import br.com.fenix.bilingualmangareader.view.adapter.page_link.PageLinkCardAdapter
import br.com.fenix.bilingualmangareader.view.adapter.page_link.PageNotLinkCardAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import java.lang.ref.WeakReference


class PagesLinkFragment : Fragment() {

    private val mViewModel: PagesLinkViewModel by viewModels()
    private lateinit var mScrollUp: FloatingActionButton
    private lateinit var mScrollDown: FloatingActionButton
    private lateinit var mRecyclePageLink: RecyclerView
    private lateinit var mRecyclePageNotLink: RecyclerView
    private lateinit var mFileLink: TextInputLayout
    private lateinit var mFileLinkAutoComplete: AutoCompleteTextView
    private lateinit var mSave: Button
    private lateinit var mRefresh : Button
    private lateinit var mFullScreen: Button
    private lateinit var mListener: PageLinkCardListener

    private lateinit var mImageFullScreen : Bitmap
    private lateinit var mImageFullScreenExit : Bitmap

    private val mImageLoadHandler: Handler = ImageLoadHandler(this)
    private var showScrollButton : Boolean = true
    private var handler = Handler(Looper.getMainLooper())
    private val dismissUpButton = Runnable { mScrollUp.hide() }
    private val dismissDownButton = Runnable { mScrollDown.hide() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_pages_link, container, false)

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
            if (showScrollButton) {
                if (yOld > 200) {
                    if (handler.hasCallbacks(dismissDownButton)) {
                        handler.removeCallbacks(dismissDownButton)
                        mScrollDown.hide()
                    }
                    handler.removeCallbacks(dismissUpButton)
                    handler.postDelayed(dismissUpButton, 3000)
                    mScrollUp.show()
                } else if (yOld < -200) {
                    if (handler.hasCallbacks(dismissUpButton)) {
                        handler.removeCallbacks(dismissUpButton)
                        mScrollUp.hide()
                    }
                    handler.removeCallbacks(dismissDownButton)
                    handler.postDelayed(dismissDownButton, 3000)
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
            startActivityForResult(intent, 200)
        }

        mSave.setOnClickListener { save() }
        mRefresh.setOnClickListener { refresh() }

        mFullScreen.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fullscreen 0, 0, 0)
        mFullScreen.setOnClickListener {
            if (mFileLink.visibility == View.GONE) {
                mFileLink.visibility = View.VISIBLE
                mSave.visibility = View.VISIBLE
                mRefresh.visibility = View.VISIBLE
                mFullScreen.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fullscreen, 0, 0, 0)
            } else {
                mFileLink.visibility = View.GONE
                mSave.visibility = View.GONE
                mRefresh.visibility = View.GONE
                mFullScreen.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fullscreen_exit, 0, 0, 0)
            }
        }

        mListener = object : PageLinkCardListener {
            override fun onClick(page: PageLink) { }

            override fun onClickLong(view : View, page: PageLink, origin : Pages): Boolean {
                val pageLink = if (origin == Pages.LINKED) mViewModel.getPageLink(page) else mViewModel.getPageNotLink(page)
                val item = ClipData.Item(pageLink)
                val dragData = ClipData(
                    page.fileLinkPageName,
                    arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                    item)
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

            override fun onDropItem(origin : Pages, destiny : Pages, dragIndex: String, drop: PageLink?) {
                when {
                    origin == Pages.LINKED && destiny == Pages.LINKED -> {
                        mViewModel.onMove(mViewModel.getPageLink(Integer.valueOf(dragIndex)), drop!!)
                        notifyItemChanged(origin, null)
                    }
                    origin == Pages.LINKED && destiny == Pages.NOT_LINKED -> {
                        mViewModel.onNotLinked(mViewModel.getPageLink(Integer.valueOf(dragIndex)))
                        notifyItemChanged(origin, Integer.valueOf(dragIndex))
                        notifyItemChanged(destiny, mViewModel.pagesLinkNotLinked.value!!.size - 1)
                    }
                    origin == Pages.NOT_LINKED && destiny == Pages.LINKED -> {
                        mViewModel.fromNotLinked(mViewModel.getPageNotLink(Integer.valueOf(dragIndex)), drop!!)
                        notifyItemChanged(origin, null)
                        notifyItemChanged(destiny, null)
                    }
                }
            }
        }

        mRecyclePageLink.setOnDragListener { _, dragEvent ->
            when (dragEvent.action) {
                DragEvent.ACTION_DRAG_LOCATION -> {
                    val divider = 4

                    val padding = if (dragEvent.y < mRecyclePageLink.height.toFloat() / divider) - 250
                    else if (dragEvent.y > mRecyclePageLink.height.toFloat() / divider * (divider - 1)) + 250
                    else 0

                    if (padding != 0)
                        mRecyclePageLink.smoothScrollBy(0, padding)
                    true
                }
                DragEvent.ACTION_DRAG_STARTED -> {
                    showScrollButton = false
                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    showScrollButton = true
                    true
                }
                else -> true
            }
        }


        mRecyclePageNotLink.setOnDragListener { _, dragEvent ->
            when (dragEvent.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    showScrollButton = false
                    dragEvent.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
                }

                DragEvent.ACTION_DRAG_ENTERED -> {
                    mRecyclePageNotLink.setBackgroundColor(requireContext().getColor(R.color.onSecondary))
                    true
                }

                DragEvent.ACTION_DRAG_LOCATION -> true
                DragEvent.ACTION_DRAG_EXITED -> {
                    mRecyclePageNotLink.setBackgroundColor(requireContext().getColor(R.color.onPrimary))
                    true
                }

                DragEvent.ACTION_DROP -> {
                    mRecyclePageNotLink.setBackgroundColor(requireContext().getColor(R.color.onPrimary))

                    if (Pages.LINKED == Pages.valueOf(dragEvent.clipData.getItemAt(1).text.toString())) {
                        mViewModel.onNotLinked(mViewModel.getPageLink(Integer.valueOf(dragEvent.clipData.getItemAt(0).text.toString())))
                        (mRecyclePageNotLink.adapter as PageNotLinkCardAdapter).notifyItemChanged(mViewModel.pagesLinkNotLinked.value!!.size - 1)
                        (mRecyclePageLink.adapter as PageLinkCardAdapter).notifyItemChanged(Integer.valueOf(dragEvent.clipData.getItemAt(0).text.toString()))
                    }

                    val v = dragEvent.localState as View
                    v.visibility = View.VISIBLE
                    true
                }

                DragEvent.ACTION_DRAG_ENDED -> {
                    showScrollButton = true
                    val v = dragEvent.localState as View
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

        val adapterPageNotLink = PageNotLinkCardAdapter()
        mRecyclePageNotLink.adapter = adapterPageNotLink
        mRecyclePageNotLink.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        adapterPageNotLink.attachListener(mListener)

        val bundle = this.arguments
        if (bundle != null && bundle.containsKey(GeneralConsts.KEYS.OBJECT.MANGA))
            mViewModel.loadManga(bundle[GeneralConsts.KEYS.OBJECT.MANGA] as Manga) { index, type -> notifyItemChanged(type, index)  }

    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int, resultData: Intent?
    ) {
        if (requestCode == 200) {
            if (resultCode == Activity.RESULT_OK) {
                resultData?.data?.also { uri ->
                    try {
                        val path = Util.normalizeFilePath(uri.path.toString())
                        var loaded = mViewModel.readFileLink(path) { index, type -> notifyItemChanged(type, index)  }
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
            }
        }
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

    private fun notifyItemChanged(type : Pages, index : Int?) {
        when {
            type == Pages.NOT_LINKED && index != null -> (mRecyclePageNotLink.adapter as PageNotLinkCardAdapter).notifyItemChanged(index)
            type == Pages.NOT_LINKED && index == null -> (mRecyclePageNotLink.adapter as PageNotLinkCardAdapter).notifyDataSetChanged()
            (type == Pages.ALL || type == Pages.LINKED || type == Pages.MANGA) && index != null -> (mRecyclePageLink.adapter as PageLinkCardAdapter).notifyItemChanged(index)
            (type == Pages.ALL || type == Pages.LINKED || type == Pages.MANGA) && index == null -> (mRecyclePageLink.adapter as PageLinkCardAdapter).notifyDataSetChanged()
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
        mViewModel.endThread()
        SubTitleController.getInstance(requireContext()).setFileLink(mViewModel.getFileLink(null))
        super.onStop()
    }

    override fun onDestroy() {
        if (handler.hasCallbacks(dismissUpButton))
            handler.removeCallbacks(dismissUpButton)
        if (handler.hasCallbacks(dismissDownButton))
            handler.removeCallbacks(dismissDownButton)

        super.onDestroy()
    }

    private inner class ImageLoadHandler(fragment: PagesLinkFragment) : Handler() {
        private val mOwner: WeakReference<PagesLinkFragment> = WeakReference(fragment)
        override fun handleMessage(msg: Message) {
            val imageLoad = msg.obj as PagesLinkViewModel.ImageLoad
            notifyItemChanged(imageLoad.type, imageLoad.index)
        }
    }

}
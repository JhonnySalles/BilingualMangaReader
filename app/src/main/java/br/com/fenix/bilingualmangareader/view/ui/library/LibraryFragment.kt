package br.com.fenix.bilingualmangareader.view.ui.library

import android.Manifest
import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.DisplayMetrics
import android.util.Log
import android.util.Pair
import android.view.*
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.model.enums.LibraryType
import br.com.fenix.bilingualmangareader.model.enums.Order
import br.com.fenix.bilingualmangareader.service.listener.MangaCardListener
import br.com.fenix.bilingualmangareader.service.repository.Storage
import br.com.fenix.bilingualmangareader.service.scanner.Scanner
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.view.adapter.library.MangaGridCardAdapter
import br.com.fenix.bilingualmangareader.view.adapter.library.MangaLineCardAdapter
import br.com.fenix.bilingualmangareader.view.ui.manga_detail.MangaDetailActivity
import br.com.fenix.bilingualmangareader.view.ui.reader.ReaderActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.slf4j.LoggerFactory
import java.lang.ref.WeakReference
import kotlin.math.max


class LibraryFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private val mLOGGER = LoggerFactory.getLogger(LibraryFragment::class.java)
    private lateinit var mViewModel: LibraryViewModel
    private var mLibraryPath: String = ""
    private var mOrderBy: Order = Order.Name
    private lateinit var mMapOrder: HashMap<Order, String>

    private lateinit var mRoot: FrameLayout
    private lateinit var mRefreshLayout: SwipeRefreshLayout
    private lateinit var mRecycleView: RecyclerView
    private lateinit var miGridType: MenuItem
    private lateinit var miGridOrder: MenuItem
    private lateinit var miSearch: MenuItem
    private lateinit var searchView: SearchView
    private lateinit var mListener: MangaCardListener
    private lateinit var mScrollUp: FloatingActionButton
    private lateinit var mScrollDown: FloatingActionButton

    private var mHandler = Handler(Looper.getMainLooper())
    private val mDismissUpButton = Runnable { mScrollUp.hide() }
    private val mDismissDownButton = Runnable { mScrollDown.hide() }

    companion object {
        var mGridType: LibraryType = LibraryType.GRID_BIG
    }

    private val mUpdateHandler: Handler = UpdateHandler(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadConfig()
        setHasOptionsMenu(true)
        Scanner.getInstance(requireContext()).addUpdateHandler(mUpdateHandler)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)

        miGridType = menu.findItem(R.id.grid_type)
        miGridOrder = menu.findItem(R.id.list_order)
        miSearch = menu.findItem(R.id.search)
        searchView = miSearch.actionView as SearchView
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                mRefreshLayout.isEnabled = newText == null || newText.isEmpty()
                filter(newText)
                return false
            }
        })
        enableSearchView(searchView, !mRefreshLayout.isRefreshing)
        onChangeIconLayout()
    }

    private fun filter(newText: String?) {
        if (mGridType != LibraryType.LINE)
            (mRecycleView.adapter as MangaGridCardAdapter).filter.filter(newText)
        else
            (mRecycleView.adapter as MangaLineCardAdapter).filter.filter(newText)
    }

    override fun onResume() {
        super.onResume()

        if (Scanner.getInstance(requireContext()).isRunning())
            setIsRefreshing(true)

        mViewModel.update()

        if (mViewModel.isEmpty())
            onRefresh()
        else if (mViewModel.listMangas.value != null) {
            sortList(mViewModel.listMangas.value!!)
            notifyDataSet(0, mViewModel.getLastIndex())
        }
    }

    override fun onDestroy() {
        Scanner.getInstance(requireContext()).removeUpdateHandler(mUpdateHandler)
        super.onDestroy()
    }

    private inner class UpdateHandler(fragment: LibraryFragment) : Handler() {
        private val mOwner: WeakReference<LibraryFragment> = WeakReference(fragment)
        override fun handleMessage(msg: Message) {
            val fragment = mOwner.get() ?: return
            when (msg.what) {
                GeneralConsts.SCANNER.MESSAGE_MANGA_UPDATED_ADD -> fragment.refreshLibraryAddDelayed()
                GeneralConsts.SCANNER.MESSAGE_MANGA_UPDATED_REMOVE -> fragment.refreshLibraryRemoveDelayed()
                GeneralConsts.SCANNER.MESSAGE_MANGA_UPDATE_FINISHED -> {
                    mViewModel.list {
                        setIsRefreshing(false)
                        sortList(mViewModel.listMangas.value!!)
                        notifyDataSet(0, mViewModel.getLastIndex())
                    }
                }
            }
        }
    }

    private fun notifyDataSet(index: Int, range: Int = 1, insert: Boolean = false, removed: Boolean = false) {
        if (insert)
            mRecycleView.adapter?.notifyItemInserted(index)
        else if (removed)
            mRecycleView.adapter?.notifyItemRemoved(index)
        else if (range > 1)
            mRecycleView.adapter?.notifyItemRangeChanged(index, range)
        else
            mRecycleView.adapter?.notifyItemChanged(index)
    }

    private fun refreshLibraryAddDelayed() {
        val indexes = mViewModel.updateListAdd()
        for (index in indexes)
            mRecycleView.adapter?.notifyItemInserted(index)
    }

    private fun refreshLibraryRemoveDelayed() {
        val indexes = mViewModel.updateListRem()
        for (index in indexes)
            mRecycleView.adapter?.notifyItemRemoved(index)
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.grid_type -> onChangeLayout()
            R.id.list_order -> onChangeSort()
        }
        return super.onOptionsItemSelected(menuItem)
    }

    private fun onChangeSort() {
        if (mRefreshLayout.isRefreshing)
            return

        mOrderBy = when (mOrderBy) {
            Order.Name -> Order.Date
            Order.Date -> Order.Favorite
            Order.Favorite -> Order.LastAccess
            else -> Order.Name
        }

        Toast.makeText(
            requireContext(),
            getString(R.string.menu_reading_order_change) + " ${mMapOrder[mOrderBy]}",
            Toast.LENGTH_SHORT
        ).show()

        val sharedPreferences = GeneralConsts.getSharedPreferences(requireContext())
        with(sharedPreferences.edit()) {
            this!!.putString(GeneralConsts.KEYS.LIBRARY.ORDER, mOrderBy.toString())
            this.commit()
        }

        if (mViewModel.listMangas.value != null) {
            sortList(mViewModel.listMangas.value!!)
            notifyDataSet(0, mViewModel.getLastIndex())
        }
    }

    private fun sortList(list: ArrayList<Manga>) {
        when (mOrderBy) {
            Order.Date -> list.sortBy { it.dateCreate }
            Order.LastAccess -> list.sortWith(compareByDescending<Manga> { it.lastAccess }.thenBy { it.name })
            Order.Favorite -> list.sortWith(compareByDescending<Manga> { it.favorite }.thenBy { it.name })
            else -> list.sortBy { it.name }
        }
    }

    private fun onChangeLayout() {
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        mGridType = when (mGridType) {
            LibraryType.LINE -> LibraryType.GRID_BIG
            LibraryType.GRID_BIG -> LibraryType.GRID_MEDIUM
            LibraryType.GRID_MEDIUM -> if (isLandscape) LibraryType.GRID_SMALL else LibraryType.LINE
            else -> LibraryType.LINE
        }

        val sharedPreferences = GeneralConsts.getSharedPreferences(requireContext())
        with(sharedPreferences.edit()) {
            this!!.putString(GeneralConsts.KEYS.LIBRARY.LIBRARY_TYPE, mGridType.toString())
            this.commit()
        }

        onChangeIconLayout()
        generateLayout()
        updateList(mViewModel.listMangas.value!!)
    }

    private fun onChangeIconLayout() {
        val icon: Int = when (mGridType) {
            LibraryType.GRID_SMALL -> R.drawable.ic_type_grid_small
            LibraryType.GRID_BIG -> R.drawable.ic_type_grid_big
            LibraryType.GRID_MEDIUM -> R.drawable.ic_type_grid_medium
            else -> R.drawable.ic_type_list
        }
        miGridType.setIcon(icon)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mViewModel = ViewModelProvider(this).get(LibraryViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_library, container, false)
        val sharedPreferences = GeneralConsts.getSharedPreferences(requireContext())
        mGridType = LibraryType.valueOf(
            sharedPreferences.getString(GeneralConsts.KEYS.LIBRARY.LIBRARY_TYPE, "LINE")
                .toString()
        )

        mMapOrder = hashMapOf(
            Order.Name to getString(R.string.config_option_order_name),
            Order.Date to getString(R.string.config_option_order_date),
            Order.LastAccess to getString(R.string.config_option_order_access),
            Order.Favorite to getString(R.string.config_option_order_favorite)
        )
        mRoot = root.findViewById(R.id.library_root)
        mRecycleView = root.findViewById(R.id.library_recycler_view)
        mRefreshLayout = root.findViewById(R.id.library_refresh)
        mScrollUp = root.findViewById(R.id.library_scroll_up)
        mScrollDown = root.findViewById(R.id.library_scroll_down)

        mRefreshLayout.setColorSchemeResources(
            R.color.on_secondary,
            R.color.on_secondary,
            R.color.white
        )

        mRefreshLayout.setOnRefreshListener(this)
        mRefreshLayout.isEnabled = true

        mScrollUp.visibility = View.GONE
        mScrollDown.visibility = View.GONE

        mScrollUp.setOnClickListener { mRecycleView.smoothScrollToPosition(0) }
        mScrollDown.setOnClickListener {
            mRecycleView.smoothScrollToPosition((mRecycleView.adapter as RecyclerView.Adapter).itemCount)
        }

        mRecycleView.setOnScrollChangeListener { _, _, _, _, yOld ->
            if (yOld > 20 && mScrollDown.visibility == View.VISIBLE) {
                if (mHandler.hasCallbacks(mDismissDownButton))
                    mHandler.removeCallbacks(mDismissDownButton)

                mScrollDown.hide()
            } else if (yOld < -20 && mScrollUp.visibility == View.VISIBLE) {
                if (mHandler.hasCallbacks(mDismissUpButton))
                    mHandler.removeCallbacks(mDismissUpButton)

                mScrollUp.hide()
            }

            if (yOld > 180) {
                mHandler.removeCallbacks(mDismissUpButton)
                mHandler.postDelayed(mDismissUpButton, 3000)
                mScrollUp.show()
            } else if (yOld < -180) {
                mHandler.removeCallbacks(mDismissDownButton)
                mHandler.postDelayed(mDismissDownButton, 3000)
                mScrollDown.show()
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecycleView)

        mListener = object : MangaCardListener {
            override fun onClick(manga: Manga) {
                val intent = Intent(context, ReaderActivity::class.java)
                val bundle = Bundle()
                bundle.putString(GeneralConsts.KEYS.MANGA.NAME, manga.title)
                bundle.putInt(GeneralConsts.KEYS.MANGA.MARK, manga.bookMark)
                bundle.putSerializable(GeneralConsts.KEYS.OBJECT.MANGA, manga)
                intent.putExtras(bundle)
                context?.startActivity(intent)
                requireActivity().overridePendingTransition(R.anim.fade_in_fragment_add_enter, R.anim.fade_out_fragment_remove_exit)
            }

            override fun onClickLong(manga: Manga, view: View, position: Int) {
                if (mRefreshLayout.isRefreshing)
                    return

                goMangaDetail(manga, view, position)
            }

        }
        observer()

        if (!Storage.isPermissionGranted(requireContext()))
            Storage.takePermission(requireContext(), requireActivity())

        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
            PackageManager.PERMISSION_GRANTED
        )

        generateLayout()
        setIsRefreshing(true)
        Scanner.getInstance(requireContext()).scanLibrary()
        return root
    }

    private var itemRefresh: Int? = null
    private fun goMangaDetail(manga: Manga, view: View, position: Int) {
        itemRefresh = position
        val intent = Intent(requireContext(), MangaDetailActivity::class.java)
        val bundle = Bundle()
        bundle.putSerializable(GeneralConsts.KEYS.OBJECT.MANGA, manga)
        intent.putExtras(bundle)
        val idText = if (mGridType != LibraryType.LINE)
            R.id.manga_grid_text_title
        else
            R.id.manga_line_text_title

        val title = view.findViewById<TextView>(idText)
        val pImageCover: Pair<View, String> = Pair(view, "transition_manga_cover")
        val pTitleCover: Pair<View, String> = Pair(title, "transition_manga_title")

        val options = ActivityOptions
            .makeSceneTransitionAnimation(requireActivity(), *arrayOf(pImageCover, pTitleCover))
        requireActivity().overridePendingTransition(R.anim.fade_in_fragment_add_enter, R.anim.fade_out_fragment_remove_exit)
        startActivityForResult(intent, GeneralConsts.MANGA_DETAIL.REQUEST_ENDED, options.toBundle())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GeneralConsts.MANGA_DETAIL.REQUEST_ENDED)
            notifyDataSet(itemRefresh!!)
    }

    private fun loadConfig() {
        val sharedPreferences = GeneralConsts.getSharedPreferences(requireContext())
        mLibraryPath =
            sharedPreferences.getString(GeneralConsts.KEYS.LIBRARY.FOLDER, "").toString()
        mOrderBy = Order.valueOf(
            sharedPreferences.getString(
                GeneralConsts.KEYS.LIBRARY.ORDER,
                Order.Name.toString()
            ).toString()
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == GeneralConsts.REQUEST.PERMISSION_FILES_ACCESS && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            onRefresh()
    }

    private fun generateLayout() {
        if (mGridType != LibraryType.LINE) {
            val gridAdapter = MangaGridCardAdapter()
            mRecycleView.adapter = gridAdapter

            val isLandscape =
                resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            val columnWidth: Int = when (mGridType) {
                LibraryType.GRID_BIG -> resources.getDimension(R.dimen.manga_grid_card_layout_width)
                    .toInt()
                LibraryType.GRID_MEDIUM -> if (isLandscape) resources.getDimension(R.dimen.manga_grid_card_layout_width_landscape_medium)
                    .toInt() else resources.getDimension(R.dimen.manga_grid_card_layout_width_medium)
                    .toInt()
                LibraryType.GRID_SMALL -> if (isLandscape) resources.getDimension(R.dimen.manga_grid_card_layout_width_small)
                    .toInt()
                else resources.getDimension(R.dimen.manga_grid_card_layout_width).toInt()
                else -> resources.getDimension(R.dimen.manga_grid_card_layout_width).toInt()
            } + 1

            val displayMetrics = DisplayMetrics()
            (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)

            val spaceCount: Int = max(1, displayMetrics.widthPixels / columnWidth)
            mRecycleView.layoutManager = GridLayoutManager(requireContext(), spaceCount)
            gridAdapter.attachListener(mListener)
            mRecycleView.layoutAnimation = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_library_grid)
        } else {
            val lineAdapter = MangaLineCardAdapter()
            mRecycleView.adapter = lineAdapter
            mRecycleView.layoutManager = GridLayoutManager(requireContext(), 1)
            lineAdapter.attachListener(mListener)
            mRecycleView.layoutAnimation = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_library_line)
        }
    }

    private fun updateList(list: ArrayList<Manga>) {
        sortList(list)
        if (mGridType != LibraryType.LINE)
            (mRecycleView.adapter as MangaGridCardAdapter).updateList(list)
        else
            (mRecycleView.adapter as MangaLineCardAdapter).updateList(list)
    }

    private fun observer() {
        mViewModel.listMangas.observe(viewLifecycleOwner) {
            updateList(it)
        }
    }

    fun setIsRefreshing(enabled: Boolean) {
        try {
            mRefreshLayout.isRefreshing = enabled

            if (!::searchView.isInitialized || !::mRecycleView.isInitialized)
                return

            if (enabled)
                searchView.clearFocus()
            enableSearchView(searchView, !enabled)
        } catch (e: Exception) {
            Log.e(GeneralConsts.TAG.LOG, "Disable search button error: " + e.message)
        }
    }

    private fun enableSearchView(view: View, enabled: Boolean) {
        view.isEnabled = enabled
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                enableSearchView(child, enabled)
            }
        }
    }

    override fun onRefresh() {
        if (mHandler.hasCallbacks(mDismissUpButton))
            mHandler.removeCallbacks(mDismissUpButton)
        if (mHandler.hasCallbacks(mDismissDownButton))
            mHandler.removeCallbacks(mDismissDownButton)

        mScrollUp.hide()
        mScrollDown.hide()

        if (!Scanner.getInstance(requireContext()).isRunning()) {
            setIsRefreshing(true)
            Scanner.getInstance(requireContext()).scanLibrary()
        }
    }

    private var itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: ViewHolder, target: ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
            val manga = mViewModel.get(viewHolder.adapterPosition) ?: return
            val position = viewHolder.adapterPosition
            mViewModel.remove(manga)
            var excluded = false
            val dialog: AlertDialog =
                AlertDialog.Builder(requireActivity(), R.style.AppCompatAlertDialogStyle)
                    .setTitle(getString(R.string.library_menu_delete))
                    .setMessage(getString(R.string.library_menu_delete_description) + "\n" + manga.file.name)
                    .setPositiveButton(
                        R.string.action_delete
                    ) { _, _ ->
                        deleteFile(manga)
                        notifyDataSet(position, removed = true)
                        excluded = true
                    }.setOnDismissListener {
                        if (!excluded) {
                            mViewModel.add(manga, position)
                            notifyDataSet(position)
                        }
                    }
                    .create()
            dialog.show()
        }
    }

    private fun deleteFile(manga: Manga?) {
        if (manga?.file != null) {
            mViewModel.delete(manga)
            if (manga.file.exists()) {
                val isDeleted = manga.file.delete()
                mLOGGER.info("File deleted ${manga.name}: $isDeleted")
            }
        }
    }

}
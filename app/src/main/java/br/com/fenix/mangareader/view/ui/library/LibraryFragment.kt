package br.com.fenix.mangareader.view.ui.library

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import br.com.fenix.mangareader.R
import br.com.fenix.mangareader.model.entity.Manga
import br.com.fenix.mangareader.model.enums.LibraryType
import br.com.fenix.mangareader.model.enums.Order
import br.com.fenix.mangareader.service.controller.ImageCoverController
import br.com.fenix.mangareader.service.listener.MangaCardListener
import br.com.fenix.mangareader.service.repository.MangaRepository
import br.com.fenix.mangareader.service.repository.Storage
import br.com.fenix.mangareader.service.scanner.Scanner
import br.com.fenix.mangareader.util.constants.GeneralConsts
import br.com.fenix.mangareader.view.adapter.library.MangaGridCardAdapter
import br.com.fenix.mangareader.view.adapter.library.MangaLineCardAdapter
import br.com.fenix.mangareader.view.ui.reader.ReaderActivity
import java.lang.ref.WeakReference
import java.time.LocalDateTime
import java.util.*
import kotlin.math.max


class LibraryFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var mViewModel: LibraryViewModel
    private var mLibraryPath: String = ""
    private var mOrderBy: Order = Order.Name
    private lateinit var mMapOrder: HashMap<Order, String>

    private lateinit var mRefreshLayout: SwipeRefreshLayout
    private lateinit var mRecycleView: RecyclerView
    private lateinit var miGridType: MenuItem
    private lateinit var miGridOrder: MenuItem
    private lateinit var miSearch: MenuItem
    private lateinit var searchView: SearchView
    private lateinit var mListener: MangaCardListener
    private var mIsRefreshPlanned = false
    private lateinit var mRepository: MangaRepository

    companion object {
        var mGridType: LibraryType = LibraryType.GRID_BIG
    }

    private val mUpdateHandler: Handler = UpdateHandler(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadConfig()
        setHasOptionsMenu(true)
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

        Scanner.getInstance().addUpdateHandler(mUpdateHandler)
        ImageCoverController.instance.addUpdateHandler(mUpdateHandler)
        if (Scanner.getInstance().isRunning())
            setIsRefreshing(true)

        mViewModel.update()
        notifyDataSet()
    }

    override fun onPause() {
        Scanner.getInstance().removeUpdateHandler(mUpdateHandler)
        ImageCoverController.instance.removeUpdateHandler(mUpdateHandler)
        super.onPause()
    }

    private inner class UpdateHandler(fragment: LibraryFragment) : Handler() {
        private val mOwner: WeakReference<LibraryFragment> = WeakReference(fragment)
        override fun handleMessage(msg: Message) {
            val fragment = mOwner.get() ?: return
            when (msg.what) {
                GeneralConsts.SCANNER.MESSAGE_MANGA_UPDATED -> {
                    fragment.refreshLibraryDelayed()
                }
                GeneralConsts.SCANNER.MESSAGE_MANGA_UPDATE_FINISHED -> {
                    mViewModel.list {
                        setIsRefreshing(false)
                        notifyDataSet()
                    }
                }
                GeneralConsts.SCANNER.MESSAGE_COVER_UPDATE_FINISHED -> {
                    val idItem = msg.data.getInt("position")
                    notifyDataSet(idItem)
                }
            }
        }
    }

    private fun notifyDataSet(idItem: Int? = null) {
        if (idItem != null)
            mRecycleView.adapter?.notifyItemChanged(idItem)
        else
            mRecycleView.adapter?.notifyDataSetChanged()
    }

    private fun refreshLibraryDelayed() {
        if (!mIsRefreshPlanned) {
            val updateRunnable = Runnable {
                mViewModel.updateList()
                notifyDataSet()
                mIsRefreshPlanned = false
            }
            mIsRefreshPlanned = true
            mRecycleView.postDelayed(updateRunnable, 100)
        }
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

        if (mViewModel.save.value != null) {
            sortList(mViewModel.save.value!!)
            notifyDataSet()
        }
    }

    private fun sortList(list: ArrayList<Manga>) {
        when (mOrderBy) {
            Order.Date -> list.sortBy { it.dateCreate }
            Order.LastAccess -> list.sortByDescending { it.lastAccess }
            Order.Favorite -> list.sortBy { it.favorite }
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
        updateList(mViewModel.save.value!!)
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

        mRepository = MangaRepository(requireContext())

        mRecycleView = root.findViewById(R.id.rv_library)
        mRefreshLayout = root.findViewById(R.id.rl_library)
        //mRefreshLayout.setColorSchemeColors(R.color.primary)
        mRefreshLayout.setOnRefreshListener(this)
        mRefreshLayout.isEnabled = true

        mListener = object : MangaCardListener {
            override fun onClick(manga: Manga) {
                val intent = Intent(context, ReaderActivity::class.java)
                val bundle = Bundle()
                manga.lastAccess = LocalDateTime.now()
                bundle.putString(GeneralConsts.KEYS.MANGA.NAME, manga.title)
                bundle.putInt(GeneralConsts.KEYS.MANGA.MARK, manga.bookMark)
                bundle.putSerializable(GeneralConsts.KEYS.OBJECT.MANGA, manga)
                intent.putExtras(bundle)
                context?.startActivity(intent)
                mViewModel.updateLastAccess(manga)
            }

            override fun onClickLong(manga: Manga, view: View, position: Int) {
                if (mRefreshLayout.isRefreshing)
                    return

                val wrapper = ContextThemeWrapper(requireContext(), R.style.PopupMenu)
                val popup = PopupMenu(wrapper, view)
                popup.menuInflater.inflate(R.menu.menu_book, popup.menu)

                if (manga.favorite)
                    popup.menu.findItem(R.id.menu_book_favorite).title = getString(R.string.library_menu_favorite_remove)
                else
                    popup.menu.findItem(R.id.menu_book_favorite).title = getString(R.string.library_menu_favorite_add)

                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.menu_book_favorite -> {
                            manga.favorite = !manga.favorite
                            mRepository.update(manga)
                            notifyDataSet(position)
                        }
                        R.id.menu_book_clear -> {
                            manga.lastAccess = LocalDateTime.MIN
                            manga.bookMark = 0
                            mRepository.update(manga)
                            notifyDataSet(position)
                        }
                        R.id.menu_book_delete -> {
                            val dialog: AlertDialog =
                                AlertDialog.Builder(requireActivity(), R.style.AppCompatAlertDialogStyle)
                                    .setTitle(getString(R.string.library_menu_delete))
                                    .setMessage(getString(R.string.library_menu_delete_description) + "\n" + manga.file.name)
                                    .setPositiveButton(
                                        R.string.action_positive
                                    ) { _, _ ->
                                        manga.file.delete()
                                        mRepository.delete(manga)
                                        mViewModel.remove(manga)
                                        notifyDataSet(position)
                                    }
                                    .setNegativeButton(
                                        R.string.action_negative
                                    ) { _, _ -> }
                                    .create()
                            dialog.show()
                        }
                    }
                    true
                }

                popup.show()
            }

        }
        observer()

        if (!Storage.isPermissionGranted(requireContext()))
            Storage.takePermission(requireContext(), requireActivity())

        generateLayout()
        setIsRefreshing(true)
        Scanner.getInstance().addUpdateHandler(mUpdateHandler)
        Scanner.getInstance().scanLibrary()
        return root
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

        if (requestCode == 121 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
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
                LibraryType.GRID_SMALL -> if (isLandscape) resources.getDimension(R.dimen.manga_grid_card_layout_height_small)
                    .toInt()
                else resources.getDimension(R.dimen.manga_grid_card_layout_width).toInt()
                else -> resources.getDimension(R.dimen.manga_grid_card_layout_width).toInt()
            }

            val displayMetrics = DisplayMetrics()
            (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)

            val spaceCount: Int = max(1, displayMetrics.widthPixels / columnWidth)
            mRecycleView.layoutManager = GridLayoutManager(requireContext(), spaceCount)
            gridAdapter.attachListener(mListener)
        } else {
            val lineAdapter = MangaLineCardAdapter()
            mRecycleView.adapter = lineAdapter
            mRecycleView.layoutManager = GridLayoutManager(requireContext(), 1)
            lineAdapter.attachListener(mListener)
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
        mViewModel.save.observe(viewLifecycleOwner, {
            updateList(it)
        })
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
        if (!Scanner.getInstance().isRunning()) {
            setIsRefreshing(true)
            Scanner.getInstance().scanLibrary()
        }
    }

}
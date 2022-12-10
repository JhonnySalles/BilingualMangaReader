package br.com.fenix.bilingualmangareader.view.ui.menu

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.SearchView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Library
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.model.enums.Libraries
import br.com.fenix.bilingualmangareader.model.enums.LibraryType
import br.com.fenix.bilingualmangareader.service.listener.MangaCardListener
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.util.helpers.MenuUtil
import br.com.fenix.bilingualmangareader.view.adapter.library.MangaGridCardAdapter
import br.com.fenix.bilingualmangareader.view.adapter.library.MangaLineCardAdapter
import br.com.fenix.bilingualmangareader.view.ui.library.LibraryFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.slf4j.LoggerFactory
import kotlin.math.max


class SelectMangaFragment : Fragment() {

    private val mLOGGER = LoggerFactory.getLogger(SelectMangaFragment::class.java)

    private val mViewModel: SelectMangaViewModel by viewModels()

    private lateinit var mRoot: ConstraintLayout
    private lateinit var mScrollUp: FloatingActionButton
    private lateinit var mScrollDown: FloatingActionButton
    private lateinit var mRecycler: RecyclerView

    private lateinit var mTitle: TextView
    private lateinit var mToolbar: androidx.appcompat.widget.Toolbar
    private lateinit var miSearch: MenuItem
    private lateinit var searchView: SearchView

    private lateinit var mListener: MangaCardListener

    private var mHandler = Handler(Looper.getMainLooper())

    private val mDismissUpButton = Runnable { mScrollUp.hide() }
    private val mDismissDownButton = Runnable { mScrollDown.hide() }

    companion object {
        var mGridType: LibraryType = LibraryType.GRID_BIG
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        if (savedInstanceState == null) {
            mViewModel.clearMangaSelected()
            arguments?.let {
                if (it.containsKey(GeneralConsts.KEYS.MANGA.ID))
                    mViewModel.id = it.getLong(GeneralConsts.KEYS.MANGA.ID)

                if (it.containsKey(GeneralConsts.KEYS.MANGA.NAME))
                    mViewModel.manga = it.getString(GeneralConsts.KEYS.MANGA.NAME)!!
            }
            mViewModel.setDefaultLibrary(Libraries.PORTUGUESE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_select_manga, menu)
        super.onCreateOptionsMenu(menu, inflater)

        miSearch = menu.findItem(R.id.menu_select_manga_search)
        searchView = miSearch.actionView as SearchView
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                mViewModel.filter.filter(newText)
                return false
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_select_manga, container, false)

        mGridType = LibraryType.valueOf(
            GeneralConsts.getSharedPreferences(requireContext())
                .getString(GeneralConsts.KEYS.LIBRARY.LIBRARY_TYPE, LibraryType.LINE.toString())
                .toString()
        )

        mRoot = root.findViewById(R.id.select_manga_root)
        mRecycler = root.findViewById(R.id.select_manga_recycler)
        mScrollUp = root.findViewById(R.id.select_manga_scroll_up)
        mScrollDown = root.findViewById(R.id.select_manga_scroll_down)
        mToolbar = root.findViewById(R.id.toolbar_select_manga)
        mTitle = root.findViewById(R.id.toolbar_select_manga_title)
        MenuUtil.tintColor(requireContext(), mTitle)

        (requireActivity() as MenuActivity).setActionBar(mToolbar)

        registerForContextMenu(mTitle)

        mScrollUp.visibility = View.GONE
        mScrollDown.visibility = View.GONE

        mScrollUp.setOnClickListener { mRecycler.smoothScrollToPosition(0) }
        mScrollDown.setOnClickListener {
            mRecycler.smoothScrollToPosition((mRecycler.adapter as RecyclerView.Adapter).itemCount)
        }

        mRecycler.setOnScrollChangeListener { _, _, _, _, yOld ->
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
                    if (mHandler.hasCallbacks(mDismissUpButton))
                        mHandler.removeCallbacks(mDismissUpButton)
                } else
                    mHandler.removeCallbacks(mDismissUpButton)

                mHandler.postDelayed(mDismissDownButton, 3000)
                mScrollDown.show()
            }
        }

        mListener = object : MangaCardListener {
            override fun onClick(manga: Manga) {
                val bundle = Bundle()
                bundle.putSerializable(GeneralConsts.KEYS.OBJECT.MANGA, manga)
                (requireActivity() as MenuActivity).onBack(bundle)
            }

            override fun onClickLong(manga: Manga, view: View, position: Int) {}
        }

        observer()
        mViewModel.list(mViewModel.id, mViewModel.manga) { }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerLayout()
    }

    private fun recyclerLayout() {
        if (LibraryFragment.mGridType != LibraryType.LINE) {
            val gridAdapter = MangaGridCardAdapter()
            mRecycler.adapter = gridAdapter

            val isLandscape =
                resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            val columnWidth: Int = when (LibraryFragment.mGridType) {
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

            val spaceCount: Int = max(1, Resources.getSystem().displayMetrics.widthPixels / columnWidth)
            mRecycler.layoutManager = GridLayoutManager(requireContext(), spaceCount)
            gridAdapter.attachListener(mListener)
            mRecycler.layoutAnimation = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_library_grid)
        } else {
            val lineAdapter = MangaLineCardAdapter()
            mRecycler.adapter = lineAdapter
            mRecycler.layoutManager = GridLayoutManager(requireContext(), 1)
            lineAdapter.attachListener(mListener)
            mRecycler.layoutAnimation = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_library_line)
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        for (library in mViewModel.getLibraryList())
            menu.add(library.title).setOnMenuItemClickListener { _: MenuItem? ->
                changeLibrary(library)
                true
            }
    }

    override fun onResume() {
        super.onResume()
        titleLibrary()
    }

    private fun changeLibrary(library: Library) {
        mViewModel.changeLibrary(library)
        titleLibrary()
    }

    private fun titleLibrary() {
        if (mViewModel.getLibrary().type == Libraries.DEFAULT)
            mTitle.text = getString(R.string.app_name)
        else
            mTitle.text = mViewModel.getLibrary().title
    }

    private fun observer() {
        mViewModel.listMangas.observe(viewLifecycleOwner) {
            if (mGridType != LibraryType.LINE)
                (mRecycler.adapter as MangaGridCardAdapter).updateList(it)
            else
                (mRecycler.adapter as MangaLineCardAdapter).updateList(it)
        }
    }

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (mHandler.hasCallbacks(mDismissUpButton))
                mHandler.removeCallbacks(mDismissUpButton)
            if (mHandler.hasCallbacks(mDismissDownButton))
                mHandler.removeCallbacks(mDismissDownButton)
        } else {
            mHandler.removeCallbacks(mDismissUpButton)
            mHandler.removeCallbacks(mDismissDownButton)
        }

        super.onDestroy()
    }

}
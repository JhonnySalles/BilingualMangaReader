package br.com.fenix.mangareader.view.ui.library

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.InputType
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import br.com.fenix.mangareader.R
import br.com.fenix.mangareader.model.entity.Book
import br.com.fenix.mangareader.model.enums.LibraryType
import br.com.fenix.mangareader.service.listener.BookCardListener
import br.com.fenix.mangareader.service.repository.Storage
import br.com.fenix.mangareader.service.scanner.Scanner
import br.com.fenix.mangareader.util.constants.GeneralConsts
import br.com.fenix.mangareader.view.adapter.library.BookGridCardAdapter
import br.com.fenix.mangareader.view.adapter.library.BookLineCardAdapter
import br.com.fenix.mangareader.view.ui.reader.ReaderActivity
import java.lang.ref.WeakReference
import android.view.ViewGroup
import org.apache.commons.compress.utils.IOUtils
import java.io.File
import java.io.FileInputStream
import java.lang.Exception


class LibraryFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var mViewModel: LibraryViewModel
    private var mLibraryPath: String = ""

    private lateinit var mRefreshLayout: SwipeRefreshLayout
    private lateinit var mRecycleView: RecyclerView
    private var mGridType: LibraryType = LibraryType.GRID
    private lateinit var miGridType: MenuItem
    private lateinit var miSearch: MenuItem
    private lateinit var searchView: SearchView
    private lateinit var mListener: BookCardListener
    private var mIsRefreshPlanned = false

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
        miSearch = menu.findItem(R.id.search)
        searchView = miSearch.actionView as SearchView
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                mRefreshLayout.isEnabled = newText == null || newText?.isEmpty()
                filter(newText)
                return false
            }
        })
    }

    private fun filter(newText: String?) {
        if (mGridType == LibraryType.GRID)
            (mRecycleView.adapter as BookGridCardAdapter).filter.filter(newText)
        else
            (mRecycleView.adapter as BookLineCardAdapter).filter.filter(newText)
    }

    override fun onResume() {
        super.onResume()

        Scanner.getInstance().addUpdateHandler(mUpdateHandler)
        if (Scanner.getInstance().isRunning())
            setRefresh(true)
        else {
            mViewModel.clear()
            Scanner.getInstance().scanLibrary()
        }
    }

    override fun onPause() {
        Scanner.getInstance().removeUpdateHandler(mUpdateHandler)
        super.onPause()
    }

    private inner class UpdateHandler(fragment: LibraryFragment) : Handler() {
        private val mOwner: WeakReference<LibraryFragment> = WeakReference(fragment)
        override fun handleMessage(msg: Message) {
            val fragment = mOwner.get() ?: return
            if (msg.what == GeneralConsts.SCANNER.MESSAGE_MEDIA_UPDATED) {
                fragment.refreshLibraryDelayed()
            } else if (msg.what == GeneralConsts.SCANNER.MESSAGE_MEDIA_UPDATE_FINISHED) {
                mViewModel.list(true)

                if (mGridType == LibraryType.GRID)
                    (mRecycleView.adapter as BookGridCardAdapter).notifyDataSetChanged()
                else
                    (mRecycleView.adapter as BookLineCardAdapter).notifyDataSetChanged()

                setRefresh(false)
            }
        }
    }

    private fun refreshLibraryDelayed() {
        if (!mIsRefreshPlanned) {
            val updateRunnable = Runnable {
                mViewModel.list(true)

                if (mGridType == LibraryType.GRID)
                    (mRecycleView.adapter as BookGridCardAdapter).notifyDataSetChanged()
                else
                    (mRecycleView.adapter as BookLineCardAdapter).notifyDataSetChanged()

                mIsRefreshPlanned = false
            }
            mIsRefreshPlanned = true
            mRecycleView.postDelayed(updateRunnable, 100)
        }
    }


    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.grid_type -> onChangeLayout()
        }
        return super.onOptionsItemSelected(menuItem)
    }

    private fun onChangeLayout() {
        mGridType = if (mGridType == LibraryType.LINE)
            LibraryType.GRID
        else
            LibraryType.LINE

        val sharedPreferences = GeneralConsts.getSharedPreferences(requireContext())
        with(sharedPreferences?.edit()) {
            this!!.putString(GeneralConsts.KEYS.LIBRARY.LIBRARY_TYPE, mGridType.toString())
            this.commit()
        }
        generateLayout()
        updateList(mViewModel.save.value!!)
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
            sharedPreferences?.getString(GeneralConsts.KEYS.LIBRARY.LIBRARY_TYPE, "LINE")
                .toString()
        )

        mRecycleView = root.findViewById(R.id.rv_library)
        mRefreshLayout = root.findViewById(R.id.rl_library)
        //mRefreshLayout.setColorSchemeColors(R.color.primary)
        mRefreshLayout.setOnRefreshListener(this)
        mRefreshLayout.isEnabled = true

        mListener = object : BookCardListener {
            override fun onClick(book: Book) {
                val intent = Intent(context, ReaderActivity::class.java)
                val bundle = Bundle()
                bundle.putString(GeneralConsts.KEYS.BOOK.NAME, book.title)
                bundle.putInt(GeneralConsts.KEYS.BOOK.MARK, book.bookMark)
                bundle.putSerializable(GeneralConsts.KEYS.OBJECT.BOOK, book)
                intent.putExtras(bundle)
                context?.startActivity(intent)
            }

            override fun onClickLong(book: Book) {
                TODO("Not yet implemented")
            }

            override fun onAddFavorite(book: Book) {
                TODO("Not yet implemented")
            }

        }
        generateLayout()
        observer()

        if (!Storage.isPermissionGranted(requireContext()))
            Storage.takePermission(requireContext(), requireActivity())

        return root
    }

    private fun loadConfig() {
        val sharedPreferences = GeneralConsts.getSharedPreferences(requireContext())
        mLibraryPath =
            sharedPreferences?.getString(GeneralConsts.KEYS.LIBRARY.FOLDER, "").toString()
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
        if (mGridType == LibraryType.GRID) {
            val gridAdapter = BookGridCardAdapter()
            mRecycleView.adapter = gridAdapter
            mRecycleView.layoutManager = GridLayoutManager(requireContext(), 2)
            gridAdapter.attachListener(mListener)
        } else {
            val lineAdapter = BookLineCardAdapter()
            mRecycleView.adapter = lineAdapter
            mRecycleView.layoutManager = GridLayoutManager(requireContext(), 1)
            lineAdapter.attachListener(mListener)
        }
    }

    private fun updateList(list: ArrayList<Book>) {
        if (mGridType == LibraryType.GRID)
            (mRecycleView.adapter as BookGridCardAdapter).updateList(list)
        else
            (mRecycleView.adapter as BookLineCardAdapter).updateList(list)
    }

    private fun observer() {
        mViewModel.save.observe(viewLifecycleOwner, {
            updateList(it)
        })
    }

    fun setRefresh(enabled: Boolean) {
        try {
            if (enabled)
                searchView.clearFocus()
            enableSearchView(searchView, !enabled)
        }catch (e : Exception) {
            Log.e(GeneralConsts.TAG.LOG, "Erro ao desabilitar o botão de pesquisa: " + e.message)
        }
        mRefreshLayout.isRefreshing = enabled
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
            setRefresh(true)
            mViewModel.clear()
            Scanner.getInstance().scanLibrary()
        }
    }

}
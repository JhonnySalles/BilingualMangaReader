package br.com.fenix.mangareader.view.ui.library

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.mangareader.R
import br.com.fenix.mangareader.model.entity.Book
import br.com.fenix.mangareader.model.enums.LibraryType
import br.com.fenix.mangareader.service.listener.BookCardListener
import br.com.fenix.mangareader.service.repository.Storage
import br.com.fenix.mangareader.util.constants.GeneralConsts
import br.com.fenix.mangareader.view.adapter.library.BookGridCardAdapter
import br.com.fenix.mangareader.view.adapter.library.BookLineCardAdapter
import br.com.fenix.mangareader.view.ui.reader.ReaderActivity


class LibraryFragment : Fragment() {

    private lateinit var mViewModel: LibraryViewModel
    private var mLibraryPath: String = ""

    private lateinit var mRecycleView: RecyclerView
    private var mGridType: LibraryType = LibraryType.GRID
    private lateinit var miGridType: MenuItem
    private lateinit var miSearch: MenuItem
    private lateinit var mListener: BookCardListener

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
        val searchView: SearchView = miSearch.actionView as SearchView

        searchView.imeOptions = EditorInfo.IME_ACTION_DONE

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
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

        mViewModel.readFiles(mLibraryPath)
        updateList(mViewModel.save.value!!)
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.grid_type -> {
                onChangeLayout()
            }
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
        mListener = object : BookCardListener {
            override fun onClick(book: Book) {
                val intent = Intent(context, ReaderActivity::class.java)
                intent.putExtra(GeneralConsts.KEYS.OBJECT.BOOK, book)
                val bundle = Bundle()
                bundle.putString(GeneralConsts.KEYS.BOOK.NAME, book.file?.path)
                bundle.putInt(GeneralConsts.KEYS.BOOK.MARK, book.bookMark)
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
            mViewModel.readFiles(mLibraryPath)
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

}
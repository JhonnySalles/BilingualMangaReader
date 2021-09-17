package br.com.fenix.mangareader.ui.library

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.mangareader.R
import br.com.fenix.mangareader.adapter.BookGridCardAdapter
import br.com.fenix.mangareader.adapter.BookLineCardAdapter
import br.com.fenix.mangareader.constants.GeneralConsts
import br.com.fenix.mangareader.enums.LibraryType
import br.com.fenix.mangareader.listener.BookCardListener
import br.com.fenix.mangareader.model.Book
import br.com.fenix.mangareader.repository.Storage


class LibraryFragment() : Fragment() {

    private lateinit var mViewModel: LibraryViewModel
    private var mLibraryPath: String = ""

    private lateinit var mRecycleView: RecyclerView
    private var mGridType: LibraryType = LibraryType.GRID
    private lateinit var miGridType: MenuItem
    private lateinit var miSearch: MenuItem
    private lateinit var mListener: BookCardListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProvider(this).get(LibraryViewModel::class.java)
        loadConfig()
        setHasOptionsMenu(true);
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)

        val sharedPreferences = GeneralConsts.getSharedPreferences(requireContext())
        mGridType = LibraryType.valueOf(
            sharedPreferences?.getString(GeneralConsts.KEYS.LIBRARY.LIBRARY_TYPE, "LINE")
                .toString()
        )

        mRecycleView = view.findViewById(R.id.rv_library)

        generateLayout()
        mListener = object : BookCardListener {
            override fun onClick(id: Long?) {
                /*val intent = Intent(context, GuestFormActivity::class.java)

                val bundle = Bundle()
                bundle.putInt(GuestConstants.GUESTID, id)

                intent.putExtras(bundle)
                startActivity(intent)*/
            }
        }
        observer()

        if (!Storage.isPermissionGranted(requireContext()))
            Storage.takePermission(requireContext(), requireActivity())

    }

    override fun onResume() {
        super.onResume()

        if (mGridType == LibraryType.GRID)
            (mRecycleView.adapter as BookGridCardAdapter).attachListener(mListener)
        else
            (mRecycleView.adapter as BookLineCardAdapter).attachListener(mListener)

        mViewModel.readFiles(mLibraryPath)
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        Log.i("Teste click", menuItem.toString())
        when (menuItem.itemId) {
            R.id.grid_type -> {
                onChangeLayout()
            }
        }
        return super.onOptionsItemSelected(menuItem);
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
        return inflater.inflate(R.layout.fragment_library, container, false)
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
            var gridAdapter = BookGridCardAdapter()
            mRecycleView.adapter = gridAdapter
            mRecycleView.layoutManager = GridLayoutManager(requireContext(), 2)
        } else {
            var lineAdapter = BookLineCardAdapter()
            mRecycleView.adapter = lineAdapter
            mRecycleView.layoutManager = GridLayoutManager(requireContext(), 1)
        }
    }

    private fun updateList(list: ArrayList<Book>) {
        if (mGridType == LibraryType.GRID)
            (mRecycleView.adapter as BookGridCardAdapter).updateList(list)
        else
            (mRecycleView.adapter as BookLineCardAdapter).updateList(list)
    }

    private fun observer() {
        mViewModel.save.observe(viewLifecycleOwner, Observer {
            updateList(it)
        })
    }

    private fun setListeners() {
        //miGridType = inflater.findViewById(R.id.grid_type)
        //miGridType.setOnMenuItemClickListener(this)
        //miSearch = view.findViewById(R.id.search)
    }

}
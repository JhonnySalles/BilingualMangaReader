package br.com.fenix.mangareader.ui.library

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
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
import br.com.fenix.mangareader.repository.Storage

class LibraryFragment() : Fragment(), Toolbar.OnMenuItemClickListener,
    MenuItem.OnMenuItemClickListener {

    private lateinit var mViewModel: LibraryViewModel
    private var libraryPath: String = ""

    private lateinit var recycleView: RecyclerView
    private var gridType: LibraryType = LibraryType.LINE
    private lateinit var miGridType: MenuItem
    private lateinit var miSearch: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProvider(this).get(LibraryViewModel::class.java)
        loadConfig()
        setListeners()
        observer()

        if (!Storage.isPermissionGranted(requireContext()))
            Storage.takePermission(requireContext(), requireActivity())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)

        val sharedPreferences = GeneralConsts.getSharedPreferences(requireContext())
        gridType = LibraryType.valueOf(
            sharedPreferences?.getString(GeneralConsts.KEYS.LIBRARY.LIBRARY_TYPE, "LINE")
                .toString()
        )

        recycleView = view.findViewById(R.id.rv_library)
        mViewModel.readFiles(libraryPath)
    }

    override fun onMenuItemClick(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.grid_type -> {
                onChangeLayout()
                return true
            }
        }
        return false
    }

    fun onChangeLayout() {
        if (gridType.equals(LibraryType.LINE))
            gridType = LibraryType.GRID
        else
            gridType = LibraryType.LINE

        val sharedPreferences = GeneralConsts.getSharedPreferences(requireContext())
        with(sharedPreferences?.edit()) {
            this!!.putString(GeneralConsts.KEYS.LIBRARY.LIBRARY_TYPE, gridType.toString())
            this.commit()
        }

        printLibraryData()
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
        libraryPath = sharedPreferences?.getString(GeneralConsts.KEYS.LIBRARY.FOLDER, "").toString()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 121 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            mViewModel.readFiles(libraryPath)
    }

    private fun printLibraryData() {
        if (gridType == LibraryType.GRID) {
            var gridAdapter = BookGridCardAdapter()
            recycleView.adapter = gridAdapter
            recycleView.layoutManager = GridLayoutManager(requireContext(), 2)
        } else {
            var lineAdapter = BookLineCardAdapter()
            recycleView.adapter = lineAdapter
            recycleView.layoutManager = GridLayoutManager(requireContext(), 1)
        }
    }

    private fun observer() {
        mViewModel.save.observe(viewLifecycleOwner, Observer {
            if (gridType == LibraryType.GRID)
                (recycleView.adapter as BookGridCardAdapter).updateList(it)
            else
                (recycleView.adapter as BookLineCardAdapter).updateList(it)
        })
    }

    private fun setListeners() {
        //miGridType = inflater.findViewById(R.id.grid_type)
        //miGridType.setOnMenuItemClickListener(this)
        //miSearch = view.findViewById(R.id.search)
    }

}
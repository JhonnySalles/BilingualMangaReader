package br.com.fenix.mangareader.fragment

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.mangareader.ImageCover
import br.com.fenix.mangareader.R
import br.com.fenix.mangareader.adapter.BookGridCardAdapter
import br.com.fenix.mangareader.adapter.BookLineCardAdapter
import br.com.fenix.mangareader.enums.LibraryType
import br.com.fenix.mangareader.model.Book
import br.com.fenix.mangareader.model.Consts
import br.com.fenix.mangareader.service.Storage
import java.io.File

class LibraryFragment : Fragment(), Toolbar.OnMenuItemClickListener,
    MenuItem.OnMenuItemClickListener {
    private var libraryPath: String = ""

    private var listFiles: ArrayList<File>? = null
    private var listBooks: ArrayList<Book>? = ArrayList()

    private lateinit var recycleView : RecyclerView
    private var gridType : LibraryType = LibraryType.LINE
    private lateinit var miGridType : MenuItem
    private lateinit var miSearch : MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadConfig()

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

        //miGridType = inflater.findViewById(R.id.grid_type)
        //miGridType.setOnMenuItemClickListener(this)
        //miSearch = view.findViewById(R.id.search)

        val sharedPreferences = Consts.getSharedPreferences(requireContext())
        gridType = LibraryType.valueOf(
            sharedPreferences?.getString(Consts.getKeyLastLibraryType(), "LINE")
            .toString())

        recycleView = view.findViewById(R.id.rv_library)
        generatedBookList()
    }

    override fun onMenuItemClick(menuItem: MenuItem): Boolean {
        when (menuItem.getItemId()) {
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

        val sharedPreferences = Consts.getSharedPreferences(requireContext())
        with(sharedPreferences?.edit()) {
            this!!.putString(Consts.getKeyLastLibraryType(), gridType.toString())
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
        val sharedPreferences = Consts.getSharedPreferences(requireContext())
        libraryPath = sharedPreferences?.getString(Consts.getKeyLibraryFolder(), "").toString()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 121 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            generatedBookList()
    }

    private fun readFilesInLibrary() {
        if (libraryPath.isEmpty())
            return

        listFiles = ArrayList()

        var file = File(libraryPath)
        file.walk()
            .filterNot { it.isDirectory() }.forEach {
                if (it.name.endsWith(".rar") ||
                    it.name.endsWith(".zip") ||
                    it.name.endsWith(".cbr") ||
                    it.name.endsWith(".cbz")
                )
                    listFiles!!.add(it)
        }
    }

    private fun generatedBookList() {
        readFilesInLibrary()
        listBooks?.clear()
        if (listFiles != null && listFiles!!.isNotEmpty()) {
            var id : Int = 0
            for (file in listFiles!!) {
                id++
                val book = Book(id, file.nameWithoutExtension, "", file, file.extension)
                book.tumbnail = ImageCover.instance.getImage(file)
                listBooks?.add(book)
            }
        }
        printLibraryData()
    }

    private fun printLibraryData() {
        if (gridType.equals(LibraryType.GRID)){
            var gridAdapter = BookGridCardAdapter(listBooks!!, requireActivity())
            recycleView.adapter = gridAdapter
            recycleView.layoutManager = GridLayoutManager(requireContext(), 2)
        } else {
            var lineAdapter = BookLineCardAdapter(listBooks!!, requireActivity())
            recycleView.adapter = lineAdapter
            recycleView.layoutManager = GridLayoutManager(requireContext(), 1)
        }
    }

}
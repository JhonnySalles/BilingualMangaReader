package br.com.fenix.mangareader.fragment

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.mangareader.model.BookItemAdapter
import br.com.fenix.mangareader.model.Consts
import br.com.fenix.mangareader.ImageCover
import br.com.fenix.mangareader.R
import br.com.fenix.mangareader.model.Book
import br.com.fenix.mangareader.service.Storage
import java.io.File

class LibraryFragment : Fragment() {
    private var libraryPath: String = ""

    private var listFiles: ArrayList<File>? = null
    private var listBooks: ArrayList<Book>? = ArrayList()

    private lateinit var recycleView : RecyclerView
    private lateinit var recyAdapter : BookItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadConfig()

        if (!Storage.isPermissionGranted(requireContext()))
            Storage.takePermission(requireContext(), requireActivity())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycleView = view.findViewById(R.id.recicle_view)
        recycleView.layoutManager = GridLayoutManager(requireActivity(), 3)
        recyAdapter = listBooks?.let { BookItemAdapter(it, requireActivity()) }!!

        printImages()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    private fun loadConfig() {
        val sharedPreferences: SharedPreferences? =
            activity?.getSharedPreferences(Consts.getKeySharedPrefs(), Context.MODE_PRIVATE)
        libraryPath = sharedPreferences?.getString(Consts.getKeyLibraryFolder(), "").toString()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 121 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            readFiles()
    }

    private fun readFiles() {
        if (libraryPath.isEmpty())
            return

        listFiles = ArrayList()

        var file : File = File(libraryPath)
        file.walk()
            .filterNot { it.isDirectory() }.forEach {
            println(it)
                if (it.name.endsWith(".rar") ||
                    it.name.endsWith(".zip") ||
                    it.name.endsWith(".cbr") ||
                    it.name.endsWith(".cbz")
                )
                    listFiles!!.add(it)
        }
    }

    private fun printImages() {
        readFiles()
        if (listFiles == null || listFiles!!.isEmpty()) {
            listBooks?.clear()
        } else {
            var id : Int = 0
            for (file in listFiles!!) {
                id++
                val book = Book(id, file.nameWithoutExtension, "", file, file.extension)
                book.tumbnail = ImageCover.instance.getImage(file)
                listBooks?.add(book)
            }
        }
    }



}
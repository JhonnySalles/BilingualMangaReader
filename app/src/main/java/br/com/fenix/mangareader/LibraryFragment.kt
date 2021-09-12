package br.com.fenix.mangareader

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class LibraryFragment : Fragment() {
    private var libraryPath: String = ""

    var permissions =arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)

    private var listFiles: ArrayList<File>? = null
    private var listBooks: ArrayList<Book>? = ArrayList()

    private lateinit var recycleView : RecyclerView
    private lateinit var recyAdapter : BookItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadConfig()
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                permissions[0]
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                permissions,
                121
            )
        }

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

        println(libraryPath)
        var file : File = File(libraryPath)
        var files : Array<out File>? = file.listFiles()
        files?.forEach {
            print(it) }

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
            for (file in listFiles!!) {
                val book = Book(file.nameWithoutExtension, "", file)
                book.image_cover = ImageCover.instance.getImage(file)
                listBooks?.add(book)
            }
        }
    }



}
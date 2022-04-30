package br.com.fenix.bilingualmangareader.view.ui.pages_link

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.model.entity.PageLink
import br.com.fenix.bilingualmangareader.model.enums.LibraryType
import br.com.fenix.bilingualmangareader.model.enums.LoadFile
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.util.helpers.Util
import br.com.fenix.bilingualmangareader.view.adapter.history.HistoryCardAdapter
import br.com.fenix.bilingualmangareader.view.adapter.library.MangaGridCardAdapter
import br.com.fenix.bilingualmangareader.view.adapter.page_link.PageLinkCardAdapter
import br.com.fenix.bilingualmangareader.view.ui.library.LibraryFragment
import com.google.android.material.textfield.TextInputLayout
import kotlin.math.max

class PagesLinkFragment : Fragment() {

    private val mViewModel: PagesLinkViewModel by viewModels()
    private lateinit var mRecycleView: RecyclerView
    private lateinit var mFileLink: TextInputLayout
    private lateinit var mFileLinkAutoComplete: AutoCompleteTextView
    private lateinit var mSave: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_pages_link, container, false)
        mRecycleView.adapter = PageLinkCardAdapter()
        mRecycleView.layoutManager = LinearLayoutManager(requireContext())
        //gridAdapter.attachListener(mListener)
        observer()
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mRecycleView = view.findViewById(R.id.rv_pages_linked)
        mFileLink = view.findViewById(R.id.txt_file_link)
        mFileLinkAutoComplete = view.findViewById(R.id.menu_autocomplete_file_link)
        mSave = view.findViewById(R.id.btn_file_link_save)

        mFileLinkAutoComplete.setOnClickListener {
            mFileLinkAutoComplete.setText("")
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/zip", "application/x-cbz", "application/rar","application/x-cbr", "application/x-rar-compressed"))
            }
            startActivityForResult(intent, 200)
        }
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int, resultData: Intent?
    ) {
        if (requestCode == 200) {
            if (resultCode == Activity.RESULT_OK) {
                resultData?.data?.also { uri ->
                    try {
                        val path = Util.normalizeFilePath(uri.path.toString())
                        mFileLinkAutoComplete.setText(path)

                        var loaded = mViewModel.readFileLink(path)
                        if (loaded != LoadFile.LOADED) {
                            AlertDialog.Builder(requireContext(), R.style.AppCompatAlertDialogStyle)
                                .setTitle(getString(R.string.popup_reading_subtitle_empty))
                                .setMessage(path)
                                .setNeutralButton(
                                    R.string.action_neutral
                                ) { _, _ -> }
                                .create()
                                .show()
                        }
                    } catch (e: Exception) {
                        Log.e(GeneralConsts.TAG.LOG, "Error when open file: " + e.message)
                    }
                }
            }
        }
    }

    private fun observer() {
        mViewModel.pagesLink.observe(viewLifecycleOwner) {
            updateList(it)
        }
    }

    private fun updateList(list: ArrayList<PageLink>) {
        (mRecycleView.adapter as PageLinkCardAdapter).updateList(list)
    }

}
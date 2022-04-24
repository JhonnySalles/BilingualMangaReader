package br.com.fenix.bilingualmangareader.view.ui.pages_link

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.FileLink
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.model.enums.LoadFile
import br.com.fenix.bilingualmangareader.service.parses.Parse
import br.com.fenix.bilingualmangareader.service.parses.ParseFactory
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.util.helpers.Util
import br.com.fenix.bilingualmangareader.view.ui.history.HistoryViewModel
import com.google.android.material.textfield.TextInputLayout
import java.io.File
import java.io.InputStream

class PagesLinkFragment(manga : Manga) : Fragment() {

    private val mViewModel: PagesLinkViewModel by viewModels()
    private lateinit var mPagesLinked: RecyclerView
    private lateinit var mFileLink: TextInputLayout
    private lateinit var mFileLinkAutoComplete: AutoCompleteTextView
    private lateinit var mSave: Button
    private var mManga = manga

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mViewModel.set(mManga)
        return inflater.inflate(R.layout.fragment_pages_link, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPagesLinked = view.findViewById(R.id.rv_pages_linked)
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
                    val path = Util.normalizeFilePath(uri.path.toString())
                    mFileLinkAutoComplete.setText(path)

                    var loaded = mViewModel.load(path)
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
                }
            }
        }
    }

}
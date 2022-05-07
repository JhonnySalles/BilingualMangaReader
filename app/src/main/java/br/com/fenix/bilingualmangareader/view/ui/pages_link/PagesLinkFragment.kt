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
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.model.entity.PageLink
import br.com.fenix.bilingualmangareader.model.enums.LoadFile
import br.com.fenix.bilingualmangareader.service.listener.PageLinkCardListener
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.util.helpers.Util
import br.com.fenix.bilingualmangareader.view.adapter.page_link.PageLinkCardAdapter
import com.google.android.material.textfield.TextInputLayout

class PagesLinkFragment : Fragment() {

    private val mViewModel: PagesLinkViewModel by viewModels()
    private lateinit var mRecycleView: RecyclerView
    private lateinit var mFileLink: TextInputLayout
    private lateinit var mFileLinkAutoComplete: AutoCompleteTextView
    private lateinit var mSave: Button
    private lateinit var mListener: PageLinkCardListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_pages_link, container, false)

        mRecycleView = root.findViewById(R.id.rv_pages_linked)
        mFileLink = root.findViewById(R.id.txt_file_link)
        mFileLinkAutoComplete = root.findViewById(R.id.menu_autocomplete_file_link)
        mSave = root.findViewById(R.id.btn_file_link_save)

        mFileLinkAutoComplete.setOnClickListener {
            mFileLinkAutoComplete.setText("")
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/zip", "application/x-cbz", "application/rar","application/x-cbr", "application/x-rar-compressed"))
            }
            startActivityForResult(intent, 200)
        }

        mListener = object : PageLinkCardListener {
            override fun onClick(page: PageLink) {
                /*if (!manga.excluded) {
                    val intent = Intent(context, ReaderActivity::class.java)
                    val bundle = Bundle()
                    manga.lastAccess = LocalDateTime.now()
                    bundle.putString(GeneralConsts.KEYS.MANGA.NAME, manga.title)
                    bundle.putInt(GeneralConsts.KEYS.MANGA.MARK, manga.bookMark)
                    bundle.putSerializable(GeneralConsts.KEYS.OBJECT.MANGA, manga)
                    intent.putExtras(bundle)
                    context?.startActivity(intent)
                    mViewModel.updateLastAccess(manga)
                } else
                    AlertDialog.Builder(requireActivity(), R.style.AppCompatAlertDialogStyle)
                        .setTitle(getString(R.string.manga_excluded))
                        .setMessage(manga.file.path)
                        .setNeutralButton(
                            R.string.action_neutral
                        ) { _, _ -> }
                        .create()
                        .show()*/
            }
        }

        observer()

        val bundle = this.arguments
        if (bundle != null && bundle.containsKey(GeneralConsts.KEYS.OBJECT.MANGA))
            mViewModel.loadManga(bundle[GeneralConsts.KEYS.OBJECT.MANGA] as Manga)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = PageLinkCardAdapter()
        mRecycleView.adapter = adapter
        mRecycleView.layoutManager = LinearLayoutManager(requireContext())
        adapter.attachListener(mListener)
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

                        var loaded = mViewModel.readFileLink(path) {
                            notifyItemChanged(it)
                        }
                        if (loaded != LoadFile.LOADED) {
                            val msg = if (loaded == LoadFile.ERROR_FILE_WRONG) getString(R.string.page_link_load_file_wrong) else getString(R.string.page_link_load_error)
                            AlertDialog.Builder(requireContext(), R.style.AppCompatAlertDialogStyle)
                                .setTitle(msg)
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

    private fun notifyItemChanged(item : Int) {
        (mRecycleView.adapter as PageLinkCardAdapter).notifyItemChanged(item)
    }

}
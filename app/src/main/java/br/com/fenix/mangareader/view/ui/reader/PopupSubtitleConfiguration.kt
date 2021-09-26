package br.com.fenix.mangareader.view.ui.reader

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import br.com.fenix.mangareader.R
import br.com.fenix.mangareader.model.entity.Chapter
import br.com.fenix.mangareader.service.controller.SubTitleController
import br.com.fenix.mangareader.service.repository.SubTitleRepository
import br.com.fenix.mangareader.util.constants.GeneralConsts
import br.com.fenix.mangareader.util.helpers.Util
import com.google.android.material.textfield.TextInputLayout
import java.io.File
import java.io.InputStream
import java.util.*

class PopupSubtitleConfiguration : Fragment() {

    private lateinit var mLoadExternalSubtitle: TextInputLayout
    private lateinit var mLoadExternalSubtitleAutoComplete: AutoCompleteTextView
    private lateinit var mSubtitleSelected: TextInputLayout
    private lateinit var mSubtitleSelectedAutoComplete: AutoCompleteTextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.popup_subtitle_configuration, container, false)
        mLoadExternalSubtitle = root.findViewById(R.id.txt_external_subtitle_select_path)
        mLoadExternalSubtitleAutoComplete =
            root.findViewById(R.id.menu_autocomplete_external_subtitle_select_path)
        mSubtitleSelected = root.findViewById(R.id.cb_subtitle_selected)
        mSubtitleSelectedAutoComplete = root.findViewById(R.id.menu_autocomplete_subtitle_selected)
        clearSubtitlesSelected(requireContext())

        mSubtitleSelected.setOnClickListener { mSubtitleSelectedAutoComplete.setText("", false) }

        mSubtitleSelectedAutoComplete.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                selectedSubtitle(
                    requireContext(),
                    parent.getItemAtPosition(position).toString()
                )
                ReaderActivity.selectTabReader()
            }

        mLoadExternalSubtitleAutoComplete.setOnClickListener {
            mLoadExternalSubtitleAutoComplete.setText("")
            clearSubtitlesSelected(requireContext())
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
            }
            startActivityForResult(intent, 200)
        }

        if (SubTitleController.mManga != null && SubTitleController.mManga!!.id != null) {
            val mSubtitleRepository = SubTitleRepository(requireContext())
            val lastSubtitle = mSubtitleRepository.findByIdManga(SubTitleController.mManga!!.id!!)
            if (lastSubtitle != null)
                initialize(requireContext(), lastSubtitle.chapterKey, lastSubtitle.pageKey)
        }

        return root
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int, resultData: Intent?
    ) {
        if (requestCode == 200) {
            if (resultCode == Activity.RESULT_OK) {
                resultData?.data?.also { uri ->
                    try {
                        val path = Util.normalizeFilePath(uri.path.toString())
                        val inputStream: InputStream = File(path).inputStream()
                        val inputString = inputStream.bufferedReader().use { it.readText() }
                        mLoadExternalSubtitleAutoComplete.setText(path)
                        SubTitleController.getChapterFromJson(listOf(inputString))
                        setSubtitlesSelected(requireContext(), SubTitleController.mComboList)
                    } catch (e: Exception) {
                        Log.e(GeneralConsts.TAG.LOG, "Erro ao abrir o arquivo " + e.message)
                    }
                }
            }
        }
    }

    fun isInitialized() = ::mSubtitleSelectedAutoComplete.isInitialized

    init {
        INSTANCE = this
    }

    companion object {
        lateinit var INSTANCE: PopupSubtitleConfiguration

        private var mComboListInternal: HashMap<String, Chapter> = hashMapOf()
        private var mComboListSelected: HashMap<String, Chapter> = hashMapOf()
        var isSelected = false

        fun initialize(context: Context, chapterKey: String, pageKey: Int) {
            if (chapterKey.isEmpty())
                return

            selectedSubtitle(context, chapterKey)
            PopupSubtitleReader.initialize(pageKey)
        }

        fun clearSubtitlesSelected(context: Context) {
            INSTANCE.mSubtitleSelectedAutoComplete.setText("")
            isSelected = false
            INSTANCE.mSubtitleSelectedAutoComplete.setAdapter(
                ArrayAdapter(
                    context,
                    R.layout.list_item,
                    mComboListInternal.keys.toTypedArray().sortedArray()
                )
            )
            PopupSubtitleReader.clearSubtitlesSelected()
        }

        fun setSubtitlesSelected(context: Context, list: HashMap<String, Chapter>) {
            mComboListSelected = list
            isSelected = true
            INSTANCE.mSubtitleSelectedAutoComplete.setAdapter(
                ArrayAdapter(
                    context,
                    R.layout.list_item,
                    mComboListSelected.keys.toTypedArray().sortedArray()
                )
            )
            INSTANCE.mSubtitleSelectedAutoComplete.setText(mComboListSelected.keys.first())
            selectedSubtitle(context, mComboListSelected.keys.first())
        }

        fun setSubtitles(context: Context, list: HashMap<String, Chapter>) {
            mComboListInternal = list


            if (::INSTANCE.isInitialized && INSTANCE.isInitialized())
                    INSTANCE.mSubtitleSelectedAutoComplete.setAdapter(
                        ArrayAdapter(
                            context,
                            R.layout.list_item,
                            mComboListInternal.keys.toTypedArray().sortedArray()
                        )
                    )
        }

        fun selectedSubtitle(context: Context, key: String) {
            if (key.isNotEmpty() && getSubtitle().containsKey(key)) {
                INSTANCE.mSubtitleSelectedAutoComplete.setText(key, false)
                PopupSubtitleReader.setChapter(
                    context,
                    getSubtitle()[key]
                )
            }
        }

        fun getSubtitle(): HashMap<String, Chapter> =
            if (isSelected) mComboListSelected else mComboListInternal

        fun getSelectedChapter(): Chapter? {
            return if (INSTANCE.mSubtitleSelectedAutoComplete.text.toString().isNotEmpty()) {
                getSubtitle()[INSTANCE.mSubtitleSelectedAutoComplete.text.toString()]
            } else
                null
        }

        fun getNextSelectSubtitle(context: Context): Boolean {
            val index: Int = if (INSTANCE.mSubtitleSelectedAutoComplete.text.toString()
                    .isNotEmpty()
            ) getSubtitle().keys.indexOf(INSTANCE.mSubtitleSelectedAutoComplete.text.toString())
                .plus(1) else 0

            return if (getSubtitle().keys.size >= index) {
                PopupSubtitleReader.setChapter(
                    context,
                    getSubtitle()[getSubtitle().keys.toTypedArray()[index]]
                )
                INSTANCE.mSubtitleSelectedAutoComplete.setText(
                    getSubtitle().keys.toTypedArray()[index],
                    false
                )
                SubTitleController.updatePageSelect()
                true
            } else
                false
        }

        fun getBeforeSelectSubtitle(context: Context): Boolean {
            val index: Int = if (INSTANCE.mSubtitleSelectedAutoComplete.text.toString()
                    .isNotEmpty()
            ) getSubtitle().keys.indexOf(INSTANCE.mSubtitleSelectedAutoComplete.text.toString())
                .minus(1) else 0

            return if (index >= 0) {
                PopupSubtitleReader.setChapter(
                    context,
                    getSubtitle()[getSubtitle().keys.toTypedArray()[index]]
                )
                INSTANCE.mSubtitleSelectedAutoComplete.setText(
                    getSubtitle().keys.toTypedArray()[index],
                    false
                )
                SubTitleController.updatePageSelect()
                true
            } else
                false
        }

        fun getPathSubtitle(): String =
            if (isSelected) INSTANCE.mLoadExternalSubtitle.editText?.text.toString() else ""

        fun getChapterKey(): String =
            if (!::INSTANCE.isInitialized || !INSTANCE.isInitialized()) "" else INSTANCE.mSubtitleSelectedAutoComplete.text.toString()

    }

}
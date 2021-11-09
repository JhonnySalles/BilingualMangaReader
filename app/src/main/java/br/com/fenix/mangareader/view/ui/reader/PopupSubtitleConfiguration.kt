package br.com.fenix.mangareader.view.ui.reader

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import br.com.fenix.mangareader.R
import br.com.fenix.mangareader.model.enums.Languages
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
    private lateinit var mSubtitleLanguage: TextInputLayout
    private lateinit var mSubtitleLanguageAutoComplete: AutoCompleteTextView

    private lateinit var mSubTitleController: SubTitleController
    private lateinit var mMapLanguage: HashMap<String, Languages>

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

        mSubtitleLanguage = root.findViewById(R.id.cb_subtitle_language)
        mSubtitleLanguageAutoComplete = root.findViewById(R.id.menu_autocomplete_subtitle_language)

        mSubTitleController = SubTitleController.getInstance(requireContext())

        mSubtitleSelectedAutoComplete.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                mSubTitleController.clearSubtitlesSelected()
            }
        }

        mSubtitleSelectedAutoComplete.setOnClickListener {
            mSubtitleSelectedAutoComplete.setText("", false)
            mSubTitleController.clearSubtitlesSelected()
            if (mSubTitleController.chaptersKeys.value == null || mSubTitleController.chaptersKeys.value!!.isEmpty())
                Toast.makeText(
                    requireActivity(),
                    getString(R.string.popup_reading_import_subtitle_is_empty),
                    Toast.LENGTH_SHORT
                ).show()
        }

        mSubtitleSelectedAutoComplete.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                mSubTitleController.selectedSubtitle(
                    parent.getItemAtPosition(position).toString()
                )
                ReaderActivity.selectTabReader()
            }


        val languages = resources.getStringArray(R.array.languages)
        mMapLanguage = hashMapOf(
            languages[0] to Languages.PORTUGUESE,
            languages[1] to Languages.ENGLISH,
            languages[2] to Languages.JAPANESE,
            languages[3] to Languages.PORTUGUESE_GOOGLE
        )

        mSubtitleLanguageAutoComplete.setAdapter(ArrayAdapter(requireContext(), R.layout.list_item, mMapLanguage.keys.toTypedArray()))
        mSubtitleLanguageAutoComplete.setOnClickListener {
            mSubtitleLanguageAutoComplete.setText("", false)
            mSubTitleController.clearLanguage()
        }

        mSubtitleLanguageAutoComplete.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                if (parent.getItemAtPosition(position).toString().isNotEmpty() &&
                    mMapLanguage.containsKey(parent.getItemAtPosition(position).toString())
                )
                    mSubTitleController.selectedLanguage(mMapLanguage[parent.getItemAtPosition(position).toString()]!!)
            }

        mLoadExternalSubtitle.editText?.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s:CharSequence, start:Int, count:Int, after:Int) {
            }
            override fun onTextChanged(s:CharSequence, start:Int, before:Int, count:Int) {
            }
            override fun afterTextChanged(s: Editable) {
                if (s.isEmpty())
                    mSubTitleController.clearSubtitlesSelected()
            }
        })


        mLoadExternalSubtitleAutoComplete.setOnClickListener {
            mLoadExternalSubtitleAutoComplete.setText("")
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
            }
            startActivityForResult(intent, 200)
        }

        if (mSubTitleController.mManga != null && mSubTitleController.mManga!!.id != null) {
            val mSubtitleRepository = SubTitleRepository(requireContext())
            val lastSubtitle = mSubtitleRepository.findByIdManga(mSubTitleController.mManga!!.id!!)
            if (lastSubtitle != null)
                mSubTitleController.initialize(lastSubtitle.chapterKey, lastSubtitle.pageKey)
        }

        observer()
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
                        mSubTitleController.getChapterFromJson(listOf(inputString), true)
                    } catch (e: Exception) {
                        Log.e(GeneralConsts.TAG.LOG, "Error when open file: " + e.message)
                    }
                }
            }
        }
    }

    private fun observer() {
        mSubTitleController.chaptersKeys.observe(viewLifecycleOwner, {
            mSubtitleSelectedAutoComplete.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    R.layout.list_item,
                    it.sorted()
                )
            )
        })

        mSubTitleController.chapterSelected.observe(viewLifecycleOwner, {
            var text = ""
            if (it != null)
                text = mSubTitleController.getChapterKey(it)

            mSubtitleSelectedAutoComplete.setText(text, false)
        })

    }

}
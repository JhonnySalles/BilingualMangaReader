package br.com.fenix.mangareader.view.ui.reader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import br.com.fenix.mangareader.R
import br.com.fenix.mangareader.service.controller.SubTitleController
import br.com.fenix.mangareader.service.kanji.Formater
import com.google.android.material.textfield.TextInputLayout


class PopupSubtitleReader : Fragment() {

    private lateinit var mSubtitlePage: TextInputLayout
    private lateinit var mSubtitlePageAutoComplete: AutoCompleteTextView
    private lateinit var mSubtitleTitle: TextView
    private lateinit var mSubtitleContent: TextView
    private lateinit var mSubtitleFileName: TextView
    private lateinit var mNavBeforeText: Button
    private lateinit var mNavNextText: Button
    private lateinit var mRefresh: Button
    private lateinit var mDraw: Button
    private lateinit var mChangeLanguage: Button
    private lateinit var mLabelChapter: String
    private lateinit var mLabelText: String

    private lateinit var mSubTitleController: SubTitleController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.popup_subtitle_reader, container, false)

        mSubtitlePageAutoComplete = root.findViewById(R.id.menu_autocomplete_subtitle_Page)
        mSubtitlePage = root.findViewById(R.id.cb_subtitle_page)
        mSubtitleTitle = root.findViewById(R.id.txt_subtitle_title)
        mSubtitleFileName = root.findViewById(R.id.txt_subtitle_file_page_name)
        mSubtitleContent = root.findViewById(R.id.txt_subtitle_content)
        mNavBeforeText = root.findViewById(R.id.nav_before_text)
        mNavNextText = root.findViewById(R.id.nav_next_text)
        mRefresh = root.findViewById(R.id.nav_refresh)
        mDraw = root.findViewById(R.id.nav_draw)
        mChangeLanguage = root.findViewById(R.id.nav_change_language)

        mLabelChapter = getString(R.string.popup_reading_subtitle_chapter)
        mLabelText = getString(R.string.popup_reading_subtitle_text)

        mSubTitleController = SubTitleController.getInstance(requireContext())

        mNavBeforeText.setOnClickListener { mSubTitleController.getBeforeText() }
        mNavNextText.setOnClickListener { mSubTitleController.getNextText() }

        mRefresh.setOnClickListener { mSubTitleController.findSubtitle() }
        mDraw.setOnClickListener { mSubTitleController.drawSelectedText() }

        mChangeLanguage.setOnClickListener { mSubTitleController.changeLanguage() }

        mSubtitlePageAutoComplete.setOnClickListener {
            mSubtitlePageAutoComplete.setText("", false)

            if (mSubTitleController.pagesKeys.value == null || mSubTitleController.pagesKeys.value!!.isEmpty())
                Toast.makeText(
                    requireActivity(),
                    getString(R.string.popup_reading_import_subtitle_page_is_empty),
                    Toast.LENGTH_SHORT
                ).show()
        }

        mSubtitlePageAutoComplete.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                mSubTitleController.selectedPage(
                    parent.getItemAtPosition(position).toString()
                )
            }

        observer()
        return root
    }

    private fun observer() {
        mSubTitleController.pagesKeys.observe(viewLifecycleOwner, {
            mSubtitlePageAutoComplete.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    R.layout.list_item,
                    it.sorted()
                )
            )
        })

        mSubTitleController.pageSelected.observe(viewLifecycleOwner, {
            var page = ""
            var key = ""
            if (it != null) {
                key = mSubTitleController.getPageKey(it)
                page = it.name
            }

            mSubtitlePageAutoComplete.setText(key, false)
            mSubtitleFileName.text = page
        })

        mSubTitleController.textSelected.observe(viewLifecycleOwner, {
            var title = ""
            mSubtitleContent.setText("")
            if (it != null) {
                val index =
                    mSubTitleController.pageSelected.value?.texts?.indexOf(mSubTitleController.textSelected?.value)
                        ?.plus(1)
                title =
                    "${mLabelChapter} ${mSubTitleController.chapterSelected?.value?.chapter.toString()} - ${mLabelText} $index/${mSubTitleController.pageSelected.value?.texts?.size}"

                Formater.generateKanjiColor(it.text) { kanji ->
                    mSubtitleContent.text = kanji
                }
            } else if (mSubTitleController.chapterSelected.value != null && mSubTitleController.pageSelected.value != null)
                title =
                    "${mLabelChapter} ${mSubTitleController.chapterSelected.value?.chapter.toString()} - ${mLabelText} 0/${if (mSubTitleController.pageSelected.value?.texts == null) 0 else mSubTitleController.pageSelected.value?.texts?.size}"

            mSubtitleTitle.text = title
        })
    }

}
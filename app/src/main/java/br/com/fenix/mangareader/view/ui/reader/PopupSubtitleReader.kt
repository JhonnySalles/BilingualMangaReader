package br.com.fenix.mangareader.view.ui.reader

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import br.com.fenix.mangareader.R
import br.com.fenix.mangareader.model.entity.Chapter
import br.com.fenix.mangareader.model.entity.Page
import br.com.fenix.mangareader.model.entity.Text
import br.com.fenix.mangareader.service.controller.SubTitleController
import com.google.android.material.textfield.TextInputLayout
import java.util.*

class PopupSubtitleReader : Fragment() {

    lateinit var mSubtitlePage: TextInputLayout
    lateinit var mSubtitlePageAutoComplete: AutoCompleteTextView
    lateinit var mSubtitleTitle: TextView
    lateinit var mSubtitleContent: TextView
    lateinit var mNavBeforeText: Button
    lateinit var mNavNextText: Button
    lateinit var mRefresh: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.popup_subtitle_reader, container, false)

        mSubtitlePageAutoComplete = root.findViewById(R.id.menu_autocomplete_subtitle_Page)
        mSubtitlePage = root.findViewById(R.id.cb_subtitle_page)
        mSubtitleTitle = root.findViewById(R.id.txt_subtitle_title)
        mSubtitleContent = root.findViewById(R.id.txt_subtitle_content)
        mNavBeforeText = root.findViewById(R.id.nav_before_text)
        mNavNextText = root.findViewById(R.id.nav_next_text)
        mRefresh = root.findViewById(R.id.nav_refresh)

        mLabelChapter = getString(R.string.popup_reading_subtitle_chapter)
        mLabelText = getString(R.string.popup_reading_subtitle_text)

        mNavBeforeText.setOnClickListener { getBeforeText() }
        mNavNextText.setOnClickListener { getNextText() }

        mRefresh.setOnClickListener { SubTitleController.findSubtitle(requireContext()) }

        mSubtitlePageAutoComplete.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                selectedPage(
                    parent.getItemAtPosition(position).toString()
                )
            }

        return root
    }

    fun isInitialized() = ::mSubtitlePageAutoComplete.isInitialized

    init {
        INSTANCE = this
    }

    companion object {
        lateinit var INSTANCE: PopupSubtitleReader
        var chapterSelected: Chapter? = null
        var pageSelected: Page? = null
        var textSelected: Text? = null

        private lateinit var mLabelChapter: String
        private lateinit var mLabelText: String
        var mListPages: HashMap<Int, Page> = hashMapOf()

        fun initialize(pageKey: Int) {
            if (pageKey == 0)
                return
            selectedPage(pageKey)
        }

        fun setChapter(context: Context, chapter: Chapter?) {
            chapterSelected = chapter
            mListPages.clear()
            if (chapterSelected != null) {
                chapterSelected!!.pages.forEach { mListPages[it.number] = it }
                INSTANCE.mSubtitlePageAutoComplete.setAdapter(
                    ArrayAdapter(
                        context,
                        R.layout.list_item,
                        mListPages.keys.toTypedArray().sorted()
                    )
                )
                INSTANCE.mSubtitlePageAutoComplete.setText(
                    chapterSelected!!.pages[0].number.toString(),
                    false
                )
                setPage(chapterSelected!!.pages[0])
            }
        }

        private fun setPage(page: Page?) {
            pageSelected = page
            if (pageSelected != null && pageSelected!!.texts.isNotEmpty())
                setText(pageSelected!!.texts[0])
            else
                setText(null)
        }

        private fun setText(text: Text?) {
            textSelected = text
            if (textSelected != null) {
                val index = pageSelected?.texts?.indexOf(textSelected)?.plus(1)
                val label =
                    "$mLabelChapter ${chapterSelected?.chapter.toString()} - $mLabelText $index/${pageSelected?.texts?.size}"
                INSTANCE.mSubtitleTitle.text = label
                INSTANCE.mSubtitleContent.text = textSelected!!.text
            } else {
                val label =
                    "$mLabelChapter ${chapterSelected?.chapter.toString()} - $mLabelText 0/${if (pageSelected?.texts == null) 0 else pageSelected?.texts?.size}"
                INSTANCE.mSubtitleTitle.text = label
                INSTANCE.mSubtitleContent.text = ""
            }
        }

        fun selectedPage(index: String) {
            if (index.isNotEmpty())
                selectedPage(index.toInt())
        }

        private fun selectedPage(index: Int) {
            if (chapterSelected != null) {
                if (mListPages.containsKey(index)) {
                    INSTANCE.mSubtitlePageAutoComplete.setText(index.toString(), false)
                    setPage(mListPages[index])
                }
            }
        }

        fun clearSubtitlesSelected() {
            chapterSelected = null
            pageSelected = null
            textSelected = null
            INSTANCE.mSubtitleTitle.text = ""
            INSTANCE.mSubtitleContent.text = ""
            INSTANCE.mSubtitlePageAutoComplete.setAdapter(null)
        }

        fun getNextSelectPage(): Boolean {
            if (chapterSelected == null)
                return true

            val index: Int =
                if (INSTANCE.mSubtitlePageAutoComplete.text.toString()
                        .isNotEmpty()
                ) mListPages.keys.indexOf(
                    INSTANCE.mSubtitlePageAutoComplete.text.toString().toInt()
                )
                    .plus(1) else 0

            return if (mListPages.size > index) {
                INSTANCE.mSubtitlePageAutoComplete.setText(
                    mListPages.keys.toTypedArray()[index].toString(),
                    false
                )
                setPage(mListPages[mListPages.keys.toTypedArray()[index]])
                true
            } else
                false
        }

        fun getBeforeSelectPage(): Boolean {
            if (chapterSelected == null)
                return true

            val index: Int =
                if (INSTANCE.mSubtitlePageAutoComplete.text.toString()
                        .isNotEmpty()
                ) mListPages.keys.indexOf(
                    INSTANCE.mSubtitlePageAutoComplete.text.toString().toInt()
                )
                    .minus(1) else 0

            return if (index >= 0) {
                INSTANCE.mSubtitlePageAutoComplete.setText(
                    mListPages.keys.toTypedArray()[index].toString(),
                    false
                )
                setPage(mListPages[mListPages.keys.toTypedArray()[index]])
                true
            } else
                false
        }

        fun getNextText(): Boolean {
            if (pageSelected == null)
                return true

            val index: Int = if (textSelected != null) pageSelected?.texts?.indexOf(textSelected)!!
                .plus(1) else 0

            return if (pageSelected?.texts != null && pageSelected?.texts?.size!! > index) {
                setText(pageSelected?.texts?.get(index))
                true
            } else {
                getNextSelectPage()
                false
            }
        }

        fun getBeforeText(): Boolean {
            if (pageSelected == null)
                return true

            val index: Int = if (textSelected != null) pageSelected?.texts?.indexOf(textSelected)!!
                .minus(1) else 0

            return if (index >= 0 && pageSelected?.texts != null && pageSelected?.texts?.size!! > 0) {
                setText(pageSelected?.texts?.get(index))
                true
            } else {
                getBeforeSelectPage()
                false
            }
        }

        fun getPageKey(): Int =
            if (!::INSTANCE.isInitialized || INSTANCE.isInitialized() || INSTANCE.mSubtitlePageAutoComplete.text.isEmpty()) 0 else INSTANCE.mSubtitlePageAutoComplete.text.toString()
                .toInt()
    }

}
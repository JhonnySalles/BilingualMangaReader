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
import com.google.android.material.textfield.TextInputLayout
import java.util.HashMap

class PopupSubtitleReader : Fragment() {

    lateinit var mNavBeforeText: Button
    lateinit var mNavNextText: Button

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

        mLabelChapter = getString(R.string.popup_reading_subtitle_chapter)
        mLabelText = getString(R.string.popup_reading_subtitle_text)

        mNavBeforeText.setOnClickListener { getBeforeText() }
        mNavNextText.setOnClickListener { getNextText() }

        mSubtitlePageAutoComplete.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                selectedPage(
                    parent.getItemAtPosition(position).toString()
                )
            }

        return root
    }

    companion object {
        var chapterSelected: Chapter? = null
        var pageSelected: Page? = null
        var textSelected: Text? = null

        lateinit var mSubtitlePage: TextInputLayout
        lateinit var mSubtitlePageAutoComplete: AutoCompleteTextView
        lateinit var mSubtitleTitle: TextView
        lateinit var mSubtitleContent: TextView

        private lateinit var mLabelChapter: String
        private lateinit var mLabelText: String
        private var mListPages: HashMap<Int, Page> = hashMapOf()

        fun initialize(context: Context, pageKey:Int) {
            if (pageKey == 0)
                return
            selectedPage(pageKey)
        }

        fun setChapter(context: Context, chapter: Chapter?) {
            chapterSelected = chapter
            mListPages.clear()
            if (chapterSelected != null) {
                chapterSelected!!.pages.forEach { mListPages[it.number] = it }
                mSubtitlePageAutoComplete.setAdapter(
                    ArrayAdapter(
                        context,
                        R.layout.list_item,
                        mListPages.keys.toTypedArray().sorted()
                    )
                )
                mSubtitlePageAutoComplete.setText(
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
                mSubtitleTitle.text =
                    "$mLabelChapter ${chapterSelected?.chapter.toString()} - $mLabelText $index/${pageSelected?.texts?.size}"
                mSubtitleContent.text = textSelected!!.text
            } else {
                mSubtitleTitle.text =
                    "$mLabelChapter ${chapterSelected?.chapter.toString()} - $mLabelText 0/${pageSelected?.texts?.size}"
                mSubtitleContent.text = ""
            }
        }

        fun selectedPage(index: String) {
            if (index != null && index.isNotEmpty())
                selectedPage(index.toInt())
        }

        fun selectedPage(index: Int) {
            if (chapterSelected != null) {
                if (mListPages.containsKey(index)) {
                    mSubtitlePageAutoComplete.setText(index.toString(), false)
                    setPage(mListPages[index])
                }
            }
        }

        fun clearSubtitlesSelected() {
            chapterSelected = null
            pageSelected = null
            textSelected = null
            mSubtitleTitle.text = ""
            mSubtitleContent.text = ""
            mSubtitlePageAutoComplete.setAdapter(null)
        }

        fun getNextSelectPage(): Boolean {
            if (chapterSelected == null)
                return true

            val index: Int =
                if (mSubtitlePageAutoComplete.text.toString().isNotEmpty()) mListPages.keys.indexOf(
                    mSubtitlePageAutoComplete.text.toString().toInt()
                )
                    .plus(1) else 0

            return if (mListPages.size > index) {
                mSubtitlePageAutoComplete.setText(
                    mListPages.keys.toTypedArray()[index].toString(),
                    false
                )
                setPage(mListPages[index])
                true
            } else
                false
        }

        fun getBeforeSelectPage(): Boolean? {
            if (chapterSelected == null)
                return true

            val index: Int =
                if (mSubtitlePageAutoComplete.text.toString().isNotEmpty()) mListPages.keys.indexOf(
                    mSubtitlePageAutoComplete.text.toString().toInt()
                )
                    .minus(1) else 0

            return if (index >= 0) {
                mSubtitlePageAutoComplete.setText(
                    mListPages.keys.toTypedArray()[index].toString(),
                    false
                )
                setPage(mListPages[index])
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

        fun getBeforeText(): Boolean? {
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
            if (!::mSubtitlePageAutoComplete.isInitialized || mSubtitlePageAutoComplete.text.isEmpty()) 0 else mSubtitlePageAutoComplete.text.toString()
                .toInt()
    }

}
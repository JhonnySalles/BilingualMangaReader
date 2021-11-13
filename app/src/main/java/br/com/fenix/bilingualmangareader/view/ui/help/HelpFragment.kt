package br.com.fenix.bilingualmangareader.view.ui.help

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import br.com.fenix.bilingualmangareader.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HelpFragment : Fragment() {

    private lateinit var mScrollView: ScrollView
    private lateinit var mScrollUp: FloatingActionButton
    private lateinit var mLibraryContent: TextView
    private lateinit var mLibraryTitle: TextView
    private lateinit var mReaderContent: TextView
    private lateinit var mReaderTitle: TextView
    private lateinit var mSubtitleContent: TextView
    private lateinit var mSubtitleTitle: TextView
    private lateinit var mSubtitleImportContent: TextView
    private lateinit var mSubtitleImportTitle: TextView
    private lateinit var mSubtitleDataContent: TextView
    private lateinit var mSubtitleDataTitle: TextView
    private lateinit var mSubtitleVocabularyContent: TextView
    private lateinit var mSubtitleVocabularyTitle: TextView
    private lateinit var mVocabularyContent: TextView
    private lateinit var mVocabularyTitle: TextView
    private lateinit var mKanjiContent: TextView
    private lateinit var mKanjiTitle: TextView
    private lateinit var mFloatingPopupContent: TextView
    private lateinit var mFloatingPopupTitle: TextView
    private lateinit var mLanguageSupportContent: TextView
    private lateinit var mLanguageSupportTitle: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_help, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mScrollView = view.findViewById(R.id.help_scroll_view)
        mScrollUp = view.findViewById(R.id.help_scroll_up)

        mScrollUp.setOnClickListener { mScrollView.smoothScrollTo(0, 0) }
        mScrollUp.visibility = View.GONE
        mScrollView.setOnScrollChangeListener { _, _, yNew, _, yOld ->
            if (yNew in 1 until yOld)
                mScrollUp.show()
            else
                mScrollUp.hide()
        }

        mLibraryContent = view.findViewById(R.id.help_library_content)
        mLibraryTitle = view.findViewById(R.id.help_library_title)
        mReaderContent = view.findViewById(R.id.help_reader_content)
        mReaderTitle = view.findViewById(R.id.help_reader_title)
        mSubtitleContent = view.findViewById(R.id.help_subtitle_content)
        mSubtitleTitle = view.findViewById(R.id.help_subtitle_title)
        mSubtitleImportContent = view.findViewById(R.id.help_subtitle_import_content)
        mSubtitleImportTitle = view.findViewById(R.id.help_subtitle_import_title)
        mSubtitleDataContent = view.findViewById(R.id.help_subtitle_data_content)
        mSubtitleDataTitle = view.findViewById(R.id.help_subtitle_data_title)
        mSubtitleVocabularyContent = view.findViewById(R.id.help_subtitle_vocabulary_content)
        mSubtitleVocabularyTitle = view.findViewById(R.id.help_subtitle_vocabulary_title)
        mVocabularyContent = view.findViewById(R.id.help_vocabulary_content)
        mVocabularyTitle = view.findViewById(R.id.help_vocabulary_title)
        mKanjiContent = view.findViewById(R.id.help_kanjis_content)
        mKanjiTitle = view.findViewById(R.id.help_kanjis_title)
        mFloatingPopupContent = view.findViewById(R.id.help_floating_popup_content)
        mFloatingPopupTitle = view.findViewById(R.id.help_floating_popup_title)
        mLanguageSupportContent = view.findViewById(R.id.help_language_support_content)
        mLanguageSupportTitle = view.findViewById(R.id.help_language_support_title)

        mLibraryContent.setOnClickListener { mScrollView.smoothScrollTo(0, mLibraryTitle.top) }
        mReaderContent.setOnClickListener { mScrollView.smoothScrollTo(0, mReaderTitle.top) }
        mSubtitleContent.setOnClickListener { mScrollView.smoothScrollTo(0, mSubtitleTitle.top) }
        mSubtitleImportContent.setOnClickListener { mScrollView.smoothScrollTo(0, mSubtitleImportTitle.top) }
        mSubtitleDataContent.setOnClickListener { mScrollView.smoothScrollTo(0, mSubtitleDataTitle.top) }
        mSubtitleVocabularyContent.setOnClickListener { mScrollView.smoothScrollTo(0, mSubtitleVocabularyTitle.top) }
        mVocabularyContent.setOnClickListener { mScrollView.smoothScrollTo(0, mVocabularyTitle.top) }
        mKanjiContent.setOnClickListener { mScrollView.smoothScrollTo(0, mKanjiTitle.top) }
        mFloatingPopupContent.setOnClickListener { mScrollView.smoothScrollTo(0, mFloatingPopupTitle.top) }
        mLanguageSupportContent.setOnClickListener { mScrollView.smoothScrollTo(0, mLanguageSupportTitle.top) }
    }
}
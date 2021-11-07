package br.com.fenix.mangareader.view.ui.help

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import br.com.fenix.mangareader.R

class HelpFragment : Fragment() {

    private lateinit var mScrollView: ScrollView
    private lateinit var mLibraryContent: TextView
    private lateinit var mLibraryTitle: TextView
    private lateinit var mReaderContent: TextView
    private lateinit var mReaderTitle: TextView
    private lateinit var mSubtitleContent: TextView
    private lateinit var mSubtitleTitle: TextView
    private lateinit var mVocabularyContent: TextView
    private lateinit var mVocabularyTitle: TextView
    private lateinit var mKanjiContent: TextView
    private lateinit var mKanjiTitle: TextView
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

        mLibraryContent = view.findViewById(R.id.help_library_content)
        mLibraryTitle = view.findViewById(R.id.help_library_title)
        mReaderContent = view.findViewById(R.id.help_reader_content)
        mReaderTitle = view.findViewById(R.id.help_reader_title)
        mSubtitleContent = view.findViewById(R.id.help_subtitle_content)
        mSubtitleTitle = view.findViewById(R.id.help_subtitle_title)
        mVocabularyContent = view.findViewById(R.id.help_vocabulary_content)
        mVocabularyTitle = view.findViewById(R.id.help_vocabulary_title)
        mKanjiContent = view.findViewById(R.id.help_kanjis_content)
        mKanjiTitle = view.findViewById(R.id.help_kanjis_title)
        mLanguageSupportContent = view.findViewById(R.id.help_language_support_content)
        mLanguageSupportTitle = view.findViewById(R.id.help_language_support_title)

        mLibraryContent.setOnClickListener { mScrollView.smoothScrollTo(0, mLibraryTitle.top) }
        mReaderContent.setOnClickListener { mScrollView.smoothScrollTo(0, mReaderTitle.top) }
        mSubtitleContent.setOnClickListener { mScrollView.smoothScrollTo(0, mSubtitleTitle.top) }
        mVocabularyContent.setOnClickListener { mScrollView.smoothScrollTo(0, mVocabularyTitle.top) }
        mKanjiContent.setOnClickListener { mScrollView.smoothScrollTo(0, mKanjiTitle.top) }
        mLanguageSupportContent.setOnClickListener { mScrollView.smoothScrollTo(0, mLanguageSupportTitle.top) }
    }
}
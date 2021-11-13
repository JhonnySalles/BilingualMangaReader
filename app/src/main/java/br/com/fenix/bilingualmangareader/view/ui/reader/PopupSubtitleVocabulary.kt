package br.com.fenix.bilingualmangareader.view.ui.reader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.service.controller.SubTitleController


class PopupSubtitleVocabulary : Fragment() {

    private lateinit var mListPageVocabulary: ListView
    private var mVocabularyItem = ArrayList<String>()

    private lateinit var mSubTitleController: SubTitleController
    private var mBackgroundColor: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.popup_subtitle_vocabulary, container, false)

        if (mBackgroundColor > 0)
            root.setBackgroundColor(ContextCompat.getColor(requireContext(), mBackgroundColor))
        else
            root.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.transparent))

        mListPageVocabulary = root.findViewById(R.id.list_subtitle_page_vocabulary)
        mListPageVocabulary.adapter = ArrayAdapter(requireContext(), R.layout.list_item_vocabulary, mVocabularyItem)
        mSubTitleController = SubTitleController.getInstance(requireContext())

        mSubTitleController.pageSelected.observe(viewLifecycleOwner, {
            if (it?.vocabulary != null && it.vocabulary.isNotEmpty()) {
                val vocabulary = it.vocabulary.map { vocab -> vocab.word + " - " + vocab.meaning + if (!vocab.revised && vocab.meaning.isNotEmpty()) "¹" else "" }
                mVocabularyItem.clear()
                mVocabularyItem.addAll(vocabulary)
            } else
                mVocabularyItem.clear()

            (mListPageVocabulary.adapter as ArrayAdapter<*>).notifyDataSetChanged()
        })

        return root
    }

    fun setBackground(color: Int) {
        mBackgroundColor = color
    }

}
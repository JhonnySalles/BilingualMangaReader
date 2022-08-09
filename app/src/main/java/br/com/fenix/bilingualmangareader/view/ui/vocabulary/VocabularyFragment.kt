package br.com.fenix.bilingualmangareader.view.ui.vocabulary

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.service.listener.VocabularyCardListener
import br.com.fenix.bilingualmangareader.view.adapter.vocabulary.VocabularyCardAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import org.slf4j.LoggerFactory


class VocabularyFragment : Fragment() {

    private val mLOGGER = LoggerFactory.getLogger(VocabularyFragment::class.java)

    private val mViewModel: VocabularyViewModel by viewModels()

    private lateinit var mRoot: ConstraintLayout
    private lateinit var mScrollUp: FloatingActionButton
    private lateinit var mScrollDown: FloatingActionButton
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mContent: LinearLayout
    private lateinit var mComic: TextInputLayout
    private lateinit var mComicAutoComplete: AutoCompleteTextView
    private lateinit var mVocabulary: TextInputLayout
    private lateinit var mVocabularyAutoComplete: AutoCompleteTextView

    private lateinit var mToolbar: androidx.appcompat.widget.Toolbar
    private lateinit var mLibrary: TextView

    private lateinit var mListener: VocabularyCardListener

    private var mHandler = Handler(Looper.getMainLooper())

    private val mDismissUpButton = Runnable { mScrollUp.hide() }
    private val mDismissDownButton = Runnable { mScrollDown.hide() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_vocabulary, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_pages_link, container, false)

        mRoot = root.findViewById(R.id.vocabulary_root)
        mRecyclerView = root.findViewById(R.id.vocabulary_recycler)

        mContent = root.findViewById(R.id.vocabulary_content)
        mComic = root.findViewById(R.id.vocabulary_manga_text)
        mComicAutoComplete = root.findViewById(R.id.vocabulary_manga_autocomplete)
        mVocabulary = root.findViewById(R.id.vocabulary_find_text)
        mVocabularyAutoComplete = root.findViewById(R.id.vocabulary_find_autocomplete)

        mScrollUp = root.findViewById(R.id.vocabulary_scroll_up)
        mScrollDown = root.findViewById(R.id.vocabulary_scroll_down)

        mToolbar = root.findViewById(R.id.vocabulary_toolbar)
        mLibrary = root.findViewById(R.id.vocabulary_toolbar_library)

        mScrollUp.visibility = View.GONE
        mScrollDown.visibility = View.GONE

        mScrollUp.setOnClickListener { mRecyclerView.smoothScrollToPosition(0) }
        mScrollDown.setOnClickListener {
            mRecyclerView.smoothScrollToPosition((mRecyclerView.adapter as RecyclerView.Adapter).itemCount)
        }

        mRecyclerView.setOnScrollChangeListener { _, _, _, _, yOld ->
            if (yOld > 20) {
                if (mHandler.hasCallbacks(mDismissDownButton))
                    mHandler.removeCallbacks(mDismissDownButton)

                mScrollDown.hide()
            } else if (yOld < -20) {
                if (mHandler.hasCallbacks(mDismissUpButton))
                    mHandler.removeCallbacks(mDismissUpButton)

                mScrollUp.hide()
            }

            if (yOld > 150) {
                if (mHandler.hasCallbacks(mDismissUpButton))
                    mHandler.removeCallbacks(mDismissUpButton)

                mHandler.postDelayed(mDismissUpButton, 3000)
                mScrollUp.show()
            } else if (yOld < -150) {
                if (mHandler.hasCallbacks(mDismissDownButton))
                    mHandler.removeCallbacks(mDismissDownButton)

                mHandler.postDelayed(mDismissDownButton, 3000)
                mScrollDown.show()
            }
        }

        observer()
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapterPageLink = VocabularyCardAdapter()
        mRecyclerView.adapter = adapterPageLink
        mRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapterPageLink.attachListener(mListener)
    }

    private fun observer() {
        mViewModel.vocabulary.observe(viewLifecycleOwner) {
            (mRecyclerView.adapter as VocabularyCardAdapter).updateList(it)
        }
    }

    override fun onDestroy() {
        if (mHandler.hasCallbacks(mDismissUpButton))
            mHandler.removeCallbacks(mDismissUpButton)
        if (mHandler.hasCallbacks(mDismissDownButton))
            mHandler.removeCallbacks(mDismissDownButton)

        super.onDestroy()
    }
}
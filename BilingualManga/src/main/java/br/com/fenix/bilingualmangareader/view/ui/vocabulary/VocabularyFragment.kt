package br.com.fenix.bilingualmangareader.view.ui.vocabulary

import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.model.entity.Vocabulary
import br.com.fenix.bilingualmangareader.service.listener.VocabularyCardListener
import br.com.fenix.bilingualmangareader.view.adapter.vocabulary.VocabularyCardAdapter
import br.com.fenix.bilingualmangareader.view.adapter.vocabulary.VocabularyLoadState
import br.com.fenix.bilingualmangareader.view.adapter.vocabulary.VocabularyMangaListCardAdapter
import br.com.fenix.bilingualmangareader.view.components.ComponentsUtil
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory


class VocabularyFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private val mLOGGER = LoggerFactory.getLogger(VocabularyFragment::class.java)

    private val mViewModel: VocabularyViewModel by viewModels()

    var mManga: Manga? = null

    private lateinit var mRoot: ConstraintLayout
    private lateinit var mRefreshLayout: SwipeRefreshLayout
    private lateinit var mScrollUp: FloatingActionButton
    private lateinit var mScrollDown: FloatingActionButton
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mContent: LinearLayout
    private lateinit var mMangaName: TextInputLayout
    private lateinit var mMangaNameEditText: TextInputEditText

    private lateinit var mFavorite: MenuItem
    private lateinit var miSearch: MenuItem
    private lateinit var searchView: SearchView

    private lateinit var mListener: VocabularyCardListener

    private var mHandler = Handler(Looper.getMainLooper())

    private val mDismissUpButton = Runnable { mScrollUp.hide() }
    private val mDismissDownButton = Runnable { mScrollDown.hide() }

    private val mSetQuery = Runnable {
        mViewModel.setQuery(
            mMangaNameEditText.text?.toString() ?: "",
            searchView.query.toString()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_vocabulary, menu)
        super.onCreateOptionsMenu(menu, inflater)

        mFavorite = menu.findItem(R.id.menu_vocabulary_favorite)

        setFavorite(mViewModel.getFavorite())
        mFavorite.setOnMenuItemClickListener {
            val favorite = !mViewModel.getFavorite()
            mFavorite.setIcon(if (favorite) R.drawable.ico_animated_favorited_marked else R.drawable.ico_animated_favorited_unmarked)
            (mFavorite.icon as AnimatedVectorDrawable).start()
            mViewModel.setQueryFavorite(!mViewModel.getFavorite())
            true
        }

        val miOrder = menu.findItem(R.id.menu_vocabulary_list_order)
        miOrder.setOnMenuItemClickListener {
            mViewModel.setQueryOrder(!mViewModel.getOrder())
            true
        }

        miSearch = menu.findItem(R.id.menu_vocabulary_search)
        searchView = miSearch.actionView as SearchView
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                mViewModel.setQuery(
                    mMangaNameEditText.text?.toString() ?: "",
                    query ?: ""
                )
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.menu_history_library -> {}
        }
        return super.onOptionsItemSelected(menuItem)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_vocabulary, container, false)

        mRoot = root.findViewById(R.id.vocabulary_root)
        mRecyclerView = root.findViewById(R.id.vocabulary_recycler)
        mRefreshLayout = root.findViewById(R.id.vocabulary_refresh)

        mContent = root.findViewById(R.id.vocabulary_content)
        mMangaName = root.findViewById(R.id.vocabulary_manga_text)
        mMangaNameEditText = root.findViewById(R.id.vocabulary_manga_edittext)

        mScrollUp = root.findViewById(R.id.vocabulary_scroll_up)
        mScrollDown = root.findViewById(R.id.vocabulary_scroll_down)

        mScrollUp.visibility = View.GONE
        mScrollDown.visibility = View.GONE

        mViewModel.isQuery.observe(viewLifecycleOwner) {
            mRefreshLayout.isRefreshing = it
        }

        mManga?.let {
            mMangaNameEditText.setText(it.name)
            mViewModel.setQueryManga(it.name)
        }

        ComponentsUtil.setThemeColor(requireContext(), mRefreshLayout)
        mRefreshLayout.setOnRefreshListener(this)
        mRefreshLayout.isEnabled = true

        mMangaNameEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (mHandler.hasCallbacks(mSetQuery))
                        mHandler.removeCallbacks(mSetQuery)
                } else
                    mHandler.removeCallbacks(mSetQuery)

                mHandler.postDelayed(mSetQuery, 1000)
            }
        })

        mScrollUp.setOnClickListener {
            (mScrollUp.drawable as AnimatedVectorDrawable).start()
            mRecyclerView.smoothScrollToPosition(0)
        }
        mScrollDown.setOnClickListener {
            (mScrollDown.drawable as AnimatedVectorDrawable).start()
            mRecyclerView.smoothScrollToPosition((mRecyclerView.adapter as RecyclerView.Adapter).itemCount)
        }

        mRecyclerView.setOnScrollChangeListener { _, _, _, _, yOld ->
            if (yOld > 20) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (mHandler.hasCallbacks(mDismissDownButton))
                        mHandler.removeCallbacks(mDismissDownButton)
                } else
                    mHandler.removeCallbacks(mDismissDownButton)

                mScrollDown.hide()
            } else if (yOld < -20) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (mHandler.hasCallbacks(mDismissUpButton))
                        mHandler.removeCallbacks(mDismissUpButton)
                } else
                    mHandler.removeCallbacks(mDismissUpButton)

                mScrollUp.hide()
            }

            if (yOld > 150) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (mHandler.hasCallbacks(mDismissUpButton))
                        mHandler.removeCallbacks(mDismissUpButton)
                } else
                    mHandler.removeCallbacks(mDismissUpButton)

                mHandler.postDelayed(mDismissUpButton, 3000)
                mScrollUp.show()
            } else if (yOld < -150) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (mHandler.hasCallbacks(mDismissDownButton))
                        mHandler.removeCallbacks(mDismissDownButton)
                } else
                    mHandler.removeCallbacks(mDismissDownButton)

                mHandler.postDelayed(mDismissDownButton, 3000)
                mScrollDown.show()
            }
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mListener = object : VocabularyCardListener {
            override fun onClick(vocabulary: Vocabulary) {
            }

            override fun onClickLong(vocabulary: Vocabulary, view: View, position: Int) {
            }

            override fun onClickFavorite(vocabulary: Vocabulary) {
                mViewModel.update(vocabulary)
            }
        }

        val adapter = VocabularyCardAdapter(mListener)
        mRecyclerView.adapter = adapter.withLoadStateFooter(VocabularyLoadState())
        mRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        lifecycleScope.launch {
            mViewModel.vocabularyPager().collectLatest {
                adapter.submitData(it)
            }
        }
    }

    override fun onDestroy() {
        VocabularyMangaListCardAdapter.clearVocabularyMangaList()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (mHandler.hasCallbacks(mDismissUpButton))
                mHandler.removeCallbacks(mDismissUpButton)
            if (mHandler.hasCallbacks(mDismissDownButton))
                mHandler.removeCallbacks(mDismissDownButton)
        } else {
            mHandler.removeCallbacks(mDismissUpButton)
            mHandler.removeCallbacks(mDismissDownButton)
        }

        super.onDestroy()
    }

    private fun setFavorite(favorite: Boolean) {
        mFavorite.setIcon(if (favorite) R.drawable.ic_favorite_mark else R.drawable.ic_favorite_unmark)
    }

    override fun onRefresh() {
        if (::searchView.isInitialized)
            mViewModel.setQuery(mMangaNameEditText.text.toString(), searchView.query.toString(), mFavorite.isChecked)
        else
            mViewModel.setQuery(mMangaNameEditText.text.toString(), "", mFavorite.isChecked)
    }
}
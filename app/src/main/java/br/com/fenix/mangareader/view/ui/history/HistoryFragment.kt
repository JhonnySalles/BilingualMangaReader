package br.com.fenix.mangareader.view.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.mangareader.R
import br.com.fenix.mangareader.model.entity.Manga
import br.com.fenix.mangareader.model.enums.LibraryType
import br.com.fenix.mangareader.service.listener.MangaCardListener
import br.com.fenix.mangareader.service.repository.MangaRepository
import br.com.fenix.mangareader.view.adapter.library.MangaGridCardAdapter
import br.com.fenix.mangareader.view.adapter.library.MangaLineCardAdapter
import br.com.fenix.mangareader.view.ui.library.LibraryFragment
import br.com.fenix.mangareader.view.ui.library.LibraryViewModel
import java.util.ArrayList

class HistoryFragment : Fragment() {

    private lateinit var mViewModel: HistoryViewModel
    private lateinit var mRecycleView: RecyclerView
    private lateinit var mListener: MangaCardListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mViewModel = ViewModelProvider(this).get(HistoryViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_history, container, false)
        mRecycleView = root.findViewById(R.id.rv_history)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val lineAdapter = MangaLineCardAdapter()
        mRecycleView.adapter = lineAdapter
        mRecycleView.layoutManager = GridLayoutManager(requireContext(), 1)
        lineAdapter.attachListener(mListener)
    }

    private fun filter(newText: String?) {
        if (LibraryFragment.mGridType != LibraryType.LINE)
            (mRecycleView.adapter as MangaGridCardAdapter).filter.filter(newText)
        else
            (mRecycleView.adapter as MangaLineCardAdapter).filter.filter(newText)
    }

    private fun updateList(list: ArrayList<Manga>) {
        if (LibraryFragment.mGridType != LibraryType.LINE)
            (mRecycleView.adapter as MangaGridCardAdapter).updateList(list)
        else
            (mRecycleView.adapter as MangaLineCardAdapter).updateList(list)
    }

    private fun observer() {
        mViewModel.save.observe(viewLifecycleOwner, {
            updateList(it)
        })
    }


}
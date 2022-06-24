package br.com.fenix.bilingualmangareader.view.ui.history

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.service.listener.MangaCardListener
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.view.adapter.history.HistoryCardAdapter
import br.com.fenix.bilingualmangareader.view.ui.reader.ReaderActivity
import java.lang.ref.WeakReference
import java.time.LocalDateTime

class HistoryFragment : Fragment() {

    private lateinit var mViewModel: HistoryViewModel
    private lateinit var mRecycleView: RecyclerView
    private lateinit var mListener: MangaCardListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mViewModel = ViewModelProvider(this).get(HistoryViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_history, container, false)
        mRecycleView = root.findViewById(R.id.rv_history)
        mListener = object : MangaCardListener {
            override fun onClick(manga: Manga) {
                if (!manga.excluded) {
                    val intent = Intent(context, ReaderActivity::class.java)
                    val bundle = Bundle()
                    manga.lastAccess = LocalDateTime.now()
                    bundle.putString(GeneralConsts.KEYS.MANGA.NAME, manga.title)
                    bundle.putInt(GeneralConsts.KEYS.MANGA.MARK, manga.bookMark)
                    bundle.putSerializable(GeneralConsts.KEYS.OBJECT.MANGA, manga)
                    intent.putExtras(bundle)
                    context?.startActivity(intent)
                    mViewModel.updateLastAccess(manga)
                } else
                    AlertDialog.Builder(requireActivity(), R.style.AppCompatAlertDialogStyle)
                            .setTitle(getString(R.string.manga_excluded))
                            .setMessage(manga.file.path)
                            .setPositiveButton(
                                R.string.action_neutral
                            ) { _, _ -> }
                            .create()
                            .show()
            }

            override fun onClickLong(manga: Manga, view: View, position: Int) {

            }
        }
        observer()
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val historyAdapter = HistoryCardAdapter()
        mRecycleView.adapter = historyAdapter
        mRecycleView.layoutManager = GridLayoutManager(requireContext(), 1)
        historyAdapter.attachListener(mListener)
    }

    override fun onResume() {
        super.onResume()
        mViewModel.list {
            notifyDataSet()
        }
    }

    private fun notifyDataSet(idItem: Int? = null) {
        if (idItem != null)
            mRecycleView.adapter?.notifyItemChanged(idItem)
        else
            mRecycleView.adapter?.notifyDataSetChanged()
    }

    private fun updateList(list: ArrayList<Manga>) {
       (mRecycleView.adapter as HistoryCardAdapter).updateList(list)
    }

    private fun observer() {
        mViewModel.save.observe(viewLifecycleOwner) {
            updateList(it)
        }
    }

}
package br.com.fenix.bilingualmangareader.view.ui.history

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.service.controller.ImageCoverController
import br.com.fenix.bilingualmangareader.service.listener.MangaCardListener
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.view.adapter.history.HistoryCardAdapter
import br.com.fenix.bilingualmangareader.view.ui.reader.ReaderActivity
import java.lang.ref.WeakReference
import java.util.*

class HistoryFragment : Fragment() {

    private lateinit var mViewModel: HistoryViewModel
    private lateinit var mRecycleView: RecyclerView
    private lateinit var mListener: MangaCardListener

    private val mUpdateHandler: Handler = UpdateHandler(this)

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
                val intent = Intent(context, ReaderActivity::class.java)
                val bundle = Bundle()
                manga.lastAccess = Calendar.getInstance().time
                bundle.putString(GeneralConsts.KEYS.MANGA.NAME, manga.title)
                bundle.putInt(GeneralConsts.KEYS.MANGA.MARK, manga.bookMark)
                bundle.putSerializable(GeneralConsts.KEYS.OBJECT.MANGA, manga)
                intent.putExtras(bundle)
                context?.startActivity(intent)
                mViewModel.updateLastAccess(manga)
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
        ImageCoverController.instance.addUpdateHandler(mUpdateHandler)
        mViewModel.list {
            notifyDataSet()
        }
    }

    override fun onPause() {
        ImageCoverController.instance.removeUpdateHandler(mUpdateHandler)
        super.onPause()
    }

    private inner class UpdateHandler(fragment: HistoryFragment) : Handler() {
        private val mOwner: WeakReference<HistoryFragment> = WeakReference(fragment)
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                GeneralConsts.SCANNER.MESSAGE_COVER_UPDATE_FINISHED -> {
                    val idItem = msg.data.getInt("position")
                    notifyDataSet(idItem)
                }
            }
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
        mViewModel.save.observe(viewLifecycleOwner, {
            updateList(it)
        })
    }

}
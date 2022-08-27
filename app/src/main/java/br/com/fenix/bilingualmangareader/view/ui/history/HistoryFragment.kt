package br.com.fenix.bilingualmangareader.view.ui.history

import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.service.listener.MangaCardListener
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.util.helpers.FileUtil
import br.com.fenix.bilingualmangareader.util.helpers.LibraryUtil
import br.com.fenix.bilingualmangareader.view.adapter.history.HistoryCardAdapter
import br.com.fenix.bilingualmangareader.view.ui.reader.ReaderActivity
import java.util.*

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
        mRecycleView = root.findViewById(R.id.history_list)
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecycleView)
        mListener = object : MangaCardListener {
            override fun onClick(manga: Manga) {
                if (!manga.excluded) {
                    val intent = Intent(context, ReaderActivity::class.java)
                    val bundle = Bundle()
                    manga.lastAccess = Date()
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
                val wrapper = ContextThemeWrapper(requireContext(), R.style.PopupMenu)
                val popup = PopupMenu(wrapper, view)
                popup.menuInflater.inflate(R.menu.menu_book, popup.menu)

                if (manga.favorite)
                    popup.menu.findItem(R.id.menu_book_favorite).title = getString(R.string.library_menu_favorite_remove)
                else
                    popup.menu.findItem(R.id.menu_book_favorite).title = getString(R.string.library_menu_favorite_add)

                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.menu_book_favorite -> {
                            manga.favorite = !manga.favorite
                            mViewModel.save(manga)
                            mRecycleView.adapter?.notifyItemChanged(position)
                        }
                        R.id.menu_book_clear -> {
                            manga.lastAccess = null
                            manga.bookMark = 0
                            mViewModel.clear(manga)
                            mRecycleView.adapter?.notifyItemChanged(position)
                        }
                        R.id.menu_book_delete -> {
                            val dialog: AlertDialog =
                                AlertDialog.Builder(requireActivity(), R.style.AppCompatAlertDialogStyle)
                                    .setTitle(getString(R.string.library_menu_delete))
                                    .setMessage(getString(R.string.history_delete_description) + "\n" + manga.file.name)
                                    .setPositiveButton(
                                        R.string.action_positive
                                    ) { _, _ ->
                                        mViewModel.deletePermanent(manga)
                                        mRecycleView.adapter?.notifyItemRemoved(position)
                                    }
                                    .setNegativeButton(
                                        R.string.action_negative
                                    ) { _, _ -> }
                                    .create()
                            dialog.show()
                        }
                        R.id.menu_book_copy_name -> FileUtil(requireContext()).copyName(manga)
                    }
                    true
                }

                popup.show()
            }
        }
        observer()
        return root
    }

    private var itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val manga = mViewModel.getAndRemove(viewHolder.adapterPosition) ?: return
            val position = viewHolder.adapterPosition
            var excluded = false
            val dialog: AlertDialog =
                AlertDialog.Builder(requireActivity(), R.style.AppCompatAlertDialogStyle)
                    .setTitle(getString(R.string.library_menu_delete))
                    .setMessage(getString(R.string.history_delete_description) + "\n" + manga.file.name)
                    .setPositiveButton(
                        R.string.action_delete
                    ) { _, _ ->
                        mViewModel.deletePermanent(manga)
                        mRecycleView.adapter?.notifyItemRemoved(position)
                        excluded = true
                    }.setOnDismissListener {
                        if (!excluded) {
                            mViewModel.add(manga, position)
                            mRecycleView.adapter?.notifyItemChanged(position)
                        }
                    }
                    .create()
            dialog.show()
        }
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
            if (it > -1)
                mRecycleView.adapter?.notifyItemChanged(0, it)
            else
                mRecycleView.adapter?.notifyDataSetChanged()
        }
    }

    private fun updateList(list: ArrayList<Manga>) {
        (mRecycleView.adapter as HistoryCardAdapter).updateList(list)
    }

    private fun observer() {
        mViewModel.listMangas.observe(viewLifecycleOwner) {
            updateList(it)
        }
    }

}
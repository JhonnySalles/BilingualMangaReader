package br.com.fenix.bilingualmangareader.view.ui.manga_detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Information
import br.com.fenix.bilingualmangareader.model.entity.Library
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.service.controller.ImageController
import br.com.fenix.bilingualmangareader.service.controller.ImageCoverController
import br.com.fenix.bilingualmangareader.service.listener.InformationCardListener
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.util.helpers.LibraryUtil
import br.com.fenix.bilingualmangareader.util.helpers.Util
import br.com.fenix.bilingualmangareader.view.adapter.manga_detail.InformationRelatedCardAdapter
import br.com.fenix.bilingualmangareader.view.ui.reader.ReaderActivity
import com.google.android.material.button.MaterialButton
import org.slf4j.LoggerFactory


class MangaDetailFragment : Fragment() {

    private val mLOGGER = LoggerFactory.getLogger(MangaDetailFragment::class.java)

    private val mViewModel: MangaDetailViewModel by viewModels()

    var mLibrary: Library? = null
    var mManga: Manga? = null

    private lateinit var mBackgroundImage: ImageView
    private lateinit var mImage: ImageView
    private lateinit var mTitle: TextView
    private lateinit var mFolder: TextView
    private lateinit var mLastAccess: TextView
    private lateinit var mDeleted: TextView
    private lateinit var mBookMark: TextView
    private lateinit var mProgress: ProgressBar
    private lateinit var mButtonsContent: LinearLayout
    private lateinit var mFavoriteButton: MaterialButton
    private lateinit var mMakReadButton: MaterialButton
    private lateinit var mClearHistoryButton: MaterialButton
    private lateinit var mDeleteButton: MaterialButton
    private lateinit var mChaptersList: ListView
    private lateinit var mFileLinkContent: LinearLayout
    private lateinit var mFileLinksList: ListView
    private lateinit var mSubtitlesContent: LinearLayout
    private lateinit var mSubtitlesList: ListView

    private lateinit var mInformationContent: LinearLayout
    private lateinit var mInformationImage: ImageView
    private lateinit var mInformationSynopsis: TextView
    private lateinit var mInformationAlternativeTitles: TextView
    private lateinit var mInformationStatus: TextView
    private lateinit var mInformationPublish: TextView
    private lateinit var mInformationVolumes: TextView
    private lateinit var mInformationAuthors: TextView
    private lateinit var mInformationGenres: TextView
    private lateinit var mInformationOrigin: TextView
    private lateinit var mRelatedContent: LinearLayout
    private lateinit var mRelatedRelatedList: RecyclerView
    private lateinit var mRelatedOrigin: TextView
    private lateinit var mListener: InformationCardListener

    private var mSubtitles: MutableList<String> = mutableListOf()
    private var mChapters: MutableList<String> = mutableListOf()
    private var mFileLinks: MutableList<String> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_manga_detail, container, false)

        mBackgroundImage = root.findViewById(R.id.manga_detail_background_image)
        mImage = root.findViewById(R.id.manga_detail_manga_image)
        mTitle = root.findViewById(R.id.manga_detail_title)
        mFolder = root.findViewById(R.id.manga_detail_folder)
        mLastAccess = root.findViewById(R.id.manga_detail_last_access)
        mDeleted = root.findViewById(R.id.manga_detail_deleted)
        mBookMark = root.findViewById(R.id.manga_detail_book_mark)
        mProgress = root.findViewById(R.id.manga_detail_progress)
        mButtonsContent = root.findViewById(R.id.manga_detail_buttons)
        mFavoriteButton = root.findViewById(R.id.manga_detail_button_favorite)
        mClearHistoryButton = root.findViewById(R.id.manga_detail_button_clear_history)
        mMakReadButton = root.findViewById(R.id.manga_detail_button_mark_read)
        mDeleteButton = root.findViewById(R.id.manga_detail_button_delete)
        mChaptersList = root.findViewById(R.id.manga_detail_chapters_list)
        mFileLinkContent = root.findViewById(R.id.manga_detail_files_link_detail)
        mFileLinksList = root.findViewById(R.id.manga_detail_files_links_list)
        mSubtitlesContent = root.findViewById(R.id.manga_detail_subtitle_content)
        mSubtitlesList = root.findViewById(R.id.manga_detail_subtitles_list)

        mInformationContent = root.findViewById(R.id.manga_detail_information)
        mInformationImage = root.findViewById(R.id.manga_detail_information_image)
        mInformationSynopsis = root.findViewById(R.id.manga_detail_information_synopsis)
        mInformationAlternativeTitles = root.findViewById(R.id.manga_detail_information_alternative_titles)
        mInformationStatus = root.findViewById(R.id.manga_detail_information_status)
        mInformationPublish = root.findViewById(R.id.manga_detail_information_publish)
        mInformationVolumes = root.findViewById(R.id.manga_detail_information_volumes_chapters)
        mInformationAuthors = root.findViewById(R.id.manga_detail_information_author)
        mInformationGenres = root.findViewById(R.id.manga_detail_information_genres)
        mInformationOrigin = root.findViewById(R.id.manga_detail_information_origin)
        mRelatedContent = root.findViewById(R.id.manga_detail_information_relations)
        mRelatedRelatedList = root.findViewById(R.id.manga_detail_relations_lists)
        mRelatedOrigin = root.findViewById(R.id.manga_detail_relations_origin)

        mMakReadButton.setOnClickListener { markRead() }
        mDeleteButton.setOnClickListener { deleteFile() }
        mFavoriteButton.setOnClickListener { favorite() }
        mClearHistoryButton.setOnClickListener { clearHistory() }

        mSubtitlesList.adapter = ArrayAdapter(requireContext(), R.layout.list_item_all_text, mSubtitles)
        mFileLinksList.adapter = ArrayAdapter(requireContext(), R.layout.list_item_all_text, mFileLinks)
        mChaptersList.adapter = ArrayAdapter(requireContext(), R.layout.list_item_all_text, mChapters)

        mRelatedRelatedList.adapter = InformationRelatedCardAdapter()
        mRelatedRelatedList.layoutManager = LinearLayoutManager(requireContext())

        mChaptersList.setOnItemClickListener { _, _, index, _ ->
            if (mManga != null && index >= 0 && mChapters.size > index) {
                val folder = mChapters[index]
                val page = mViewModel.getPage(folder)
                mManga!!.bookMark = page
                mViewModel.save(mManga)
                val intent = Intent(context, ReaderActivity::class.java)
                val bundle = Bundle()
                bundle.putSerializable(GeneralConsts.KEYS.OBJECT.LIBRARY, mLibrary)
                bundle.putString(GeneralConsts.KEYS.MANGA.NAME, mManga!!.title)
                bundle.putInt(GeneralConsts.KEYS.MANGA.MARK, mManga!!.bookMark)
                bundle.putSerializable(GeneralConsts.KEYS.OBJECT.MANGA, mManga!!)
                intent.putExtras(bundle)
                context?.startActivity(intent)
            }
        }

        mInformationContent.setOnLongClickListener {
            if (mViewModel.information.value != null)
                openUrl(mViewModel.information.value!!.link)
            true
        }

        mListener = object : InformationCardListener {
            override fun onClickLong(url: String) {
                openUrl(url)
            }
        }

        (mRelatedRelatedList.adapter as InformationRelatedCardAdapter).attachListener(mListener)

        observer()

        if (mManga != null)
            mViewModel.setManga(mManga!!)

        return root
    }

    override fun onResume() {
        super.onResume()

        mViewModel.getInformation()
    }

    private fun observer() {
        mViewModel.manga.observe(viewLifecycleOwner) {
            if (it != null) {
                ImageCoverController.instance.setImageCoverAsync(requireContext(), it, arrayListOf(mBackgroundImage, mImage), false)
                mTitle.text = it.name
                mFolder.text = it.path
                val folder = mViewModel.getChapterFolder(it.bookMark)
                mBookMark.text = "${it.bookMark} / ${it.pages}" + if (folder.isNotEmpty()) " - $folder" else ""
                mLastAccess.text = if (it.lastAccess == null) "" else GeneralConsts.formatterDate(requireContext(), it.lastAccess!!)
                mProgress.max = it.pages
                mProgress.setProgress(it.bookMark, false)

                if (it.favorite) {
                    mFavoriteButton.setIconResource(R.drawable.ic_favorite_mark)
                    mFavoriteButton.setIconTintResource(R.color.on_secondary)
                } else {
                    mFavoriteButton.setIconResource(R.drawable.ic_favorite_unmark)
                    mFavoriteButton.setIconTintResource(R.color.text_primary)
                }

                if (it.excluded) {
                    mDeleted.text = getString(R.string.manga_detail_manga_deleted)
                    mDeleted.visibility = View.VISIBLE
                } else {
                    mDeleted.text = ""
                    mDeleted.visibility = View.GONE
                }

            } else {
                mBackgroundImage.setImageBitmap(null)
                mImage.setImageBitmap(null)

                mTitle.text = ""
                mFolder.text = ""
                mLastAccess.text = ""
                mDeleted.text = ""
                mBookMark.text = ""
                mProgress.max = 1
                mProgress.setProgress(0, false)
                mFavoriteButton.setIconResource(R.drawable.ic_favorite_unmark)
                mFavoriteButton.setIconTintResource(R.color.text_primary)
            }
        }

        mViewModel.listChapters.observe(viewLifecycleOwner) {
            mChapters.clear()
            mChapters.addAll(it)
            (mChaptersList.adapter as ArrayAdapter<*>).notifyDataSetChanged()

            val manga = mViewModel.manga.value
            if (manga != null)
                mBookMark.text = "${manga.bookMark} / ${manga.pages} - " + mViewModel.getChapterFolder(manga.bookMark)
        }

        mViewModel.listSubtitles.observe(viewLifecycleOwner) {
            mSubtitles.clear()
            mSubtitles.addAll(it)
            (mSubtitlesList.adapter as ArrayAdapter<*>).notifyDataSetChanged()
            mSubtitlesContent.visibility = if (it.isNotEmpty())
                View.VISIBLE
            else
                View.GONE
        }

        mViewModel.listFileLinks.observe(viewLifecycleOwner) { fileLinks ->
            val list = fileLinks?.map { it.path }?.toMutableList() ?: mutableListOf()
            mFileLinks.clear()
            mFileLinks.addAll(list)
            (mFileLinksList.adapter as ArrayAdapter<*>).notifyDataSetChanged()
            mFileLinkContent.visibility = if (mFileLinks.isNotEmpty())
                View.VISIBLE
            else
                View.GONE
        }

        mViewModel.information.observe(viewLifecycleOwner) {
            if (it != null) {
                mInformationContent.visibility = View.VISIBLE
                mInformationSynopsis.text = it.synopsis.replace("\\n", "\n")

                mInformationImage.visibility = View.GONE
                mInformationImage.setImageBitmap(null)

                if (it.imageLink != null)
                    ImageController.instance.setImageAsync(requireContext(), it.imageLink!!, mInformationImage)

                mInformationAlternativeTitles.text = Html.fromHtml(Util.setBold(requireContext().getString(R.string.manga_detail_information_alternative_titles)) + " " + it.alternativeTitles)
                mInformationStatus.text = Html.fromHtml(Util.setBold(requireContext().getString(R.string.manga_detail_information_status)) + " " + it.status)
                mInformationPublish.text = Html.fromHtml(
                    Util.setBold(requireContext().getString(R.string.manga_detail_information_publish)) + " " + Util.formatterDate(
                        requireContext(),
                        it.startDate
                    ) + " " + requireContext().getString(R.string.manga_detail_information_publish_to) + " " + Util.formatterDate(
                        requireContext(),
                        it.endDate
                    )
                )
                mInformationVolumes.text = Html.fromHtml(
                    Util.setBold(requireContext().getString(R.string.manga_detail_information_volumes)) + " " + it.volumes + ", " + Util.setBold(
                        requireContext().getString(R.string.manga_detail_information_chapters)
                    ) + " " + it.chapters
                )
                mInformationAuthors.text = Html.fromHtml(Util.setBold(requireContext().getString(R.string.manga_detail_information_authors)) + " " + it.authors)
                mInformationGenres.text = Html.fromHtml(Util.setBold(requireContext().getString(R.string.manga_detail_information_genre)) + " " + it.genres)
                mInformationOrigin.text = it.origin
            } else {
                mInformationContent.visibility = View.GONE
                mInformationImage.visibility = View.GONE

                mInformationSynopsis.text = ""
                mInformationAlternativeTitles.text = ""
                mInformationStatus.text = ""
                mInformationPublish.text = ""
                mInformationVolumes.text = ""
                mInformationAuthors.text = ""
                mInformationGenres.text = ""
                mInformationOrigin.text = ""
            }
        }

        mViewModel.informationRelations.observe(viewLifecycleOwner) {
            mRelatedContent.visibility = if (it != null && it.isNotEmpty()) View.VISIBLE else View.GONE
            updateRelatedList(it)
        }

    }

    private fun deleteFile() {
        val manga = mViewModel.manga.value ?: return
        AlertDialog.Builder(requireActivity(), R.style.AppCompatAlertDialogStyle)
            .setTitle(getString(R.string.library_menu_delete))
            .setMessage(getString(R.string.library_menu_delete_description) + "\n" + manga.file.name)
            .setPositiveButton(
                R.string.action_positive
            ) { _, _ ->
                mViewModel.delete()
                if (manga.file.exists()) {
                    val isDeleted = manga.file.delete()
                    mLOGGER.info("File deleted ${manga.name}: $isDeleted")
                }
                (requireActivity() as MangaDetailActivity).onBackPressed()
            }
            .setNegativeButton(
                R.string.action_negative
            ) { _, _ -> }
            .create().show()
    }

    private fun clearHistory() {
        mViewModel.clearHistory()
    }

    private fun markRead() {
        mViewModel.markRead()
    }

    private fun favorite() {
        val manga = mViewModel.manga.value ?: return
        manga.favorite = !manga.favorite
        mViewModel.save(manga)
    }

    private fun updateRelatedList(list: MutableList<Information>?) {
        (mRelatedRelatedList.adapter as InformationRelatedCardAdapter).updateList(list)
        mRelatedOrigin.text = if (list.isNullOrEmpty()) "" else list[0].origin
    }

    private fun openUrl(url: String) {
        if (url.isEmpty()) return

        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url)
            )
        )
    }

}
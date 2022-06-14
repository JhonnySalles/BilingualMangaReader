package br.com.fenix.bilingualmangareader.view.adapter.page_link

import android.content.ClipDescription
import android.content.res.Configuration
import android.view.DragEvent
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.PageLink
import br.com.fenix.bilingualmangareader.model.enums.Pages
import br.com.fenix.bilingualmangareader.service.listener.PageLinkCardListener
import br.com.fenix.bilingualmangareader.util.constants.PageLinkConsts
import com.google.android.material.card.MaterialCardView


class PageLinkViewHolder(itemView: View, private val listener: PageLinkCardListener) :
    RecyclerView.ViewHolder(itemView) {

    companion object {
        var mIsTabletOrLandscape: Boolean = false
        var mPageLinkCardWidth: Int = 0
        var mPageLinkCardWidthInDual: Int = 0
    }

    init {
        val root = itemView.findViewById<ConstraintLayout>(R.id.page_link_card)
        mIsTabletOrLandscape = if (root.tag != null)
            itemView.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE || root.tag.toString().compareTo("tablet", true) == 0
        else itemView.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        mPageLinkCardWidth = itemView.resources.getDimension(R.dimen.page_link_card_layout_width).toInt()
        mPageLinkCardWidthInDual = if (mIsTabletOrLandscape)
            itemView.resources.getDimension(R.dimen.dual_page_link_card_layout_width_tablet).toInt()
        else
            itemView.resources.getDimension(R.dimen.page_link_card_layout_width_in_dual).toInt()
    }

    fun bind(page: PageLink) {
        val root = itemView.findViewById<ConstraintLayout>(R.id.page_link_card)
        val mangaRoot = itemView.findViewById<MaterialCardView>(R.id.manga_link_root)
        val mangaImage = itemView.findViewById<ImageView>(R.id.manga_link_image)
        val mangaNumber = itemView.findViewById<TextView>(R.id.manga_link_page_number)
        val mangaName = itemView.findViewById<TextView>(R.id.manga_link_page_name)
        val mangaProgress = itemView.findViewById<ProgressBar>(R.id.manga_link_progress_bar)

        val pageRoot = itemView.findViewById<MaterialCardView>(R.id.page_link_root)
        val pageImage = itemView.findViewById<ImageView>(R.id.page_link_image)
        val pageNumber = itemView.findViewById<TextView>(R.id.page_link_page_number)
        val pageName = itemView.findViewById<TextView>(R.id.page_link_page_name)
        val pageProgress = itemView.findViewById<ProgressBar>(R.id.page_link_progress_bar)

        val rootDualPageDrop = itemView.findViewById<View>(R.id.dual_page_link_drop_root)

        val dualPageRoot = itemView.findViewById<MaterialCardView>(R.id.dual_page_link_root)
        val dualPageImage = itemView.findViewById<ImageView>(R.id.dual_page_link_image)
        val dualPageNumber = itemView.findViewById<TextView>(R.id.dual_page_link_page_number)
        val dualPageName = itemView.findViewById<TextView>(R.id.dual_page_link_page_name)
        val dualProgress = itemView.findViewById<ProgressBar>(R.id.dual_page_progress_bar)

        root.setBackgroundColor(itemView.context.getColor(R.color.onPrimary))
        pageRoot.setOnClickListener { listener.onClick(page) }

        mangaNumber.text = page.mangaPage.toString()
        mangaName.text = page.mangaPageName

        if (page.imageMangaPage != null) {
            mangaImage.setImageBitmap(page.imageMangaPage)
            mangaImage.visibility = View.VISIBLE
            mangaProgress.visibility = View.GONE
        } else {
            mangaImage.visibility = View.GONE
            mangaProgress.visibility = View.VISIBLE
        }

        pageNumber.text = if (page.fileLinkPage >= 0) page.fileLinkPage.toString() else ""
        pageName.text = page.fileLinkPageName

        if (page.imageLeftFileLinkPage != null) {
            pageImage.setImageBitmap(page.imageLeftFileLinkPage)
            pageImage.visibility = View.VISIBLE
            pageProgress.visibility = View.GONE
            pageRoot.setOnLongClickListener { listener.onClickLong(it, page, Pages.LINKED) }
        } else {
            pageImage.visibility = View.GONE
            pageProgress.visibility = if (page.isFileLinkLoading) View.VISIBLE else View.GONE
            pageRoot.setOnLongClickListener(null)
        }

        dualPageNumber.text = if (page.fileRightLinkPage >= 0) page.fileRightLinkPage.toString() else ""
        dualPageName.text = page.fileRightLinkPageName
        dualPageRoot.layoutParams.width = mPageLinkCardWidthInDual

        if (page.dualImage) {
            pageRoot.layoutParams.width = mPageLinkCardWidthInDual
            dualPageRoot.visibility = View.VISIBLE
            dualPageImage.setImageBitmap(page.imageRightFileLinkPage)
            dualPageRoot.setOnLongClickListener { listener.onClickLong(it, page, Pages.DUAL_PAGE) }

            if (page.imageRightFileLinkPage == null) {
                dualPageImage.visibility = View.GONE
                dualProgress.visibility = View.VISIBLE
            } else {
                dualPageImage.visibility = View.VISIBLE
                dualProgress.visibility = View.GONE
            }
        } else {
            pageRoot.layoutParams.width = mPageLinkCardWidth
            dualPageRoot.visibility = View.GONE
            dualPageRoot.setOnClickListener(null)
        }

        root.setOnDragListener { view, dragEvent ->
            when(dragEvent.action) {
                DragEvent.ACTION_DRAG_ENTERED -> {
                    root.background = itemView.context.getDrawable(R.drawable.file_linked_background_selected)
                    if (!page.dualImage && page.fileLinkPage != -1) {
                        dualPageRoot.visibility = View.INVISIBLE
                        pageRoot.layoutParams.width = mPageLinkCardWidthInDual
                    } else if (page.dualImage)
                        pageRoot.layoutParams.width = mPageLinkCardWidthInDual
                    true
                }

                DragEvent.ACTION_DRAG_LOCATION -> {
                    val point = IntArray(2)
                    view.getLocationOnScreen(point)
                    listener.onDragScrolling(point)
                    true
                }

                DragEvent.ACTION_DRAG_EXITED  -> {
                    root.setBackgroundColor(itemView.context.getColor(R.color.fileLinkBackground))
                    pageRoot.layoutParams.width = mPageLinkCardWidth
                    if (!page.dualImage) dualPageRoot.visibility = View.GONE
                    true
                }

                DragEvent.ACTION_DROP -> {
                    root.setBackgroundColor(itemView.context.getColor(R.color.fileLinkBackground))
                    listener.onDropItem(
                        Pages.valueOf(dragEvent.clipData.getItemAt(1).text.toString()),
                        Pages.LINKED,
                        dragEvent.clipData.getItemAt(0).text.toString(),
                        page
                    )
                    true
                }

                else -> true
            }
        }

        pageRoot.setOnDragListener { view, dragEvent ->
            when(dragEvent.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    dragEvent.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
                }

                DragEvent.ACTION_DRAG_ENTERED -> {
                    root.background = itemView.context.getDrawable(R.drawable.file_linked_background_selected)
                    if (!page.dualImage && page.fileLinkPage != -1) {
                        dualPageRoot.visibility = View.INVISIBLE
                        pageRoot.layoutParams.width = mPageLinkCardWidthInDual
                    } else if (page.dualImage)
                        pageRoot.layoutParams.width = mPageLinkCardWidthInDual
                    true
                }

                DragEvent.ACTION_DRAG_LOCATION -> {
                    val point = IntArray(2)
                    view.getLocationOnScreen(point)
                    listener.onDragScrolling(point)
                    true
                }

                DragEvent.ACTION_DRAG_EXITED -> {
                    root.setBackgroundColor(itemView.context.getColor(R.color.fileLinkBackground))
                    pageRoot.layoutParams.width = mPageLinkCardWidth
                    if (!page.dualImage) dualPageRoot.visibility = View.GONE
                    true
                }

                DragEvent.ACTION_DROP -> {
                    listener.onDropItem(
                        Pages.valueOf(dragEvent.clipData.getItemAt(1).text.toString()),
                        Pages.LINKED,
                        dragEvent.clipData.getItemAt(0).text.toString(),
                        page
                    )
                    true
                }

                DragEvent.ACTION_DRAG_ENDED -> {
                    root.setBackgroundColor(itemView.context.getColor(R.color.fileLinkBackground))
                    val v = dragEvent.localState as View
                    if (!dragEvent.result || v.tag.toString().compareTo(PageLinkConsts.TAG.PAGE_LINK_RIGHT, true) != 0)
                        v.visibility = View.VISIBLE
                    true
                }

                else -> true
            }
        }

        rootDualPageDrop.setOnDragListener { _, dragEvent ->
            when(dragEvent.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    dragEvent.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
                }

                DragEvent.ACTION_DRAG_ENTERED -> {
                    rootDualPageDrop.background = itemView.context.getDrawable(R.drawable.file_linked_background_selected)
                    if (!page.dualImage && page.fileLinkPage != -1) {
                        dualPageRoot.visibility = View.INVISIBLE
                        pageRoot.layoutParams.width = mPageLinkCardWidthInDual
                    } else if (page.dualImage)
                        pageRoot.layoutParams.width = mPageLinkCardWidthInDual
                    true
                }

                DragEvent.ACTION_DRAG_EXITED -> {
                    rootDualPageDrop.setBackgroundColor(itemView.context.getColor(R.color.transparent))
                    pageRoot.layoutParams.width = mPageLinkCardWidth
                    if (!page.dualImage) dualPageRoot.visibility = View.GONE
                    true
                }

                DragEvent.ACTION_DROP -> {
                    listener.onDropItem(
                        Pages.valueOf(dragEvent.clipData.getItemAt(1).text.toString()),
                        Pages.DUAL_PAGE,
                        dragEvent.clipData.getItemAt(0).text.toString(),
                        page
                    )
                    true
                }

                DragEvent.ACTION_DRAG_ENDED -> {
                    rootDualPageDrop.setBackgroundColor(itemView.context.getColor(R.color.transparent))
                    val v = dragEvent.localState as View
                    if (!dragEvent.result || v.tag.toString().compareTo(PageLinkConsts.TAG.PAGE_LINK_RIGHT, true) != 0)
                        v.visibility = View.VISIBLE
                    true
                }

                else -> true
            }
        }
    }

}
package br.com.fenix.bilingualmangareader.view.adapter.page_link

import android.content.ClipDescription
import android.content.res.Configuration
import android.graphics.Point
import android.graphics.Rect
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
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.util.constants.PageLinkConsts
import com.google.android.material.card.MaterialCardView
import kotlin.math.roundToInt


class PageLinkViewHolder(itemView: View, private val listener: PageLinkCardListener) :
    RecyclerView.ViewHolder(itemView) {


    companion object {
        var mIsLandscape: Boolean = false
        var mIsTablet: Boolean = false
        var mPageLinkCardWidth: Int = 0
        var mPageLinkCardWidthInDual: Int = 0
        var mPageLinkRightSelectStroke: Int = 0
        var mUsePagePath = false
    }

    init {
        val root = itemView.findViewById<ConstraintLayout>(R.id.page_link_card)
        mIsLandscape = itemView.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        mIsTablet = if (root.tag != null) root.tag.toString().compareTo("tablet", true) == 0 else false

        mPageLinkCardWidth = when {
            mIsTablet ->  itemView.resources.getDimension(R.dimen.page_link_card_layout_width_tablet).toInt()
            mIsLandscape -> itemView.resources.getDimension(R.dimen.page_link_card_layout_width_land).toInt()
            else -> itemView.resources.getDimension(R.dimen.page_link_card_layout_width).toInt()
        }

        mPageLinkCardWidthInDual = when {
            mIsTablet ->  itemView.resources.getDimension(R.dimen.dual_page_link_card_layout_width_tablet).toInt()
            mIsLandscape -> itemView.resources.getDimension(R.dimen.dual_page_link_card_layout_width_land).toInt()
            else -> itemView.resources.getDimension(R.dimen.page_link_card_layout_width_in_dual).toInt()
        }

        mPageLinkRightSelectStroke  = itemView.resources.getDimension(R.dimen.dual_page_link_card_selected_stroke).toInt()

        mUsePagePath = GeneralConsts.getSharedPreferences().getBoolean(GeneralConsts.KEYS.PAGE_LINK.USE_PAGE_PATH_FOR_LINKED, false)
    }

    fun bind(page: PageLink, position: Int) {
        val root = itemView.findViewById<ConstraintLayout>(R.id.page_link_card)
        val mangaRoot = itemView.findViewById<MaterialCardView>(R.id.manga_link_root)
        val mangaImage = itemView.findViewById<ImageView>(R.id.manga_link_image)
        val mangaNumber = itemView.findViewById<TextView>(R.id.manga_link_page_number)
        val mangaName = itemView.findViewById<TextView>(R.id.manga_link_page_name)
        val mangaProgress = itemView.findViewById<ProgressBar>(R.id.manga_link_progress_bar)

        val pageDrop = itemView.findViewById<ConstraintLayout>(R.id.page_link_container)
        val pageRoot = itemView.findViewById<MaterialCardView>(R.id.page_link_root)
        val pageImage = itemView.findViewById<ImageView>(R.id.page_link_image)
        val pageNumber = itemView.findViewById<TextView>(R.id.page_link_page_number)
        val pageName = itemView.findViewById<TextView>(R.id.page_link_page_name)
        val pageProgress = itemView.findViewById<ProgressBar>(R.id.page_link_progress_bar)

        val dualPageRoot = itemView.findViewById<MaterialCardView>(R.id.dual_page_link_root)
        val dualPageImage = itemView.findViewById<ImageView>(R.id.dual_page_link_image)
        val dualPageNumber = itemView.findViewById<TextView>(R.id.dual_page_link_page_number)
        val dualPageName = itemView.findViewById<TextView>(R.id.dual_page_link_page_name)
        val dualProgress = itemView.findViewById<ProgressBar>(R.id.dual_page_progress_bar)

        root.setBackgroundColor(itemView.context.getColor(R.color.onPrimary))
        pageRoot.setOnClickListener { listener.onClick(page) }

        mangaNumber.text = page.mangaPage.toString()
        mangaName.text = if (mUsePagePath && page.mangaPage != PageLinkConsts.VALUES.PAGE_EMPTY)
            page.mangaPagePath + "\\" + page.mangaPageName
        else
            page.mangaPageName

        if (page.imageMangaPage != null) {
            mangaImage.setImageBitmap(page.imageMangaPage)
            mangaImage.visibility = View.VISIBLE
            mangaProgress.visibility = View.GONE
        } else {
            mangaImage.visibility = View.GONE
            mangaProgress.visibility = if (page.mangaPage != PageLinkConsts.VALUES.PAGE_EMPTY) View.VISIBLE else View.GONE
        }

        pageNumber.text = if (page.fileLinkPage != PageLinkConsts.VALUES.PAGE_EMPTY) page.fileLinkPage.toString() else ""
        pageName.text = if (mUsePagePath && page.fileLinkPage != PageLinkConsts.VALUES.PAGE_EMPTY)
            page.fileLinkPagePath + "\\" + page.fileLinkPageName
        else
            page.fileLinkPageName

        if (page.imageLeftFileLinkPage != null) {
            pageImage.setImageBitmap(page.imageLeftFileLinkPage)
            pageImage.visibility = View.VISIBLE
            pageProgress.visibility = View.GONE
            pageRoot.setOnLongClickListener { listener.onClickLong(it, page, Pages.LINKED, position) }
        } else {
            pageImage.visibility = View.GONE
            pageProgress.visibility = if (page.fileLinkPage != PageLinkConsts.VALUES.PAGE_EMPTY) View.VISIBLE else View.GONE
            pageRoot.setOnLongClickListener(null)
        }

        dualPageNumber.text = if (page.fileRightLinkPage != PageLinkConsts.VALUES.PAGE_EMPTY) page.fileRightLinkPage.toString() else ""
        dualPageRoot.layoutParams.width = mPageLinkCardWidthInDual

        dualPageName.text = if (mUsePagePath && page.fileRightLinkPage != PageLinkConsts.VALUES.PAGE_EMPTY)
            page.fileRightLinkPagePath + "\\" + page.fileRightLinkPageName
        else
            page.fileRightLinkPageName

        if (page.dualImage) {
            pageRoot.layoutParams.width = mPageLinkCardWidthInDual
            dualPageRoot.visibility = View.VISIBLE
            dualPageImage.setImageBitmap(page.imageRightFileLinkPage)
            dualPageRoot.setOnLongClickListener { listener.onClickLong(it, page, Pages.DUAL_PAGE, position) }

            if (page.imageRightFileLinkPage != null) {
                dualPageImage.visibility = View.VISIBLE
                dualProgress.visibility = View.GONE
            } else {
                dualPageImage.visibility = View.GONE
                dualProgress.visibility = if (page.fileRightLinkPage != PageLinkConsts.VALUES.PAGE_EMPTY) View.VISIBLE else View.GONE
            }
        } else {
            pageRoot.layoutParams.width = mPageLinkCardWidth
            dualPageRoot.visibility = View.GONE
            dualPageImage.visibility = View.GONE
            dualProgress.visibility = View.GONE
            dualPageRoot.setOnClickListener(null)
        }

        root.setOnDragListener { view, dragEvent ->
            when(dragEvent.action) {
                DragEvent.ACTION_DRAG_ENTERED -> {
                    root.background = itemView.context.getDrawable(R.drawable.file_linked_background_selected)
                    true
                }

                DragEvent.ACTION_DRAG_LOCATION -> {
                    val item = IntArray(2)
                    view.getLocationOnScreen(item)
                    val point = Point(item[0] + dragEvent.x.roundToInt(), item[1] + dragEvent.y.roundToInt())
                    listener.onDragScrolling(point)
                    true
                }

                DragEvent.ACTION_DRAG_EXITED -> {
                    root.setBackgroundColor(itemView.context.getColor(R.color.fileLinkBackground))
                    setSelectedPageLink(page, pageRoot, dualPageRoot, isClear = true)
                    true
                }

                DragEvent.ACTION_DROP -> {
                    root.setBackgroundColor(itemView.context.getColor(R.color.fileLinkBackground))
                    listener.onDropItem(
                        Pages.valueOf(dragEvent.clipData.getItemAt(PageLinkConsts.CLIPDATA.PAGE_TYPE).text.toString()),
                        Pages.LINKED,
                        dragEvent.clipData.getItemAt(PageLinkConsts.CLIPDATA.PAGE_LINK).text.toString(),
                        page
                    )
                    true
                }

                else -> true
            }
        }

        pageDrop.setOnDragListener { view, dragEvent ->
            when(dragEvent.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    dragEvent.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
                }

                DragEvent.ACTION_DRAG_ENTERED -> {
                    root.background = itemView.context.getDrawable(R.drawable.file_linked_background_selected)
                    setSelectedPageLink(page, pageRoot, dualPageRoot, position, dragEvent.clipDescription.label.toString().toInt())
                    true
                }

                DragEvent.ACTION_DRAG_LOCATION -> {
                    val item = Rect()
                    view.getGlobalVisibleRect(item)
                    val point = Point(item.left + dragEvent.x.roundToInt(), item.top + dragEvent.y.roundToInt())
                    listener.onDragScrolling(point)
                    true
                }

                DragEvent.ACTION_DRAG_EXITED -> {
                    root.setBackgroundColor(itemView.context.getColor(R.color.fileLinkBackground))
                    setSelectedPageLink(page, pageRoot, dualPageRoot, isClear = true)
                    true
                }

                DragEvent.ACTION_DROP -> {
                    listener.onDropItem(
                        Pages.valueOf(dragEvent.clipData.getItemAt(PageLinkConsts.CLIPDATA.PAGE_TYPE).text.toString()),
                        Pages.LINKED,
                        dragEvent.clipData.getItemAt(PageLinkConsts.CLIPDATA.PAGE_LINK).text.toString(),
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

        dualPageRoot.setOnDragListener { _, dragEvent ->
            when(dragEvent.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    dragEvent.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
                }

                DragEvent.ACTION_DRAG_ENTERED -> {
                    setSelectedPageLink(page, pageRoot, dualPageRoot, position, dragEvent.clipDescription.label.toString().toInt(), isDualPageDrop = true)
                    true
                }

                DragEvent.ACTION_DRAG_EXITED -> {
                    setSelectedPageLink(page, pageRoot, dualPageRoot, isClear = true, isDualPageDrop = true)
                    pageRoot.layoutParams.width = mPageLinkCardWidth
                    if (!page.dualImage) dualPageRoot.visibility = View.GONE
                    true
                }

                DragEvent.ACTION_DROP -> {
                    listener.onDropItem(
                        Pages.valueOf(dragEvent.clipData.getItemAt(PageLinkConsts.CLIPDATA.PAGE_TYPE).text.toString()),
                        Pages.DUAL_PAGE,
                        dragEvent.clipData.getItemAt(PageLinkConsts.CLIPDATA.PAGE_LINK).text.toString(),
                        page
                    )
                    true
                }

                DragEvent.ACTION_DRAG_ENDED -> {
                    val v = dragEvent.localState as View
                    if (!dragEvent.result || v.tag.toString().compareTo(PageLinkConsts.TAG.PAGE_LINK_RIGHT, true) != 0)
                        v.visibility = View.VISIBLE
                    true
                }

                else -> true
            }
        }
    }

    private fun setSelectedPageLink(page: PageLink, pageRoot: MaterialCardView, dualPageRoot: MaterialCardView, itemPosition: Int = -1,
                                    dragPosition : Int = 0, isClear: Boolean = false, isDualPageDrop: Boolean = false) {
        if(isClear || (itemPosition != -1 && itemPosition.compareTo(dragPosition) == 0 && !page.dualImage && page.fileLinkPage != PageLinkConsts.VALUES.PAGE_EMPTY)) {
            dualPageRoot.strokeWidth = 0
            pageRoot.layoutParams.width = if (page.dualImage)
                mPageLinkCardWidthInDual
            else {
                dualPageRoot.visibility = View.GONE
                mPageLinkCardWidth
            }
        } else {
            if(isDualPageDrop)
                dualPageRoot.strokeWidth = mPageLinkRightSelectStroke

            pageRoot.layoutParams.width = if (!page.dualImage && page.fileLinkPage != PageLinkConsts.VALUES.PAGE_EMPTY) {
                dualPageRoot.visibility = View.VISIBLE
                mPageLinkCardWidthInDual
            } else if (page.dualImage)
                mPageLinkCardWidthInDual
            else
                mPageLinkCardWidth

        }
    }

}
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
import br.com.fenix.bilingualmangareader.util.helpers.Util.Utils.getColorFromAttr
import com.google.android.material.card.MaterialCardView
import com.pedromassango.doubleclick.DoubleClick
import com.pedromassango.doubleclick.DoubleClickListener
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
        const val MIN_SWIPE_DISTANCE = -200
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

        mUsePagePath = GeneralConsts.getSharedPreferences(itemView.context).getBoolean(GeneralConsts.KEYS.PAGE_LINK.USE_PAGE_PATH_FOR_LINKED, false)
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

        root.setBackgroundColor(itemView.context.getColorFromAttr(R.attr.colorSurface))
        mangaRoot.setOnClickListener(getDoubleClick(mangaRoot, page, true))
        pageRoot.setOnClickListener(getDoubleClick(pageRoot, page))

        mangaName.background.alpha = 180
        pageName.background.alpha = 180
        dualPageName.background.alpha = 180

        mangaNumber.text = page.mangaPage.toString()
        mangaName.text = if (mUsePagePath && page.mangaPage != PageLinkConsts.VALUES.PAGE_EMPTY)
            page.mangaPagePath + "\\" + page.mangaPageName
        else
            page.mangaPageName

        mangaName.visibility = if (mangaName.text.isEmpty()) View.INVISIBLE else View.VISIBLE

        if (page.imageMangaPage != null) {
            mangaImage.setImageBitmap(page.imageMangaPage)
            mangaImage.visibility = View.VISIBLE
            mangaProgress.visibility = View.GONE
        } else {
            mangaImage.visibility = View.GONE
            mangaProgress.visibility = if (page.mangaPage != PageLinkConsts.VALUES.PAGE_EMPTY) View.VISIBLE else View.GONE
        }

        pageNumber.text = if (page.fileLinkLeftPage != PageLinkConsts.VALUES.PAGE_EMPTY) page.fileLinkLeftPage.toString() else ""
        pageName.text = if (mUsePagePath && page.fileLinkLeftPage != PageLinkConsts.VALUES.PAGE_EMPTY)
            page.fileLinkLeftPagePath + "\\" + page.fileLinkLeftPageName
        else
            page.fileLinkLeftPageName

        pageName.visibility = if (pageName.text.isEmpty()) View.INVISIBLE else View.VISIBLE

        if (page.imageLeftFileLinkPage != null) {
            pageImage.setImageBitmap(page.imageLeftFileLinkPage)
            pageImage.visibility = View.VISIBLE
            pageProgress.visibility = View.GONE
            pageRoot.setOnLongClickListener { listener.onClickLong(it, page, Pages.LINKED, position) }
        } else {
            pageImage.visibility = View.GONE
            pageProgress.visibility = if (page.fileLinkLeftPage != PageLinkConsts.VALUES.PAGE_EMPTY) View.VISIBLE else View.GONE
            pageRoot.setOnLongClickListener(null)
        }

        dualPageNumber.text = if (page.fileLinkRightPage != PageLinkConsts.VALUES.PAGE_EMPTY) page.fileLinkRightPage.toString() else ""
        dualPageRoot.layoutParams.width = mPageLinkCardWidthInDual

        dualPageName.text = if (mUsePagePath && page.fileLinkRightPage != PageLinkConsts.VALUES.PAGE_EMPTY)
            page.fileLinkRightPagePath + "\\" + page.fileLinkRightPageName
        else
            page.fileLinkRightPageName

        dualPageName.visibility = if (dualPageName.text.isEmpty()) View.INVISIBLE else View.VISIBLE

        if (page.isDualImage) {
            dualPageRoot.setOnClickListener(getDoubleClick(dualPageRoot, page, isRight = true))

            pageRoot.layoutParams.width = mPageLinkCardWidthInDual
            dualPageRoot.visibility = View.VISIBLE
            dualPageImage.setImageBitmap(page.imageRightFileLinkPage)
            dualPageRoot.setOnLongClickListener { listener.onClickLong(it, page, Pages.DUAL_PAGE, position) }

            if (page.imageRightFileLinkPage != null) {
                dualPageImage.visibility = View.VISIBLE
                dualProgress.visibility = View.GONE
            } else {
                dualPageImage.visibility = View.GONE
                dualProgress.visibility = if (page.fileLinkRightPage != PageLinkConsts.VALUES.PAGE_EMPTY) View.VISIBLE else View.GONE
            }
        } else {
            dualPageRoot.setOnClickListener { }
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
                    root.setBackgroundColor(itemView.context.getColorFromAttr(R.attr.colorOnSurfaceInverse))
                    setSelectedPageLink(page, pageRoot, dualPageRoot, isClear = true)
                    true
                }

                DragEvent.ACTION_DROP -> {
                    root.setBackgroundColor(itemView.context.getColorFromAttr(R.attr.colorOnSurfaceInverse))
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
                    root.setBackgroundColor(itemView.context.getColorFromAttr(R.attr.colorOnSurfaceInverse))
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
                    root.setBackgroundColor(itemView.context.getColorFromAttr(R.attr.colorOnSurfaceInverse))
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
                    if (!page.isDualImage) dualPageRoot.visibility = View.GONE
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
        if(isClear || (itemPosition != -1 && itemPosition.compareTo(dragPosition) == 0 && !page.isDualImage && page.fileLinkLeftPage != PageLinkConsts.VALUES.PAGE_EMPTY)) {
            dualPageRoot.strokeWidth = 0
            pageRoot.layoutParams.width = if (page.isDualImage)
                mPageLinkCardWidthInDual
            else {
                dualPageRoot.visibility = View.GONE
                mPageLinkCardWidth
            }
        } else {
            if(isDualPageDrop)
                dualPageRoot.strokeWidth = mPageLinkRightSelectStroke

            pageRoot.layoutParams.width = if (!page.isDualImage && page.fileLinkLeftPage != PageLinkConsts.VALUES.PAGE_EMPTY) {
                dualPageRoot.visibility = View.VISIBLE
                mPageLinkCardWidthInDual
            } else if (page.isDualImage)
                mPageLinkCardWidthInDual
            else
                mPageLinkCardWidth

        }
    }

    private fun getDoubleClick(root: View, page: PageLink, isManga : Boolean = false, isRight: Boolean = false) : DoubleClick {
        return DoubleClick(object : DoubleClickListener {
            override fun onSingleClick(view: View?) {
                listener.onClick(root, page, isManga, isRight)
            }
            override fun onDoubleClick(view: View?) {
                listener.onDoubleClick(root, page, isManga, isRight)
            }
        }, 500)
    }

}
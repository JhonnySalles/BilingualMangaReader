package br.com.fenix.bilingualmangareader.view.adapter.page_link

import android.content.ClipDescription
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
import com.google.android.material.card.MaterialCardView


class PageLinkViewHolder(itemView: View, private val listener: PageLinkCardListener) :
    RecyclerView.ViewHolder(itemView) {

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

        root.setBackgroundColor(itemView.context.getColor(R.color.onPrimary))
        pageRoot.setOnClickListener { listener.onClick(page) }
        pageRoot.setOnLongClickListener { true }

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

        if (page.imageFileLinkPage != null) {
            pageImage.setImageBitmap(page.imageFileLinkPage)
            pageImage.visibility = View.VISIBLE
            pageProgress.visibility = View.GONE
            pageRoot.setOnLongClickListener { listener.onClickLong(it, page, Pages.LINKED) }
        } else {
            pageImage.visibility = View.GONE
            pageProgress.visibility = if (page.isFileLinkLoading) View.VISIBLE else View.GONE
        }

        root.setOnDragListener { view, dragEvent ->
            when(dragEvent.action) {
                DragEvent.ACTION_DRAG_ENTERED -> {
                    root.background = itemView.context.getDrawable(R.drawable.file_linked_background_selected)
                    true
                }

                DragEvent.ACTION_DRAG_LOCATION -> {
                    val point = IntArray(2)
                    view.getLocationOnScreen(point)
                    listener.onDragScrolling(point)
                    true
                }

                DragEvent.ACTION_DRAG_EXITED, DragEvent.ACTION_DROP  -> {
                    root.setBackgroundColor(itemView.context.getColor(R.color.fileLinkBackground))
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
                    val v = dragEvent.localState as View
                    v.visibility = View.VISIBLE
                    true
                }

                DragEvent.ACTION_DRAG_ENDED -> {
                    val v = dragEvent.localState as View
                    v.visibility = View.VISIBLE
                    true
                }
                else -> true
            }
        }
    }

}
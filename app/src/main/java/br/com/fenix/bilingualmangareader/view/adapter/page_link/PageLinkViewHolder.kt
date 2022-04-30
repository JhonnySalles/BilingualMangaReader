package br.com.fenix.bilingualmangareader.view.adapter.page_link

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.PageLink
import br.com.fenix.bilingualmangareader.service.listener.PageLinkCardListener
import com.google.android.material.card.MaterialCardView


class PageLinkViewHolder(itemView: View, private val listener: PageLinkCardListener) :
    RecyclerView.ViewHolder(itemView) {

    fun bind(page: PageLink) {
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

        if (page.imageFileLinkPage != null) {
            pageImage.setImageBitmap(page.imageFileLinkPage)
            pageImage.visibility = View.VISIBLE
            pageProgress.visibility = View.GONE
        } else {
            pageImage.visibility = View.GONE
            pageProgress.visibility = if (page.isFileLinkLoading) View.VISIBLE else View.GONE
        }
    }

}
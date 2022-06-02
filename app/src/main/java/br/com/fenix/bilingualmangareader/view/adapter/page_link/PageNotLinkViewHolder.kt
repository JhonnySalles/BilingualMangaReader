package br.com.fenix.bilingualmangareader.view.adapter.page_link

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.PageLink
import br.com.fenix.bilingualmangareader.model.enums.Pages
import br.com.fenix.bilingualmangareader.service.listener.PageLinkCardListener
import com.google.android.material.card.MaterialCardView


class PageNotLinkViewHolder(itemView: View, private val listener: PageLinkCardListener) :
    RecyclerView.ViewHolder(itemView) {

    fun bind(page: PageLink) {
        val root = itemView.findViewById<MaterialCardView>(R.id.page_not_link_card)
        val image = itemView.findViewById<ImageView>(R.id.page_not_link_image)
        if (page.imageFileLinkPage != null)
            image.setImageBitmap(page.imageFileLinkPage)

        root.setOnLongClickListener { listener.onClickLong(it, page, Pages.NOT_LINKED) }
    }

}
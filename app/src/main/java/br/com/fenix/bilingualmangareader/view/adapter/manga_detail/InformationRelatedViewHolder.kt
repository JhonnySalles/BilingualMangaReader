package br.com.fenix.bilingualmangareader.view.adapter.manga_detail

import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Information
import br.com.fenix.bilingualmangareader.service.controller.ImageController
import br.com.fenix.bilingualmangareader.service.listener.InformationCardListener
import br.com.fenix.bilingualmangareader.util.helpers.Util

class InformationRelatedViewHolder(itemView: View, private val listener: InformationCardListener) : RecyclerView.ViewHolder(itemView) {

    fun bind(information: Information) {
        val image = itemView.findViewById<ImageView>(R.id.manga_detail_information_related_related_cover)
        val title = itemView.findViewById<TextView>(R.id.manga_detail_information_related_related_title)
        val alternativeTitles = itemView.findViewById<TextView>(R.id.manga_detail_information_related_alternative_titles)
        val status = itemView.findViewById<TextView>(R.id.manga_detail_information_related_status)
        val publish = itemView.findViewById<TextView>(R.id.manga_detail_information_related_publish)
        val volumes = itemView.findViewById<TextView>(R.id.manga_detail_information_related_volumes_chapters)
        val authors = itemView.findViewById<TextView>(R.id.manga_detail_information_related_author)
        val genres = itemView.findViewById<TextView>(R.id.manga_detail_information_related_genres)
        val card = itemView.findViewById<LinearLayout>(R.id.manga_detail_information_related_related_card)

        card.setOnLongClickListener {
            listener.onClickLong(information.link)
            true
        }

        image.setImageBitmap(null)
        image.visibility = View.GONE

        if (information.imageLink != null)
            ImageController.instance.setImageAsync(itemView.context, information.imageLink!!, image)

        title.text = information.title
        alternativeTitles.text = Html.fromHtml(Util.setBold(itemView.context.getString(R.string.manga_detail_information_alternative_titles)) + " " + information.alternativeTitles)
        status.text = Html.fromHtml(Util.setBold(itemView.context.getString(R.string.manga_detail_information_status)) + " " + information.status)
        publish.text = Html.fromHtml(
            itemView.context.getString(
                R.string.manga_detail_information_publish, Util.formatterDate(
                    itemView.context,
                    information.startDate
                ), Util.formatterDate(
                    itemView.context,
                    information.endDate
                )
            )
        )
        volumes.text = Html.fromHtml(
            Util.setBold(itemView.context.getString(R.string.manga_detail_information_volumes)) + " " + information.volumes + ", " + Util.setBold(
                itemView.context.getString(R.string.manga_detail_information_chapters)
            ) + " " + information.chapters
        )
        authors.text = Html.fromHtml(Util.setBold(itemView.context.getString(R.string.manga_detail_information_authors)) + " " + information.authors)
        genres.text = Html.fromHtml(Util.setBold(itemView.context.getString(R.string.manga_detail_information_genre)) + " " + information.genres)
    }

}
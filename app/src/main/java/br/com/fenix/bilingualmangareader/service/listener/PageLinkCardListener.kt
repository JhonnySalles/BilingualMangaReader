package br.com.fenix.bilingualmangareader.service.listener

import android.view.View
import br.com.fenix.bilingualmangareader.model.entity.PageLink
import br.com.fenix.bilingualmangareader.model.enums.Pages

interface PageLinkCardListener {
    fun onClick(page: PageLink)
    fun onClickLong(view : View, page: PageLink, origin : Pages, position: Int) : Boolean
    fun onDropItem(origin : Pages, destiny : Pages, dragIndex: String, drop: PageLink)
    fun onDragScrolling(pointScreen : IntArray)
}
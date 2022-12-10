package br.com.fenix.bilingualmangareader.service.listener

import android.graphics.Point
import android.view.View
import br.com.fenix.bilingualmangareader.model.entity.PageLink
import br.com.fenix.bilingualmangareader.model.enums.Pages

interface PageLinkCardListener {
    fun onClick(view: View, page: PageLink, isManga : Boolean, isRight: Boolean)
    fun onClickLong(view : View, page: PageLink, origin : Pages, position: Int) : Boolean
    fun onDoubleClick(view: View, page: PageLink, isManga : Boolean, isRight: Boolean)
    fun onDropItem(origin : Pages, destiny : Pages, dragIndex: String, drop: PageLink)
    fun onDragScrolling(pointScreen : Point)
    fun onAddNotLink(page: PageLink, isRight: Boolean)
}
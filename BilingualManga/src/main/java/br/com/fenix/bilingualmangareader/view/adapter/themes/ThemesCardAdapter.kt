package br.com.fenix.bilingualmangareader.view.adapter.themes

import android.annotation.SuppressLint
import android.content.Context
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.enums.Themes
import br.com.fenix.bilingualmangareader.service.listener.ThemesListener
import br.com.fenix.bilingualmangareader.util.helpers.Util
import com.google.android.material.card.MaterialCardView

class ThemesCardAdapter(var context: Context, list: MutableList<Pair<Themes, Boolean>>, listener: ThemesListener) : BaseAdapter() {

    companion object {
        var mPageSelectStroke: Int = 0
    }

    init {
        mPageSelectStroke = context.resources.getDimension(R.dimen.config_theme_selected_stroke).toInt()
    }

    private var mListener: ThemesListener = listener
    private var mList: MutableList<Pair<Themes, Boolean>> = list

    fun updateList(list: MutableList<Pair<Themes, Boolean>>) {
        mList = list
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return mList.size
    }

    override fun getItem(index: Int): Pair<Themes, Boolean> {
        return mList[index]
    }

    override fun getItemId(index: Int): Long {
        return index.toLong()
    }

    //cannot reuse the view, as it contains another theme and cannot apply the corresponding theme to an already created view,
    // performing the creation we guarantee that the colors will be the same as the theme in the list
    @SuppressLint("ViewHolder")
    override fun getView(index: Int, view: View?, parent: ViewGroup?): View {
        val theme = getItem(index)

        val newView = LayoutInflater.from(context)
                .cloneInContext(ContextThemeWrapper(context, theme.first.getValue()))
                .inflate(R.layout.grid_card_theme, parent, false)

        newView?.findViewById<MaterialCardView>(R.id.theme_card)?.strokeWidth = if (theme.second) mPageSelectStroke else 0
        newView?.findViewById<TextView>(R.id.theme_name)?.text = Util.themeDescription(context, theme.first)

        newView?.findViewById<LinearLayout>(R.id.theme_root)?.setOnClickListener {
            mListener.onClick(theme)
            true
        }

        return newView
    }

}
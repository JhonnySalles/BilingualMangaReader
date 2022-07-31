package br.com.fenix.bilingualmangareader.view.adapter.configuration

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Library
import br.com.fenix.bilingualmangareader.service.listener.LibrariesCardListener
import com.google.android.material.switchmaterial.SwitchMaterial

class LibrariesLineViewHolder(itemView: View, private val listener: LibrariesCardListener) :
    RecyclerView.ViewHolder(itemView) {

    fun bind(library: Library) {
        val container = itemView.findViewById<LinearLayout>(R.id.library_line_container)
        val title = itemView.findViewById<TextView>(R.id.library_line_text)
        val path = itemView.findViewById<TextView>(R.id.library_line_path)
        val switch = itemView.findViewById<SwitchMaterial>(R.id.library_line_switch_enabled)

        title.text = library.title
        path.text = library.path

        container.setOnLongClickListener {
            listener.onClickLong(library)
            true
        }

        switch.isChecked = library.enabled
        switch.setOnCheckedChangeListener { _, isChecked ->
            library.enabled = isChecked
            listener.changeEnable(library)
        }
    }

}
package br.com.fenix.bilingualmangareader.view.ui.pages_link

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import br.com.fenix.bilingualmangareader.R
import com.google.android.material.textfield.TextInputLayout

class PagesLinkFragment : Fragment() {

    private lateinit var mPagesLinked: RecyclerView
    private lateinit var mFileLink: TextInputLayout
    private lateinit var mFileLinkAutoComplete: AutoCompleteTextView
    private lateinit var mSave: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pages_link, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPagesLinked = view.findViewById(R.id.rv_pages_linked)
        mFileLink = view.findViewById(R.id.txt_file_link)
        mFileLinkAutoComplete = view.findViewById(R.id.menu_autocomplete_file_link)
        mSave = view.findViewById(R.id.btn_file_link_save)

    }

}
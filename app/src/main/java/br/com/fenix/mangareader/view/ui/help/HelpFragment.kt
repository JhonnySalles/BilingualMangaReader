package br.com.fenix.mangareader.view.ui.help

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import br.com.fenix.mangareader.R

class HelpFragment : Fragment() {

    private lateinit var mApplicationVersion: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_help, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mApplicationVersion = view.findViewById(R.id.help_app_version_number)
        mApplicationVersion.text = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0).versionName
    }
}
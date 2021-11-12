package br.com.fenix.mangareader.view.ui.help

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import br.com.fenix.mangareader.R

class AboutFragment : Fragment() {

    private lateinit var mApplicationVersion: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mApplicationVersion = view.findViewById(R.id.about_app_version_number)
        mApplicationVersion.text = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0).versionName

        view.findViewById<Button>(R.id.about_btn_rate_us).setOnClickListener {
            try {
                val uri = Uri.parse("market://details?id=" + requireActivity().getPackageName())
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            } catch (e : ActivityNotFoundException) {
                val uri = Uri.parse("http://play.google.com/store/apps/details?id=" + requireActivity().getPackageName())
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
        }

        view.findViewById<Button>(R.id.about_btn_shared).setOnClickListener {
            val shareBody = "Download Bilingual Manga Reader on Play Store: link"
            val shareSub = "Bilingual Manga Reader : Support to reading manga with vocabulary, meaning and kanji reading features."

            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_SUBJECT, shareSub)
            intent.putExtra(Intent.EXTRA_TEXT, shareBody)
            startActivity(intent)
        }

        view.findViewById<Button>(R.id.about_btn_suggestion).setOnClickListener {
            val address = requireContext().getString(R.string.about_app_mail_address)
            val subject = requireContext().getString(R.string.about_app_suggestion)
            val message = ""

            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            intent.putExtra(Intent.EXTRA_EMAIL, address)
            intent.putExtra(Intent.EXTRA_SUBJECT, subject)
            intent.putExtra(Intent.EXTRA_TEXT, message)

            if (intent.resolveActivity(requireContext().packageManager) != null)
                startActivity(intent)
            else
                Toast.makeText(context,
                    context?.getString(R.string.action_app_not_installed),
                    Toast.LENGTH_SHORT
                ).show()
        }

        view.findViewById<Button>(R.id.about_btn_email).setOnClickListener {
            val address = requireContext().getString(R.string.about_app_mail_address)
            val subject = ""
            val message = ""

            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            intent.putExtra(Intent.EXTRA_EMAIL, address)
            intent.putExtra(Intent.EXTRA_SUBJECT, subject)
            intent.putExtra(Intent.EXTRA_TEXT, message)

            if (intent.resolveActivity(requireContext().packageManager) != null)
                startActivity(intent)
            else
                Toast.makeText(context,
                    context?.getString(R.string.action_app_not_installed),
                    Toast.LENGTH_SHORT
                ).show()
        }

        view.findViewById<Button>(R.id.about_btn_github).setOnClickListener {
            val uri = Uri.parse(requireContext().getString(R.string.about_app_github_link))
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        val library = resources.getStringArray(R.array.about_app_library_content)
        val txtLibrary = view.findViewById<TextView>(R.id.about_app_library)
        txtLibrary.movementMethod = LinkMovementMethod.getInstance()
        txtLibrary.text = library.joinToString("\r\n")
    }
}
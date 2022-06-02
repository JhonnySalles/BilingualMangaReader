package br.com.fenix.bilingualmangareader.view.ui.pages_link

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts


class PagesLinkActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pages_link)
        val extras = intent.extras
        val fragment = PagesLinkFragment()

        if (extras != null) {
            val bundle = Bundle()
            bundle.putSerializable(GeneralConsts.KEYS.OBJECT.MANGA, extras.getSerializable(GeneralConsts.KEYS.OBJECT.MANGA) as Manga)
            fragment.arguments = bundle
        }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.root_frame_pages_link, fragment)
            .commit()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {

        }
    }

}
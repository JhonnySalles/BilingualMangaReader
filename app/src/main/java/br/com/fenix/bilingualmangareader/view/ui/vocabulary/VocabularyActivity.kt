package br.com.fenix.bilingualmangareader.view.ui.vocabulary

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Library
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.model.enums.Themes
import br.com.fenix.bilingualmangareader.service.controller.ImageCoverController
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.util.helpers.LibraryUtil
import br.com.fenix.bilingualmangareader.util.helpers.MenuUtil


class VocabularyActivity : AppCompatActivity() {

    private lateinit var mBackgroundImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        val theme = Themes.valueOf(GeneralConsts.getSharedPreferences(this).getString(GeneralConsts.KEYS.THEME.THEME_USED, Themes.ORIGINAL.toString())!!)
        setTheme(theme.getValue())

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vocabulary)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_vocabulary)
        MenuUtil.tintToolbar(toolbar, theme)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        val bundle: Bundle? = intent.extras
        val manga = if (bundle != null && bundle.containsKey(GeneralConsts.KEYS.OBJECT.MANGA))
            bundle[GeneralConsts.KEYS.OBJECT.MANGA] as Manga
        else
            null

        mBackgroundImage = findViewById(R.id.vocabulary_background_image)

        if (manga != null)
            ImageCoverController.instance.setImageCoverAsync(this, manga, arrayListOf(mBackgroundImage), false)
        else
            mBackgroundImage.setImageBitmap(null)

        val fragment = VocabularyFragment()
        fragment.mManga = manga

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.root_frame_vocabulary, fragment)
            .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                supportFinishAfterTransition()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent()
        setResult(RESULT_OK, intent)
        supportFinishAfterTransition()
    }

}
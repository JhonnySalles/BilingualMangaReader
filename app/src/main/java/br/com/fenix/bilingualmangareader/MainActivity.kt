package br.com.fenix.bilingualmangareader

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import br.com.fenix.bilingualmangareader.model.entity.Library
import br.com.fenix.bilingualmangareader.service.listener.MainListener
import br.com.fenix.bilingualmangareader.service.repository.LibraryRepository
import br.com.fenix.bilingualmangareader.service.scanner.Scanner
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.util.helpers.LibraryUtil
import br.com.fenix.bilingualmangareader.util.helpers.MsgUtil
import br.com.fenix.bilingualmangareader.view.ui.about.AboutFragment
import br.com.fenix.bilingualmangareader.view.ui.configuration.ConfigFragment
import br.com.fenix.bilingualmangareader.view.ui.help.HelpFragment
import br.com.fenix.bilingualmangareader.view.ui.history.HistoryFragment
import br.com.fenix.bilingualmangareader.view.ui.library.LibraryFragment
import br.com.fenix.bilingualmangareader.view.ui.library.LibraryViewModel
import br.com.fenix.bilingualmangareader.view.ui.vocabulary.VocabularyFragment
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.io.File


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, MainListener {

    private val mLOGGER = LoggerFactory.getLogger(MainActivity::class.java)

    private lateinit var mLibraryModel: LibraryViewModel
    private lateinit var mToolBar: Toolbar
    private lateinit var mFragmentManager: FragmentManager
    private lateinit var mNavigationView: NavigationView
    private lateinit var mMenu: Menu
    private lateinit var mToggle: ActionBarDrawerToggle
    private lateinit var mDrawer: DrawerLayout

    private val mDefaultUncaughtHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            mLOGGER.error("*** CRASH APP *** ", e)
            mDefaultUncaughtHandler?.uncaughtException(t, e)
        }

        clearCache()

        setContentView(R.layout.activity_main)

        mToolBar = findViewById(R.id.main_toolbar)
        setSupportActionBar(mToolBar)

        // drawer_Layout is a default layout from app
        mDrawer = findViewById(R.id.drawer_layout)
        mToggle = ActionBarDrawerToggle(
            this,
            mDrawer,
            mToolBar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        mDrawer.addDrawerListener(mToggle)
        mToggle.syncState()

        // nav_view have a menu layout
        mNavigationView = findViewById(R.id.nav_view)
        mNavigationView.setNavigationItemSelectedListener(this)

        mLibraryModel = ViewModelProvider(this).get(LibraryViewModel::class.java)
        mLibraryModel.setDefaultLibrary(LibraryUtil.getDefault(this))

        mFragmentManager = supportFragmentManager

        // content_fragment use for receive fragments layout
        mFragmentManager.beginTransaction().replace(R.id.main_content_root, LibraryFragment())
            .commit()

        libraries()
    }

    private fun clearCache() {
        val cacheDir = GeneralConsts.getCacheDir(this)
        CoroutineScope(Dispatchers.IO).launch {
            async {
                try {
                    val rar = File(cacheDir, GeneralConsts.CACHE_FOLDER.RAR)
                    if (rar.exists())
                        rar.listFiles()?.let {
                            for (f in it)
                                f.delete()
                        }

                    val images = File(cacheDir, GeneralConsts.CACHE_FOLDER.IMAGE)
                    if (images.exists())
                        images.listFiles()?.let {
                            for (f in it)
                                f.delete()
                        }
                } catch (e: Exception) {
                    mLOGGER.error("Error clearing cache folders.", e)
                }
            }
        }
    }

    private fun libraries() {
        try {
            val repository = LibraryRepository(this)
            val libraries = repository.listEnabled()
            if (libraries.isNotEmpty()) {
                setLibraries(libraries)
                Scanner(this).scanLibrariesSilent(libraries)
            }
        } catch (e: Exception) {
            mLOGGER.error("Error clearing cache folders.", e)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        mMenu = mNavigationView.menu

        val fragment = supportFragmentManager.findFragmentById(item.itemId)
        val newFragment = fragment ?: when (item.itemId) {
            R.id.menu_library_default -> {
                mLibraryModel.setLibrary(LibraryUtil.getDefault(this))
                LibraryFragment()
            }
            R.id.menu_configuration -> ConfigFragment()
            R.id.menu_help -> HelpFragment()
            R.id.menu_about -> AboutFragment()
            R.id.menu_history -> HistoryFragment()
            R.id.menu_vocabulary -> VocabularyFragment()
            in GeneralConsts.KEYS.LIBRARIES.INDEX_LIBRARIES..(GeneralConsts.KEYS.LIBRARIES.INDEX_LIBRARIES + mLibraries.size) -> {
                val index = item.itemId - GeneralConsts.KEYS.LIBRARIES.INDEX_LIBRARIES
                val library = LibraryFragment()
                mLibraryModel.setLibrary(mLibraries[index])
                library
            }
            else -> null
        }

        if (newFragment != null)
            openFragment(newFragment)

        mDrawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == GeneralConsts.REQUEST.PERMISSION_FILES_ACCESS) {
            MsgUtil.validPermission(this, grantResults)
        }
    }

    override fun showUpButton() {
        //mDrawer.removeDrawerListener(mToggle)
        //supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    override fun hideUpButton() {
        //Achar uma forma de habilitar e desabilitar o menu principal
        //mDrawer.addDrawerListener(mToggle)
        //supportActionBar?.setDisplayHomeAsUpEnabled(false)
        //supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun changeLibraryTitle(library: String) {
        mToolBar.title = library
    }

    override fun clearLibraryTitle() {
        mToolBar.title = getString(R.string.app_name)
    }

    private fun openFragment(fragment: Fragment) {
        mFragmentManager.beginTransaction().setCustomAnimations(
            R.anim.slide_fragment_add_enter,
            R.anim.slide_fragment_add_exit,
            R.anim.slide_fragment_remove_enter,
            R.anim.slide_fragment_remove_exit
        )
            .replace(R.id.main_content_root, fragment)
            .addToBackStack(null)
            .commit()
    }

    private lateinit var mLibraries: List<Library>
    fun setLibraries(libraries: List<Library>) {
        val menu = mNavigationView.menu
        val submenu: Menu = menu.findItem(R.id.menu_library_content).subMenu

        if (!::mLibraries.isInitialized)
            mLibraries = libraries

        for ((index, _) in mLibraries.withIndex())
            submenu.removeItem(GeneralConsts.KEYS.LIBRARIES.INDEX_LIBRARIES + index)

        for ((index, library) in libraries.withIndex())
            submenu.add(0, GeneralConsts.KEYS.LIBRARIES.INDEX_LIBRARIES + index, 0, library.title).apply { setIcon(R.drawable.ic_library) }

        mLibraries = libraries
        mNavigationView.invalidate()
    }

}
package br.com.fenix.bilingualmangareader

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import br.com.fenix.bilingualmangareader.service.ocr.Tesseract
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.view.ui.configuration.ConfigFragment
import br.com.fenix.bilingualmangareader.view.ui.help.AboutFragment
import br.com.fenix.bilingualmangareader.view.ui.help.HelpFragment
import br.com.fenix.bilingualmangareader.view.ui.history.HistoryFragment
import br.com.fenix.bilingualmangareader.view.ui.library.LibraryFragment
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.io.File


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val mLOGGER = LoggerFactory.getLogger(MainActivity::class.java)

    private lateinit var mToolBar: Toolbar
    private lateinit var mFragmentManager: FragmentManager
    private lateinit var mNavigationView: NavigationView
    private lateinit var mMenu: Menu

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
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this,
            drawer,
            mToolBar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        // nav_view have a menu layout
        mNavigationView = findViewById(R.id.nav_view)
        mNavigationView.setNavigationItemSelectedListener(this)

        mFragmentManager = supportFragmentManager

        // content_fragment use for receive fragments layout
        mFragmentManager.beginTransaction().replace(R.id.content_root, LibraryFragment())
            .commit()
    }

    private fun clearCache() {
        val cacheDir = GeneralConsts.getCacheDir(this)
        CoroutineScope(Dispatchers.IO).launch {
            async {
                try {
                    val rar = File(cacheDir, GeneralConsts.CACHEFOLDER.RAR)
                    if (rar.exists())
                        for (f in rar.listFiles()!!)
                            f.delete()

                    val images = File(cacheDir, GeneralConsts.CACHEFOLDER.IMAGE)

                    if (images.exists())
                        for (f in rar.listFiles()!!)
                            f.delete()

                    val linked = File(cacheDir, GeneralConsts.CACHEFOLDER.LINKED)

                    if (linked.exists())
                        for (f in rar.listFiles()!!)
                            f.delete()
                } catch (e : Exception) {
                    mLOGGER.error("Error clearing cache folders.", e)
                }
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        mMenu = mNavigationView.menu
        when (item.itemId) {
            R.id.menu_library -> mFragmentManager.beginTransaction()
                .replace(R.id.content_root, LibraryFragment())
                .commit()
            R.id.menu_configuration -> mFragmentManager.beginTransaction()
                .replace(R.id.content_root, ConfigFragment()).commit()
            R.id.menu_help -> mFragmentManager.beginTransaction()
                .replace(R.id.content_root, HelpFragment())
                .commit()
            R.id.menu_about -> mFragmentManager.beginTransaction()
                .replace(R.id.content_root, AboutFragment())
                .commit()
            R.id.menu_history -> mFragmentManager.beginTransaction()
                .replace(R.id.content_root, HistoryFragment())
                .commit()
        }
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }
}
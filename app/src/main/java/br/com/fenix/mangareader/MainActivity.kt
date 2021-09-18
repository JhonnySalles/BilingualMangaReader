package br.com.fenix.mangareader

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
import br.com.fenix.mangareader.view.ui.configuration.ConfigFragment
import br.com.fenix.mangareader.view.ui.help.HelpFragment
import br.com.fenix.mangareader.view.ui.history.HistoryFragment
import br.com.fenix.mangareader.view.ui.library.LibraryFragment
import com.google.android.material.navigation.NavigationView


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var toolBar: Toolbar
    private lateinit var fragmentManager: FragmentManager
    private lateinit var navigationView: NavigationView
    private lateinit var menu: Menu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolBar = findViewById(R.id.main_toolbar)
        setSupportActionBar(toolBar)

        // drawer_Layout é o layout padrão do aplicativo
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this,
            drawer,
            toolBar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        // nav_view contém o layout do menu
        navigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        fragmentManager = supportFragmentManager

        // content_fragment usado para receber os layouts dos fragmentos
        fragmentManager.beginTransaction().replace(R.id.content_root, LibraryFragment())
            .commit()
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        mContext = context
        return super.onCreateView(name, context, attrs)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        menu = navigationView.menu
        when (item.itemId) {
            R.id.menu_library -> fragmentManager.beginTransaction()
                .replace(R.id.content_root, LibraryFragment())
                .commit()
            R.id.menu_configuration -> fragmentManager.beginTransaction()
                .replace(R.id.content_root, ConfigFragment()).commit()
            R.id.menu_help -> fragmentManager.beginTransaction()
                .replace(R.id.content_root, HelpFragment())
                .commit()
            R.id.menu_history -> fragmentManager.beginTransaction()
                .replace(R.id.content_root, HistoryFragment())
                .commit()
        }
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    companion object {
        private lateinit var mContext : Context
        fun getAppContext() = mContext
    }
}
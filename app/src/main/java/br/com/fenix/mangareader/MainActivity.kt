package br.com.fenix.mangareader

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import br.com.fenix.mangareader.fragment.ConfigFragment
import br.com.fenix.mangareader.fragment.HelpFragment
import br.com.fenix.mangareader.fragment.HistoryFragment
import br.com.fenix.mangareader.fragment.LibraryFragment
import com.google.android.material.navigation.NavigationView


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var toolbar: Toolbar;
    private lateinit var fragmentManager: FragmentManager
    private lateinit var navigationView: NavigationView;
    private lateinit var menu: Menu;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        // drawer_Layout é o layout padrão do aplicativo
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this,
            drawer,
            toolbar,
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.main_menu, menu);
        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId

        // TODO: opter ID para a opção selecionada no MENU DRAWER
        if (id == R.id.menu_library) {
            menu = navigationView.menu
            fragmentManager.beginTransaction().replace(R.id.content_root, LibraryFragment())
                .commit()
        } else if (id == R.id.menu_configuration) {
            menu = navigationView.menu
            fragmentManager.beginTransaction()
                .replace(R.id.content_root, ConfigFragment()).commit()
        } else if (id == R.id.menu_help) {
            menu = navigationView.menu
            fragmentManager.beginTransaction().replace(R.id.content_root, HelpFragment())
                .commit()
        } else if (id == R.id.menu_history) {
            menu = navigationView.menu
            fragmentManager.beginTransaction().replace(R.id.content_root, HistoryFragment())
                .commit()
        }

        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }
}
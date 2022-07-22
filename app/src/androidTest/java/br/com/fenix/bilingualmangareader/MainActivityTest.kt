package br.com.fenix.bilingualmangareader

import android.content.res.Resources
import android.view.Gravity
import android.view.KeyEvent
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.contrib.DrawerMatchers.isClosed
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.fenix.bilingualmangareader.view.ui.about.AboutFragment
import br.com.fenix.bilingualmangareader.view.ui.configuration.ConfigFragment
import br.com.fenix.bilingualmangareader.view.ui.help.HelpFragment
import br.com.fenix.bilingualmangareader.view.ui.history.HistoryFragment
import br.com.fenix.bilingualmangareader.view.ui.library.LibraryFragment
import junit.framework.TestCase.assertTrue
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    var activityScenarioRule: ActivityScenario<MainActivity> = ActivityScenario.launch(MainActivity::class.java)

    private val animationTimer = 600L

    @Test
    fun `1_open_screens_in_app`() {
        val waiter = CountDownLatch(1)

        // History
        onView(withId(R.id.drawer_layout))
            .check(matches(isClosed(Gravity.START)))
            .perform(DrawerActions.open())

        onView(withId(R.id.nav_view))
            .perform(NavigationViewActions.navigateTo(R.id.menu_history))

        waiter.await(animationTimer, TimeUnit.MILLISECONDS)

        activityScenarioRule.onActivity {
            val fragment = it.supportFragmentManager.findFragmentById(R.id.main_content_root)
            assertTrue(fragment is HistoryFragment)
        }

        // Configuration
        onView(withId(R.id.drawer_layout))
            .check(matches(isClosed(Gravity.START)))
            .perform(DrawerActions.open())

        onView(withId(R.id.nav_view))
            .perform(NavigationViewActions.navigateTo(R.id.menu_configuration))

        waiter.await(animationTimer, TimeUnit.MILLISECONDS)

        activityScenarioRule.onActivity {
            val fragment = it.supportFragmentManager.findFragmentById(R.id.main_content_root)
            assertTrue(fragment is ConfigFragment)
        }

        // Help
        onView(withId(R.id.drawer_layout))
            .check(matches(isClosed(Gravity.START)))
            .perform(DrawerActions.open())

        onView(withId(R.id.nav_view))
            .perform(NavigationViewActions.navigateTo(R.id.menu_help))

        waiter.await(animationTimer, TimeUnit.MILLISECONDS)

        activityScenarioRule.onActivity {
            val fragment = it.supportFragmentManager.findFragmentById(R.id.main_content_root)
            assertTrue(fragment is HelpFragment)
        }

        // About
        onView(withId(R.id.drawer_layout))
            .check(matches(isClosed(Gravity.START)))
            .perform(DrawerActions.open())

        onView(withId(R.id.nav_view))
            .perform(NavigationViewActions.navigateTo(R.id.menu_about))

        waiter.await(animationTimer, TimeUnit.MILLISECONDS)

        activityScenarioRule.onActivity {
            val fragment = it.supportFragmentManager.findFragmentById(R.id.main_content_root)
            assertTrue(fragment is AboutFragment)
        }

        // Library
        onView(withId(R.id.drawer_layout))
            .check(matches(isClosed(Gravity.START)))
            .perform(DrawerActions.open())

        onView(withId(R.id.nav_view))
            .perform(NavigationViewActions.navigateTo(R.id.menu_library))

        waiter.await(animationTimer, TimeUnit.MILLISECONDS)

        activityScenarioRule.onActivity {
            val fragment = it.supportFragmentManager.findFragmentById(R.id.main_content_root)
            assertTrue(fragment is LibraryFragment)
        }

        pressBack()

        waiter.await(animationTimer, TimeUnit.MILLISECONDS)

        activityScenarioRule.onActivity {
            val fragment = it.supportFragmentManager.findFragmentById(R.id.main_content_root)
            assertTrue(fragment is AboutFragment)
        }

        pressBack()

        waiter.await(animationTimer, TimeUnit.MILLISECONDS)

        activityScenarioRule.onActivity {
            val fragment = it.supportFragmentManager.findFragmentById(R.id.main_content_root)
            assertTrue(fragment is HelpFragment)
        }

        pressBack()

        waiter.await(animationTimer, TimeUnit.MILLISECONDS)

        activityScenarioRule.onActivity {
            val fragment = it.supportFragmentManager.findFragmentById(R.id.main_content_root)
            assertTrue(fragment is ConfigFragment)
        }

        pressBack()

        waiter.await(animationTimer, TimeUnit.MILLISECONDS)

        activityScenarioRule.onActivity {
            val fragment = it.supportFragmentManager.findFragmentById(R.id.main_content_root)
            assertTrue(fragment is HistoryFragment)
        }

        pressBack()

        waiter.await(animationTimer, TimeUnit.MILLISECONDS)

        activityScenarioRule.onActivity {
            val fragment = it.supportFragmentManager.findFragmentById(R.id.main_content_root)
            assertTrue(fragment is LibraryFragment)
        }
    }


    @Test
    fun `2_test_in_library`() {
        val waiter = CountDownLatch(1)
        activityScenarioRule.recreate() // Re load app

        waiter.await(animationTimer, TimeUnit.MILLISECONDS)

        onView(withId(R.id.menu_library_grid_type)).perform(click())

        waiter.await(animationTimer, TimeUnit.MILLISECONDS)

        onView(withId(R.id.menu_library_grid_type)).perform(click())

        waiter.await(animationTimer, TimeUnit.MILLISECONDS)

        onView(withId(R.id.menu_library_grid_type)).perform(click())

        waiter.await(animationTimer, TimeUnit.MILLISECONDS)

        onView(withId(R.id.menu_library_list_order)).perform(click())

        waiter.await(animationTimer, TimeUnit.MILLISECONDS)

        onView(withId(R.id.menu_library_list_order)).perform(click())

        waiter.await(animationTimer, TimeUnit.MILLISECONDS)

        onView(withId(R.id.menu_library_list_order)).perform(click())

        waiter.await(animationTimer, TimeUnit.MILLISECONDS)

        activityScenarioRule.onActivity {
            val recyclerView = it.findViewById<RecyclerView>(R.id.library_recycler_view)
            val itemCount = recyclerView.adapter?.itemCount ?: 0

            recyclerView.smoothScrollToPosition(itemCount)

            waiter.await(2, TimeUnit.SECONDS)

            recyclerView.smoothScrollToPosition(0)

            waiter.await(2, TimeUnit.SECONDS)
        }

        onView(withId(R.id.menu_library_search)).perform(click())

        onView(
            withId(
                Resources.getSystem().getIdentifier(
                    "search_src_text",
                    "id", "android"
                )
            )
        ).perform(clearText(), typeText("Test"))
            .perform(pressKey(KeyEvent.KEYCODE_ENTER))

        onView(
            withId(
                Resources.getSystem().getIdentifier(
                    "search_src_text",
                    "id", "android"
                )
            )
        ).perform(clearText())
            .perform(pressKey(KeyEvent.KEYCODE_ENTER))

        onView(
            withId(
                Resources.getSystem().getIdentifier(
                    "search_close_btn",
                    "id", "android"
                )
            )
        ).perform(click())

        onView(
            withId(
                Resources.getSystem().getIdentifier(
                    "search_close_btn",
                    "id", "android"
                )
            )
        ).perform(closeSoftKeyboard())
    }


}
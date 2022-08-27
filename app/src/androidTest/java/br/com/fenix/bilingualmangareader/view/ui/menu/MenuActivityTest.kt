package br.com.fenix.bilingualmangareader.view.ui.menu

import android.content.Intent
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import junit.framework.TestCase
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import java.util.concurrent.CountDownLatch

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4::class)
class MenuActivityTest {

    private var intent: Intent? = null

    init {
        intent = Intent(ApplicationProvider.getApplicationContext(), MenuActivity::class.java)

        val bundle = Bundle()
        bundle.putInt(GeneralConsts.KEYS.FRAGMENT.ID, R.id.frame_select_manga)
        intent?.putExtras(bundle)
    }


    @get:Rule
    val activityScenarioRule = ActivityScenarioRule<MenuActivity>(intent)

    private val awaitProcessSeconds = 2L

    @Test
    fun `1_test_select_manga`() {
        val waiter = CountDownLatch(1)
        val scenario = activityScenarioRule.scenario

        scenario.onActivity {
            val fragment = it.supportFragmentManager.findFragmentById(R.id.root_frame_menu)
            TestCase.assertTrue(fragment is SelectMangaFragment)
        }

        waiter.await()
    }
}
package br.com.fenix.bilingualmangareader.view.ui.reader

import android.content.Intent
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.custom.CustomTypes
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.utils.TestUtils
import junit.framework.TestCase.assertFalse
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
class ReaderActivityTest {

    // Inform a file test here
    private val filePath = "/storage/emulated/0/Manga/jp/Aho Girl - Volume 01 (Jap).cbr" // "storage/emulated/0/Manga/Manga of test.cbr"
    private val manga: Manga = TestUtils.getManga(ApplicationProvider.getApplicationContext(), filePath)
    private var intent: Intent? = null

    init {
        assertFalse("Not informed comic file, please declare 'filePath' in ReaderActivityTest", filePath.isEmpty())
        assertTrue("Comic file informed not found, please verify declared 'filePath' in ReaderActivityTest", manga.file.exists())

        intent = Intent(ApplicationProvider.getApplicationContext(), ReaderActivity::class.java)

        val bundle = Bundle()
        bundle.putSerializable(GeneralConsts.KEYS.OBJECT.MANGA, manga)
        bundle.putString(GeneralConsts.KEYS.MANGA.NAME, manga.title)
        bundle.putInt(GeneralConsts.KEYS.MANGA.PAGE_NUMBER, 0)
        bundle.putInt(GeneralConsts.KEYS.MANGA.MARK, 0)
        intent?.putExtras(bundle)
    }


    @get:Rule
    val activityScenarioRule = ActivityScenarioRule<ReaderActivity>(intent)

    private val awaitProcessSeconds = 2L

    @Test
    fun `1_test_manga_detail`() {
        val waiter = CountDownLatch(1)
        val scenario = activityScenarioRule.scenario

        scenario.onActivity {
            val fragment = it.supportFragmentManager.findFragmentById(R.id.root_frame_reader)
            assertTrue(fragment is ReaderFragment)
        }

        waiter.await(awaitProcessSeconds, TimeUnit.SECONDS)
        onView(withId(R.id.root_frame_reader)).perform(click())

        waiter.await(awaitProcessSeconds, TimeUnit.SECONDS)
        onView(withId(R.id.btn_popup_open_floating)).perform(click())

        waiter.await(awaitProcessSeconds, TimeUnit.SECONDS)
        onView(withId(R.id.root_frame_reader)).perform(click())

        waiter.await(awaitProcessSeconds, TimeUnit.SECONDS)
        onView(withId(R.id.btn_menu_page_linked)).perform(click())

        waiter.await(awaitProcessSeconds, TimeUnit.SECONDS)
        onView(withId(R.id.root_frame_reader)).perform(click())

        waiter.await(awaitProcessSeconds, TimeUnit.SECONDS)
        onView(withId(R.id.btn_popup_color)).perform(click())

        waiter.await(awaitProcessSeconds, TimeUnit.SECONDS)
        onView(withId(R.id.root_frame_reader)).perform(click())

        waiter.await(awaitProcessSeconds, TimeUnit.SECONDS)
        onView(withId(R.id.btn_popup_subtitle)).perform(click())

        waiter.await(awaitProcessSeconds, TimeUnit.SECONDS)
        onView(withId(R.id.root_frame_reader)).perform(click())

        waiter.await(awaitProcessSeconds, TimeUnit.SECONDS)
        onView(withId(R.id.btn_menu_ocr)).perform(click())

        waiter.await(awaitProcessSeconds, TimeUnit.SECONDS)
        onView(withId(R.id.root_frame_reader)).perform(click())

        waiter.await(awaitProcessSeconds, TimeUnit.SECONDS)
        onView(withId(R.id.btn_screen_rotate)).perform(click())

        waiter.await(awaitProcessSeconds, TimeUnit.SECONDS)
        onView(withId(R.id.root_frame_reader)).perform(click())

        waiter.await(awaitProcessSeconds, TimeUnit.SECONDS)
        onView(withId(R.id.nav_next_file)).perform(click())

        onView(withId(CustomTypes.AlertDialogButton.POSITIVE.resId)).perform(click())

        waiter.await(awaitProcessSeconds, TimeUnit.SECONDS)
        onView(withId(R.id.root_frame_reader)).perform(click())

        waiter.await(awaitProcessSeconds, TimeUnit.SECONDS)
        onView(withId(R.id.nav_previous_file)).perform(click())

        onView(withId(CustomTypes.AlertDialogButton.POSITIVE.resId)).perform(click())

    }

}
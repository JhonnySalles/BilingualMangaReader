package br.com.fenix.bilingualmangareader.view.ui.manga_detail

import android.content.Intent
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.custom.CustomMatchers
import br.com.fenix.bilingualmangareader.model.entity.Library
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.model.enums.Libraries
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.utils.TestUtils
import junit.framework.TestCase.assertTrue
import org.hamcrest.Matchers.not
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4::class)
class MangaDetailActivityTest {

    private var manga: Manga
    private var intent: Intent? = null

    init {
        intent = Intent(ApplicationProvider.getApplicationContext(), MangaDetailActivity::class.java)
        val bundle = Bundle()
        manga = TestUtils.getManga(ApplicationProvider.getApplicationContext())
        bundle.putSerializable(GeneralConsts.KEYS.OBJECT.LIBRARY, Library(-1, Libraries.DEFAULT.name, ""))
        bundle.putSerializable(GeneralConsts.KEYS.OBJECT.MANGA, manga)
        intent?.putExtras(bundle)
    }


    @get:Rule
    val activityScenarioRule = ActivityScenarioRule<MangaDetailActivity>(intent)

    @Test
    fun `1_test_manga_detail`() {
        val scenario = activityScenarioRule.scenario

        scenario.onActivity {
            val fragment = it.supportFragmentManager.findFragmentById(R.id.root_frame_manga_detail)
            assertTrue(fragment is MangaDetailFragment)
        }

        onView(withId(R.id.frame_manga_detail_root)).check(matches(isDisplayed()))

        onView(withId(R.id.manga_detail_title)).check(matches(withText(manga.name)))

        onView(withId(R.id.manga_detail_folder)).check(matches(withText(manga.path)))

        onView(withId(R.id.manga_detail_last_access)).check(matches(not(withText(""))))

        onView(withId(R.id.manga_detail_book_mark)).check(matches(not(withText(""))))

        if (manga.favorite)
            onView(withId(R.id.manga_detail_button_favorite)).check(
                matches(
                    CustomMatchers.Button.withActionIconDrawable(
                        R.drawable.ic_favorite_mark,
                        true
                    )
                )
            )
        else
            onView(withId(R.id.manga_detail_button_favorite)).check(
                matches(
                    CustomMatchers.Button.withActionIconDrawable(
                        R.drawable.ic_favorite_unmark,
                        true
                    )
                )
            )

        if (manga.excluded)
            onView(withId(R.id.manga_detail_deleted)).check(matches(isDisplayed()))
        else
            onView(withId(R.id.manga_detail_deleted)).check(matches(not(isDisplayed())))

        //val waiter = CountDownLatch(1)
        //waiter.await(10, TimeUnit.SECONDS)
    }

}
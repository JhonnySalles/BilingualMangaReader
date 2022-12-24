package br.com.fenix.bilingualmangareader.custom

import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.test.espresso.matcher.BoundedMatcher
import com.google.android.material.button.MaterialButton
import org.hamcrest.Description
import org.hamcrest.Matcher


object CustomMatchers {
    object Button {
        fun withActionIconDrawable(@DrawableRes resourceId: Int, ignoreColor: Boolean = false): Matcher<View?> {
            return object : BoundedMatcher<View?, MaterialButton>(MaterialButton::class.java) {
                override fun describeTo(description: Description) {
                    description.appendText("has image drawable resource $resourceId")
                }

                override fun matchesSafely(buttonView: MaterialButton): Boolean {
                    return CustomUtils.sameBitmap(
                        buttonView.context,
                        buttonView.icon,
                        resourceId,
                        buttonView,
                        ignoreColor
                    )
                }
            }
        }
    }

    object MenuItem {
        fun withActionIconDrawable(@DrawableRes resourceId: Int, ignoreColor: Boolean = false): Matcher<View?> {
            return object : BoundedMatcher<View?, ActionMenuItemView>(ActionMenuItemView::class.java) {
                override fun describeTo(description: Description) {
                    description.appendText("has image drawable resource $resourceId")
                }

                override fun matchesSafely(actionMenuItemView: ActionMenuItemView): Boolean {
                    return CustomUtils.sameBitmap(
                        actionMenuItemView.context,
                        actionMenuItemView.itemData.icon,
                        resourceId,
                        actionMenuItemView,
                        ignoreColor
                    )
                }
            }
        }
    }
}

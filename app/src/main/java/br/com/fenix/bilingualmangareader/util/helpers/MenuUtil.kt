package br.com.fenix.bilingualmangareader.util.helpers

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.appcompat.widget.Toolbar
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.enums.Themes
import br.com.fenix.bilingualmangareader.util.helpers.Util.Utils.getColorFromAttr
import com.google.android.material.textfield.TextInputLayout


class MenuUtil {
    companion object MenuUtils {

        fun tintBackground(context: Context, background: View) {
            background.setBackgroundColor(context.getColorFromAttr(R.attr.colorSurface))
        }

        fun tintToolbar(toolbar: Toolbar, theme: Themes) {
            toolbar.popupTheme = theme.getValue()
            toolbar.context.setTheme(getToolbarTheme(theme))
        }

        fun getToolbarTheme(theme: Themes): Int {
            return when (theme) {
                Themes.BLOOD_RED -> R.style.MainToolbarTheme_BloodRed
                Themes.BLUE -> R.style.MainToolbarTheme_Blue
                Themes.FOREST_GREEN -> R.style.MainToolbarTheme_ForestGreen
                Themes.GREEN -> R.style.MainToolbarTheme_Green
                Themes.NEON_BLUE -> R.style.MainToolbarTheme_NeonBlue
                Themes.NEON_GREEN -> R.style.MainToolbarTheme_NeonGreen
                Themes.OCEAN_BLUE -> R.style.MainToolbarTheme_OceanBlue
                Themes.PINK -> R.style.MainToolbarTheme_Pink
                Themes.RED -> R.style.MainToolbarTheme_Red
                else -> R.style.MainToolbarTheme
            }
        }

        fun tintColor(context: Context, textView: TextView) {
            textView.setTextColor(context.getColorFromAttr(R.attr.colorOnSurfaceVariant))
        }

        fun tintColor(context: Context, textInput: TextInputLayout) {
            textInput.hintTextColor = ColorStateList.valueOf(context.getColorFromAttr(R.attr.colorSecondary))
            textInput.boxBackgroundColor = context.getColorFromAttr(R.attr.colorOnSurface)
            textInput.boxStrokeColor = context.getColorFromAttr(R.attr.colorSurface)
            textInput.placeholderTextColor = ColorStateList.valueOf(context.getColorFromAttr(R.attr.colorPrimary))
            tintIcons(context, textInput.startIconDrawable, R.attr.colorSecondary)
            tintIcons(context, textInput.endIconDrawable, R.attr.colorSecondary)
        }

        fun tintIcons(context: Context, icon: Drawable?, color: Int) {
            icon?.setTint(context.getColorFromAttr(color))
        }

        fun tintIcons(context: Context, icon: Drawable) {
            icon.setTint(context.getColorFromAttr(R.attr.colorOnSurfaceVariant))
        }

        fun tintIcons(context: Context, icon: ImageView) {
            icon.setColorFilter(context.getColorFromAttr(R.attr.colorOnSurfaceVariant))
        }

        fun tintAllIcons(context: Context, menu: Menu) {
            for (i in 0 until menu.size())
                menu.getItem(i).icon?.setTint(context.getColorFromAttr(R.attr.colorOnSurfaceVariant))
        }

        fun tintIcons(context: Context, searchView: SearchView) {
            tintIcons(context, searchView.findViewById<ImageView>(context.resources.getIdentifier("android:id/search_button", null, null)))
            tintIcons(
                context,
                searchView.findViewById<ImageView>(context.resources.getIdentifier("android:id/search_close_btn", null, null))
            )
            tintIcons(
                context,
                searchView.findViewById<ImageView>(context.resources.getIdentifier("android:id/search_mag_icon", null, null))
            )
            tintIcons(
                context,
                searchView.findViewById<ImageView>(context.resources.getIdentifier("android:id/search_voice_btn", null, null))
            )
        }

        fun tintIcons(context: Context, drawer: DrawerArrowDrawable) {
            drawer.color = context.getColorFromAttr(R.attr.colorOnSurfaceVariant)
        }

    }
}
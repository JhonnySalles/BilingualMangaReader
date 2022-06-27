package br.com.fenix.bilingualmangareader.view.components

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.provider.Settings
import android.view.View


class ComponentsUtil {

    companion object ComponentsUtils {
        fun canDrawOverlays(context: Context): Boolean =
            Settings.canDrawOverlays(context)

        private const val duration = 300L
        fun changeAnimateVisibility(component: View, visible: Boolean) {
            if ((visible && component.visibility == View.VISIBLE) || (!visible && component.visibility != View.VISIBLE))
                return

            val visibility = if (visible) View.VISIBLE else View.GONE
            val initialAlpha = if (visible) 0.0f else 1.0f
            val finalAlpha = if (visible) 1.0f else 0.0f


            if (visible) {
                component.visibility = visibility
                component.alpha = initialAlpha
            }

            component.animate().alpha(finalAlpha).setDuration(duration)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        component.visibility = visibility
                    }
                })
        }

        fun changeAnimateVisibility(components: List<View>, visible: Boolean) {
            val visibility = if (visible) View.VISIBLE else View.GONE
            val initialAlpha = if (visible) 0.0f else 1.0f
            val finalAlpha = if (visible) 1.0f else 0.0f

            for (component in components) {
                if ((visible && component.visibility == View.VISIBLE) || (!visible && component.visibility != View.VISIBLE))
                    continue

                if (visible) {
                    component.visibility = visibility
                    component.alpha = initialAlpha
                }

                component.animate().alpha(finalAlpha).setDuration(duration)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            super.onAnimationEnd(animation)
                            component.visibility = visibility
                        }
                    })
            }
        }

    }
}
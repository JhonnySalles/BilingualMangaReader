package br.com.fenix.bilingualmangareader.view.components

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager

class ComponentsUtil {

    companion object ComponentsUtils {

        fun setSystemUITransparency(window: Window) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            } else {
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                )
            }

            window.setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            )
        }

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
package br.com.fenix.bilingualmangareader.custom

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.view.View

class CustomUtils {

    companion object CustomUtils {
        private fun getBitmapFromDrawable(drawable: Drawable, ignoreColor: Boolean): Bitmap {
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)

            if (ignoreColor)
                drawable.setTint(Color.WHITE)

            drawable.draw(canvas)
            return bitmap
        }

        fun sameBitmap(
            context: Context,
            drawable: Drawable?,
            resourceId: Int,
            view: View,
            ignoreColor: Boolean
        ): Boolean {
            var drawable = drawable
            val otherDrawable: Drawable? = context.resources.getDrawable(resourceId)
            if (drawable == null || otherDrawable == null) {
                return false
            }

            if (drawable is StateListDrawable) {
                val getStateDrawableIndex =
                    StateListDrawable::class.java.getMethod(
                        "getStateDrawableIndex",
                        IntArray::class.java
                    )
                val getStateDrawable =
                    StateListDrawable::class.java.getMethod(
                        "getStateDrawable",
                        Int::class.javaPrimitiveType
                    )
                val index = getStateDrawableIndex.invoke(drawable, view.drawableState)
                drawable = getStateDrawable.invoke(drawable, index) as Drawable
            }

            val bitmap = getBitmapFromDrawable(drawable, ignoreColor)
            val otherBitmap = getBitmapFromDrawable(otherDrawable, ignoreColor)

            return bitmap.sameAs(otherBitmap)
        }

    }

}
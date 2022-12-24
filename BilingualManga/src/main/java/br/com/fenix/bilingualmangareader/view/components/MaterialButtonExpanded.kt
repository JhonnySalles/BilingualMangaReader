package br.com.fenix.bilingualmangareader.view.components

import android.content.Context
import android.util.AttributeSet
import br.com.fenix.bilingualmangareader.R
import com.google.android.material.button.MaterialButton


class MaterialButtonExpanded(context: Context, attrs: AttributeSet) : MaterialButton(context, attrs) {

    private val STATE_EXPANDED = intArrayOf(R.attr.state_expanded)

    private var mIsExpanded = false

    fun setIsExpanded(isExpanded: Boolean) {
        mIsExpanded = isExpanded
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray? {
        val drawableState = super.onCreateDrawableState(extraSpace + 2)
        if (mIsExpanded)
            mergeDrawableStates(drawableState, STATE_EXPANDED)
        return drawableState
    }
}
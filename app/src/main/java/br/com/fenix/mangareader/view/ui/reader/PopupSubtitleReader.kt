package br.com.fenix.mangareader.view.ui.reader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import br.com.fenix.mangareader.R
import com.google.android.material.tabs.TabLayout

class PopupSubtitleReader: Fragment() {

    lateinit var mMenuPopup: FrameLayout
    lateinit var mPopupTab: TabLayout
    lateinit var mPopupView: ViewPager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.popup_subtitle_reader, container, false)
    }
}
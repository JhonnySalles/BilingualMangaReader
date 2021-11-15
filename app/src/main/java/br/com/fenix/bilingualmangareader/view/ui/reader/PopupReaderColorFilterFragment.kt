package br.com.fenix.bilingualmangareader.view.ui.reader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import br.com.fenix.bilingualmangareader.R
import com.google.android.material.switchmaterial.SwitchMaterial


class PopupReaderColorFilterFragment : Fragment() {

    private val mViewModel: ReaderViewModel by activityViewModels()
    private lateinit var mCustomFilter: SwitchMaterial
    private lateinit var mColorRed: SeekBar
    private lateinit var mColorGreen: SeekBar
    private lateinit var mColorBlue: SeekBar
    private lateinit var mColorAlpha: SeekBar
    private lateinit var mGrayScale: SwitchMaterial
    private lateinit var mInvertColor: SwitchMaterial
    private lateinit var mBlueLight: SwitchMaterial
    private lateinit var mBlueLightAlpha: SeekBar
    private lateinit var mSepia: SwitchMaterial

    private lateinit var mFilterRed: TextView
    private lateinit var mFilterBlue: TextView
    private lateinit var mFilterGreen: TextView
    private lateinit var mFilterAlpha: TextView
    private lateinit var mFilterBlueLightAlpha: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mCustomFilter = view.findViewById(R.id.switch_color_filter)
        mColorRed = view.findViewById(R.id.seekbar_color_filter_red)
        mColorGreen = view.findViewById(R.id.seekbar_color_filter_green)
        mColorBlue = view.findViewById(R.id.seekbar_color_filter_blue)
        mColorAlpha = view.findViewById(R.id.seekbar_color_filter_alpha)
        mGrayScale = view.findViewById(R.id.switch_grayscale)
        mInvertColor = view.findViewById(R.id.switch_invert_color)
        mBlueLight = view.findViewById(R.id.switch_blue_light)
        mBlueLightAlpha = view.findViewById(R.id.seekbar_blue_light_alpha)
        mSepia = view.findViewById(R.id.switch_sepia_color)

        mFilterRed = view.findViewById(R.id.txt_color_filter_red_value)
        mFilterBlue = view.findViewById(R.id.txt_color_filter_blue_value)
        mFilterGreen = view.findViewById(R.id.txt_color_filter_green_value)
        mFilterAlpha = view.findViewById(R.id.txt_color_filter_alpha_value)
        mFilterBlueLightAlpha = view.findViewById(R.id.txt_color_blue_light_value)

        mCustomFilter.isChecked = mViewModel.customFilter.value!!
        mGrayScale.isChecked = mViewModel.grayScale.value!!
        mInvertColor.isChecked = mViewModel.invertColor.value!!
        mBlueLight.isChecked = mViewModel.blueLight.value!!
        mSepia.isChecked = mViewModel.sepia.value!!

        mCustomFilter.setOnClickListener { mViewModel.changeCustomFilter(mCustomFilter.isChecked) }
        mGrayScale.setOnClickListener { mViewModel.changeGrayScale(mGrayScale.isChecked) }
        mInvertColor.setOnClickListener { mViewModel.changeInvertColor(mInvertColor.isChecked) }
        mBlueLight.setOnClickListener { mViewModel.changeBlueLight(mBlueLight.isChecked) }
        mSepia.setOnClickListener { mViewModel.changeSepia(mSepia.isChecked) }

        mColorRed.progress = mViewModel.colorRed.value!!
        mColorGreen.progress = mViewModel.colorGreen.value!!
        mColorBlue.progress = mViewModel.colorBlue.value!!
        mColorAlpha.progress = mViewModel.colorAlpha.value!!
        mBlueLightAlpha.progress = mViewModel.blueLightAlpha.value!!

        mColorRed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser)
                    mViewModel.changeColorsFilter(
                        progress,
                        mViewModel.colorGreen.value!!,
                        mViewModel.colorBlue.value!!,
                        mViewModel.colorAlpha.value!!
                    )
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        mColorGreen.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser)
                    mViewModel.changeColorsFilter(
                        mViewModel.colorRed.value!!,
                        progress,
                        mViewModel.colorBlue.value!!,
                        mViewModel.colorAlpha.value!!
                    )
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        mColorBlue.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser)
                    mViewModel.changeColorsFilter(
                        mViewModel.colorRed.value!!,
                        mViewModel.colorGreen.value!!,
                        progress,
                        mViewModel.colorAlpha.value!!
                    )
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        mColorAlpha.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser)
                    mViewModel.changeColorsFilter(
                        mViewModel.colorRed.value!!,
                        mViewModel.colorGreen.value!!,
                        mViewModel.colorBlue.value!!,
                        progress
                    )
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        mBlueLightAlpha.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser)
                    mViewModel.changeBlueLightAlpha(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.popup_reader_color_filter, container, false)

        mViewModel.colorRed.observe(viewLifecycleOwner, { mFilterRed.text = it.toString() })
        mViewModel.colorGreen.observe(viewLifecycleOwner, { mFilterGreen.text = it.toString() })
        mViewModel.colorBlue.observe(viewLifecycleOwner, { mFilterBlue.text = it.toString() })
        mViewModel.colorAlpha.observe(viewLifecycleOwner, { mFilterAlpha.text = it.toString() })
        mViewModel.blueLightAlpha.observe(viewLifecycleOwner, {
            val value = if (it > 0) (it * 100 / 200) else 0
            mFilterBlueLightAlpha.text = "$value %"
        })

        return root
    }

}
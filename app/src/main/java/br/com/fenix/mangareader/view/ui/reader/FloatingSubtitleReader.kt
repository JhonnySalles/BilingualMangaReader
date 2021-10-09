package br.com.fenix.mangareader.view.ui.reader

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.*
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import br.com.fenix.mangareader.R
import br.com.fenix.mangareader.model.entity.Text
import br.com.fenix.mangareader.service.controller.SubTitleController
import br.com.fenix.mangareader.service.kanji.Formatter
import br.com.fenix.mangareader.service.kanji.FuriganaView
import kotlin.math.abs

class FloatingSubtitleReader constructor(private val context: Context) {

    private var windowManager: WindowManager? = null
        get() {
            if (field == null) field =
                (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
            return field
        }

    private var mFloatingView: View =
        LayoutInflater.from(context).inflate(R.layout.floating_subtitle_reader, null)

    private lateinit var layoutParams: WindowManager.LayoutParams

    private var lastX: Int = 0
    private var lastY: Int = 0
    private var firstX: Int = 0
    private var firstY: Int = 0

    var isShowing = false
    private var touchConsumedByMove = false

    private val onTouchListener = View.OnTouchListener { view, event ->
        val totalDeltaX = lastX - firstX
        val totalDeltaY = lastY - firstY

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.rawX.toInt()
                lastY = event.rawY.toInt()
                firstX = lastX
                firstY = lastY
            }
            MotionEvent.ACTION_UP -> {
                view.performClick()
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.rawX.toInt() - lastX
                val deltaY = event.rawY.toInt() - lastY
                lastX = event.rawX.toInt()
                lastY = event.rawY.toInt()
                if (abs(totalDeltaX) >= 5 || abs(totalDeltaY) >= 5) {
                    if (event.pointerCount == 1) {
                        layoutParams.x += deltaX
                        layoutParams.y += deltaY
                        touchConsumedByMove = true
                        windowManager?.apply {
                            updateViewLayout(mFloatingView, layoutParams)
                        }
                    } else {
                        touchConsumedByMove = false
                    }
                } else {
                    touchConsumedByMove = false
                }
            }
            else -> {
            }
        }
        touchConsumedByMove
    }

    private var mSubTitleController: SubTitleController
    private var mLabelChapter: String
    private var mLabelPage: String
    private var mLabelText: String
    private var mSubtitleTitle: TextView
    private var mSubtitleContent: FuriganaView

    init {
        with(mFloatingView) {
            mSubTitleController = SubTitleController.getInstance(context)
            mLabelChapter = context.getString(R.string.popup_reading_subtitle_chapter)
            mLabelPage = context.getString(R.string.popup_reading_subtitle_page)
            mLabelText = context.getString(R.string.popup_reading_subtitle_text)

            this.findViewById<AppCompatImageButton>(R.id.nav_floating_before_text)
                .setOnClickListener { mSubTitleController.getBeforeText() }
            this.findViewById<AppCompatImageButton>(R.id.nav_floating_next_text)
                .setOnClickListener { mSubTitleController.getNextText() }
            this.findViewById<AppCompatImageButton>(R.id.nav_floating_refresh)
                .setOnClickListener { mSubTitleController.findSubtitle() }
            this.findViewById<AppCompatImageButton>(R.id.nav_floating_draw)
                .setOnClickListener { mSubTitleController.drawSelectedText() }

            this.findViewById<AppCompatImageButton>(R.id.nav_floating_draw)
                .setOnClickListener { mSubTitleController.drawSelectedText() }

            this.findViewById<AppCompatImageButton>(R.id.nav_floating_change_language)
                .setOnClickListener { mSubTitleController.changeLanguage() }

            mSubtitleTitle = this.findViewById(R.id.txt_floating_title)
            mSubtitleContent = this.findViewById(R.id.txt_floating_content)

            this.findViewById<AppCompatImageButton>(R.id.imgbtn_close)
                .setOnClickListener { dismiss() }
        }

        mFloatingView.setOnTouchListener(onTouchListener)

        layoutParams = WindowManager.LayoutParams().apply {
            format = PixelFormat.TRANSLUCENT
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            @Suppress("DEPRECATION")
            type = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ->
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else -> WindowManager.LayoutParams.TYPE_TOAST
            }

            gravity = Gravity.CENTER
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
    }

    fun updateText(text: Text?) {
        var title = ""
        mSubtitleContent.setText("")
        if (text != null) {
            val index =
                mSubTitleController.pageSelected.value?.texts?.indexOf(mSubTitleController.textSelected.value)
                    ?.plus(1)

            title =
                "${mLabelChapter} ${mSubTitleController.chapterSelected.value?.chapter.toString()} - " +
                        "${mLabelPage} ${mSubTitleController.pageSelected.value!!.number} - " +
                        "${mLabelText} $index/${mSubTitleController.pageSelected.value?.texts?.size}"

            Formatter.generateFurigana(text.text) { furigana -> mSubtitleContent.setText(furigana) }
        } else if (mSubTitleController.chapterSelected.value != null && mSubTitleController.pageSelected.value != null) {
            title =
                "${mLabelChapter} ${mSubTitleController.chapterSelected.value?.chapter.toString()} - " +
                        "${mLabelPage} ${mSubTitleController.pageSelected.value!!.number} - "
            "${mLabelText} 0/${if (mSubTitleController.pageSelected.value?.texts == null) 0 else mSubTitleController.pageSelected.value?.texts?.size}"
        }

        mSubtitleTitle.text = title
    }

    fun show() {
        if (canDrawOverlays(context)) {
            dismiss()
            isShowing = true
            windowManager?.addView(mFloatingView, layoutParams)
        }
    }

    fun dismiss() {
        if (isShowing) {
            windowManager?.removeView(mFloatingView)
            isShowing = false
        }
    }

    companion object {
        fun canDrawOverlays(context: Context): Boolean = Settings.canDrawOverlays(context)
    }
}
package br.com.fenix.bilingualmangareader.view.ui.window

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Build
import android.provider.Settings
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.view.*
import android.widget.*
import android.widget.AdapterView.OnItemLongClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.entity.Page
import br.com.fenix.bilingualmangareader.model.entity.Text
import br.com.fenix.bilingualmangareader.service.controller.SubTitleController
import br.com.fenix.bilingualmangareader.service.kanji.Formatter
import com.pedromassango.doubleclick.DoubleClick
import com.pedromassango.doubleclick.DoubleClickListener
import kotlin.math.abs


@SuppressLint("ClickableViewAccessibility")
class FloatingSubtitleReader constructor(private val context: Context, private val activity: AppCompatActivity) {

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

    private var mSubtitleContent: ConstraintLayout
    private var mOcrContent: LinearLayout

    private var mSubTitleController: SubTitleController
    private var mSubtitleScrollContent: ScrollView
    private var mLabelChapter: String
    private var mLabelPage: String
    private var mLabelText: String
    private var mSubtitleTitle: TextView
    private var mSubtitleText: TextView
    private var mListPageVocabulary: ListView
    private var mVocabulary: Map<String, String> = mapOf()
    private var mVocabularyItem = ArrayList<String>()
    private var mOriginalHeight: Int = 0
    private var mGestureDetector: GestureDetector

    private var mOcrText: TextView
    private var mOcrItem = ArrayList<String>()
    private var mOcrListText: ListView
    private var mOcrScrollContent: ScrollView

    private var mBtnExpanded: AppCompatImageButton
    private var mIconExpanded: Drawable?
    private var mIconRetracted: Drawable?

    init {
        with(mFloatingView) {
            mSubTitleController = SubTitleController.getInstance(context)
            mSubtitleScrollContent = this.findViewById(R.id.floating_subtitle_scroll)
            mLabelChapter = context.getString(R.string.popup_reading_subtitle_chapter)
            mLabelPage = context.getString(R.string.popup_reading_subtitle_page)
            mLabelText = context.getString(R.string.popup_reading_subtitle_text)

            this.findViewById<AppCompatImageButton>(R.id.nav_floating_subtitle_close)
                .setOnClickListener { dismiss() }
            this.findViewById<AppCompatImageButton>(R.id.nav_floating_subtitle_before_text)
                .setOnClickListener { mSubTitleController.getBeforeText() }
            this.findViewById<AppCompatImageButton>(R.id.nav_floating_subtitle_next_text)
                .setOnClickListener { mSubTitleController.getNextText() }
            this.findViewById<AppCompatImageButton>(R.id.nav_floating_subtitle_refresh)
                .setOnClickListener { mSubTitleController.findSubtitle() }
            this.findViewById<AppCompatImageButton>(R.id.nav_floating_subtitle_draw)
                .setOnClickListener { mSubTitleController.drawSelectedText() }
            this.findViewById<AppCompatImageButton>(R.id.nav_floating_subtitle_change_language)
                .setOnClickListener { mSubTitleController.changeLanguage() }
            this.findViewById<AppCompatImageButton>(R.id.nav_floating_subtitle_page_linked)
                .setOnClickListener { mSubTitleController.drawPageLinked() }

            this.findViewById<AppCompatImageButton>(R.id.nav_floating_subtitle_change_subtitle)
                .setOnClickListener { changeLayout() }

            this.findViewById<AppCompatImageButton>(R.id.nav_floating_subtitle_change_ocr)
                .setOnClickListener { changeLayout(false) }

            this.findViewById<AppCompatImageButton>(R.id.nav_floating_subtitle_go_to_top).setOnClickListener {
                mSubtitleScrollContent.smoothScrollTo(0, 0)
            }

            mIconExpanded = AppCompatResources.getDrawable(context, R.drawable.ic_expanded)
            mIconRetracted = AppCompatResources.getDrawable(context, R.drawable.ic_retracted)

            mBtnExpanded = this.findViewById(R.id.nav_floating_subtitle_expanded)
            mBtnExpanded.setOnClickListener { expanded() }

            mSubtitleContent = this.findViewById(R.id.floating_subtitle_subtitle_content)
            mSubtitleTitle = this.findViewById(R.id.floating_subtitle_title)
            mSubtitleText = this.findViewById(R.id.floating_subtitle_subtitle)
            mListPageVocabulary = this.findViewById(R.id.floating_subtitle_list_page_vocabulary)
            mListPageVocabulary.adapter = ArrayAdapter(context, R.layout.list_item_vocabulary_small, mVocabularyItem)
            mListPageVocabulary.choiceMode = ListView.CHOICE_MODE_SINGLE
            mListPageVocabulary.isLongClickable = true
            mListPageVocabulary.onItemLongClickListener = OnItemLongClickListener { _, _, index, _ ->
                if (index >= 0 && mVocabularyItem.size > index) {
                    val text = mVocabularyItem[index]
                    Toast.makeText(
                        context,
                        context.getString(R.string.action_copy) + " $text",
                        Toast.LENGTH_SHORT
                    ).show()

                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Copied Text", text)
                    clipboard.setPrimaryClip(clip)
                }
                true
            }

            mSubtitleText.setOnClickListener(
                DoubleClick(object : DoubleClickListener {
                    override fun onSingleClick(view: View?) {}
                    override fun onDoubleClick(view: View?) {
                        mSubTitleController.getNextText()
                    }
                }, 500)
            )

            mSubtitleText.setOnLongClickListener {
                val text = mSubTitleController.textSelected.value?.text ?: ""
                if (text.isNotEmpty()) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.action_copy) + " $text",
                        Toast.LENGTH_SHORT
                    ).show()

                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Copied Text", text)
                    clipboard.setPrimaryClip(clip)
                }
                true
            }

            mSubtitleText.movementMethod = LinkMovementMethod.getInstance()

            mOcrContent = this.findViewById(R.id.floating_subtitle_ocr_content)
            mOcrText = this.findViewById(R.id.floating_subtitle_ocr_text)
            mOcrText.movementMethod = LinkMovementMethod.getInstance()
            mOcrScrollContent = this.findViewById(R.id.floating_subtitle_ocr_scroll)

            mOcrListText = this.findViewById(R.id.floating_subtitle_ocr_list)
            mOcrListText.adapter = ArrayAdapter(context, R.layout.list_item_vocabulary_small, mOcrItem)
            mOcrListText.choiceMode = ListView.CHOICE_MODE_SINGLE
            mOcrListText.isLongClickable = true
            mOcrListText.onItemClickListener = AdapterView.OnItemClickListener { _, _, index, _ ->
                if (index >= 0 && mOcrItem.size > index)
                    updateTextOcr(mOcrItem[index], false)
                true
            }

            mOcrListText.onItemLongClickListener = OnItemLongClickListener { _, _, index, _ ->
                if (index >= 0 && mOcrItem.size > index) {
                    val text = mOcrItem[index]
                    Toast.makeText(
                        context,
                        context.getString(R.string.action_copy) + " $text",
                        Toast.LENGTH_SHORT
                    ).show()

                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Copied Text", text)
                    clipboard.setPrimaryClip(clip)
                }
                true
            }

            this.findViewById<AppCompatImageButton>(R.id.floating_subtitle_ocr_clear_list).setOnClickListener {
                mOcrItem.clear()
                (mOcrListText.adapter as ArrayAdapter<*>).notifyDataSetChanged()
            }

        }

        mGestureDetector = GestureDetector(context, ChangeTextTouchListener())
        mSubtitleText.setOnTouchListener { _, motionEvent ->
            mGestureDetector.onTouchEvent(motionEvent)
        }
        mListPageVocabulary.setOnTouchListener { _, motionEvent -> mGestureDetector.onTouchEvent(motionEvent) }

        mSubtitleTitle.setOnTouchListener(onTouchListener)
        mFloatingView.setOnTouchListener(onTouchListener)

        val layoutType = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_PHONE
        else
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

        layoutParams = WindowManager.LayoutParams().apply {
            format = PixelFormat.TRANSLUCENT
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            type = layoutType
            gravity = Gravity.TOP
            width = context.resources.getDimension(R.dimen.floating_reader_width).toInt()
            height = context.resources.getDimension(R.dimen.floating_reader_height).toInt()
        }
    }

    inner class ChangeTextTouchListener : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            if (e2 != null && e1 != null) {
                if (abs(e1.x - e2.x) > 200)
                    if (abs(e2.x) > abs(e1.x))
                        mSubTitleController.getBeforeText()
                    else if (abs(e2.x) < abs(e1.x))
                        mSubTitleController.getNextText()
            }

            return super.onFling(e1, e2, velocityX, velocityY)
        }
    }

    fun updatePage(page: Page?) {
        if (page != null) {
            if (page.vocabulary != null && page.vocabulary.isNotEmpty()) {
                mVocabulary = page.vocabulary.map { vocab -> vocab.word to vocab.word + " - " + vocab.meaning + if (!vocab.revised) "¹" else "" }
                    .toMap()
                mVocabularyItem.clear()
                mVocabularyItem.addAll(page.vocabulary.map { vocab -> vocab.word + " - " + vocab.meaning + if (!vocab.revised) "¹" else "" })
                mListPageVocabulary.visibility = View.VISIBLE
            } else {
                mVocabularyItem.clear()
                mListPageVocabulary.visibility = View.GONE
            }
            (mListPageVocabulary.adapter as ArrayAdapter<*>).notifyDataSetChanged()
        }
    }

    private fun findVocabulary(vocabulary: String) {
        mListPageVocabulary.clearChoices()
        if (mVocabularyItem.isNotEmpty()) {
            var index = -1

            if (mVocabulary.containsKey(vocabulary))
                index = mVocabularyItem.indexOf(mVocabulary[vocabulary])
            else {
                for (word in mVocabulary.keys)
                    if (vocabulary in word) {
                        index = mVocabularyItem.indexOf(mVocabulary[word])
                        break
                    }

                if (index < 0)
                    return
            }

            mSubtitleScrollContent.smoothScrollTo(0, mListPageVocabulary.top)
            mListPageVocabulary.smoothScrollToPosition(index)
            mListPageVocabulary.requestFocusFromTouch()
            mListPageVocabulary.setSelection(index)
        }
    }

    fun expanded(expand : Boolean = false) {
        if (mOriginalHeight == 0)
            mOriginalHeight = mFloatingView.height

        if (expand || mFloatingView.height != mOriginalHeight) {
            val params = mFloatingView.layoutParams as WindowManager.LayoutParams
            params.height = mOriginalHeight
            mFloatingView.layoutParams = params
            mBtnExpanded.setImageDrawable(mIconRetracted)
        } else {
            val params = mFloatingView.layoutParams as WindowManager.LayoutParams
            params.height = context.resources.getDimension(R.dimen.floating_reader_button_close).toInt()
            mFloatingView.layoutParams = params
            mBtnExpanded.setImageDrawable(mIconExpanded)
        }

        windowManager?.apply {
            updateViewLayout(mFloatingView, mFloatingView.layoutParams)
        }
    }

    fun updateText(text: Text?) {
        var title = ""
        mSubtitleText.text = ""
        mSubtitleScrollContent.smoothScrollTo(0, 0)
        if (text != null) {
            val index =
                mSubTitleController.pageSelected.value?.texts?.indexOf(mSubTitleController.textSelected.value)
                    ?.plus(1)

            title =
                "$mLabelChapter ${mSubTitleController.chapterSelected.value?.chapter.toString()} - " +
                        "$mLabelPage ${mSubTitleController.pageSelected.value!!.number} - " +
                        "$mLabelText $index/${mSubTitleController.pageSelected.value?.texts?.size}"

            Formatter.generateFurigana(text.text, furigana = { mSubtitleText.text = it }, vocabularyClick = { findVocabulary(it) })
        } else if (mSubTitleController.chapterSelected.value != null && mSubTitleController.pageSelected.value != null) {
            title =
                "$mLabelChapter ${mSubTitleController.chapterSelected.value?.chapter.toString()} - " +
                        "$mLabelPage ${mSubTitleController.pageSelected.value!!.number} - "
            "$mLabelText 0/${if (mSubTitleController.pageSelected.value?.texts == null) 0 else mSubTitleController.pageSelected.value?.texts?.size}"
        }

        mSubtitleTitle.text = title

        changeLayout()
    }

    fun updateTextOcr(text: String?, isNew: Boolean = true) {
        if (isNew && text != null)
            mOcrItem.add(text)

        if (text != null) {
            changeLayout(false)
            Formatter.generateKanjiColor(activity, text) { kanji ->
                mOcrText.text = kanji
            }
        } else
            mOcrText.text = ""
    }

    fun updateTextOcr(texts: ArrayList<String>) {
        mOcrText.text = ""
        changeLayout(false)
        mOcrItem.addAll(texts)
        (mOcrListText.adapter as ArrayAdapter<*>).notifyDataSetChanged()
    }

    fun changeLayout(isSubtitle: Boolean = true) {
        if (isSubtitle) {
            mSubtitleContent.visibility = View.VISIBLE
            mOcrContent.visibility = View.GONE
        } else {
            mSubtitleContent.visibility = View.GONE
            mOcrContent.visibility = View.VISIBLE
        }
    }

    fun show() {
        if (canDrawOverlays(context)) {
            dismiss()
            isShowing = true
            windowManager?.addView(mFloatingView, layoutParams)
        }
    }

    fun showWithoutDismiss() {
        if (!isShowing)
            show()
    }

    fun dismiss() {
        if (isShowing) {
            windowManager?.removeView(mFloatingView)
            isShowing = false
        }
    }

    fun destroy() {
        dismiss()
    }

    companion object {
        fun canDrawOverlays(context: Context): Boolean =
            Settings.canDrawOverlays(context)
    }
}
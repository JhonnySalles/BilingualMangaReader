package br.com.fenix.mangareader.view.ui.configuration

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import br.com.fenix.mangareader.R
import br.com.fenix.mangareader.model.enums.Languages
import br.com.fenix.mangareader.model.enums.Order
import br.com.fenix.mangareader.model.enums.PageMode
import br.com.fenix.mangareader.model.enums.ReaderMode
import br.com.fenix.mangareader.service.repository.Storage
import br.com.fenix.mangareader.util.constants.GeneralConsts
import br.com.fenix.mangareader.util.helpers.Util
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.*


class ConfigFragment : Fragment() {

    private lateinit var mLibraryPath: TextInputLayout
    private lateinit var mLibraryPathAutoComplete: AutoCompleteTextView
    private lateinit var mLibraryOrder: TextInputLayout
    private lateinit var mLibraryOrderAutoComplete: AutoCompleteTextView

    private lateinit var mDefaultSubtitleLanguage: TextInputLayout
    private lateinit var mDefaultSubtitleLanguageAutoComplete: AutoCompleteTextView
    private lateinit var mDefaultSubtitleTranslate: TextInputLayout
    private lateinit var mSubtitleTranslateAutoComplete: AutoCompleteTextView

    private lateinit var mReaderComicMode: TextInputLayout
    private lateinit var mReaderComicModeAutoComplete: AutoCompleteTextView
    private lateinit var mReaderPageMode: TextInputLayout
    private lateinit var mPageModeAutoComplete: AutoCompleteTextView

    private lateinit var mSystemFormatDate: TextInputLayout
    private lateinit var mSystemFormatDateAutoComplete: AutoCompleteTextView

    private var mDateSelect: String = GeneralConsts.CONFIG.DATA_FORMAT[0]
    private val mDatePattern = GeneralConsts.CONFIG.DATA_FORMAT
    private var mPageModeSelect: PageMode = PageMode.Comics
    private var mReaderModeSelect: ReaderMode = ReaderMode.FIT_WIDTH
    private var mOrderSelect: Order = Order.Name

    private var mDefaultSubtitleLanguageSelect: Languages = Languages.JAPANESE
    private var mDefaultSubtitleTranslateSelect: Languages = Languages.PORTUGUESE

    private lateinit var mMapOrder: HashMap<String, Order>
    private lateinit var mMapPageMode: HashMap<String, PageMode>
    private lateinit var mMapReaderMode: HashMap<String, ReaderMode>
    private lateinit var mMapLanguage: HashMap<String, Languages>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mLibraryPath = view.findViewById(R.id.txt_library_path)
        mLibraryPathAutoComplete = view.findViewById(R.id.menu_autocomplete_library_path)
        mLibraryOrder = view.findViewById(R.id.txt_library_order)
        mLibraryOrderAutoComplete = view.findViewById(R.id.menu_autocomplete_library_order)

        mDefaultSubtitleLanguage = view.findViewById(R.id.txt_default_subtitle_language)
        mDefaultSubtitleLanguageAutoComplete =
            view.findViewById(R.id.menu_autocomplete_default_subtitle_language)
        mDefaultSubtitleTranslate = view.findViewById(R.id.txt_default_subtitle_translate)
        mSubtitleTranslateAutoComplete =
            view.findViewById(R.id.menu_autocomplete_default_subtitle_translate)

        mReaderComicMode = view.findViewById(R.id.txt_reader_comic_mode)
        mReaderComicModeAutoComplete = view.findViewById(R.id.menu_autocomplete_reader_comic_mode)
        mReaderPageMode = view.findViewById(R.id.txt_reader_page_mode)
        mPageModeAutoComplete = view.findViewById(R.id.menu_autocomplete_page_mode)

        mSystemFormatDate = view.findViewById(R.id.txt_system_format_date)
        mSystemFormatDateAutoComplete = view.findViewById(R.id.menu_autocomplete_system_format_date)

        mLibraryPathAutoComplete.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            startActivityForResult(intent, 101)
        }

        val languages = resources.getStringArray(R.array.languages)
        mMapLanguage = hashMapOf(
            languages[0] to Languages.PORTUGUESE,
            languages[1] to Languages.ENGLISH,
            languages[2] to Languages.JAPANESE
        )

        mMapOrder = hashMapOf(
            getString(R.string.config_option_order_name) to Order.Name,
            getString(R.string.config_option_order_date) to Order.Date,
            getString(R.string.config_option_order_access) to Order.LastAccess,
            getString(R.string.config_option_order_favorite) to Order.Favorite
        )

        mMapPageMode = hashMapOf(
            getString(R.string.menu_reading_mode_left_to_right) to PageMode.Comics,
            getString(R.string.menu_reading_mode_right_to_left) to PageMode.Manga
        )

        mMapReaderMode = hashMapOf(
            getString(R.string.menu_view_mode_aspect_fill) to ReaderMode.ASPECT_FILL,
            getString(R.string.menu_view_mode_aspect_fit) to ReaderMode.ASPECT_FIT,
            getString(R.string.menu_view_mode_fit_width) to ReaderMode.FIT_WIDTH
        )

        val adapterOrder =
            ArrayAdapter(requireContext(), R.layout.list_item, mMapOrder.keys.toTypedArray())
        mLibraryOrderAutoComplete.setAdapter(adapterOrder)
        mLibraryOrderAutoComplete.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                mOrderSelect = if (parent.getItemAtPosition(position).toString().isNotEmpty() &&
                    mMapOrder.containsKey(parent.getItemAtPosition(position).toString())
                )
                    mMapOrder[parent.getItemAtPosition(position).toString()]!!
                else
                    Order.Name
            }

        val adapterLanguage =
            ArrayAdapter(requireContext(), R.layout.list_item, mMapLanguage.keys.toTypedArray())
        mDefaultSubtitleLanguageAutoComplete.setAdapter(adapterLanguage)
        mDefaultSubtitleLanguageAutoComplete.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                mDefaultSubtitleLanguageSelect =
                    if (parent.getItemAtPosition(position).toString().isNotEmpty() &&
                        mMapLanguage.containsKey(parent.getItemAtPosition(position).toString())
                    )
                        mMapLanguage[parent.getItemAtPosition(position).toString()]!!
                    else
                        Languages.JAPANESE
            }

        mSubtitleTranslateAutoComplete.setAdapter(adapterLanguage)
        mSubtitleTranslateAutoComplete.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                mDefaultSubtitleTranslateSelect =
                    if (parent.getItemAtPosition(position).toString().isNotEmpty() &&
                        mMapLanguage.containsKey(parent.getItemAtPosition(position).toString())
                    )
                        mMapLanguage[parent.getItemAtPosition(position).toString()]!!
                    else
                        Languages.PORTUGUESE
            }

        val adapterReaderMode =
            ArrayAdapter(requireContext(), R.layout.list_item, mMapReaderMode.keys.toTypedArray())
        mReaderComicModeAutoComplete.setAdapter(adapterReaderMode)
        mReaderComicModeAutoComplete.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                mReaderModeSelect = if (parent.getItemAtPosition(position).toString().isNotEmpty() &&
                    mMapReaderMode.containsKey(parent.getItemAtPosition(position).toString())
                )
                    mMapReaderMode[parent.getItemAtPosition(position).toString()]!!
                else
                    ReaderMode.FIT_WIDTH
            }

        val adapterPageMode =
            ArrayAdapter(requireContext(), R.layout.list_item, mMapPageMode.keys.toTypedArray())
        mPageModeAutoComplete.setAdapter(adapterPageMode)
        mPageModeAutoComplete.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                mPageModeSelect = if (parent.getItemAtPosition(position).toString().isNotEmpty() &&
                    mMapPageMode.containsKey(parent.getItemAtPosition(position).toString())
                )
                    mMapPageMode[parent.getItemAtPosition(position).toString()]!!
                else
                    PageMode.Comics
            }

        val date0 = SimpleDateFormat(mDatePattern[0]).format(Date())
        val date1 = SimpleDateFormat(mDatePattern[1]).format(Date())
        val date2 = SimpleDateFormat(mDatePattern[2]).format(Date())
        val date3 = SimpleDateFormat(mDatePattern[3]).format(Date())

        val dataFormat = listOf(
            getString(R.string.config_option_date_time_format_0).format(date0),
            getString(R.string.config_option_date_time_format_1).format(date1),
            getString(R.string.config_option_date_time_format_2).format(date2),
            getString(R.string.config_option_date_time_format_3).format(date3)
        )
        val adapterDataFormat = ArrayAdapter(requireContext(), R.layout.list_item, dataFormat)
        mSystemFormatDateAutoComplete.setAdapter(adapterDataFormat)
        mSystemFormatDateAutoComplete.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                mDateSelect = if (mDatePattern.size > position && position >= 0)
                    mDatePattern[position]
                else
                    GeneralConsts.CONFIG.DATA_FORMAT[0]
            }

        loadConfig()
    }

    override fun onDestroyView() {
        saveConfig()
        super.onDestroyView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        var folder = ""
        if (data != null && resultCode == RESULT_OK) {
            folder = Util.normalizeFilePath(data.data?.path.toString())

            if (!Storage.isPermissionGranted(requireContext()))
                Storage.takePermission(requireContext(), requireActivity())
        }

        when (requestCode) {
            101 -> mLibraryPathAutoComplete.setText(folder)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty()) {
            val readExternalStorage: Boolean = grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (!readExternalStorage)
                Storage.takePermission(requireContext(), requireActivity())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_config, container, false)
    }

    private fun saveConfig() {
        val sharedPreferences =
            GeneralConsts.getSharedPreferences(requireContext())
        with(sharedPreferences.edit()) {
            this!!.putString(
                GeneralConsts.KEYS.LIBRARY.FOLDER,
                mLibraryPath.editText?.text.toString()
            )
            this.putString(
                GeneralConsts.KEYS.LIBRARY.ORDER,
                mOrderSelect.toString()
            )
            this.putString(
                GeneralConsts.KEYS.SUBTITLE.LANGUAGE,
                mDefaultSubtitleLanguageSelect.toString()
            )
            this.putString(
                GeneralConsts.KEYS.SUBTITLE.TRANSLATE,
                mDefaultSubtitleTranslateSelect.toString()
            )
            this.putString(
                GeneralConsts.KEYS.READER.PAGE_MODE,
                mPageModeSelect.toString()
            )
            this.putString(
                GeneralConsts.KEYS.READER.READER_MODE,
                mReaderModeSelect.toString()
            )
            this.putString(
                GeneralConsts.KEYS.SYSTEM.FORMAT_DATA,
                mDateSelect
            )

            this.commit()
        }

        Log.i(
            GeneralConsts.TAG.LOG,
            "Save prefer CONFIG:" + "\n[Library] Path " + mLibraryPath.editText?.text +
                    " - Order " + mLibraryOrder.editText?.text +
                    "\n[SubTitle] Language " + mDefaultSubtitleLanguage.editText?.text +
                    " - Translate " + mDefaultSubtitleTranslate.editText?.text +
                    "\n[System] Format Data " + mSystemFormatDate.editText?.text
        )

    }

    private fun loadConfig() {
        val sharedPreferences = GeneralConsts.getSharedPreferences(requireContext())

        mLibraryPath.editText?.setText(
            sharedPreferences.getString(
                GeneralConsts.KEYS.LIBRARY.FOLDER,
                ""
            )
        )

        mDateSelect = sharedPreferences.getString(
            GeneralConsts.KEYS.SYSTEM.FORMAT_DATA,
            GeneralConsts.CONFIG.DATA_FORMAT[0]
        )!!
        mPageModeSelect = PageMode.valueOf(
            sharedPreferences.getString(
                GeneralConsts.KEYS.READER.PAGE_MODE,
                PageMode.Comics.toString()
            )!!
        )
        mReaderModeSelect = ReaderMode.valueOf(
            sharedPreferences.getString(
                GeneralConsts.KEYS.READER.READER_MODE,
                ReaderMode.FIT_WIDTH.toString()
            )!!
        )
        mOrderSelect = Order.valueOf(
            sharedPreferences.getString(
                GeneralConsts.KEYS.LIBRARY.ORDER,
                Order.Name.toString()
            )!!
        )
        mDefaultSubtitleLanguageSelect = Languages.valueOf(
            sharedPreferences.getString(
                GeneralConsts.KEYS.SUBTITLE.LANGUAGE,
                Languages.JAPANESE.toString()
            )!!
        )
        mDefaultSubtitleTranslateSelect = Languages.valueOf(
            sharedPreferences.getString(
                GeneralConsts.KEYS.SUBTITLE.TRANSLATE,
                Languages.PORTUGUESE.toString()
            )!!
        )

        mLibraryOrderAutoComplete.setText(
            mMapOrder.filterValues { it == mOrderSelect }.keys.first(),
            false
        )
        mDefaultSubtitleLanguageAutoComplete.setText(
            mMapLanguage.filterValues { it == mDefaultSubtitleLanguageSelect }.keys.first(),
            false
        )
        mSubtitleTranslateAutoComplete.setText(
            mMapLanguage.filterValues { it == mDefaultSubtitleTranslateSelect }.keys.first(),
            false
        )

        mSystemFormatDateAutoComplete.setText(
            "$mDateSelect (%s)".format(
                SimpleDateFormat(mDateSelect).format(
                    Date()
                )
            ), false
        )
        mReaderComicModeAutoComplete.setText(
            mMapReaderMode.filterValues { it == mReaderModeSelect }.keys.first(),
            false
        )
        mPageModeAutoComplete.setText(
            mMapPageMode.filterValues { it == mPageModeSelect }.keys.first(),
            false
        )

    }
}
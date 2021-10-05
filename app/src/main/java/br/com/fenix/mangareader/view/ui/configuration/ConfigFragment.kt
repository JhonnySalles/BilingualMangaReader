package br.com.fenix.mangareader.view.ui.configuration

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.SharedPreferences
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

    private lateinit var txtLibraryPath: TextInputLayout
    private lateinit var autoCompleteLibraryPath: AutoCompleteTextView
    private lateinit var txtLibraryOrder: TextInputLayout
    private lateinit var autoCompleteLibraryOrder: AutoCompleteTextView

    private lateinit var txtDefaultSubtitleLanguage: TextInputLayout
    private lateinit var autoCompleteDefaultSubtitleLanguage: AutoCompleteTextView
    private lateinit var txtDefaultSubtitleTranslate: TextInputLayout
    private lateinit var autoCompleteDefaultSubtitleTranslate: AutoCompleteTextView

    private lateinit var txtReaderComicMode: TextInputLayout
    private lateinit var autocompleteReaderComicMode: AutoCompleteTextView
    private lateinit var txtReaderPageMode: TextInputLayout
    private lateinit var autocompletePageMode: AutoCompleteTextView

    private lateinit var txtSystemFormatDate: TextInputLayout
    private lateinit var autoCompleteSystemFormatDate: AutoCompleteTextView

    private var dateSelect: String = GeneralConsts.CONFIG.DATA_FORMAT[0]
    private val datePattern = GeneralConsts.CONFIG.DATA_FORMAT
    private var pageModeSelect: PageMode = PageMode.Comics
    private var readerModeSelect: ReaderMode = ReaderMode.FIT_WIDTH
    private var orderSelect: Order = Order.Name

    private var defaultSubtitleLanguageSelect: Languages = Languages.JAPANESE
    private var defaultSubtitleTranslateSelect: Languages = Languages.PORTUGUESE

    private lateinit var mapOrder: HashMap<String, Order>
    private lateinit var mapPageMode: HashMap<String, PageMode>
    private lateinit var mapReaderMode: HashMap<String, ReaderMode>
    private lateinit var mapLanguage: HashMap<String, Languages>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtLibraryPath = view.findViewById(R.id.txt_library_path)
        autoCompleteLibraryPath = view.findViewById(R.id.menu_autocomplete_library_path)
        txtLibraryOrder = view.findViewById(R.id.txt_library_order)
        autoCompleteLibraryOrder = view.findViewById(R.id.menu_autocomplete_library_order)

        txtDefaultSubtitleLanguage = view.findViewById(R.id.txt_default_subtitle_language)
        autoCompleteDefaultSubtitleLanguage =
            view.findViewById(R.id.menu_autocomplete_default_subtitle_language)
        txtDefaultSubtitleTranslate = view.findViewById(R.id.txt_default_subtitle_translate)
        autoCompleteDefaultSubtitleTranslate =
            view.findViewById(R.id.menu_autocomplete_default_subtitle_translate)

        txtReaderComicMode = view.findViewById(R.id.txt_reader_comic_mode)
        autocompleteReaderComicMode = view.findViewById(R.id.menu_autocomplete_reader_comic_mode)
        txtReaderPageMode = view.findViewById(R.id.txt_reader_page_mode)
        autocompletePageMode = view.findViewById(R.id.menu_autocomplete_page_mode)

        txtSystemFormatDate = view.findViewById(R.id.txt_system_format_date)
        autoCompleteSystemFormatDate = view.findViewById(R.id.menu_autocomplete_system_format_date)

        autoCompleteLibraryPath.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            startActivityForResult(intent, 101)
        }

        val languages = resources.getStringArray(R.array.languages)
        mapLanguage = hashMapOf(
            languages[0] to Languages.PORTUGUESE,
            languages[1] to Languages.ENGLISH,
            languages[2] to Languages.JAPANESE
        )

        mapOrder = hashMapOf(
            getString(R.string.config_option_order_name) to Order.Name,
            getString(R.string.config_option_order_date) to Order.Date,
            getString(R.string.config_option_order_access) to Order.LastAcess,
            getString(R.string.config_option_order_favorite) to Order.Favorite
        )

        mapPageMode = hashMapOf(
            getString(R.string.menu_reading_mode_left_to_right) to PageMode.Comics,
            getString(R.string.menu_reading_mode_right_to_left) to PageMode.Manga
        )

        mapReaderMode = hashMapOf(
            getString(R.string.menu_view_mode_aspect_fill) to ReaderMode.ASPECT_FILL,
            getString(R.string.menu_view_mode_aspect_fit) to ReaderMode.ASPECT_FIT,
            getString(R.string.menu_view_mode_fit_width) to ReaderMode.FIT_WIDTH
        )

        val adapterOrder =
            ArrayAdapter(requireContext(), R.layout.list_item, mapOrder.keys.toTypedArray())
        autoCompleteLibraryOrder.setAdapter(adapterOrder)
        autoCompleteLibraryOrder.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                orderSelect = if (parent.getItemAtPosition(position).toString().isNotEmpty() &&
                    mapOrder.containsKey(parent.getItemAtPosition(position).toString())
                )
                    mapOrder[parent.getItemAtPosition(position).toString()]!!
                else
                    Order.Name
            }

        val adapterLanguage =
            ArrayAdapter(requireContext(), R.layout.list_item, mapLanguage.keys.toTypedArray())
        autoCompleteDefaultSubtitleLanguage.setAdapter(adapterLanguage)
        autoCompleteDefaultSubtitleTranslate.setAdapter(adapterLanguage)
        autoCompleteDefaultSubtitleLanguage.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                defaultSubtitleLanguageSelect =
                    if (parent.getItemAtPosition(position).toString().isNotEmpty() &&
                        mapLanguage.containsKey(parent.getItemAtPosition(position).toString())
                    )
                        mapLanguage[parent.getItemAtPosition(position).toString()]!!
                    else
                        Languages.JAPANESE
            }

        autoCompleteDefaultSubtitleTranslate.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                defaultSubtitleTranslateSelect =
                    if (parent.getItemAtPosition(position).toString().isNotEmpty() &&
                        mapLanguage.containsKey(parent.getItemAtPosition(position).toString())
                    )
                        mapLanguage[parent.getItemAtPosition(position).toString()]!!
                    else
                        Languages.PORTUGUESE
            }

        val adapterReaderMode =
            ArrayAdapter(requireContext(), R.layout.list_item, mapReaderMode.keys.toTypedArray())
        autocompleteReaderComicMode.setAdapter(adapterReaderMode)
        autocompleteReaderComicMode.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                readerModeSelect = if (parent.getItemAtPosition(position).toString().isNotEmpty() &&
                    mapReaderMode.containsKey(parent.getItemAtPosition(position).toString())
                )
                    mapReaderMode[parent.getItemAtPosition(position).toString()]!!
                else
                    ReaderMode.FIT_WIDTH
            }

        val adapterPageMode =
            ArrayAdapter(requireContext(), R.layout.list_item, mapPageMode.keys.toTypedArray())
        autocompletePageMode.setAdapter(adapterPageMode)
        autocompletePageMode.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                pageModeSelect = if (parent.getItemAtPosition(position).toString().isNotEmpty() &&
                    mapPageMode.containsKey(parent.getItemAtPosition(position).toString())
                )
                    mapPageMode[parent.getItemAtPosition(position).toString()]!!
                else
                    PageMode.Comics
            }

        val date0 = SimpleDateFormat(datePattern[0]).format(Date())
        val date1 = SimpleDateFormat(datePattern[1]).format(Date())
        val date2 = SimpleDateFormat(datePattern[2]).format(Date())
        val date3 = SimpleDateFormat(datePattern[3]).format(Date())

        val dataFormat = listOf(
            getString(R.string.config_option_date_time_format_0).format(date0),
            getString(R.string.config_option_date_time_format_1).format(date1),
            getString(R.string.config_option_date_time_format_2).format(date2),
            getString(R.string.config_option_date_time_format_3).format(date3)
        )
        val adapterDataFormat = ArrayAdapter(requireContext(), R.layout.list_item, dataFormat)
        autoCompleteSystemFormatDate.setAdapter(adapterDataFormat)
        autoCompleteSystemFormatDate.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                dateSelect = if (position != null && datePattern.size > position && position >= 0)
                    datePattern[position]
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
            101 -> autoCompleteLibraryPath.setText(folder)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty()) {
            val readExternalStorege: Boolean = grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (!readExternalStorege)
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
        val sharedPreferences: SharedPreferences? =
            GeneralConsts.getSharedPreferences(requireContext())
        with(sharedPreferences?.edit()) {
            this!!.putString(
                GeneralConsts.KEYS.LIBRARY.FOLDER,
                txtLibraryPath.editText?.text.toString()
            )
            this.putString(
                GeneralConsts.KEYS.LIBRARY.ORDER,
                orderSelect.toString()
            )
            this.putString(
                GeneralConsts.KEYS.SUBTITLE.LANGUAGE,
                defaultSubtitleLanguageSelect.toString()
            )
            this.putString(
                GeneralConsts.KEYS.SUBTITLE.TRANSLATE,
                defaultSubtitleTranslateSelect.toString()
            )
            this.putString(
                GeneralConsts.KEYS.READER.PAGE_MODE,
                pageModeSelect.toString()
            )
            this.putString(
                GeneralConsts.KEYS.READER.READER_MODE,
                readerModeSelect.toString()
            )
            this.putString(
                GeneralConsts.KEYS.SYSTEM.FORMAT_DATA,
                dateSelect
            )

            this.commit()
        }

        Log.i(
            GeneralConsts.TAG.LOG,
            "Save prefer CONFIG:" + "\n[Library] Path " + txtLibraryPath.editText?.text +
                    " - Order " + txtLibraryOrder.editText?.text +
                    "\n[SubTitle] Language " + txtDefaultSubtitleLanguage.editText?.text +
                    " - Translate " + txtDefaultSubtitleTranslate.editText?.text +
                    "\n[System] Format Data " + txtSystemFormatDate.editText?.text
        )

    }

    private fun loadConfig() {
        val sharedPreferences = GeneralConsts.getSharedPreferences(requireContext()) ?: return

        txtLibraryPath.editText?.setText(
            sharedPreferences.getString(
                GeneralConsts.KEYS.LIBRARY.FOLDER,
                ""
            )
        )

        dateSelect = sharedPreferences.getString(
            GeneralConsts.KEYS.SYSTEM.FORMAT_DATA,
            GeneralConsts.CONFIG.DATA_FORMAT[0]
        )!!
        pageModeSelect = PageMode.valueOf(
            sharedPreferences.getString(
                GeneralConsts.KEYS.READER.PAGE_MODE,
                PageMode.Comics.toString()
            )!!
        )
        readerModeSelect = ReaderMode.valueOf(
            sharedPreferences.getString(
                GeneralConsts.KEYS.READER.READER_MODE,
                ReaderMode.FIT_WIDTH.toString()
            )!!
        )
        orderSelect = Order.valueOf(
            sharedPreferences.getString(
                GeneralConsts.KEYS.LIBRARY.ORDER,
                Order.Name.toString()
            )!!
        )
        defaultSubtitleLanguageSelect = Languages.valueOf(
            sharedPreferences.getString(
                GeneralConsts.KEYS.SUBTITLE.LANGUAGE,
                Languages.JAPANESE.toString()
            )!!
        )
        defaultSubtitleTranslateSelect = Languages.valueOf(
            sharedPreferences.getString(
                GeneralConsts.KEYS.SUBTITLE.TRANSLATE,
                Languages.PORTUGUESE.toString()
            )!!
        )

        autoCompleteLibraryOrder.setText(
            mapOrder.filterValues { it == orderSelect }.keys.first(),
            false
        )
        autoCompleteDefaultSubtitleLanguage.setText(
            mapLanguage.filterValues { it == defaultSubtitleLanguageSelect }.keys.first(),
            false
        )
        autoCompleteDefaultSubtitleTranslate.setText(
            mapLanguage.filterValues { it == defaultSubtitleTranslateSelect }.keys.first(),
            false
        )

        autoCompleteSystemFormatDate.setText(
            "$dateSelect (%s)".format(
                SimpleDateFormat(dateSelect).format(
                    Date()
                )
            ), false
        )
        autocompleteReaderComicMode.setText(
            mapReaderMode.filterValues { it == readerModeSelect }.keys.first(),
            false
        )
        autocompletePageMode.setText(
            mapPageMode.filterValues { it == pageModeSelect }.keys.first(),
            false
        )

    }
}
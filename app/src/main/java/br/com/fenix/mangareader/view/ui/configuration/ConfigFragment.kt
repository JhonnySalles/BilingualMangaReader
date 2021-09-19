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
import androidx.room.FtsOptions
import br.com.fenix.mangareader.R
import br.com.fenix.mangareader.model.enums.Languages
import br.com.fenix.mangareader.model.enums.Order
import br.com.fenix.mangareader.model.enums.PageMode
import br.com.fenix.mangareader.model.enums.ReaderMode
import br.com.fenix.mangareader.service.repository.Storage
import br.com.fenix.mangareader.util.constants.GeneralConsts
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.*


class ConfigFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private lateinit var txtLibraryPath: TextInputLayout
    private lateinit var autoCompleteLibraryPath: AutoCompleteTextView
    private lateinit var txtLibraryOrder: TextInputLayout
    private lateinit var autoCompleteLibraryOrder: AutoCompleteTextView

    private lateinit var txtSubtitlePath: TextInputLayout
    private lateinit var autoCompleteSubtitlePath: AutoCompleteTextView
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
    private lateinit var txtSystemLanguage: TextInputLayout
    private lateinit var autoCompleteSystemLanguage: AutoCompleteTextView

    private var dateSelect : String = GeneralConsts.CONFIG.DATA_FORMAT[0]
    private val datePattern = GeneralConsts.CONFIG.DATA_FORMAT

    private var pageModeSelect : PageMode = PageMode.Manga
    private lateinit var pageMode : List<String>

    private var readerModeSelect : ReaderMode = ReaderMode.ASPECT_FILL
    private lateinit var readerMode : List<String>

    private var orderSelect : Order = Order.Name
    private lateinit var orders : List<String>

    private var defaultSubtitleLanguageSelect : Languages = Languages.JP
    private var defaultSubtitleTranslateSelect : Languages = Languages.PT
    private var defaultSystemLanguageSelect : Languages = Languages.PT

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtLibraryPath = view.findViewById(R.id.txt_library_path)
        autoCompleteLibraryPath = view.findViewById(R.id.menu_autocomplete_library_path)
        txtLibraryOrder = view.findViewById(R.id.txt_library_order)
        autoCompleteLibraryOrder = view.findViewById(R.id.menu_autocomplete_library_order)

        txtSubtitlePath = view.findViewById(R.id.txt_subtitle_path)
        autoCompleteSubtitlePath = view.findViewById(R.id.menu_autocomplete_subtitle_path)
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
        txtSystemLanguage = view.findViewById(R.id.txt_system_language)
        autoCompleteSystemLanguage = view.findViewById(R.id.menu_autocomplete_system_language)

        autoCompleteLibraryPath.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            startActivityForResult(intent, 101)
        }

        autoCompleteSubtitlePath.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            startActivityForResult(intent, 102)
        }

        pageMode = listOf(
            getString(R.string.menu_view_mode_aspect_fill),
            getString(R.string.menu_view_mode_aspect_fit),
            getString(R.string.menu_view_mode_fit_width)
        )

        orders = listOf(
            getString(R.string.config_option_order_name),
            getString(R.string.config_option_order_date),
            getString(R.string.config_option_order_access)
        )

        readerMode = listOf(
            getString(R.string.menu_reading_mode_left_to_right),
            getString(R.string.menu_reading_mode_right_to_left)
        )

        val adapterOrder = ArrayAdapter(requireContext(), R.layout.list_item, orders)
        autoCompleteLibraryOrder.setAdapter(adapterOrder)
        autoCompleteLibraryOrder.onItemSelectedListener = this

        val languages = resources.getStringArray(R.array.languages)
        val adapterLanguage = ArrayAdapter(requireContext(), R.layout.list_item, languages)
        autoCompleteDefaultSubtitleLanguage.setAdapter(adapterLanguage)
        autoCompleteDefaultSubtitleTranslate.setAdapter(adapterLanguage)
        autoCompleteSystemLanguage.setAdapter(adapterLanguage)

        autoCompleteDefaultSubtitleLanguage.onItemSelectedListener = this
        autoCompleteDefaultSubtitleTranslate.onItemSelectedListener = this
        autoCompleteSystemLanguage.onItemSelectedListener = this

        val adapterReaderMode = ArrayAdapter(requireContext(), R.layout.list_item, readerMode)
        autocompleteReaderComicMode.setAdapter(adapterReaderMode)
        autocompleteReaderComicMode.onItemSelectedListener = this

        val adapterPageMode = ArrayAdapter(requireContext(), R.layout.list_item, pageMode)
        autocompletePageMode.setAdapter(adapterPageMode)
        autocompletePageMode.onItemSelectedListener = this

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
        autoCompleteSystemFormatDate.onItemSelectedListener = this

        loadConfig()
    }

    override fun onDestroyView() {
        saveConfig()
        super.onDestroyView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        var folder: String = ""
        if (data != null && resultCode == RESULT_OK) {
            folder = data.data?.path.toString()
            folder = folder.replace("/tree", "/storage").replace(":", "/")

            if (!Storage.isPermissionGranted(requireContext()))
                Storage.takePermission(requireContext(), requireActivity())
        }

        when (requestCode) {
            101 -> autoCompleteLibraryPath.setText(folder)
            102 -> autoCompleteSubtitlePath.setText(folder)
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
                GeneralConsts.KEYS.SUBTITLE.FOLDER,
                txtSubtitlePath.editText?.text.toString()
            )
            this.putString(
                GeneralConsts.KEYS.LIBRARY.ORDER,
                orderSelect.toString()
            )
            this.putString(
                GeneralConsts.KEYS.SUBTITLE.LANGUAGE,
                txtDefaultSubtitleLanguage.editText?.text.toString()
            )
            this.putString(
                GeneralConsts.KEYS.SUBTITLE.TRANSLATE,
                txtDefaultSubtitleTranslate.editText?.text.toString()
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
                GeneralConsts.KEYS.SYSTEM.LANGUAGE,
                txtSystemLanguage.editText?.text.toString()
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
                    "\n[SubTitle] Path " + txtSubtitlePath.editText?.text +
                    " - Language " + txtDefaultSubtitleLanguage.editText?.text +
                    " - Translate " + txtDefaultSubtitleTranslate.editText?.text +
                    "\n[System] Language " + txtSystemLanguage.editText?.text +
                    " - Format Data " + txtSystemFormatDate.editText?.text
        )

        //Toast.makeText(requireActivity(), getString(R.string.alert_save_sucess), Toast.LENGTH_SHORT).show()
    }

    private fun loadConfig() {
        val sharedPreferences = GeneralConsts.getSharedPreferences(requireContext()) ?: return

        txtLibraryPath.editText?.setText(
            sharedPreferences.getString(
                GeneralConsts.KEYS.LIBRARY.FOLDER,
                ""
            )
        )
        txtSubtitlePath.editText?.setText(
            sharedPreferences?.getString(
                GeneralConsts.KEYS.SUBTITLE.FOLDER,
                ""
            )
        )

        dateSelect =  sharedPreferences.getString(
            GeneralConsts.KEYS.SYSTEM.FORMAT_DATA,
            GeneralConsts.CONFIG.DATA_FORMAT[0]
        )!!
        pageModeSelect = PageMode.valueOf(
            sharedPreferences.getString(
                GeneralConsts.KEYS.LIBRARY.ORDER,
                PageMode.Manga.toString()
            )!!
        )
        readerModeSelect = ReaderMode.valueOf(
            sharedPreferences.getString(
                GeneralConsts.KEYS.LIBRARY.ORDER,
                ReaderMode.ASPECT_FILL.toString()
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
                Languages.JP.toString()
            )!!
        )
        defaultSubtitleTranslateSelect = Languages.valueOf(
            sharedPreferences.getString(
                GeneralConsts.KEYS.SUBTITLE.TRANSLATE,
                Languages.PT.toString()
            )!!
        )
        defaultSystemLanguageSelect = Languages.valueOf(
            sharedPreferences.getString(
                GeneralConsts.KEYS.SYSTEM.LANGUAGE,
                Languages.PT.toString()
            )!!
        )

        autoCompleteLibraryOrder.setText( orderSelect.toString(), false)
        autoCompleteDefaultSubtitleLanguage.setText( defaultSubtitleLanguageSelect.toString(), false)
        autoCompleteDefaultSubtitleTranslate.setText( defaultSubtitleTranslateSelect.toString(), false)
        autoCompleteSystemLanguage.setText( defaultSystemLanguageSelect.toString(), false)

        autoCompleteSystemFormatDate.setText(dateSelect, false)
        autocompleteReaderComicMode.setText(readerModeSelect.toString(), false)
        autocompletePageMode.setText(pageModeSelect.toString(), false)

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        Log.i("teste", "pos: $pos - id: $id")

        when (parent) {

        }


        /*dateSelect = GeneralConsts.CONFIG.DATA_FORMAT[0]
        pageModeSelect = PageMode.Manga
        readerModeSelect = ReaderMode.ASPECT_FILL
        orderSelect = Order.Name
        defaultSubtitleLanguageSelect = Languages.JP
        defaultSubtitleTranslateSelect = Languages.PT
        defaultSystemLanguageSelect = Languages.PT*/


    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

        dateSelect = GeneralConsts.CONFIG.DATA_FORMAT[0]
        pageModeSelect = PageMode.Manga
        readerModeSelect = ReaderMode.ASPECT_FILL
        orderSelect = Order.Name
        defaultSubtitleLanguageSelect = Languages.JP
        defaultSubtitleTranslateSelect = Languages.PT
        defaultSystemLanguageSelect = Languages.PT
    }
}
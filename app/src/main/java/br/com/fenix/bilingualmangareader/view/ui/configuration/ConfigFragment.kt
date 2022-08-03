package br.com.fenix.bilingualmangareader.view.ui.configuration

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import br.com.fenix.bilingualmangareader.MainActivity
import br.com.fenix.bilingualmangareader.R
import br.com.fenix.bilingualmangareader.model.enums.Languages
import br.com.fenix.bilingualmangareader.model.enums.Order
import br.com.fenix.bilingualmangareader.model.enums.PageMode
import br.com.fenix.bilingualmangareader.model.enums.ReaderMode
import br.com.fenix.bilingualmangareader.service.repository.DataBase
import br.com.fenix.bilingualmangareader.service.repository.Storage
import br.com.fenix.bilingualmangareader.util.constants.GeneralConsts
import br.com.fenix.bilingualmangareader.util.helpers.*
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputLayout
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*


class ConfigFragment : Fragment() {

    private val mLOGGER = LoggerFactory.getLogger(ConfigFragment::class.java)

    private val mViewModel: ConfigLibrariesViewModel by viewModels()

    private lateinit var mLibraryPath: TextInputLayout
    private lateinit var mLibraryPathAutoComplete: AutoCompleteTextView
    private lateinit var mLibraryOrder: TextInputLayout
    private lateinit var mLibraryOrderAutoComplete: AutoCompleteTextView
    private lateinit var mLibrariesButton: Button

    private lateinit var mDefaultSubtitleLanguage: TextInputLayout
    private lateinit var mDefaultSubtitleLanguageAutoComplete: AutoCompleteTextView
    private lateinit var mDefaultSubtitleTranslate: TextInputLayout
    private lateinit var mSubtitleTranslateAutoComplete: AutoCompleteTextView

    private lateinit var mReaderComicMode: TextInputLayout
    private lateinit var mReaderComicModeAutoComplete: AutoCompleteTextView
    private lateinit var mReaderPageMode: TextInputLayout
    private lateinit var mPageModeAutoComplete: AutoCompleteTextView
    private lateinit var mShowClockAndBattery: SwitchMaterial

    private lateinit var mSystemFormatDate: TextInputLayout
    private lateinit var mSystemFormatDateAutoComplete: AutoCompleteTextView

    private lateinit var mUseDualPageCalculate: SwitchMaterial
    private lateinit var mUsePathNameForLinked: SwitchMaterial

    private lateinit var mBackup: Button
    private lateinit var mRestore: Button
    private lateinit var mLastBackup: TextView

    private lateinit var mMalMonitoring: LinearLayout
    private lateinit var mMalMonitoringChecked: ImageView

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
        mLibrariesButton = view.findViewById(R.id.btn_libraries)

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
        mShowClockAndBattery = view.findViewById(R.id.switch_show_clock_and_battery)

        mSystemFormatDate = view.findViewById(R.id.txt_system_format_date)
        mSystemFormatDateAutoComplete = view.findViewById(R.id.menu_autocomplete_system_format_date)

        mUseDualPageCalculate = view.findViewById(R.id.switch_use_dual_page_calculate)
        mUsePathNameForLinked = view.findViewById(R.id.switch_use_path_name_for_linked)

        mBackup = view.findViewById(R.id.btn_backup)
        mRestore = view.findViewById(R.id.btn_restore)
        mLastBackup = view.findViewById(R.id.txt_last_backup)

        mMalMonitoring = view.findViewById(R.id.tracker_my_anime_list)
        mMalMonitoringChecked = view.findViewById(R.id.tracker_checked)

        mLibraryPathAutoComplete.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            startActivityForResult(intent, GeneralConsts.REQUEST.OPEN_MANGA_FOLDER)
        }

        mLibrariesButton.setOnClickListener { openLibraries() }

        mMapLanguage = Util.getLanguages(requireContext())

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

        mBackup.setOnClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/*"
                putExtra(
                    Intent.EXTRA_MIME_TYPES, arrayOf(
                        "application/db"
                    )
                )

                val fileName: String = "BilingualManga_" + SimpleDateFormat(
                    GeneralConsts.PATTERNS.BACKUP_DATE_PATTERN,
                    Locale.getDefault()
                ).format(
                    Date()
                ) + ".db"
                putExtra(Intent.EXTRA_TITLE, fileName)
            }
            startActivityForResult(intent, GeneralConsts.REQUEST.GENERATE_BACKUP)
        }

        mRestore.setOnClickListener {
            val i = Intent(Intent.ACTION_GET_CONTENT)
            i.type = "*/*"
            startActivityForResult(
                Intent.createChooser(i, getString(R.string.config_database_select_file)),
                GeneralConsts.REQUEST.RESTORE_BACKUP
            )
        }

        mMalMonitoring.setOnClickListener { changeMonitoring() }

        loadConfig()

        mViewModel.load()
    }

    override fun onDestroyView() {
        saveConfig()

        mViewModel.removeDefault(mLibraryPath.editText?.text.toString())
        (requireActivity() as MainActivity).setLibraries(mViewModel.getList())

        super.onDestroyView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            GeneralConsts.REQUEST.OPEN_MANGA_FOLDER -> {
                var folder = ""
                if (data != null && resultCode == RESULT_OK) {
                    folder = Util.normalizeFilePath(data.data?.path.toString())

                    if (!Storage.isPermissionGranted(requireContext()))
                        Storage.takePermission(requireContext(), requireActivity())
                }

                mLibraryPathAutoComplete.setText(folder)
            }

            GeneralConsts.CONFIG.RETURN -> {
                mViewModel.load()
                (requireActivity() as MainActivity).setLibraries(mViewModel.getList())
            }

            GeneralConsts.REQUEST.GENERATE_BACKUP -> {
                val fileUri: Uri? = data?.data
                try {
                    fileUri?.let {
                        DataBase.backupDatabase(requireContext(), File(Util.normalizeFilePath(fileUri.path.toString())))
                    }
                } catch (e: BackupError) {
                    MsgUtil.error(
                        requireContext(),
                        getString(R.string.config_database_restore),
                        getString(R.string.config_database_error_backup)
                    ) { _, _ -> }
                } catch (e: Exception) {
                    mLOGGER.warn("Backup Generate Failed.", e)
                    MsgUtil.error(
                        requireContext(),
                        getString(R.string.config_database_restore),
                        getString(R.string.config_database_error_backup)
                    ) { _, _ -> }
                }
            }

            GeneralConsts.REQUEST.RESTORE_BACKUP -> {
                val fileUri: Uri? = data?.data
                try {
                    fileUri?.let {
                        val inputStream: InputStream? = requireContext().contentResolver.openInputStream(fileUri)
                        inputStream?.let {
                            if (DataBase.validDatabaseFile(requireContext(), fileUri))
                                DataBase.restoreDatabase(requireContext(), inputStream)
                            else
                                MsgUtil.alert(
                                    requireContext(),
                                    getString(R.string.config_database_restore),
                                    getString(R.string.config_database_invalid_file)
                                ) { _, _ -> }

                        }
                        inputStream?.close()
                    }
                } catch (e: InvalidDatabase) {
                    MsgUtil.error(
                        requireContext(),
                        getString(R.string.config_database_restore),
                        getString(R.string.config_database_invalid_file)
                    ) { _, _ -> }
                } catch (e: RestoredNewDatabase) {
                    MsgUtil.error(
                        requireContext(),
                        getString(R.string.config_database_restore),
                        getString(R.string.config_database_error_new_database)
                    ) { _, _ -> }
                } catch (e: ErrorRestoreDatabase) {
                    MsgUtil.error(
                        requireContext(),
                        getString(R.string.config_database_restore),
                        getString(R.string.config_database_error_restore)
                    ) { _, _ -> }
                } catch (e: IOException) {
                    mLOGGER.warn("Backup Restore Failed.", e)
                    MsgUtil.error(
                        requireContext(),
                        getString(R.string.config_database_restore),
                        getString(R.string.config_database_error_read_file)
                    ) { _, _ -> }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == GeneralConsts.REQUEST.PERMISSION_FILES_ACCESS && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder(requireContext(), R.style.AppCompatAlertDialogStyle)
                .setTitle(requireContext().getString(R.string.alert_permission_files_access_denied_title))
                .setMessage(requireContext().getString(R.string.alert_permission_files_access_denied))
                .setPositiveButton(R.string.action_neutral) { _, _ -> }.create().show()
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

            this.putBoolean(
                GeneralConsts.KEYS.READER.SHOW_CLOCK_AND_BATTERY,
                mShowClockAndBattery.isChecked
            )

            this.putString(
                GeneralConsts.KEYS.SYSTEM.FORMAT_DATA,
                mDateSelect
            )

            this.putBoolean(
                GeneralConsts.KEYS.PAGE_LINK.USE_DUAL_PAGE_CALCULATE,
                mUseDualPageCalculate.isChecked
            )

            this.putBoolean(
                GeneralConsts.KEYS.PAGE_LINK.USE_PAGE_PATH_FOR_LINKED,
                mUsePathNameForLinked.isChecked
            )

            this.putBoolean(
                GeneralConsts.KEYS.MONITORING.MY_ANIME_LIST,
                mMalMonitoringChecked.visibility == View.VISIBLE
            )

            this.commit()
        }

        mLOGGER.info(
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

        mShowClockAndBattery.isChecked = sharedPreferences.getBoolean(
            GeneralConsts.KEYS.READER.SHOW_CLOCK_AND_BATTERY,
            false
        )

        mUseDualPageCalculate.isChecked = sharedPreferences.getBoolean(
            GeneralConsts.KEYS.PAGE_LINK.USE_DUAL_PAGE_CALCULATE,
            false
        )

        mUsePathNameForLinked.isChecked = sharedPreferences.getBoolean(
            GeneralConsts.KEYS.PAGE_LINK.USE_PAGE_PATH_FOR_LINKED,
            false
        )

        if (sharedPreferences.contains(GeneralConsts.KEYS.DATABASE.LAST_BACKUP)) {
            val backup = sharedPreferences.getString(
                GeneralConsts.KEYS.DATABASE.LAST_BACKUP,
                Date().toString()
            )?.let {
                SimpleDateFormat(GeneralConsts.PATTERNS.DATE_TIME_PATTERN, Locale.getDefault()).parse(
                    it
                )
            }
            mLastBackup.text = getString(
                R.string.config_database_last_backup,
                backup?.let {
                    SimpleDateFormat(mDateSelect + " " + GeneralConsts.PATTERNS.TIME_PATTERN, Locale.getDefault()).format(
                        it
                    )
                }
            )
        }

        mMalMonitoringChecked.visibility = if (sharedPreferences.getBoolean(
                GeneralConsts.KEYS.MONITORING.MY_ANIME_LIST,
                false
            )
        ) View.VISIBLE else View.GONE

    }

    private fun changeMonitoring() {
        if (mMalMonitoringChecked.visibility == View.GONE) {

        } else {
            val dialog: AlertDialog =
                AlertDialog.Builder(requireActivity(), R.style.AppCompatAlertDialogStyle)
                    .setTitle(getString(R.string.library_menu_delete))
                    .setMessage(getString(R.string.config_monitoring_disconnect, getString(R.string.config_monitoring_mal)))
                    .setPositiveButton(
                        R.string.action_disconnect
                    ) { _, _ ->
                        mMalMonitoringChecked.visibility = View.GONE
                    }.create()
            dialog.show()
        }
    }

    private fun openLibraries() {
        val intent = Intent(requireContext(), ConfigLibrariesActivity::class.java)
        requireActivity().overridePendingTransition(R.anim.fade_in_fragment_add_enter, R.anim.fade_out_fragment_remove_exit)
        startActivityForResult(intent, GeneralConsts.CONFIG.RETURN, null)
    }

}
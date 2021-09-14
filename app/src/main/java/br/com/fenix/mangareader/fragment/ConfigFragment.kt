package br.com.fenix.mangareader.fragment

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import br.com.fenix.mangareader.R
import br.com.fenix.mangareader.model.Consts
import br.com.fenix.mangareader.service.Storage
import com.google.android.material.textfield.TextInputLayout


class ConfigFragment : Fragment() {

    lateinit var txtLibraryPath: TextInputLayout
    lateinit var inputLibraryPath: AutoCompleteTextView
    lateinit var txtLibraryOrder: TextInputLayout
    lateinit var inputLibraryOrder: AutoCompleteTextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtLibraryPath = requireView().findViewById(R.id.txt_library_path)
        txtLibraryOrder = requireView().findViewById(R.id.txt_library_order)
        inputLibraryOrder = requireView().findViewById(R.id.menu_autocomplete_library_order)
        inputLibraryPath = requireView().findViewById(R.id.input_library_path)

        inputLibraryPath.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            startActivityForResult(intent, 101)
        }

        val items = listOf(
            getString(R.string.option_order_name),
            getString(R.string.option_order_date),
            getString(R.string.option_order_access)
        )
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, items)
        inputLibraryOrder.setAdapter(adapter)
        loadConfig()
    }


    override fun onDestroyView() {
        saveConfig()
        super.onDestroyView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null && resultCode == RESULT_OK && requestCode == 101) {
            if (!Storage.isPermissionGranted(requireContext()))
                Storage.takePermission(requireContext(), requireActivity())
            var folder: String = data.data?.path.toString()
            folder = folder.replace("/tree", "/storage").replace(":", "/")
            inputLibraryPath.setText(folder)
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
        val sharedPreferences: SharedPreferences? = activity?.getSharedPreferences(
            Consts.getKeySharedPrefs(),
            Context.MODE_PRIVATE
        )
        with(sharedPreferences?.edit()) {
            this!!.putString(Consts.getKeyLibraryFolder(), txtLibraryPath.editText?.text.toString())
            this.putString(Consts.getKeyLibraryOrder(), txtLibraryOrder.editText?.text.toString())
            this.commit()
        }

        Log.i(Consts.getTagLog(), "Save prefer CONFIG: " + "Path " + txtLibraryPath.editText?.text + " - Order " + txtLibraryOrder.editText?.text)

        //Toast.makeText(requireActivity(), getString(R.string.alert_save_sucess), Toast.LENGTH_SHORT).show()
    }

    private fun loadConfig() {
        val sharedPreferences = Consts.getSharedPreferences(requireContext())

        txtLibraryPath.editText?.setText(
            sharedPreferences?.getString(
                Consts.getKeyLibraryFolder(),
                ""
            )
        )
        inputLibraryOrder.setText(
            sharedPreferences?.getString(
                Consts.getKeyLibraryOrder(),
                ""
            ), false
        )
    }
}
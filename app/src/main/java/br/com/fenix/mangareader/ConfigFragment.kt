package br.com.fenix.mangareader

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputLayout


class ConfigFragment : Fragment() {

    lateinit var txt_library_path: TextInputLayout
    lateinit var txt_library_order: TextInputLayout
    lateinit var menu_autocomplete_library_order: AutoCompleteTextView
    lateinit var input_library_path: AutoCompleteTextView
    lateinit var explorer: Intent

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txt_library_path = requireView().findViewById<TextInputLayout>(R.id.txt_library_path)
        txt_library_order = requireView().findViewById<TextInputLayout>(R.id.txt_library_order)
        menu_autocomplete_library_order =
            requireView().findViewById<AutoCompleteTextView>(R.id.menu_autocomplete_library_order)
        input_library_path = requireView().findViewById<AutoCompleteTextView>(R.id.input_library_path)

        input_library_path.setOnClickListener {
            explorer = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            explorer.addCategory(Intent.CATEGORY_DEFAULT)
            startActivityForResult(
                Intent.createChooser(
                    explorer,
                    getString(R.string.choose_library_folder)
                ), 9999
            )
        }

        val items = listOf(
            getString(R.string.option_order_name),
            getString(R.string.option_order_date),
            getString(R.string.option_order_access)
        )
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, items)
        menu_autocomplete_library_order.setAdapter(adapter)
        loadConfig()
    }

    override fun onDestroyView() {
        saveConfig()
        super.onDestroyView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 9999) {
            //Está com problema para obter a apasta
            //txt_library_path.editText?.setText(data!!.data!!.path.toString())
            txt_library_path.editText?.setText(Environment.getExternalStorageDirectory().toString()+"/Manga/")
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
            this!!.putString(Consts.getKeyLibraryFolder(), txt_library_path.editText?.text.toString())
            this.putString(Consts.getKeyLibraryOrder(), txt_library_order.editText?.text.toString())
            this.commit()
        }

        //Toast.makeText(requireActivity(), getString(R.string.alert_save_sucess), Toast.LENGTH_SHORT).show()
    }

    private fun loadConfig() {
        val sharedPreferences: SharedPreferences? =
            activity?.getSharedPreferences(Consts.getKeySharedPrefs(), Context.MODE_PRIVATE)
        txt_library_path.editText?.setText(
            sharedPreferences?.getString(
                Consts.getKeyLibraryFolder(),
                ""
            )
        )
        menu_autocomplete_library_order.setText(
            sharedPreferences?.getString(
                Consts.getKeyLibraryOrder(),
                ""
            ), false
        )
    }
}
package com.hallen.school.ui.groups

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.hallen.school.R
import com.hallen.school.databinding.FragmentDaysBinding
import com.hallen.school.model.group.DayAdapter
import java.text.DateFormat
import java.text.SimpleDateFormat

class Days(private val group: DatabaseReference, private val function: (String) -> Unit) : Fragment() {
    private lateinit var binding: FragmentDaysBinding
    private lateinit var adapter: DayAdapter
    @SuppressLint("SimpleDateFormat")
    private val dateFormat: DateFormat = SimpleDateFormat("dd-MM-yyyy")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDaysBinding.inflate(inflater, container, false); return binding.root
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden){
            (context as Activity).findViewById<LinearLayout>(R.id.home_container).visibility = View.VISIBLE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val daysInDatabase = arrayListOf<String>()
        val presentes: MutableMap<String, Int> = mutableMapOf()
        var matricula = 0
        binding.daysRecyclerView.layoutManager = LinearLayoutManager(context)
        // Load students days
        group.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists() or !snapshot.hasChildren()) return

                for (childSnapshot in snapshot.children){
                    val allAsistance = childSnapshot.child("asistance").value; matricula++
                    if (allAsistance != null) {
                        for (asistence in allAsistance  as Map<String, Map<String, Any>>){
                            val key = asistence.key
                            if (key !in daysInDatabase) daysInDatabase.add(key)
                            if (asistence.value["present"] == null) continue
                            if (asistence.value["present"] as Boolean){
                                if (presentes[key] != null){
                                    presentes[key] =  presentes[key]!! + 1
                                } else presentes[asistence.key] = 1
                            }
                        }
                    }
                }

                // Aqui vamos a ordenar las fechas cronologicamente

                for (i in daysInDatabase){
                    Log.i("FECHAS", i)
                }
                // Convertimos las Fechas de String a Dates, se utiliza el metodo map para recorrer
                // toda la lista y convertir cada string en un Date.
                val dates = daysInDatabase.map { dateFormat.parse(it) }
                // ordenamos la lista con el metodo sorted() y luego la recorremos con map para
                // convertirla nuevamente a otra lista de tipo string usando el dateFormat
                val fechas: ArrayList<String> = dates.sorted().map { dateFormat.format(it) } as ArrayList<String>

                // Por ultimo creamos el adapter con las fechas ya ordenadas
                adapter = DayAdapter(fechas, presentes, matricula)

                adapter.setOnItemClickListener(object : DayAdapter.OnItemClickListener {
                    override fun onItemClick(fecha: String) {
                        val ft = parentFragmentManager.beginTransaction()
                        ft.hide(this@Days)
                        function(fecha)
                        ft.commit()
                    }
                })
                adapter.setOnItemLongClickListener(object : DayAdapter.OnItemLongClickListener {
                    override fun onItemLongClick(fecha: String, menu: ContextMenu) {
                        menu.add(0, 0, 0, "Eliminar")
                        menu.findItem(0).setOnMenuItemClickListener {
                            deleteDay(fecha)
                            true
                        }
                    }
                })
                binding.daysRecyclerView.adapter = adapter
                binding.newDayButton.setOnClickListener {
                    addNewDay()
                }
            }

            override fun onCancelled(error: DatabaseError) {    }

        })

    }

    private fun delete(date: String) {
        val indice = adapter.days.indexOf(date)
        if (indice < 0) {
            Toast.makeText(requireContext(), "A ocurrido un error al eliminar el día: $date", Toast.LENGTH_SHORT).show()
            return
        }
        group.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists() or !snapshot.hasChildren()) return
                for (childSnapshot in snapshot.children){
                    val allAsistance = childSnapshot.child("asistance").value ?: continue
                    for (asistance in allAsistance as Map<String, Map<String, Any>>){
                        if (date == asistance.key){
                            childSnapshot.child("asistance").child(date).ref.removeValue().addOnFailureListener {
                                it.printStackTrace()
                                Toast.makeText(requireContext(), "A ocurrido un error al eliminar el día: $date", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                try {
                    adapter.days.removeAt(indice)
                    adapter.notifyItemRemoved(indice)
                } catch (e:UnsupportedOperationException){
                    adapter.notifyDataSetChanged()
                } catch (e: IndexOutOfBoundsException){
                    adapter.notifyDataSetChanged()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "A ocurrido un error al eliminar el día: $date", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteDay(fecha: String) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.yes_or_no_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val textWarning = dialog.findViewById<TextView>(R.id.dialog_text_warning)
        textWarning.text = "Estas seguro que desea eliminar los registros del día $fecha?"
        val cancelButton = dialog.findViewById<TextView>(R.id.cancel_button)
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        val aceptButton = dialog.findViewById<TextView>(R.id.ok_button)
        aceptButton.setOnClickListener{
            val dateDate = dateFormat.parse(fecha) ?: return@setOnClickListener
            val date = dateFormat.format(dateDate)
            delete(date)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun addNewDay() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.date_picker_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val cancelButton: TextView   = dialog.findViewById(R.id.cancel_button)
        val datePicker:   DatePicker = dialog.findViewById(R.id.date_picker)
        val aceptButton:  TextView   = dialog.findViewById(R.id.ok_button)


        cancelButton.setOnClickListener {   dialog.dismiss()    }
        aceptButton.setOnClickListener {
            val fecha = "${datePicker.dayOfMonth}-${datePicker.month + 1}-${datePicker.year}"
            val date = dateFormat.parse(fecha) ?: return@setOnClickListener
            val ft = parentFragmentManager.beginTransaction()
            ft.hide(this@Days)
            function(dateFormat.format(date))
            ft.commit()
        }

        dialog.show()
    }

}
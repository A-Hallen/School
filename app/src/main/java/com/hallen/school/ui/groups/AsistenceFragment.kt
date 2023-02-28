package com.hallen.school.ui.groups

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.hallen.school.R
import com.hallen.school.databinding.FragmentAsistenceBinding
import com.hallen.school.model.group.AsistanceAdapter
import com.hallen.school.model.group.Student
import com.hallen.school.model.group.AsistanceAdapter.OnItemCheckListener
import com.hallen.school.model.group.AsistancePageAdapter.Companion.FECHA
import com.hallen.school.ui.BubblePopupMenu
import java.text.DateFormat.getDateInstance
import java.text.SimpleDateFormat
import java.util.*


class AsistenceFragment(private val group: DatabaseReference, private var date: String? = null) : Fragment() {
    private lateinit var binding: FragmentAsistenceBinding
    private lateinit var adapter: AsistanceAdapter
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        if (date == null) {
            date = dateFormat.format(Date()); FECHA = date
        }
        binding = FragmentAsistenceBinding.inflate(inflater, container, false); return binding.root
        /*
        Quiero ejecutar un codigo solo cuando el fragment es visible, cuando el fragment se esconde y
        vuelve a mostrarse quiero que la informacion en el se actualize
         */
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.topGroupName.text = group.key
        loadView()
    }

    private fun loadView() {
        adapter = AsistanceAdapter(arrayListOf(), date!!)
        val layoutManager = LinearLayoutManager(context)
        binding.asistanceRecyclerView.layoutManager = layoutManager
        binding.asistanceRecyclerView.adapter = adapter
        binding.topFecha.setOnClickListener {  loadAllDays() }
        val dateCool = dateFormat.parse(date!!)
        if (dateCool == null) {
            binding.topFecha.text = date
        } else {
            binding.topFecha.text = getDateInstance().format(dateCool)
        }
        loadStudents()

        adapter.setOnItemCheckListener(object : OnItemCheckListener{
            override fun onItemCheck(position: Int, checkBox: CheckBox, isChecked: Boolean) {
                // Que hago con el checkBox
                val key = adapter.students[position].key ?: return
                group.child(key).child("asistance").child(date!!).child("present")
                    .setValue(isChecked).addOnFailureListener {
                        Toast.makeText(requireContext(),
                            "A ocurrido un error al actualizar la asistencia en la base de datos", Toast.LENGTH_SHORT).show()
                        it.printStackTrace()
                    }
            }
        })
        adapter.setOnChangeNoteListener(object : AsistanceAdapter.OnChangeNoteListener{
            override fun onChangeNote(position: Int, button: ImageView, textView: TextView) {
                val popupMenu = BubblePopupMenu(requireContext(), button, object : BubblePopupMenu.PopupMenuCustomOnClickListener {
                    override fun onClick(index: Int, view: View) {
                        textView.visibility = View.VISIBLE; button.visibility = View.GONE
                        val nota = (view as TextView).text
                        textView.text = nota
                        val key = adapter.students[position].key ?: return
                        group.child(key).child("asistance").child(date!!).child("nota")
                            .setValue(nota.toString().toInt()).addOnFailureListener {
                                Toast.makeText(requireContext(),
                                    "A ocurrido un error al actualizar la nota en la base de datos", Toast.LENGTH_SHORT).show()
                            }
                    }
                })
                popupMenu.show()
            }
        })
    }


    private fun loadAllDays() {
        val daysFragment = Days(group){ fecha: String ->
            reload(fecha)
        }
        val ft = parentFragmentManager.beginTransaction()
        ft.replace(R.id.welcome_screen, daysFragment)
        ft.show(daysFragment)
        ft.addToBackStack("fragment")
        ft.commit()
    }

    private fun reload(fecha: String) {
        FECHA = date
        date = fecha; adapter.date = date!!
        val dateCool = dateFormat.parse(date!!)
        if (dateCool == null) binding.topFecha.text = date else {
            binding.topFecha.text = getDateInstance().format(dateCool)
        }
        adapter.notifyDataSetChanged()
    }

    private fun loadStudents() {
        if (adapter.students.isNotEmpty()) return // Si el adapter contiene algun item entonces no se ejecuta
        //esta funcion, eso evita que se ejecute varias veces al llamarse a la funcion onViewCreated
        group.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists() or !snapshot.hasChildren()) return
                for (childSnapshot in snapshot.children) {
                    val key = childSnapshot.key
                    val movil = childSnapshot.child("movil").getValue(String::class.java) // Obtiene el email del grupo
                    val name = childSnapshot.child("name").getValue(String::class.java) // Obtiene el nombre del grupo
                    val asistance = childSnapshot.child("asistance").value
                    val student = if (asistance != null){
                        Student(name ?: "", movil ?: "", key, asistance = asistance as Map<String, Map<String, Any>> )
                    } else {
                        Student(name ?: "", movil ?: "", key)
                    }
                    adapter.addData(student)
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) { }
        })

    }
}
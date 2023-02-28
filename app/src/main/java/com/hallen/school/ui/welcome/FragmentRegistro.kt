package com.hallen.school.ui.welcome

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.hallen.school.databinding.FragmentRegistroBinding
import com.hallen.school.model.group.AdapterRegistro
import com.hallen.school.model.group.LineChartXAxisFormater
import com.hallen.school.model.group.StudentData
import java.text.DateFormat
import java.text.SimpleDateFormat


class FragmentRegistro(private val group: DatabaseReference) : Fragment() {
    private lateinit var binding: FragmentRegistroBinding
    private lateinit var adapter: AdapterRegistro
    private val daysInDatabase = arrayListOf<String>()
    private val dateFormat: DateFormat = SimpleDateFormat("dd-MM-yyyy")
    private val studentList: ArrayList<String> = arrayListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        adapter = AdapterRegistro(arrayListOf())
        binding = FragmentRegistroBinding.inflate(inflater, container, false); return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager = LinearLayoutManager(context)
        binding.registroRecyclerView.layoutManager = layoutManager
        binding.registroRecyclerView.adapter = adapter
        if (arguments != null){
            when(requireArguments().get("fragment")){
                "grafics" -> loadGrafic()
                "registro" -> loadRegister()
            }
        }
        binding.studentChart.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long,
            ) {
                createGrafic(studentList[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }

        }
    }

    private fun loadRegister() {
        binding.grafica.visibility         = View.GONE
        binding.hearderGraficas.visibility = View.GONE
        binding.studentChart.visibility = View.GONE

        binding.headerRegistro.visibility       = View.VISIBLE
        binding.registroRecyclerView.visibility = View.VISIBLE
        loadStudents()
    }

    private fun loadGrafic() {
        binding.grafica.visibility = View.VISIBLE
        binding.hearderGraficas.visibility = View.VISIBLE
        binding.studentChart.visibility = View.VISIBLE

        binding.headerRegistro.visibility = View.GONE
        binding.registroRecyclerView.visibility = View.GONE
        loadStudents { student: String ->
            createGrafic(student)
        }
    }

    private fun createGrafic(student: String) {
        var lineList: ArrayList<Entry> = ArrayList()
        val lineChartXAxisFormater = LineChartXAxisFormater()
        group.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.hasChildren()){
                    val snap = snapshot.children.first { it.child("name").value == student }
                    val name = snap.child("name").value
                    val asistencia = snap.child("asistance").value as Map<String, Map<String, Any>>
                    for (day in daysInDatabase.sorted()){
                        val dia = asistencia[day] ?: continue
                        val nota = dia["nota"] ?: continue
                        val date = dateFormat.parse(day) ?: continue
                        try {
                            val note = nota.toString().toFloat()
                            // Xnew = Xold - reference_timestamp
                            if (lineChartXAxisFormater.originalTimestamp == 0L){
                                lineChartXAxisFormater.originalTimestamp = date.time
                            }
                            val xnew = (date.time - lineChartXAxisFormater.originalTimestamp).toFloat()
                            val entry = Entry(xnew, note)
                            lineList.add(entry)
                        } catch (e: Exception) {  throw e; continue    }


                    }
                    var lineDataSet = LineDataSet(lineList, "Notas")
                    var lineData = LineData(lineDataSet)
                    binding.grafica.data = lineData
                    binding.grafica.axisLeft.granularity = 1.0f
                    binding.grafica.axisRight.granularity = 1.0f
                    binding.grafica.axisLeft.axisMaximum = 6f
                    binding.grafica.axisRight.axisMaximum = 6f
                    binding.grafica.axisLeft.axisMinimum = 0f
                    binding.grafica.axisRight.axisMinimum = 0f
                    binding.grafica.extraLeftOffset = 10f
                    binding.grafica.extraRightOffset = 10f
                    binding.grafica.extraTopOffset = 10f
                    binding.grafica.description = Description().also {  it.text = name.toString()   }
                    binding.grafica.xAxis.valueFormatter = lineChartXAxisFormater
                    //lineDataSet.setColors(*ColorTemplate.JOYFUL_COLORS)
                    lineDataSet.valueTextColor = Color.BLUE
                    lineDataSet.valueTextSize = 20f
                    binding.grafica.invalidate()
                }
            }

            override fun onCancelled(error: DatabaseError) {    }
        })
    }


    private fun loadStudents(function: (String) -> Unit = {}) {


        if (adapter.studentsData.isNotEmpty()) return // Si el adapter contiene algun item entonces no se ejecuta
        //esta funcion, eso evita que se ejecute varias veces al llamarse a la funcion onViewCreated
        group.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists() or !snapshot.hasChildren()) return
                for (childSnapshot in snapshot.children){
                    val allAsistance = childSnapshot.child("asistance").value
                    if (allAsistance != null){
                        for (asistence in allAsistance  as Map<String, Map<String, Any>>){
                            val key = asistence.key
                            if (key !in daysInDatabase) daysInDatabase.add(key)
                        }
                    }
                }
                for (childSnapshot in snapshot.children) {
                    val name = childSnapshot.child("name").getValue(String::class.java) // Obtiene el nombre del alumno
                    val asistance = childSnapshot.child("asistance").value // Obtiene su asistencia (incluye las notas)
                    var studentData: StudentData? = null
                    if (asistance != null){
                        var notas: ArrayList<Int> = arrayListOf()
                        var clasesPresente = 0
                        for (clase in (asistance as Map<String, Map<String, Any>>).keys){
                            val leccion = asistance[clase]
                            if (leccion != null){
                                if(leccion["present"] == true) clasesPresente++
                                val nota = leccion["nota"]
                                if (nota != null) notas.add((nota as Long).toInt())
                            }
                        }
                        //Log.i("HALLEN", "$name: $notas \n{\n${notas.average()}\n}\n")
                        val promedioDeNotas      = notas.average()
                        val porcientoDeAucencias = 100 - (100.0 * clasesPresente / daysInDatabase.size)
                        studentData = StudentData(name ?: "",  promedioDeNotas, porcientoDeAucencias.toInt())

                    }
                    if (studentData != null) adapter.addData(studentData)
                }

                val data = adapter.studentsData.map { it.name } // Extraemos los nombres y lo asignamos al spinner de las graficas
                val spinnerAdapter = ArrayAdapter<String>(requireContext(), com.google.android.material.R.layout.support_simple_spinner_dropdown_item)
                for (d in data){
                    studentList.add(d)
                    spinnerAdapter.add(d)
                } // Ahora lo agregamos al adapter del spinner
                binding.studentChart.adapter = spinnerAdapter // Agregamos el adapter al spinner
                if (adapter.studentsData.isNotEmpty()) function(adapter.studentsData.first().name) // llamamos a la funcion para crear la grafica
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "A ocurrido un error", Toast.LENGTH_SHORT).show()
            }
        })

    }
}
package com.hallen.school.ui.welcome

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog
import com.github.dhaval2404.colorpicker.listener.ColorListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.hallen.school.R
import com.hallen.school.databinding.FragmentHorarioBinding
import com.hallen.school.model.Clase
import java.text.SimpleDateFormat


class FragmentHorario : Fragment() {
    private lateinit var binding: FragmentHorarioBinding
    private lateinit var clasesRef: DatabaseReference
    private var auth: FirebaseUser? = null
    private val clasesList: ArrayList<Clase> = ArrayList()
    private val dias = listOf("lunes", "martes", "miercoles", "jueves", "viernes")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance().currentUser
        val database = FirebaseDatabase.getInstance("https://school-e8de3-default-rtdb.firebaseio.com/")
        clasesRef = database.getReference("usuarios").child(auth!!.uid).child("clases")

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHorarioBinding.inflate(inflater, container, false); return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadHorario()
        listeners()
    }

    private fun listeners() {
        binding.newClase.setOnClickListener {
            editHora(Clase())
        }
    }

/*    private fun createData() {
        val clase1 = Clase("Histologia", "BAC 3", "jueves",  "09:20", "11:00")
        val clase2 = Clase("Histologia", "BAC 3", "viernes", "08:00", "09:20")
        val semana = listOf<Clase>()
        clasesRef.setValue(semana).addOnSuccessListener {
            Toast.makeText(requireContext(), "Exito al actualizar el horario", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            it.printStackTrace()
            Toast.makeText(requireContext(), "A ocurrido un error al actualizar el horario", Toast.LENGTH_SHORT).show()
        }
    }*/

    private fun loadHorario() {
        clasesRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.hasChildren()){
                    for(claseSnapshot in snapshot.children){
                        val clase = claseSnapshot.getValue(Clase::class.java)
                        if (clase != null){
                            clasesList.add(clase)
                        }
                    }
                    crearTabla()
                }
            }

            override fun onCancelled(error: DatabaseError) {    }
        })
    }

    @SuppressLint("SimpleDateFormat")
    private fun editHora(clase: Clase, indexOfDia: Int = 0) {
        // inicializamos algunas variables
        var claseColor: String

        // Creamos el dialogo
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.horario_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        // asignamos las vistas
        val local      = dialog.findViewById<EditText>(R.id.horario_lugar)
        val dia        = dialog.findViewById<Spinner>(R.id.horario_spinner)
        val start      = dialog.findViewById<EditText>(R.id.horario_time_start)
        val end        = dialog.findViewById<EditText>(R.id.horario_time_end)
        val asignatura = dialog.findViewById<EditText>(R.id.horario_asignatura)
        val colorView  = dialog.findViewById<ImageView>(R.id.color)
        val cancel     = dialog.findViewById<TextView>(R.id.cancel_button)
        val acept      = dialog.findViewById<TextView>(R.id.ok_button)
        val grupo      = dialog.findViewById<EditText>(R.id.grupo)

        // Adapter for the spinner
        val arrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item,
            dias.map { it -> it.replaceFirstChar { it.uppercase() } }) // mayusculas ;)

        // extraemos el color de la clase
        val color = if (clase.color.isBlank() || clase.color == "#FFFFFF") {
            ContextCompat.getColor(requireContext(), R.color.blue_light)
        } else Color.parseColor(clase.color)

        // nos permite identificar la clase a editar por las horas de inicio y fin
        val startEnd: List<String> = listOf(clase.horaInicio, clase.horaFin)

        claseColor = clase.color

        // agregamos los datos a las vistas
        dia.adapter = arrayAdapter
        dia.setSelection(indexOfDia) // por defecto el spinner va a estar en el dia selecionado.
        local.setText(clase.local)
        start.setText(clase.horaInicio)
        end.setText(clase.horaFin)
        asignatura.setText(clase.asignatura)
        colorView.setBackgroundColor(color)
        grupo.setText(clase.grupo)

        // el dialogo de color
        colorView.setOnClickListener {
            MaterialColorPickerDialog
                .Builder(requireContext())
                .setTitle("Escoge un color para la clase")
                .setColorListener(object : ColorListener{
                    override fun onColorSelected(color: Int, colorHex: String) {
                        colorView.setBackgroundColor(color); claseColor = colorHex
                    }

                }).show()

        }

        // Los listeners
        cancel.setOnClickListener { dialog.dismiss() }

        acept.setOnClickListener {
            // extraemos la hora de inicio
            val formatter = SimpleDateFormat("hh:mm") // Establecemos un formato de hora
            val startText  = start.text.toString()
            val endText = end.text.toString()
            if (startText.isBlank() || endText.isBlank()){
                Toast.makeText(requireContext(), "La hora de inicio y fin deben ser establecidas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val timeInicio = formatter.parse(startText) // formateamos el texto del edit
            if (timeInicio == null){ // Si el texto esta mal escrito no procedemos
                Toast.makeText(requireContext(), "Hora de inicio incorrecta", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } // extraemos la hora de fin.
            val timeEnd = formatter.parse(endText) // formateamos el texto del edit
            if(timeEnd == null){ // Si el texto esta mal escrito no procedemos
                Toast.makeText(requireContext(), "Hora de fin incorrecta", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (asignatura.text.isBlank()){
                Toast.makeText(requireContext(), "Falta el nombre del grupo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            clase.local      = local.text.toString()
            clase.dia        = dias[dia.selectedItemPosition]
            clase.horaInicio = formatter.format(timeInicio)
            clase.horaFin    = formatter.format(timeEnd)
            clase.asignatura = asignatura.text.toString()
            clase.color      = claseColor
            clase.grupo      = grupo.text.toString()

            guardarClase(clase, startEnd) // Si esta ok, actualizamos los datos en la bd
            dialog.dismiss()
        }

        // Por ultimo mostramos el dialogo
        dialog.show()
    }

    private fun guardarClase(clase: Clase, startEnd: List<String>) {
        val semana: ArrayList<Clase> = clasesList.filter { it.horaInicio != startEnd[0] && it.horaFin != startEnd[1] } as ArrayList<Clase>
        semana.add(clase)
        clasesRef.setValue(semana).addOnSuccessListener {
            crearTabla()
        }.addOnFailureListener {
            it.printStackTrace()
            Toast.makeText(requireContext(), "A ocurrido un error al actualizar el horario", Toast.LENGTH_SHORT).show()
        }
    }

    private fun crearTabla() {
        val tableLayout = binding.tablaHorarioId
        tableLayout.removeAllViews()

        // Determine el rango de horas de las clases
        val horas = clasesList.map { it.horaInicio to it.horaFin }.sortedBy { it.first }

        // Agrega la fila de encabezado con los días de la semana
        val encabezadoRow = TableRow(requireContext())
        val headerHora = HeaderCell(requireContext())
        headerHora.text = requireContext().getString(R.string.hora)
        headerHora.backgroundTintList = ColorStateList.valueOf(Color.RED)
        headerHora.background = ContextCompat.getDrawable(requireContext(), R.drawable.horario_top_left_corner)
        encabezadoRow.addView(headerHora)
        for (dia in dias) {
            val headerDay = HeaderCell(requireContext())
            headerDay.text = dia.replaceFirstChar { it.uppercase() }
            if (dia == dias.last()){
                headerDay.background = ContextCompat.getDrawable(requireContext(), R.drawable.horario_top_right_corner)
                headerDay.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.steel_blue))
            }
            encabezadoRow.addView(headerDay)
        }
        tableLayout.addView(encabezadoRow)
        // Agrega las filas de datos con las clases
        for (hora in horas) {
            /*val clasesInHora = clasesList.filter { it.horaInicio == hora }
            val grupos = clasesInHora.map { it.grupo }
            Log.i("GRUPOS: ", grupos.toString())*/
            val row = TableRow(requireContext()).apply {
                layoutParams = TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT)
            }
            row.addView(TextView(requireContext())
                .apply {
                    text = getString(R.string.hora_template, hora.first, hora.second)
                    setTextColor(Color.BLACK)
                    if (hora == horas.last()){
                        background = ContextCompat.getDrawable(requireContext(), R.drawable.horario_bottom_left_corner)
                        backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.steel_blue_light))
                    } else setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.steel_blue_light))
                    setPadding(5.dpToPx(context), 0, 5.dpToPx(context), 0)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                    layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT)
                    gravity = Gravity.CENTER
                })
            for (dia in dias) {
                val clasesDelDia = clasesList.filter { it.dia == dia }
                val clasesDeLaHora = clasesDelDia.filter { it.horaInicio == hora.first }
                val grupoText = clasesDeLaHora.joinToString{ it.grupo }
                val localText = clasesDeLaHora.joinToString{ it.local }
                val claseCell = ClaseCell(requireContext()).apply {
                    grupo.text = grupoText; local.text = localText
                    background = if (dia == dias.last() && hora == horas.last()){ // si es la ultima celda, de la ultima fila, cambiamos el fondo
                        ContextCompat.getDrawable(requireContext(), R.drawable.horario_bottom_right_corner)
                    } else {
                        ContextCompat.getDrawable(requireContext(), R.drawable.horario_no_border_no_corner)
                    }
                    val color = clasesDeLaHora.joinToString { it.color }

                    val colorBg = if (color.isNotBlank()){
                        try {Color.parseColor(color)} catch (e: IllegalArgumentException){
                            ContextCompat.getColor(requireContext(), R.color.blue_light)    }
                    } else ContextCompat.getColor(requireContext(), R.color.blue_light)

                    backgroundTintList = ColorStateList.valueOf(colorBg)
                    setTextColor(Color.WHITE)
                }
                claseCell.setOnClickListener {
                    if (clasesDeLaHora.isNotEmpty()) editHora(clasesDeLaHora[0], dias.indexOf(dia)) else {
                        editHora(Clase(), dias.indexOf(dia))
                    }
                }
                row.addView(claseCell)
            }
            tableLayout.addView(row)
        }
    }



}

private fun Int.dpToPx(context: Context): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        context.resources.displayMetrics
    ).toInt()
}

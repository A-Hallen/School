package com.hallen.school.model.group

import android.content.res.ColorStateList
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hallen.school.R
import java.math.RoundingMode
import java.text.DecimalFormat

class AdapterRegistro(val studentsData: ArrayList<StudentData>): RecyclerView.Adapter<AdapterRegistro.ViewHolder>() {

    override fun getItemCount(): Int = studentsData.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_registro, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = studentsData[position]

        if (currentItem.aucencias != null){
            setDangerBg(holder.container, currentItem.aucencias!!) // Cambiamos el fondo
            if (currentItem.aucencias!! > 40){ // Si hay muchas aucencias el fondo es muy oscuro
                setTextColor(listOf(           // Hay que cambiar el color del texto
                    holder.indexView,
                    holder.nameText,
                    holder.notaPromedio,
                    holder.promedioAusencias))
            }
        }

        // Asignamos valores a las vistas
        holder.nameText.text  = currentItem.name
        val deciMalFormat = DecimalFormat("#.#")
        deciMalFormat.roundingMode = RoundingMode.DOWN
        val notas = currentItem.notas
        if (currentItem.notas != null) holder.notaPromedio.text   = deciMalFormat.format(notas)
        holder.promedioAusencias.text = (currentItem.aucencias ?: "").toString()
        if (currentItem.index != null) holder.indexView.text = currentItem.index.toString()
    }

    private fun setTextColor(textViewList: List<TextView>) {
        for (view in textViewList){
            setTextColor(view)
        }
    }

    private fun setTextColor(textView: TextView){
        textView.setTextColor(Color.WHITE)
    }

    private fun setDangerBg(view: LinearLayout, percent: Int) {
        val red   = 255
        val blue  = if (percent > 80) 0 else 255 - 3 * percent
        val green = if (percent > 80) 0 else 255 - 3 * percent
        val color = Color.argb(255, red, green, blue)
        view.backgroundTintList = ColorStateList.valueOf(color)
    }

    fun addData(studentData: StudentData) {
        studentsData.add(studentData)
        notifyItemInserted(studentsData.size - 1)
    }


    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val indexView:         TextView = view.findViewById(R.id.index_edit)
        val nameText:          TextView = view.findViewById(R.id.name)
        val notaPromedio:      TextView = view.findViewById(R.id.nota_promedio)
        val promedioAusencias: TextView = view.findViewById(R.id.ausencias)
        val container:     LinearLayout = view.findViewById(R.id.container_registro)

        init {
            nameText.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {  }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {     }
                override fun afterTextChanged(s: Editable?) {
                    studentsData[adapterPosition].index = adapterPosition + 1
                }

            })
        }
    }
}
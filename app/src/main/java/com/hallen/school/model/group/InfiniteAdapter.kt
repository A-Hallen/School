package com.hallen.school.model.group

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hallen.school.R

class InfiniteAdapter(var data: ArrayList<Student>) : RecyclerView.Adapter<InfiniteAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // Inflamos la vista
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_student, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = data[position] // Item actual
        holder.textView.setText(currentItem.name) // Agregamos el nombre del alumno en el TextView
        if (currentItem.index != null) holder.indexText.text = currentItem.index.toString() // Mostramos el indice del alumno
    }

    override fun getItemCount(): Int = data.size // retornamos el tamaño de la variable data.

    // Esta funcion agrega nuevos datos a la variable data, aumentando el número de items.
    fun addData(newData: List<Student>) {
        val oldSize = data.size
        data.addAll(newData) // Añadimos los datos a la variable data
        notifyItemRangeInserted(oldSize, newData.size) // Notificamos los cambios en el adapter
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val textView: EditText = view.findViewById(R.id.name) // El editText para cambiar o agregar el nombre del estudiante
        val indexText:TextView = view.findViewById(R.id.index_edit) // el textView donde se va a mostrar su índice
        private val cellNum: ImageView = view.findViewById(R.id.cell) // ImageView que contiene el icono del telefono

        init {
            textView.addTextChangedListener(object : TextWatcher { // listener para observar los cambios en el editText
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {  }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {     }
                override fun afterTextChanged(s: Editable?) {
                    data[adapterPosition].name = textView.text.toString() // Actualizamos los cambios del edit el la variable data
                    data[adapterPosition].index = adapterPosition + 1 // Hacemos lo mismo con el índice
                }

            })
            textView.setOnFocusChangeListener { _, hasFocus -> // Listener para hacer más grande el editText y sea más facil escribir
                if (hasFocus){ // Se ejecuta si el editText tiene le foco
                    indexText.visibility = View.GONE // Hacemos invisible el TextView del índice
                } else {
                    indexText.visibility = View.VISIBLE // Hacemos visible el TextView del índice.
                }
            }
         cellNum.setOnClickListener { askNum(view.context) } // Listener para cuando hacemos click en el imageView del teléfono
        }

        private fun askNum(context: Context) {
            val dialog = Dialog(context) // Creamos un diálogo
            dialog.setContentView(R.layout.dialog_phone_number) // Le asignamos la vista dialog_phone_number
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            val phoneNumberEditText = dialog.findViewById<EditText>(R.id.phone_number_edittext) // EditText donde escribimos el número de telefono.
            phoneNumberEditText.setText(data[adapterPosition].movil) // Añadimos el número de teléfono que contiene el estudiante o "" si no tiene aún.
            val cancelButton = dialog.findViewById<Button>(R.id.cancel_button) // Referencia al botón cancelar
            cancelButton.setOnClickListener { // Le añadimos un listener al botón cancelar
                dialog.dismiss() // Cierra el diálogo
            }

            val okButton = dialog.findViewById<Button>(R.id.ok_button) // Referencia al boton OK
            okButton.setOnClickListener { // Listener del botón OK
                val phoneNumber = phoneNumberEditText.text.toString() // Texto del EditText
                data[adapterPosition].movil = phoneNumber // Actualizamos los datos en la variable data
                dialog.dismiss() // Cerramos el diálogo.
            }

            dialog.show() // Mostramos el diálogo después de haberlo configurado.
        }
    }
}
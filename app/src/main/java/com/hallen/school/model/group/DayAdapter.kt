package com.hallen.school.model.group

import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hallen.school.R
import java.text.DateFormat
import java.text.SimpleDateFormat

class DayAdapter(
    val days: ArrayList<String>,
    val presentes: MutableMap<String, Int>,
    val matricula: Int
): RecyclerView.Adapter<DayAdapter.ViewHolder>() {
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy")

    interface OnItemLongClickListener {     fun onItemLongClick (fecha: String, menu: ContextMenu)   }
    interface OnItemClickListener     {     fun onItemClick     (fecha: String)                      }

    private lateinit var clickListener: OnItemClickListener
    private lateinit var longClickListener: OnItemLongClickListener

    fun setOnItemClickListener    (listener: OnItemClickListener)    {    clickListener     = listener }
    fun setOnItemLongClickListener(listener: OnItemLongClickListener){    longClickListener = listener }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.day, parent, false)
        return ViewHolder(view, clickListener, longClickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = days[position]
        val fecha = currentItem
        val coolFecha = dateFormat.parse(fecha)

        holder.fechaTextView.text = if (coolFecha == null) fecha else {
            DateFormat.getDateInstance().format(coolFecha)
        }
        val presents = presentes[fecha]
        val aucentes = matricula - (presents ?: 0)

        holder.presentes.text = "presentes: " + (presents ?: "0").toString()
        holder.ausentes.text = "ausentes: $aucentes"
    }

    override fun getItemCount(): Int = days.size
    inner class ViewHolder(view: View, itemClickListener: OnItemClickListener, itemLongClickListener: OnItemLongClickListener):
        RecyclerView.ViewHolder(view) {

        val fechaTextView:  TextView = view.findViewById(R.id.fecha)
        val ausentes:       TextView = view.findViewById(R.id.aucentes)
        val presentes:      TextView = view.findViewById(R.id.presentes)

        init {
            view.setOnClickListener {
                itemClickListener.onItemClick(days[adapterPosition])
            }
            view.setOnCreateContextMenuListener { menu, _, _ ->
                itemLongClickListener.onItemLongClick(days[adapterPosition], menu)
            }
        }
    }
}
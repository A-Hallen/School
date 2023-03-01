package com.hallen.school.model.events

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.hallen.school.R
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class EventAdapter(private val context: Context, private val events: ArrayList<Map<String, String>>): BaseAdapter() {
    private val littleFormat: DateFormat = SimpleDateFormat("hh:mm a", Locale.US)
    private val colorTemplates = mapOf("Alta" to Color.parseColor("#ff0000"),
        "Media" to Color.parseColor("#0000ff"), "Baja" to Color.parseColor("#43AC00"))

    override fun getCount(): Int = events.size
    override fun getItem(position: Int): Any = events[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val currentItem = events[position]
        // Asignamos las vistas
        val view        = LayoutInflater.from(context).inflate(R.layout.event_list_item, parent, false)
        val titleView   = view.findViewById<TextView>(R.id.event_title)
        val contentView = view.findViewById<TextView>(R.id.event_content)
        val hora        = view.findViewById<TextView>(R.id.hora_event_item)
        // Agregamos el contenido a las vistas
        hora.text        = currentItem["hora"]
        titleView.text   = currentItem["title"]
        contentView.text = currentItem["details"]
        val color = colorTemplates[currentItem["color"]] ?: return view
        titleView.setTextColor(color)

        // Retornamos la vista
        return view
    }

    fun addView(map: Map<String, String>){
        events.add(map)
        try {
            events.sortBy {
                littleFormat.parse(it["hora"]!!)!!.time
            }
        } catch (e: Exception){
            e.printStackTrace()
            events.sortBy { it["hora"] }
        }
        notifyDataSetChanged()
    }

    fun clear() {
        events.clear()
        notifyDataSetChanged()
    }

    fun remove(position: Int) {
        events.removeAt(position)
        notifyDataSetChanged()
    }


}

// Create a recyclerView Adapter with params context: Context, events: ArrayList<Map<String, String>>